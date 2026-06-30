package com.ticketti.ms_mensajeria.service;

import com.ticketti.ms_mensajeria.config.MailConfig;
import com.ticketti.ms_mensajeria.dto.NotificacionContactoRequest;
import com.ticketti.ms_mensajeria.enums.EstadoNotificacion;
import com.ticketti.ms_mensajeria.model.NotificacionModel;
import com.ticketti.ms_mensajeria.model.RegistroEnvioModel;
import com.ticketti.ms_mensajeria.repository.NotificacionRepository;
import com.ticketti.ms_mensajeria.repository.RegistroEnvioRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailAsyncSender {

    private final JavaMailSender mailSender;
    private final MailConfig mailConfig;
    private final NotificacionRepository notificacionRepository;
    private final RegistroEnvioRepository registroEnvioRepository;

    @Async
    public void enviarCorreoConQr(NotificacionModel notif, byte[] qrBytes) {
        RegistroEnvioModel registro = RegistroEnvioModel.builder()
                .notificacion(notif)
                .build();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailConfig.getMailFrom());
            helper.setTo(notif.getCorreoDestinatario());
            helper.setSubject(notif.getAsunto());
            helper.setText(notif.getContenido(), true);
            helper.addAttachment("entrada-qr.png", new ByteArrayResource(qrBytes));
            mailSender.send(message);
            notif.setEstado(EstadoNotificacion.ENVIADO);
            notif.setFechaEnvio(LocalDateTime.now());
            notificacionRepository.save(notif);
            registro.setExitoso(true);
            log.info("Correo con QR enviado a {}", notif.getCorreoDestinatario());
        } catch (Exception e) {
            notif.setEstado(EstadoNotificacion.FALLIDO);
            notificacionRepository.save(notif);
            registro.setExitoso(false);
            registro.setDetalleError(e.getMessage());
            log.error("Error enviando correo con QR a {}: {}", notif.getCorreoDestinatario(), e.getMessage());
        } finally {
            registroEnvioRepository.save(registro);
        }
    }

    @Async
    public void enviarDocumentoCausa(NotificacionModel notif, byte[] archivoBytes, String nombreArchivo) {
        RegistroEnvioModel registro = RegistroEnvioModel.builder()
                .notificacion(notif)
                .build();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailConfig.getMailFrom());
            helper.setTo(notif.getCorreoDestinatario());
            helper.setSubject(notif.getAsunto());
            helper.setText(notif.getContenido(), true);
            helper.addAttachment(nombreArchivo, new ByteArrayResource(archivoBytes));
            mailSender.send(message);
            notif.setEstado(EstadoNotificacion.ENVIADO);
            notif.setFechaEnvio(LocalDateTime.now());
            notificacionRepository.save(notif);
            registro.setExitoso(true);
            log.info("Documento de causa enviado a {}", notif.getCorreoDestinatario());
        } catch (Exception e) {
            notif.setEstado(EstadoNotificacion.FALLIDO);
            notificacionRepository.save(notif);
            registro.setExitoso(false);
            registro.setDetalleError(e.getMessage());
            log.error("Error enviando documento de causa a {}: {}", notif.getCorreoDestinatario(), e.getMessage());
        } finally {
            registroEnvioRepository.save(registro);
        }
    }

    @Async
    public void enviarCorreoSimple(NotificacionModel notif) {
        RegistroEnvioModel registro = RegistroEnvioModel.builder()
                .notificacion(notif)
                .build();
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(mailConfig.getMailFrom());
            helper.setTo(notif.getCorreoDestinatario());
            helper.setSubject(notif.getAsunto());
            helper.setText(notif.getContenido(), true);
            mailSender.send(message);
            notif.setEstado(EstadoNotificacion.ENVIADO);
            notif.setFechaEnvio(LocalDateTime.now());
            notificacionRepository.save(notif);
            registro.setExitoso(true);
        } catch (Exception e) {
            notif.setEstado(EstadoNotificacion.FALLIDO);
            notificacionRepository.save(notif);
            registro.setExitoso(false);
            registro.setDetalleError(e.getMessage());
            log.error("Error enviando correo a {}: {}", notif.getCorreoDestinatario(), e.getMessage());
        } finally {
            registroEnvioRepository.save(registro);
        }
    }

    @Async
    public void enviarAutoReply(NotificacionContactoRequest req) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(mailConfig.getMailFrom());
            helper.setTo(req.getCorreo());
            helper.setSubject("Recibimos tu mensaje — Ticketti");
            helper.setText(String.format("""
                <h2>¡Gracias por contactarnos, %s!</h2>
                <p>Hemos recibido tu consulta sobre <strong>"%s"</strong>.</p>
                <p>Nuestro equipo te responderá a la brevedad al correo <strong>%s</strong>.</p>
                <br>
                <p>El equipo Ticketti</p>
                """, req.getNombre(), req.getAsunto(), req.getCorreo()), true);
            mailSender.send(message);
            log.info("Auto-reply de contacto enviado a {}", req.getCorreo());
        } catch (Exception e) {
            log.error("Error enviando auto-reply de contacto a {}: {}", req.getCorreo(), e.getMessage());
        }
    }
}
