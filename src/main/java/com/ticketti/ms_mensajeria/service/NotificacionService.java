package com.ticketti.ms_mensajeria.service;

import com.ticketti.ms_mensajeria.config.MailConfig;
import com.ticketti.ms_mensajeria.dto.*;
import com.ticketti.ms_mensajeria.enums.EstadoNotificacion;
import com.ticketti.ms_mensajeria.enums.TipoNotificacion;
import com.ticketti.ms_mensajeria.exception.BusinessException;
import com.ticketti.ms_mensajeria.exception.ResourceNotFoundException;
import com.ticketti.ms_mensajeria.factory.NotificationFactory;
import com.ticketti.ms_mensajeria.model.NotificacionModel;
import com.ticketti.ms_mensajeria.model.RegistroEnvioModel;
import com.ticketti.ms_mensajeria.repository.NotificacionRepository;
import com.ticketti.ms_mensajeria.repository.RegistroEnvioRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final RegistroEnvioRepository registroEnvioRepository;
    private final NotificationFactory factory;
    private final QrService qrService;
    private final JavaMailSender mailSender;
    private final MailConfig mailConfig;

    /**
     * POST /enviar-ticket
     * Flujo: Factory crea notificación → guarda en BD →
     * genera QR → envía correo con QR adjunto → registra resultado
     */
    public NotificacionResponseDTO enviarTicket(
            EnviarTicketRequestDTO dto) {

        // Evitar duplicados: una confirmación por compra
        if (notificacionRepository.existsByIdCompraAndTipo(
                dto.getIdCompra(),
                TipoNotificacion.CONFIRMACION_COMPRA)) {
            throw new BusinessException(
                    "Ya se envió confirmación para la compra "
                            + dto.getIdCompra());
        }

        // 1. Factory construye la notificación
        NotificacionModel notif = factory.crearConfirmacionCompra(dto);
        notif = notificacionRepository.save(notif);

        // 2. Generar QR como imagen PNG
        byte[] qrBytes = qrService.generarQr(dto.getCodigoQr());

        // 3. Enviar correo con QR adjunto
        enviarCorreoConQr(notif, qrBytes, dto.getCodigoQr());

        return mapToResponse(notif);
    }

    /** POST /enviar-recomendacion */
    public NotificacionResponseDTO enviarRecomendacion(
            EnviarRecomendacionRequestDTO dto) {

        // Regla de negocio: solo si hay consentimiento explícito
        if (!Boolean.TRUE.equals(dto.getTieneConsentimiento())) {
            throw new BusinessException(
                    "No se puede enviar recomendación sin consentimiento");
        }

        NotificacionModel notif = factory.crearRecomendacion(dto);
        notif = notificacionRepository.save(notif);
        enviarCorreoSimple(notif);
        return mapToResponse(notif);
    }

    /** POST /enviar-devolucion/{idDevolucion} */
    public NotificacionResponseDTO enviarDevolucion(
            Long idDevolucion, EnviarDevolucionRequestDTO dto) {

        dto.setIdDevolucion(idDevolucion);
        NotificacionModel notif = factory.crearDevolucion(dto);
        notif = notificacionRepository.save(notif);
        enviarCorreoSimple(notif);
        return mapToResponse(notif);
    }

    /** POST /recordatorio */
    public NotificacionResponseDTO enviarRecordatorio(
            RecordatorioRequestDTO dto) {

        NotificacionModel notif = factory.crearRecordatorio(dto);
        notif = notificacionRepository.save(notif);
        enviarCorreoSimple(notif);
        return mapToResponse(notif);
    }

    /** GET /historial/{idUsuario} */
    public List<NotificacionResponseDTO> historial(Long idUsuario) {
        return notificacionRepository
                .findByIdUsuario(idUsuario)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /** GET /obtener/{id} */
    public NotificacionResponseDTO obtener(Long id) {
        NotificacionModel notif = notificacionRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notificacion", id));
        return mapToResponse(notif);
    }

    /** DELETE /cancelar/{id} */
    public void cancelar(Long id) {
        NotificacionModel notif = notificacionRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notificacion", id));

        if (notif.getEstado() == EstadoNotificacion.ENVIADO) {
            throw new BusinessException(
                    "No se puede cancelar una notificación ya enviada");
        }
        notif.setEstado(EstadoNotificacion.CANCELADO);
        notificacionRepository.save(notif);
    }

    // --- Métodos privados de envío ---

    private void enviarCorreoConQr(NotificacionModel notif,
                                   byte[] qrBytes, String nombreArchivoQr) {
        RegistroEnvioModel registro = RegistroEnvioModel.builder()
                .notificacion(notif)
                .build();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setFrom(mailConfig.getMailFrom());
            helper.setTo(notif.getCorreoDestinatario());
            helper.setSubject(notif.getAsunto());
            helper.setText(notif.getContenido(), true); // true = HTML

            // Adjuntar QR como archivo PNG
            helper.addAttachment(
                    "entrada-qr.png",
                    new ByteArrayResource(qrBytes));

            mailSender.send(message);

            // Marcar como enviado
            notif.setEstado(EstadoNotificacion.ENVIADO);
            notif.setFechaEnvio(LocalDateTime.now());
            notificacionRepository.save(notif);

            registro.setExitoso(true);
            log.info("Correo enviado a {}", notif.getCorreoDestinatario());

        } catch (MessagingException e) {
            notif.setEstado(EstadoNotificacion.FALLIDO);
            notificacionRepository.save(notif);

            registro.setExitoso(false);
            registro.setDetalleError(e.getMessage());
            log.error("Error enviando correo a {}: {}",
                    notif.getCorreoDestinatario(), e.getMessage());
        } finally {
            registroEnvioRepository.save(registro);
        }
    }

    private void enviarCorreoSimple(NotificacionModel notif) {
        RegistroEnvioModel registro = RegistroEnvioModel.builder()
                .notificacion(notif)
                .build();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, false, "UTF-8");

            helper.setFrom(mailConfig.getMailFrom());
            helper.setTo(notif.getCorreoDestinatario());
            helper.setSubject(notif.getAsunto());
            helper.setText(notif.getContenido(), true);

            mailSender.send(message);

            notif.setEstado(EstadoNotificacion.ENVIADO);
            notif.setFechaEnvio(LocalDateTime.now());
            notificacionRepository.save(notif);

            registro.setExitoso(true);
        } catch (MessagingException e) {
            notif.setEstado(EstadoNotificacion.FALLIDO);
            notificacionRepository.save(notif);

            registro.setExitoso(false);
            registro.setDetalleError(e.getMessage());
            log.error("Error enviando correo: {}", e.getMessage());
        } finally {
            registroEnvioRepository.save(registro);
        }
    }

    private NotificacionResponseDTO mapToResponse(
            NotificacionModel n) {
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