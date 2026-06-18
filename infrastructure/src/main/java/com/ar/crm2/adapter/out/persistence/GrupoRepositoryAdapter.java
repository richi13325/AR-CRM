package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.entity.GrupoEntity;
import com.ar.crm2.adapter.out.persistence.entity.MensajeGrupoEntity;
import com.ar.crm2.adapter.out.persistence.repository.GrupoRepository;
import com.ar.crm2.adapter.out.persistence.repository.MensajeGrupoRepository;
import com.ar.crm2.whatsapp.application.grupo.port.out.GrupoRepositoryPort;
import com.ar.crm2.whatsapp.application.grupo.port.out.MensajeGrupoRepositoryPort;
import com.ar.crm2.whatsapp.domain.entity.Grupo;
import com.ar.crm2.whatsapp.domain.entity.MensajeGrupo;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import com.ar.crm2.whatsapp.domain.vo.GrupoId;
import com.ar.crm2.whatsapp.domain.vo.MensajeGrupoId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class GrupoRepositoryAdapter implements GrupoRepositoryPort, MensajeGrupoRepositoryPort {

    private final GrupoRepository grupoRepository;
    private final MensajeGrupoRepository mensajeRepository;

    // ── GrupoRepositoryPort ──
    @Override
    public Grupo save(Grupo grupo) {
        return toGrupo(grupoRepository.save(toGrupoEntity(grupo)));
    }

    @Override
    public Optional<Grupo> findByJid(String jid) {
        return grupoRepository.findByJid(jid).map(this::toGrupo);
    }

    @Override
    public Optional<Grupo> findById(GrupoId id) {
        return grupoRepository.findById(id.value().toString()).map(this::toGrupo);
    }

    @Override
    public List<Grupo> findAll() {
        return grupoRepository.findAllByOrderByUltimoMensajeAtDesc().stream().map(this::toGrupo).toList();
    }

    // ── MensajeGrupoRepositoryPort ──
    @Override
    public MensajeGrupo save(MensajeGrupo mensaje) {
        return toMensaje(mensajeRepository.save(toMensajeEntity(mensaje)));
    }

    @Override
    public boolean existsByWaMessageId(String waMessageId) {
        return mensajeRepository.existsByWaMessageId(waMessageId);
    }

    @Override
    public List<MensajeGrupo> findByGrupoOrdenado(GrupoId grupoId) {
        return mensajeRepository.findByGrupoIdOrderByTimestampAsc(grupoId.value().toString())
                .stream().map(this::toMensaje).toList();
    }

    // ── Mapeo ──
    private GrupoEntity toGrupoEntity(Grupo d) {
        return GrupoEntity.builder()
                .id(d.getId().value().toString())
                .canalId(d.getCanalId() != null ? d.getCanalId().value().toString() : null)
                .jid(d.getJid())
                .nombre(d.getNombre())
                .noLeidos(d.getNoLeidos())
                .ultimoMensajeAt(d.getUltimoMensajeAt())
                .creadoEn(d.getCreadoEn())
                .build();
    }

    private Grupo toGrupo(GrupoEntity e) {
        return Grupo.reconstitute(
                GrupoId.from(UUID.fromString(e.getId())),
                e.getCanalId() != null ? CanalWhatsappId.from(UUID.fromString(e.getCanalId())) : null,
                e.getJid(), e.getNombre(), e.getNoLeidos(), e.getUltimoMensajeAt(), e.getCreadoEn());
    }

    private MensajeGrupoEntity toMensajeEntity(MensajeGrupo d) {
        return MensajeGrupoEntity.builder()
                .id(d.getId().value().toString())
                .grupoId(d.getGrupoId().value().toString())
                .direccion(d.getDireccion())
                .tipo(d.getTipo())
                .contenido(d.getContenido())
                .mediaUrl(d.getMediaUrl())
                .remitente(d.getRemitente())
                .remitenteTel(d.getRemitenteTel())
                .status(d.getStatus())
                .waMessageId(d.getWaMessageId())
                .timestamp(d.getTimestamp())
                .creadoEn(d.getCreadoEn())
                .build();
    }

    private MensajeGrupo toMensaje(MensajeGrupoEntity e) {
        return MensajeGrupo.reconstitute(
                MensajeGrupoId.from(UUID.fromString(e.getId())),
                GrupoId.from(UUID.fromString(e.getGrupoId())),
                e.getDireccion(), e.getTipo(), e.getContenido(), e.getMediaUrl(),
                e.getRemitente(), e.getRemitenteTel(), e.getStatus(),
                e.getWaMessageId(), e.getTimestamp(), e.getCreadoEn());
    }
}
