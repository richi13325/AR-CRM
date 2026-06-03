package com.ar.crm2.adapter.out.scheduler;

import com.ar.crm2.application.agenda.port.in.SendAgendaRemindersUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgendaReminderScheduler {

    private final SendAgendaRemindersUseCase sendAgendaRemindersUseCase;

    @Scheduled(fixedDelayString = "${crm2.reminder.fixed-delay-ms:60000}")
    public void processDueReminders() {
        log.debug("Checking for due agenda reminders...");
        try {
            sendAgendaRemindersUseCase.sendDueReminders();
        } catch (Exception e) {
            log.error("Error processing agenda reminders: {}", e.getMessage(), e);
        }
    }
}
