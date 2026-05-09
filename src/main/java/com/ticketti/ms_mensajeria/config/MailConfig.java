package com.ticketti.ms_mensajeria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

/**
 * Lee las propiedades de correo del application.yml
 * y las expone para usar en los servicios.
 */
@Configuration
@Getter
public class MailConfig {

    @Value("${spring.mail.username}")
    private String mailFrom;
}