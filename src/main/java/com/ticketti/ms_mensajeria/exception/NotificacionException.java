package com.ticketti.ms_mensajeria.exception;

/** Error específico al intentar enviar un correo */
public class NotificacionException extends RuntimeException {
    public NotificacionException(String mensaje) {
        super(mensaje);
    }
}
