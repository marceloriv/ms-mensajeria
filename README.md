# MS-Mensajeria — Ticketti

Microservicio de notificaciones por correo electrónico. Gestiona el envío de confirmaciones de compra (con QR), recomendaciones, devoluciones, recordatorios de eventos y el formulario de contacto público.

## Stack Tecnológico

- **Lenguaje:** Java 17 + Spring Boot 3.4.5
- **Base de Datos:** MySQL 8 (`mensajeria_db`)
- **Mensajería:** RabbitMQ (consumer de `pago.aprobado`)
- **SMTP:** Gmail (`notificaciones.ticketti@gmail.com`)
- **QR:** ZXing (generación de códigos QR como imagen PNG)
- **Service Discovery:** Eureka Client
- **Config Remoto:** Spring Cloud Config Server
- **Documentación:** SpringDoc OpenAPI (Swagger UI)
- **Containerización:** Docker & Docker Compose

## Patrones Aplicados

- **Factory Method** — `NotificationFactory` centraliza la creación de `NotificacionModel` para cada tipo de notificación
- **Repository Pattern** — acceso a datos vía `JpaRepository`
- **Async Pattern** — el envío SMTP corre en threads de background (`@Async` vía `MailAsyncSender`) para que el endpoint responda de inmediato sin bloquear por latencia de red

---

## Endpoints REST

Todos los endpoints están bajo `/api/v1/notificaciones`.

| Método | Ruta | Acceso | Descripción |
|--------|------|--------|-------------|
| `POST` | `/enviar-ticket` | ADMINPLATAFORMA | Genera QR y envía confirmación de compra |
| `POST` | `/enviar-recomendacion` | ADMINPLATAFORMA | Envía recomendación (requiere consentimiento explícito) |
| `POST` | `/enviar-devolucion/{id}` | ADMINPLATAFORMA | Notifica devolución aprobada |
| `POST` | `/recordatorio` | ADMINPLATAFORMA | Recordatorio de evento próximo |
| `POST` | `/contacto` | **Público** | Formulario de contacto: notifica al equipo y envía auto-reply al remitente |
| `GET` | `/historial/{idUsuario}` | Autenticado | Historial de notificaciones de un usuario |
| `GET` | `/obtener/{id}` | ADMINPLATAFORMA | Detalle de una notificación |
| `DELETE` | `/cancelar/{id}` | ADMINPLATAFORMA | Cancela una notificación pendiente |

### Formulario de Contacto (`POST /contacto`)

Ruta pública — no requiere JWT. Recibe:

```json
{
  "nombre": "string",
  "correo": "string",
  "asunto": "string",
  "mensaje": "string"
}
```

Al recibir la request:
1. Persiste la notificación en BD (estado inicial `PENDIENTE`)
2. En background: envía correo al equipo Ticketti con los datos del formulario
3. En background: envía auto-reply de confirmación al remitente
4. Responde `200 OK` de inmediato (sin esperar al SMTP)

---

## Seguridad

`SecurityConfig` (microservicio propio):

- `/v3/api-docs/**`, `/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` → público (Swagger)
- `/actuator/health`, `/actuator/info` → público
- `GET /api/v1/notificaciones/**` → público (consumido internamente)
- `POST /api/v1/notificaciones/contacto` → **público**
- `POST /api/v1/notificaciones/**` → autenticado (JWT)
- `PUT`, `DELETE /api/v1/notificaciones/**` → autenticado

El BFF y el API Gateway tienen sus propias reglas de acceso por rol sobre estas mismas rutas.

---

## Tipos de Notificación (`TipoNotificacion`)

```java
CONFIRMACION_COMPRA  // Correo con QR adjunto al comprar entrada
RECOMENDACION        // Sugerencia de evento (requiere consentimiento)
DEVOLUCION           // Notificación de reembolso aprobado
RECORDATORIO_EVENTO  // Aviso previo al evento
CONTACTO             // Formulario de contacto (equipo + auto-reply)
```

---

## Integración con RabbitMQ

**Consumer:** escucha el evento de compra confirmada desde ms-carrito.

```yaml
Exchange:    ticketti.exchange (Topic)
Queue:       pago.aprobado
Routing Key: pago.aprobado
```

**Flujo al recibir el evento:**
1. `MensajeriaConsumer` recibe `CompraConfirmadaEvent`
2. `NotificationFactory.crearConfirmacionCompra()` construye la notificación
3. Se persiste en BD
4. `MailAsyncSender.enviarCorreoConQr()` genera el QR y envía el correo en background

---

## Envío Asíncrono de Emails

El componente `MailAsyncSender` contiene los métodos `@Async` de envío SMTP. El servicio llama a estos métodos y retorna sin bloquear:

```
POST /contacto
    │
    ├─ save(notificacion)          ← sincrónico (responde 200 aquí)
    ├─ enviarCorreoSimple(notif)   ← @Async (background)
    └─ enviarAutoReply(req)        ← @Async (background)
```

