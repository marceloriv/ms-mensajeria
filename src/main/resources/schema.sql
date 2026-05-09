-- MS-Mensajeria | mensajeria_db

CREATE DATABASE IF NOT EXISTS mensajeria_db
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mensajeria_db;

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
    fecha_creacion      DATETIME      NOT NULL,
    fecha_envio         DATETIME,
    INDEX idx_usuario (id_usuario),
    INDEX idx_compra   (id_compra),
    INDEX idx_evento   (id_evento)
    );

CREATE TABLE IF NOT EXISTS registro_envio (
                                              id_registro         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                              id_notificacion     BIGINT        NOT NULL,
                                              fecha_intento       DATETIME      NOT NULL,
                                              exitoso             TINYINT(1)    NOT NULL DEFAULT 0,
    detalle_error       VARCHAR(500),
    CONSTRAINT fk_registro_notif
    FOREIGN KEY (id_notificacion)
    REFERENCES notificacion(id_notificacion)
    );