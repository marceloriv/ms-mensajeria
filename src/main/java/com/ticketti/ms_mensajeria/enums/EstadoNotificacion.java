package com.ticketti.ms_mensajeria.enums;

public enum EstadoNotificacion {
    PENDIENTE,   // en cola, aún no enviado
    ENVIADO,     // enviado exitosamente
    FALLIDO,     // error al enviar
    CANCELADO    // detenido (ej: evento suspendido)
}
