package com.ar.crm2.adapter.out.email;

import com.ar.crm2.adapter.out.email.config.EmailProperties;
import com.ar.crm2.application.agenda.port.out.SendAgendaEmailPort;
import com.ar.crm2.model.entity.Agenda;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
public class AgendaEmailAdapter implements SendAgendaEmailPort {

    private final JavaMailSender mailSender;
    private final EmailProperties emailProperties;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void sendAgendaCreatedEmail(Agenda agenda, String recipientEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setFrom(emailProperties.getFromAddress());
        message.setSubject("Agenda programada: " + agenda.getAsunto());
        message.setText(buildCreatedEmailBody(agenda));

        log.info("Sending agenda created email for agenda {} to {}", agenda.getId().value(), recipientEmail);
        mailSender.send(message);
    }

    @Override
    public void sendReminderEmail(Agenda agenda, String recipientEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipientEmail);
        message.setFrom(emailProperties.getFromAddress());
        message.setSubject("Recordatorio: " + agenda.getAsunto());
        message.setText(buildReminderEmailBody(agenda));

        log.info("Sending reminder email for agenda {} to {}", agenda.getId().value(), recipientEmail);
        mailSender.send(message);
    }

    private String buildCreatedEmailBody(Agenda agenda) {
        StringBuilder body = new StringBuilder();
        body.append("Hola,\n\n");
        body.append("Se ha programado una nueva actividad en tu agenda:\n\n");
        body.append("Tipo: ").append(agenda.getTipo()).append("\n");
        body.append("Asunto: ").append(agenda.getAsunto()).append("\n");

        if (agenda.getDescripcion() != null && !agenda.getDescripcion().isBlank()) {
            body.append("Descripción: ").append(agenda.getDescripcion()).append("\n");
        }

        body.append("Fecha: ").append(agenda.getFecha().format(DATE_FORMAT)).append("\n");
        body.append("Hora de inicio: ").append(agenda.getHoraInicio().format(TIME_FORMAT)).append("\n");

        if (agenda.getHoraFin() != null) {
            body.append("Hora de fin: ").append(agenda.getHoraFin().format(TIME_FORMAT)).append("\n");
        }

        if (agenda.getUbicacion() != null && !agenda.getUbicacion().isBlank()) {
            body.append("Ubicación: ").append(agenda.getUbicacion()).append("\n");
        }

        if (agenda.getLinkVideollamada() != null && !agenda.getLinkVideollamada().isBlank()) {
            body.append("Link de videollamada: ").append(agenda.getLinkVideollamada()).append("\n");
        }

        body.append("\n---\n");
        body.append("Este es un correo de confirmación de CRM2.");

        return body.toString();
    }

    private String buildReminderEmailBody(Agenda agenda) {
        StringBuilder body = new StringBuilder();
        body.append("Hola,\n\n");
        body.append("Tienes un recordatorio para la siguiente actividad:\n\n");
        body.append("Tipo: ").append(agenda.getTipo()).append("\n");
        body.append("Asunto: ").append(agenda.getAsunto()).append("\n");

        if (agenda.getDescripcion() != null && !agenda.getDescripcion().isBlank()) {
            body.append("Descripción: ").append(agenda.getDescripcion()).append("\n");
        }

        body.append("Fecha: ").append(agenda.getFecha().format(DATE_FORMAT)).append("\n");
        body.append("Hora de inicio: ").append(agenda.getHoraInicio().format(TIME_FORMAT)).append("\n");

        if (agenda.getHoraFin() != null) {
            body.append("Hora de fin: ").append(agenda.getHoraFin().format(TIME_FORMAT)).append("\n");
        }

        if (agenda.getUbicacion() != null && !agenda.getUbicacion().isBlank()) {
            body.append("Ubicación: ").append(agenda.getUbicacion()).append("\n");
        }

        if (agenda.getLinkVideollamada() != null && !agenda.getLinkVideollamada().isBlank()) {
            body.append("Link de videollamada: ").append(agenda.getLinkVideollamada()).append("\n");
        }

        body.append("\n---\n");
        body.append("Este es un recordatorio automático de CRM2.");

        return body.toString();
    }
}
