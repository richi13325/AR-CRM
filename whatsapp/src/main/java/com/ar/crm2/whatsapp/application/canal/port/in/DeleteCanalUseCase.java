package com.ar.crm2.whatsapp.application.canal.port.in;

import java.util.UUID;

public interface DeleteCanalUseCase {
    void delete(UUID canalId);
}
