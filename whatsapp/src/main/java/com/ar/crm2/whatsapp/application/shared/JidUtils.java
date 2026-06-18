package com.ar.crm2.whatsapp.application.shared;

public final class JidUtils {

    private JidUtils() {}

    /**
     * Extrae el número en limpio de un JID individual de WhatsApp. Devuelve
     * null para grupos (@g.us), difusión (@broadcast) o ids alternos de
     * Evolution (@lid), que no representan un contacto individual del CRM.
     */
    public static String limpiarTelefono(String remoteJid) {
        if (remoteJid == null || !remoteJid.endsWith("@s.whatsapp.net")) return null;
        return remoteJid.substring(0, remoteJid.indexOf('@'));
    }
}
