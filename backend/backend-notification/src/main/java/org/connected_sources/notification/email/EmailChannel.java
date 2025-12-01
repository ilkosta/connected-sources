package org.connected_sources.notification.email;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.connected_sources.notification.core.BaseChannelAdapter;
import org.connected_sources.notification.core.Channel;
import org.connected_sources.notification.core.RenderedMessage;
import org.connected_sources.notification.core.SendResult;
import org.connected_sources.notification.service.ContactInformationRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class EmailChannel extends BaseChannelAdapter {

    private static final Logger logger = LoggerFactory.getLogger(EmailChannel.class);

    private final JavaMailSender mailSender;
    private final ContactInformationRepo contacts;
    private final String defaultFrom;

    private final Parser mdParser = Parser.builder().build();
    private final HtmlRenderer mdHtmlRenderer = HtmlRenderer.builder().build();

    public EmailChannel(JavaMailSender mailSender, ContactInformationRepo contacts,
                        @Value("${notification.email.from}") String defaultFrom) {
        this.mailSender = mailSender;
        this.contacts = contacts;
        this.defaultFrom = defaultFrom;
    }

    @Override
    public Channel type() {
        return Channel.EMAIL;
    }

    private Content renderHtml(RenderedMessage msg) {

        String md = msg.bodyMd() != null ? msg.bodyMd() : null;
        if (md != null && !md.isBlank()) {
            Node doc = mdParser.parse(md);
            String html = mdHtmlRenderer.render(doc);
            return Content.html(html);
        }

        String plain = msg.bodyMd() != null ? msg.bodyMd() : "(no content)";
        return Content.plain(plain);
    }

    private String resolveRecipientEmail(RenderedMessage msg) {
        // 1. If there is an explicit recipient, it is a source of truth
        if (msg.recipient() != null && !msg.recipient().isBlank()) {
            return msg.recipient();
        }

        // 2. Failing that, explicit override via providerHints.email
        Map<String, Object> hints = msg.providerHints();
        if (hints != null && hints.get("email") instanceof String s && !s.isBlank()) {
            return s;
        }

        // 3. Fallback: from the preferred email contact_information by userId
        Long userId = msg.userId();
        if (userId == null) return null;
        Optional<String> email = contacts.findPrimaryEmail(userId);
        return email.orElse(null);
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

            try {
                mm.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
                mm.setSubject(MimeUtility.encodeText(msg.subject(), StandardCharsets.UTF_8.name(), "B"));

                var content = renderHtml(msg);
                mm.setContent(content.value(), content.isHtml() ? "text/html; charset=UTF-8" : "text/; charset=UTF-8");


                // Optional headers for traceability
                if (msg.correlationId() != null) {
                    mm.setHeader("X-Correlation-Id", msg.correlationId());
                }

                logger.debug("Sending email",
                        Map.of("to", to,
                                "subject", msg.subject(),
                                "correlationId", msg.correlationId()));

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
                logFailedSend(to, msg, err, mse.toString());
                return new SendResult(false, null, err.code, err.permanent);
            } catch (MessagingException me) {
                SmtpError err = classifySmtpError(me);
                logFailedSend(to, msg, err, me.toString());
                return new SendResult(false, null, err.code, err.permanent);
            } catch (Exception ex) {
                logUnknownErr(msg, ex);
                return new SendResult(false, null, "EMAIL_UNKNOWN", false);
            }
        } catch (Exception ex) {
            logUnknownErr(msg, ex);
            return new SendResult(false, null, "EMAIL_RECIPIENT_UNKNOWN", false);

        }


    }

    private static void logFailedSend(String to, RenderedMessage msg, SmtpError err, String me) {
        logger.error("Email send failed",
                Map.of("to", to,
                        "subject", msg.subject(),
                        "correlationId", msg.correlationId(),
                        "errorCode", err.code(),
                        "permanent", err.permanent(),
                        "exception", me));
    }

    private static void logUnknownErr(RenderedMessage msg, Exception ex) {
        logger.error("Unknown email error",
                Map.of("subject", msg.subject(),
                        "correlationId", msg.correlationId(),
                        "exception", ex.toString()));
    }

    private String safeMessageId(MimeMessage mm) {
        try {
            String[] ids = mm.getHeader("Message-ID");
            return (ids != null && ids.length > 0) ? ids[0] : null;
        } catch (MessagingException _) {
            return null;
        }
    }

    /**
     * Map SMTP/Jakarta exceptions to know if pemranent.
     */
    private SmtpError classifySmtpError(Exception e) {
        // Defaults
        SmtpError out = new SmtpError("EMAIL_SEND_FAILED", false);
        final String SMTP_ = "SMTP_";
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
                        if (code >= 500 && code < 600) return new SmtpError(SMTP_ + code, true);
                        if (code >= 400 && code < 500) return new SmtpError(SMTP_ + code, false);
                    }
                    // Without code: treat as transient network issue
                    return new SmtpError("SMTP_SEND_FAILED", false);
                }
                case MessagingException me -> {
                    var ne = me.getNextException();
                    if (ne != null) {
                        var code = smtpReplyCode(ne.getMessage());
                        if (code >= 500 && code < 600) return new SmtpError(SMTP_ + code, true);
                        if (code >= 400 && code < 500) return new SmtpError(SMTP_ + code, false);
                    }
                }
                default -> {
                  //return new SmtpError("SMTP_SEND_FAILED_UNMANAGED", false);
                }
            }
            cause = cause.getCause();
        }
        return out;
    }

    /**
     * Extract leading SMTP reply code from a provider message (e.g., "550 5.1.1 ...").
     */
    private int smtpReplyCode(String msg) {
        if (msg == null || msg.length() < 3) return -1;
        try {
            return Integer.parseInt(msg.substring(0, 3));
        } catch (NumberFormatException _) {
            return -1;
        }
    }

    private record Content(boolean html, String value) {
        static Content html(String v) {
            return new Content(true, v);
        }

        static Content plain(String v) {
            return new Content(false, v);
        }

        boolean isHtml() {
            return html;
        }
//    String value() { return value; }
    }

    private record SmtpError(String code, boolean permanent) {
    }
}
