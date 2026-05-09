package com.ticketti.ms_mensajeria.factory;

import com.ticketti.ms_mensajeria.dto.*;
import com.ticketti.ms_mensajeria.enums.TipoNotificacion;
import com.ticketti.ms_mensajeria.model.NotificacionModel;
import org.springframework.stereotype.Component;

/**
 * Factory Method Pattern.
 *
 * Centraliza la construcción de objetos Notificacion.
 * Cada método factory sabe qué asunto, contenido
 * y tipo asignar según la operación solicitada.
 *
 * El servicio solo llama a la fábrica y recibe
 * una Notificacion lista — sin lógica de construcción.
 */
@Component
public class NotificationFactory {

    /**
     * Crea la notificación de confirmación de compra.
     * El contenido HTML se arma con los datos del ticket.
     */
    public NotificacionModel crearConfirmacionCompra(
            EnviarTicketRequestDTO dto) {
        return NotificacionModel.builder()
                .idUsuario(dto.getIdUsuario())
                .idCompra(dto.getIdCompra())
                .idEvento(dto.getIdEvento())
                .correoDestinatario(dto.getCorreoDestinatario())
                .nombreDestinatario(dto.getNombreDestinatario())
                .tipo(TipoNotificacion.CONFIRMACION_COMPRA)
                .asunto("Tu entrada para " + dto.getNombreEvento()
                        + " — Ticketti")
                .contenido(buildContenidoTicket(dto))
                .build();
    }

    /**
     * Crea la notificación de devolución.
     * El asunto varía según si fue aprobada o rechazada.
     */
    public NotificacionModel crearDevolucion(
            EnviarDevolucionRequestDTO dto) {
        String asunto = "APROBADA".equals(dto.getEstadoDevolucion())
                ? "Tu devolución fue aprobada — Ticketti"
                : "Actualización sobre tu solicitud de devolución — Ticketti";

        return NotificacionModel.builder()
                .idUsuario(dto.getIdUsuario())
                .idDevolucion(dto.getIdDevolucion())
                .correoDestinatario(dto.getCorreoDestinatario())
                .nombreDestinatario(dto.getNombreDestinatario())
                .tipo(TipoNotificacion.DEVOLUCION)
                .asunto(asunto)
                .contenido(buildContenidoDevolucion(dto))
                .build();
    }

    /**
     * Crea la notificación de recomendación personalizada.
     * Solo debe llamarse si tieneConsentimiento = true.
     */
    public NotificacionModel crearRecomendacion(
            EnviarRecomendacionRequestDTO dto) {
        return NotificacionModel.builder()
                .idUsuario(dto.getIdUsuario())
                .correoDestinatario(dto.getCorreoDestinatario())
                .nombreDestinatario(dto.getNombreDestinatario())
                .tipo(TipoNotificacion.RECOMENDACION)
                .asunto("Eventos que te pueden interesar — Ticketti")
                .contenido(buildContenidoRecomendacion(dto))
                .build();
    }

    /**
     * Crea el recordatorio de evento próximo.
     */
    public NotificacionModel crearRecordatorio(
            RecordatorioRequestDTO dto) {
        return NotificacionModel.builder()
                .idUsuario(dto.getIdUsuario())
                .idEvento(dto.getIdEvento())
                .correoDestinatario(dto.getCorreoDestinatario())
                .nombreDestinatario(dto.getNombreDestinatario())
                .tipo(TipoNotificacion.RECORDATORIO_EVENTO)
                .asunto("Mañana es tu evento: " + dto.getNombreEvento()
                        + " — Ticketti")
                .contenido(buildContenidoRecordatorio(dto))
                .build();
    }

    // --- Constructores de contenido HTML ---

    private String buildContenidoTicket(EnviarTicketRequestDTO dto) {
        return String.format("""
            <h2>¡Tu compra fue confirmada!</h2>
            <p>Hola %s,</p>
            <p>Tu entrada para <strong>%s</strong> está lista.</p>
            <ul>
              <li>Fecha: %s</li>
              <li>Lugar: %s</li>
              <li>Total pagado: $%s</li>
              <li>Donación: $%s a %s</li>
            </ul>
            <p>Tu código QR se adjunta a este correo.</p>
            """,
                dto.getNombreDestinatario(),
                dto.getNombreEvento(),
                dto.getFechaEvento(),
                dto.getLugarEvento(),
                dto.getMontoTotal(),
                dto.getMontoDonacion(),
                dto.getNombreCausa()
        );
    }

    private String buildContenidoDevolucion(
            EnviarDevolucionRequestDTO dto) {
        if ("APROBADA".equals(dto.getEstadoDevolucion())) {
            return String.format("""
                <h2>Devolución aprobada</h2>
                <p>Hola %s,</p>
                <p>Tu reembolso de <strong>$%s</strong> fue aprobado.</p>
                <p>Se acreditará en %d días hábiles.</p>
                """,
                    dto.getNombreDestinatario(),
                    dto.getMontoDevolucion(),
                    dto.getPlazoAcreditacion()
            );
        }
        return String.format("""
            <h2>Devolución no aprobada</h2>
            <p>Hola %s,</p>
            <p>Tu solicitud no pudo ser procesada: %s</p>
            """,
                dto.getNombreDestinatario(),
                dto.getMotivoRechazo()
        );
    }

    private String buildContenidoRecomendacion(
            EnviarRecomendacionRequestDTO dto) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "<h2>Hola %s, eventos para ti</h2><ul>",
                dto.getNombreDestinatario()));
        if (dto.getEventosRecomendados() != null) {
            dto.getEventosRecomendados().forEach(e ->
                    sb.append("<li>").append(e).append("</li>"));
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private String buildContenidoRecordatorio(
            RecordatorioRequestDTO dto) {
        return String.format("""
            <h2>Tu evento es mañana</h2>
            <p>Hola %s,</p>
            <p><strong>%s</strong> es mañana %s en %s.</p>
            <p>Recuerda traer tu QR de acceso.</p>
            """,
                dto.getNombreDestinatario(),
                dto.getNombreEvento(),
                dto.getFechaEvento(),
                dto.getLugarEvento()
        );
    }
}