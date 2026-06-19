package com.ar.crm2.whatsapp.application.shared;

public final class JidUtils {

    private JidUtils() {}

    /**
     * Extrae el número en limpio de un JID individual de WhatsApp. Devuelve
     * null para grupos (@g.us), difusión (@broadcast) o ids alternos de
     * Evolution (@lid), que no representan un contacto individual del CRM.
     *
     * Normaliza a SOLO dígitos: toma la parte antes de '@', descarta el sufijo
     * de dispositivo tras ':' (ej. "5215512345678:12@s.whatsapp.net") y elimina
     * cualquier caracter no numérico. Así el mismo número siempre matchea aunque
     * Evolution lo mande con o sin sufijo de dispositivo.
     */
    public static String limpiarTelefono(String remoteJid) {
        if (remoteJid == null || !remoteJid.endsWith("@s.whatsapp.net")) return null;
        String local = remoteJid.substring(0, remoteJid.indexOf('@'));
        int colon = local.indexOf(':');
        if (colon >= 0) local = local.substring(0, colon);
        String soloDigitos = local.replaceAll("\\D", "");
        return soloDigitos.isBlank() ? null : soloDigitos;
    }
}