Si el SMTP falla (credenciales incorrectas, red, etc.), el estado de la notificación se actualiza a `FALLIDO` en BD y se registra en `RegistroEnvioModel`. El endpoint siempre devuelve 200.

---

## Configuración SMTP

La configuración de Gmail en `config-server/src/main/resources/config/ms-mensajeria.yml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
      mail.smtp.starttls.required: true
```

**Importante:** Gmail requiere una **Contraseña de Aplicación** (App Password) cuando la cuenta tiene verificación en 2 pasos activa. No usar la contraseña normal de la cuenta.

Para generar una App Password:
1. Google Account → Seguridad → Verificación en 2 pasos → Contraseñas de aplicaciones
2. Seleccionar "Correo" + "Otro dispositivo"
3. Copiar los 16 caracteres generados (con o sin espacios) al `.env`

---

## Ejecución

### Docker Compose (Recomendado)

```bash
# Levantar todos los servicios
docker compose up -d

# Ver logs de la app
docker compose logs -f app

# Rebuild solo la app (tras cambios en código)
docker compose up -d --build app

# Detener
docker compose down
```

**Servicios que se levantan:**
- MySQL 8 → `localhost:3308`
- MS-Mensajeria → `localhost:8085`

> RabbitMQ se espera externo en la red `ticketti-network` (levantado desde otro servicio).

### Local

```bash
mvn spring-boot:run
```

---

## Variables de Entorno (`.env`)

| Variable | Descripción |
|----------|-------------|
| `MYSQL_ROOT_PASSWORD` | Password root de MySQL |
| `MYSQL_DATABASE` | Nombre de la BD (`mensajeria_db`) |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la BD |
| `SPRING_DATASOURCE_PASSWORD` | Password de la BD |
| `RABBITMQ_USER` | Usuario de RabbitMQ |
| `RABBITMQ_PASS` | Password de RabbitMQ |
| `MAIL_USERNAME` | Dirección Gmail remitente |
| `MAIL_PASSWORD` | App Password de Gmail (16 caracteres) |
| `JWT_SECRET` | Secret compartido para validar JWT |

---

## Estructura del Proyecto

```
ms-mensajeria/
├── src/main/java/com/ticketti/ms_mensajeria/
│   ├── config/
│   │   ├── MailConfig.java            # Lee spring.mail.username
│   │   ├── OpenApiConfig.java         # Configuración Swagger
│   │   └── RabbitMQConfig.java        # Configuración colas
│   ├── controller/
│   │   └── NotificacionController.java
│   ├── dto/
│   │   ├── EnviarTicketRequestDTO.java
│   │   ├── EnviarRecomendacionRequestDTO.java
│   │   ├── EnviarDevolucionRequestDTO.java
│   │   ├── RecordatorioRequestDTO.java
│   │   ├── NotificacionContactoRequest.java  # DTO formulario de contacto
│   │   └── NotificacionResponseDTO.java
│   ├── enums/
│   │   ├── EstadoNotificacion.java    # PENDIENTE, ENVIADO, FALLIDO, CANCELADO
│   │   └── TipoNotificacion.java      # CONFIRMACION_COMPRA, RECOMENDACION, DEVOLUCION, RECORDATORIO_EVENTO, CONTACTO
│   ├── exception/
│   │   └── GlobalExceptionHandler.java
│   ├── factory/
│   │   └── NotificationFactory.java   # Factory Method: crearConfirmacionCompra, crearRecomendacion, crearDevolucion, crearRecordatorio, crearContacto
│   ├── messaging/
│   │   └── MensajeriaConsumer.java    # Listener pago.aprobado
│   ├── model/
│   │   ├── NotificacionModel.java
│   │   └── RegistroEnvioModel.java    # Log de intentos de envío
│   ├── repository/
│   │   ├── NotificacionRepository.java
│   │   └── RegistroEnvioRepository.java
│   ├── security/
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── JwtService.java
│   │   └── SecurityConfig.java
│   ├── service/
│   │   ├── MailAsyncSender.java       # Envío SMTP asíncrono (@Async)
│   │   ├── NotificacionService.java   # Lógica de negocio
│   │   └── QrService.java            # Generación de QR con ZXing
│   └── MsMensajeriaApplication.java  # @EnableAsync habilitado
├── src/main/resources/
│   ├── application.yml                # Solo import del config server
│   └── schema.sql
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

---

## Verificación

```bash
# Health check
curl http://localhost:8085/actuator/health

# Swagger UI
http://localhost:8085/swagger-ui.html

# Probar contacto (sin JWT)
curl -X POST http://localhost:8085/api/v1/notificaciones/contacto \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Test","correo":"test@test.com","asunto":"Prueba","mensaje":"Hola"}'
```

---

## Comandos Útiles

```bash
mvn clean install                       # Compilar y empaquetar
mvn test                                # Ejecutar tests
docker compose up -d --build app        # Rebuild y levantar
docker compose logs -f app              # Logs en tiempo real
docker compose down -v                  # Detener y eliminar volúmenes
```

---

## Equipo

Equipo 7 — DSY1106 — Duoc UC

**Última actualización:** 27 de junio de 2026
