package org.connected_sources.notification;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    @Scheduled(cron = "0 0 6 * * ?")
    public void sendReminders() {
        // TODO: scan for pending registrations and send reminders
        System.out.println("Sending reminder emails for pending producer registrations...");
    }
}