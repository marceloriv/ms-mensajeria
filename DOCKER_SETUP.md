# MS-Mensajeria - Docker Setup

Este documento explica cómo ejecutar `ms-mensajeria` con Docker y Docker Compose.

## Archivos Docker

- **`Dockerfile`**: Imagen multi-stage (compile con Maven, ejecuta con Java 17 alpine)
- **`docker-compose.yml`**: Orquesta MySQL, RabbitMQ y la aplicación
- **`.env`**: Variables de entorno con credenciales (NO subir a Git)
- **`.env.example`**: Template para documentar qué variables se necesitan

## Quick Start

### 1. Configurar credenciales
Copia `.env.example` a `.env` y actualiza las contraseñas:

```bash
cp .env.example .env
# Edita .env y cambia las contraseñas sensibles
```

### 2. Levantar los servicios
```bash
docker compose up -d --build
```

Esto inicia:
- `ms-mensajeria-mysql` (puerto 3307)
- `ms-mensajeria-rabbitmq` (puertos 5673 AMQP, 15673 Management UI)
- `ms-mensajeria-app` (puerto 8085)

### 3. Verificar que todo está corriendo
```bash
docker compose ps
```

### 4. Ver logs de la aplicación
```bash
docker compose logs -f app
```

### 5. Detener servicios
```bash
docker compose down
```

## Mapeo de Puertos

| Servicio | Puerto Local | Puerto Contenedor |
|----------|-------------|------------------|
| MySQL | 3307 | 3306 |
| RabbitMQ AMQP | 5673 | 5672 |
| RabbitMQ UI | 15673 | 15672 |
| App | 8085 | 8085 |

## Variables de Entorno (`.env`)

Las siguientes variables **deben estar definidas** en `.env`:

- `MYSQL_ROOT_PASSWORD`: Contraseña root de MySQL
- `DB_PASSWORD`: Contraseña del usuario ticketti_app
- `MAIL_PASSWORD`: Contraseña de aplicación de Gmail (16 caracteres)

Las opcionales tienen valores por defecto:
- `DB_PORT`: 3307 (puerto local MySQL)
- `APP_PORT`: 8085
- `RABBITMQ_USER`: guest
- `RABBITMQ_PASS`: guest
- `MAIL_USERNAME`: notificaciones.ticketti@gmail.com
- `CONFIG_SERVER_URL`: http://host.docker.internal:8888
- `EUREKA_URL`: http://host.docker.internal:8761/eureka/

## Cómo funciona el mapeo de credenciales

1. Las variables del `.env` se inyectan a través del `docker-compose.yml`
2. El contenedor recibe `SPRING_DATASOURCE_PASSWORD`, `MAIL_PASSWORD`, etc.
3. Spring Boot interpreta `${VARIABLE_NAME}` en `application.yml` y carga los valores

Ejemplo:
```yaml
# application.yml
spring:
  mail:
    password: ${MAIL_PASSWORD:}  # Lee de la env var MAIL_PASSWORD
```

## Seguridad

✅ **Lo que está seguro:**
- `.env` está en `.gitignore` (no se sube al repo)
- `.env.example` sirve como template
- `application-local.yml` también en `.gitignore`

⚠️ **Consideraciones:**
- Las credenciales se pasan como variables de entorno (visibles en `docker inspect`)
- Para producción, usar Docker Secrets o HashiCorp Vault

## Troubleshooting

### Error: "Set MYSQL_ROOT_PASSWORD in .env"
→ Falta la variable obligatoria. Crea `.env` con las variables requeridas.

### MySQL tardía en iniciar
→ Docker espera el healthcheck. Verifica `docker compose logs mysql`

### La app no conecta a RabbitMQ
→ Revisa que RabbitMQ esté healthy: `docker compose logs rabbitmq`

### Cambiar configuración sin rebuild
→ Modifica `.env` y haz `docker compose up` de nuevo (solo reinicia, no rebuild)

## Proximos pasos

- Descomentar y configurar Config Server
- Integrar con ms-usuarios, ms-donaciones, etc.
- Setup de volúmenes persistentes para MySQL