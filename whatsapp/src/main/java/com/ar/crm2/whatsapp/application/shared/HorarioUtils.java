package com.ar.crm2.whatsapp.application.shared;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Utilidad para el horario de atención de WhatsApp.
 * Días: CSV 1-7 (lunes=1 ... domingo=7), igual que DayOfWeek.getValue().
 * Horas: "HH:mm". Si el formato es inválido, se considera DENTRO de horario
 * (no bloquear ni spamear por una mala config).
 */
public final class HorarioUtils {

    private HorarioUtils() {}

    public static boolean dentroDeHorario(LocalDateTime momento, String inicio, String fin, String diasCsv) {
        try {
            int diaActual = momento.getDayOfWeek().getValue(); // 1=lunes ... 7=domingo
            boolean diaHabil = diasCsv != null && java.util.Arrays.stream(diasCsv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .anyMatch(s -> Integer.parseInt(s) == diaActual);
            if (!diaHabil) return false;

            LocalTime ahora = momento.toLocalTime();
            LocalTime ini = LocalTime.parse(inicio);
            LocalTime f = LocalTime.parse(fin);
            // Rango normal (ini < fin). No contemplamos horarios que cruzan medianoche.
            return !ahora.isBefore(ini) && !ahora.isAfter(f);
        } catch (Exception e) {
            return true; // config inválida: no aplicar autocontestador
        }
    }

    // Validación opcional de DayOfWeek (no usada directamente, referencia de mapeo).
    static int mondayFirst(DayOfWeek d) {
        return d.getValue();
    }
}
