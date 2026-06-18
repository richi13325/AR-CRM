package com.ar.crm2.whatsapp.domain.entity;

import com.ar.crm2.shared.DomainAssert;
import com.ar.crm2.whatsapp.domain.vo.BotId;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bot de n8n conectado al CRM (modelo "Agent Bot" de Chatwoot): recibe los mensajes
 * entrantes vía webhook saliente y responde llamando a la API del CRM con
 * {@code apiAccessToken}. Si canalId es null, el bot aplica a todos los canales.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class Bot {

    @EqualsAndHashCode.Include
    private final BotId id;

    private final String nombre;
    private final CanalWhatsappId canalId;     // nullable — null = aplica a todos los canales
    private final String webhookUrl;           // URL del nodo Webhook de n8n
    private final String apiAccessToken;        // token con el que el bot llama de vuelta al CRM
    private final boolean activo;
    private final LocalDateTime creadoEn;
    private final LocalDateTime actualizadoEn;

    public static Bot create(String nombre, CanalWhatsappId canalId, String webhookUrl) {
        DomainAssert.lengthBetween(nombre, "nombre", 1, 100);
        DomainAssert.link(webhookUrl, "webhookUrl");

        LocalDateTime now = LocalDateTime.now();
        return Bot.builder()
                .id(BotId.create())
                .nombre(nombre)
                .canalId(canalId)
                .webhookUrl(webhookUrl)
                .apiAccessToken(generarToken())
                .activo(true)
                .creadoEn(now)
                .actualizadoEn(now)
                .build();
    }

    public static Bot reconstitute(
            BotId id,
            String nombre,
            CanalWhatsappId canalId,
            String webhookUrl,
            String apiAccessToken,
            boolean activo,
            LocalDateTime creadoEn,
            LocalDateTime actualizadoEn
    ) {
        return Bot.builder()
                .id(id).nombre(nombre).canalId(canalId).webhookUrl(webhookUrl)
                .apiAccessToken(apiAccessToken).activo(activo)
                .creadoEn(creadoEn).actualizadoEn(actualizadoEn)
                .build();
    }

    public Bot editar(String nombre, CanalWhatsappId canalId, String webhookUrl) {
        DomainAssert.lengthBetween(nombre, "nombre", 1, 100);
        DomainAssert.link(webhookUrl, "webhookUrl");
        return toBuilder().nombre(nombre).canalId(canalId).webhookUrl(webhookUrl)
                .actualizadoEn(LocalDateTime.now()).build();
    }

    public Bot activar() {
        if (this.activo) return this;
        return toBuilder().activo(true).actualizadoEn(LocalDateTime.now()).build();
    }

    public Bot desactivar() {
        if (!this.activo) return this;
        return toBuilder().activo(false).actualizadoEn(LocalDateTime.now()).build();
    }

    /** True si este bot aplica al canal dado (canalId null = aplica a todos). */
    public boolean aplicaACanal(CanalWhatsappId otroCanalId) {
        return this.canalId == null || this.canalId.equals(otroCanalId);
    }

    private static String generarToken() {
        return UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
    }

    private BotBuilder toBuilder() {
        return Bot.builder()
                .id(this.id).nombre(this.nombre).canalId(this.canalId)
                .webhookUrl(this.webhookUrl).apiAccessToken(this.apiAccessToken)
                .activo(this.activo).creadoEn(this.creadoEn).actualizadoEn(this.actualizadoEn);
    }
}
