package com.ticketti.ms_mensajeria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Microservicio MS-Mensajería.
 * Responsable de:
 * - Generar el código QR único por entrada comprada
 * - Enviar el correo de confirmación al comprador
 * - Escuchar el evento pago.aprobado desde RabbitMQ
 *
 * Patrón aplicado: Factory Method (NotificationFactory)
 */
@SpringBootApplication
@EnableDiscoveryClient
public class MsMensajeriaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsMensajeriaApplication.class, args);
	}
}
