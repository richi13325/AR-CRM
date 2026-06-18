package com.ar.crm2.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "crm2.wa")
public record WaProperties(String webhookApiKey, String webhookBaseUrl) {}
