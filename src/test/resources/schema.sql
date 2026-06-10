-- Schema H2 compatible para tests
-- DATETIME -> TIMESTAMP (H2 no soporta DATETIME)
-- TINYINT(1) -> BOOLEAN (más compatible con H2)

CREATE TABLE IF NOT EXISTS notificacion (
                                            id_notificacion     BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            id_usuario          BIGINT        NOT NULL,
                                            id_compra           BIGINT,
                                            id_evento           BIGINT,
                                            id_devolucion       BIGINT,
                                            correo_destinatario VARCHAR(100)  NOT NULL,
    nombre_destinatario VARCHAR(150),
    tipo                VARCHAR(30)   NOT NULL,
    asunto              VARCHAR(200)  NOT NULL,
    contenido           TEXT,
    estado              VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    fecha_creacion      TIMESTAMP     NOT NULL,
    fecha_envio         TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS registro_envio (
                                              id_registro         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              id_notificacion     BIGINT        NOT NULL,
                                              fecha_intento       TIMESTAMP     NOT NULL,
                                              exitoso             BOOLEAN       NOT NULL DEFAULT FALSE,
                                              detalle_error       VARCHAR(500),
    CONSTRAINT fk_registro_notif
    FOREIGN KEY (id_notificacion)
    REFERENCES notificacion(id_notificacion)
    );