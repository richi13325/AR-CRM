package com.ar.crm2.whatsapp.application.canal.port.out;

import com.ar.crm2.model.vo.ContactoId;
import com.ar.crm2.model.vo.EmpresaId;

/**
 * Crea o reutiliza un Contacto del CRM general a partir de un teléfono visto
 * en WhatsApp (Evolution API). Vive en whatsapp/application porque es lo único
 * que este módulo necesita del módulo de Contactos: no conoce el resto de su
 * ciclo de vida (edición, estados, etc.).
 */
public interface UpsertContactoPort {

    /**
     * Busca un Contacto por (empresaId, telefono); si no existe, lo crea.
     *
     * @return el id del Contacto encontrado o creado, o null si telefono es null/blank.
     */
    ContactoId upsertPorTelefono(EmpresaId empresaId, String telefono, String nombre);

    /** Renombra un Contacto existente (rename manual desde el chat). No-op si no existe. */
    void actualizarNombre(ContactoId contactoId, String nombre);
}
