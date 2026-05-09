package com.ticketti.ms_mensajeria.enums;

/**
 * Tipos de notificación que puede generar el sistema.
 * El Factory Method usa este enum para decidir qué
 * implementación de notificación construir.
 */
public enum TipoNotificacion {
    CONFIRMACION_COMPRA,   // correo + QR tras pago aprobado
    RECOMENDACION,         // solo si hay consentimiento explícito
    DEVOLUCION,            // notificación de reembolso aprobado/rechazado
    RECORDATORIO_EVENTO    // aviso "tu evento es mañana"
}