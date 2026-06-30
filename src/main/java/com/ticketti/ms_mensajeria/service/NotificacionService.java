package com.ticketti.ms_mensajeria.service;

import com.ticketti.ms_mensajeria.config.MailConfig;
import com.ticketti.ms_mensajeria.dto.*;
import com.ticketti.ms_mensajeria.enums.EstadoNotificacion;
import com.ticketti.ms_mensajeria.enums.TipoNotificacion;
import com.ticketti.ms_mensajeria.exception.BusinessException;
import com.ticketti.ms_mensajeria.exception.ResourceNotFoundException;
import com.ticketti.ms_mensajeria.factory.NotificationFactory;
import com.ticketti.ms_mensajeria.model.NotificacionModel;
import com.ticketti.ms_mensajeria.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final NotificationFactory factory;
    private final QrService qrService;
    private final MailConfig mailConfig;
    private final MailAsyncSender mailAsyncSender;

    /**
     * POST /enviar-ticket
     * Flujo: Factory crea notificación → guarda en BD →
     * genera QR → envía correo con QR adjunto (asíncrono)
     */
    public NotificacionResponseDTO enviarTicket(EnviarTicketRequestDTO dto) {

        if (notificacionRepository.existsByIdCompraAndTipo(
                dto.getIdCompra(),
                TipoNotificacion.CONFIRMACION_COMPRA)) {
            throw new BusinessException(
                    "Ya se envió confirmación para la compra " + dto.getIdCompra());
        }

        NotificacionModel notif = factory.crearConfirmacionCompra(dto);
        notif = notificacionRepository.save(notif);

        byte[] qrBytes = qrService.generarQr(dto.getCodigoQr());
        mailAsyncSender.enviarCorreoConQr(notif, qrBytes);

        return mapToResponse(notif);
    }

    /** POST /enviar-recomendacion */
    public NotificacionResponseDTO enviarRecomendacion(EnviarRecomendacionRequestDTO dto) {

        if (!Boolean.TRUE.equals(dto.getTieneConsentimiento())) {
            throw new BusinessException(
                    "No se puede enviar recomendación sin consentimiento");
        }

        NotificacionModel notif = factory.crearRecomendacion(dto);
        notif = notificacionRepository.save(notif);
        mailAsyncSender.enviarCorreoSimple(notif);
        return mapToResponse(notif);
    }

    /** POST /enviar-devolucion/{idDevolucion} */
    public NotificacionResponseDTO enviarDevolucion(
            Long idDevolucion, EnviarDevolucionRequestDTO dto) {

        dto.setIdDevolucion(idDevolucion);
        NotificacionModel notif = factory.crearDevolucion(dto);
        notif = notificacionRepository.save(notif);
        mailAsyncSender.enviarCorreoSimple(notif);
        return mapToResponse(notif);
    }

    /** POST /recordatorio */
    public NotificacionResponseDTO enviarRecordatorio(RecordatorioRequestDTO dto) {

        NotificacionModel notif = factory.crearRecordatorio(dto);
        notif = notificacionRepository.save(notif);
        mailAsyncSender.enviarCorreoSimple(notif);
        return mapToResponse(notif);
    }

    /** POST /contacto — guarda en BD y dispara correos en background */
    public void enviarContacto(NotificacionContactoRequest req) {
        NotificacionModel notif = factory.crearContacto(req, mailConfig.getMailFrom());
        notif = notificacionRepository.save(notif);
        mailAsyncSender.enviarCorreoSimple(notif);
        mailAsyncSender.enviarAutoReply(req);
    }

    /**
     * POST /causa-documento — el PDF de respaldo de la causa social no se
     * persiste (no hay disco compartido en ECS/Fargate); se reenvía por
     * correo al equipo para validación manual.
     */
    public void enviarDocumentoCausa(EnviarDocumentoCausaRequestDTO dto) {
        NotificacionModel notif = factory.crearDocumentoCausa(dto, mailConfig.getMailFrom());
        notif = notificacionRepository.save(notif);
        byte[] archivoBytes = java.util.Base64.getDecoder().decode(dto.getArchivoBase64());
        mailAsyncSender.enviarDocumentoCausa(notif, archivoBytes, dto.getNombreArchivo());
    }

    /** GET /historial/{idUsuario} */
    public List<NotificacionResponseDTO> historial(Long idUsuario) {
        return notificacionRepository
                .findByIdUsuario(idUsuario)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /** GET /obtener/{id} */
    public NotificacionResponseDTO obtener(Long id) {
        NotificacionModel notif = notificacionRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", id));
        return mapToResponse(notif);
    }

    /** DELETE /cancelar/{id} */
    public void cancelar(Long id) {
        NotificacionModel notif = notificacionRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacion", id));

        if (notif.getEstado() == EstadoNotificacion.ENVIADO) {
            throw new BusinessException(
                    "No se puede cancelar una notificación ya enviada");
        }
        notif.setEstado(EstadoNotificacion.CANCELADO);
        notificacionRepository.save(notif);
    }

    private NotificacionResponseDTO mapToResponse(NotificacionModel n) {
        NotificacionResponseDTO dto = new NotificacionResponseDTO();
        dto.setIdNotificacion(n.getIdNotificacion());
        dto.setIdUsuario(n.getIdUsuario());
        dto.setIdCompra(n.getIdCompra());
        dto.setIdEvento(n.getIdEvento());
        dto.setCorreoDestinatario(n.getCorreoDestinatario());
        dto.setTipo(n.getTipo());
        dto.setAsunto(n.getAsunto());
        dto.setEstado(n.getEstado());
        dto.setFechaCreacion(n.getFechaCreacion());
        dto.setFechaEnvio(n.getFechaEnvio());
        return dto;
    }
}
