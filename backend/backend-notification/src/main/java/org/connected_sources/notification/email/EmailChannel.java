package org.connected_sources.notification.email;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.internet.AddressException;

import org.connected_sources.notification.core.BaseChannelAdapter;
import org.connected_sources.notification.core.Channel;
import org.connected_sources.notification.core.RenderedMessage;
import org.connected_sources.notification.core.SendResult;
import org.connected_sources.notification.service.ContactInformationRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class EmailChannel extends BaseChannelAdapter {

  private final JavaMailSender mailSender;
  private final ContactInformationRepo contacts;
  private final String defaultFrom;

  private final Parser mdParser = Parser.builder().build();
  private final HtmlRenderer mdHtmlRenderer = HtmlRenderer.builder().build();
//  private final TextContentRenderer mdTxtRenderer = TextContentRenderer.builder().build();


  public EmailChannel(JavaMailSender mailSender, ContactInformationRepo contacts,
                      @Value("${notification.email.from}") String defaultFrom) {
    this.mailSender = mailSender;
      this.contacts = contacts;
      this.defaultFrom = defaultFrom;
  }

  @Override public Channel type() { return Channel.EMAIL; }

  private record Content(boolean html, String value) {
    static Content html(String v)  { return new Content(true,  v); }
    static Content plain(String v) { return new Content(false, v); }
    boolean isHtml() { return html; }
//    String value() { return value; }
  }

  
  @Override
  protected SendResult sendInternal(@NonNull RenderedMessage msg) {
    try {
      MimeMessage mm = mailSender.createMimeMessage();

      // From
      mm.setFrom(new InternetAddress(defaultFrom));

      String to = resolveRecipientEmail(msg);
        if (to == null) {
            throw new AssertionError();
        }
        mm.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
      mm.setSubject(MimeUtility.encodeText(msg.subject(), StandardCharsets.UTF_8.name(), "B"));

      mm.setContent(msg.body(), "text/; charset=UTF-8" );



      // Optional headers for traceability
      if (msg.correlationId() != null) {
        mm.setHeader("X-Correlation-Id", msg.correlationId());
      }

      mailSender.send(mm);

      // Gmail does not return a provider message-id -> read headers if present
      String providerId = safeMessageId(mm);
      if (providerId == null || providerId.isBlank()) {
        providerId = UUID.randomUUID().toString();
      }
      return new SendResult(true, providerId, null, false);

    } catch (MailSendException mse) {
      // Unwrap and classify
      SmtpError err = classifySmtpError(mse);
      return new SendResult(false, null, err.code, err.permanent);
    } catch (MessagingException me) {
      SmtpError err = classifySmtpError(me);
      return new SendResult(false, null, err.code, err.permanent);
    } catch (Exception ex) {
      // Unknown -> transient
      return new SendResult(false, null, "EMAIL_UNKNOWN", false);
    }
  }

    private String resolveRecipientEmail(RenderedMessage msg) {
      return "costantino.giuliodori@gmail.com";
    }

    private String safeMessageId(MimeMessage mm) {
    try {
      String[] ids = mm.getHeader("Message-ID");
      return (ids != null && ids.length > 0) ? ids[0] : null;
    } catch (MessagingException ignored) {
      return null;
    }
  }

  /** Map SMTP/Jakarta exceptions to know if pemranent. */
  private SmtpError classifySmtpError(Exception e) {
    // Defaults
    SmtpError out = new SmtpError("EMAIL_SEND_FAILED", false);

    Throwable cause = e;
    while (cause != null) {
      // Address issues: 550 5.1.1 etc. -> permanent
        switch (cause) {
            case AddressException _ -> {
                return new SmtpError("SMTP_550_ADDRESS", true);
            }
            case SendFailedException sfe -> {
                var ne = sfe.getNextException();
                if (ne != null) {
                    var code = smtpReplyCode(ne.getMessage());
                    if (code >= 500 && code < 600) return new SmtpError("SMTP_" + code, true);
                    if (code >= 400 && code < 500) return new SmtpError("SMTP_" + code, false);
                }
                // Without code: treat as transient network issue
                return new SmtpError("SMTP_SEND_FAILED", false);
            }
            case MessagingException me -> {
                var ne = me.getNextException();
                if (ne != null) {
                    var code = smtpReplyCode(ne.getMessage());
                    if (code >= 500 && code < 600) return new SmtpError("SMTP_" + code, true);
                    if (code >= 400 && code < 500) return new SmtpError("SMTP_" + code, false);
                }
            }
            default -> {
            }
        }
        cause = cause.getCause();
    }
    return out;
  }

  /** Extract leading SMTP reply code from a provider message (e.g., "550 5.1.1 ..."). */
  private int smtpReplyCode(String msg) {
    if (msg == null || msg.length() < 3) return -1;
    try {
      return Integer.parseInt(msg.substring(0, 3));
    } catch (NumberFormatException nfe) {
      return -1;
    }
  }

  private record SmtpError(String code, boolean permanent) {}
}
