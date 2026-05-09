# MS-Mensajeria — Ticketti

Microservicio de notificaciones. Escucha el evento de compra
confirmada, genera el QR de acceso y envia el correo al comprador.

## Stack
- Java 17 + Spring Boot 3.4.5
- MySQL 8 (BD mensajeria_db)
- RabbitMQ (consumer de pago.aprobado)
- SMTP Gmail (notificaciones.ticketti@gmail.com)
- ZXing (generacion de QR)
- Eureka Client + Spring Cloud Config
- Maven

## Patrones aplicados
- Repository Pattern (acceso a datos via JpaRepository)
- Factory Method (NotificationFactory)

## Orden de arranque
1. Config Server (puerto 8888)
2. Discovery Server / Eureka (puerto 8761)
3. RabbitMQ (puerto 5672)
4. ms-mensajeria (puerto 8085)

## Como ejecutar localmente
1. Copiar application-local.yml.example a application-local.yml
2. Completar MAIL_PASSWORD con la contrasena de aplicacion Gmail
3. mvn spring-boot:run -Dspring-boot.run.profiles=local

## Equipo 7 - DSY1106 - Duoc UC