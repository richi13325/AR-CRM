package com.ar.crm2.adapter.out.email.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "crm2.mail")
public class EmailProperties {

    private String fromAddress;
    private String fromName = "CRM2 Notificaciones";
}
