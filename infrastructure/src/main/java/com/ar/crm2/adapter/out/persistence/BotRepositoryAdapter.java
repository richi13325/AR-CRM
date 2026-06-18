package com.ar.crm2.adapter.out.persistence;

import com.ar.crm2.adapter.out.persistence.mapper.BotMapper;
import com.ar.crm2.adapter.out.persistence.repository.BotRepository;
import com.ar.crm2.whatsapp.application.bot.port.out.DeleteBotByIdPort;
import com.ar.crm2.whatsapp.application.bot.port.out.FindAllBotsPort;
import com.ar.crm2.whatsapp.application.bot.port.out.FindBotActivoParaCanalPort;
import com.ar.crm2.whatsapp.application.bot.port.out.FindBotByIdPort;
import com.ar.crm2.whatsapp.application.bot.port.out.FindBotByTokenPort;
import com.ar.crm2.whatsapp.application.bot.port.out.SaveBotPort;
import com.ar.crm2.whatsapp.domain.entity.Bot;
import com.ar.crm2.whatsapp.domain.vo.BotId;
import com.ar.crm2.whatsapp.domain.vo.CanalWhatsappId;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BotRepositoryAdapter implements
        SaveBotPort, FindBotByIdPort, FindAllBotsPort, DeleteBotByIdPort,
        FindBotByTokenPort, FindBotActivoParaCanalPort {

    private final BotRepository repository;

    @Override
    public Bot save(Bot bot) {
        return BotMapper.toDomain(repository.save(BotMapper.toEntity(bot)));
    }

    @Override
    public Optional<Bot> findById(BotId id) {
        return repository.findById(id.value().toString()).map(BotMapper::toDomain);
    }

    @Override
    public List<Bot> findAll() {
        return repository.findAll().stream().map(BotMapper::toDomain).toList();
    }

    @Override
    public void deleteById(BotId id) {
        repository.deleteById(id.value().toString());
    }

    @Override
    public Optional<Bot> findByApiAccessToken(String apiAccessToken) {
        return repository.findByApiAccessToken(apiAccessToken).map(BotMapper::toDomain);
    }

    @Override
    public Optional<Bot> findActivoParaCanal(CanalWhatsappId canalId) {
        var especificos = repository.findByActivoTrueAndCanalId(canalId.value().toString());
        if (!especificos.isEmpty()) return Optional.of(BotMapper.toDomain(especificos.get(0)));

        var generales = repository.findByActivoTrueAndCanalIdIsNull();
        return generales.isEmpty() ? Optional.empty() : Optional.of(BotMapper.toDomain(generales.get(0)));
    }
}
