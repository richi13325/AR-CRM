package com.ar.crm2.whatsapp.application.canal.port.in;

import java.util.UUID;

public interface SyncChatsUseCase {
    int syncChats(UUID canalId);
}
