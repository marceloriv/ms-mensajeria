package com.ticketti.ms_mensajeria.messaging;

import com.ticketti.ms_mensajeria.config.RabbitMQConfig;
import com.ticketti.ms_mensajeria.dto.EnviarDevolucionRequestDTO;
import com.ticketti.ms_mensajeria.dto.EnviarTicketRequestDTO;
import com.ticketti.ms_mensajeria.service.NotificacionService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer de RabbitMQ para MS-Mensajería.
 *
 * Escucha la queue "mensajeria.queue" y al recibir
 * el evento de compra confirmada, construye el DTO
 * y delega el envío al NotificacionService.
 *
 * Flujo:
 * MSCarrito publica → RabbitMQ exchange ticketti.exchange
 * → mensajeria.queue → MensajeriaConsumer
 * → NotificacionService → correo con QR al comprador
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MensajeriaConsumer {

    private final NotificacionService notificacionService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_MENSAJERIA)
    public void procesarCompraConfirmada(
            CompraConfirmadaEvent evento) {

        log.info("Evento recibido en mensajeria.queue: " +
                        "carrito={}, usuario={}",
                evento.getIdCarrito(),
                evento.getUsuarioId());

        if (evento.getCorreoUsuario() == null || evento.getNombreUsuario() == null) {
            log.warn("Evento pago.aprobado carrito {} descartado: " +
                    "correoUsuario o nombreUsuario nulos — revisar enriquecimiento en ms-carrito",
                    evento.getIdCarrito());
            return;
        }

        try {
            // Construir el DTO que espera el servicio
            EnviarTicketRequestDTO dto =
                    new EnviarTicketRequestDTO();

            dto.setIdUsuario(evento.getUsuarioId());
            dto.setIdCompra(evento.getIdCarrito());
            dto.setIdEvento(evento.getEventoId());
            dto.setCorreoDestinatario(evento.getCorreoUsuario());
            dto.setNombreDestinatario(evento.getNombreUsuario());
            dto.setNombreEvento(evento.getNombreEvento());
            dto.setFechaEvento(evento.getFechaEvento());
            dto.setLugarEvento(evento.getLugarEvento());
            dto.setMontoTotal(evento.getTotal());
            dto.setMontoDonacion(evento.getMontoDonacion());
            dto.setNombreCausa(evento.getNombreCausa());

            // Generar código QR único si no viene en el evento
            String codigoQr = evento.getCodigoQr() != null
                    ? evento.getCodigoQr()
                    : "TICKETTI-" + evento.getIdCarrito()
                      + "-" + evento.getEventoId();
            dto.setCodigoQr(codigoQr);

            notificacionService.enviarTicket(dto);

            log.info("Correo enviado exitosamente: carrito={}",
                    evento.getIdCarrito());

        } catch (Exception e) {
            log.error("Error procesando evento: carrito={}, " +
                            "error={}",
                    evento.getIdCarrito(), e.getMessage());
            // Re-lanzar para que RabbitMQ reintente
            throw e;
        }
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_DEVOLUCION)
    public void procesarDevolucion(CompraConfirmadaEvent evento) {
        log.info("Evento recibido en devolucion.queue: carrito={}, usuario={}",
                evento.getIdCarrito(), evento.getUsuarioId());

        if (evento.getCorreoUsuario() == null || evento.getNombreUsuario() == null) {
            log.warn("Evento compra.revertida carrito {} descartado: correo o nombre nulos",
                    evento.getIdCarrito());
            return;
        }

        try {
            BigDecimal total = evento.getTotal() != null ? evento.getTotal() : BigDecimal.ZERO;
            BigDecimal montoDonacion = evento.getMontoDonacion() != null
                    ? evento.getMontoDonacion() : BigDecimal.ZERO;
            BigDecimal montoDevolucion = total.subtract(montoDonacion)
                    .multiply(new BigDecimal("0.85"));

            EnviarDevolucionRequestDTO dto = new EnviarDevolucionRequestDTO();
            dto.setIdUsuario(evento.getUsuarioId());
            dto.setIdDevolucion(evento.getIdCarrito());
            dto.setCorreoDestinatario(evento.getCorreoUsuario());
            dto.setNombreDestinatario(evento.getNombreUsuario());
            dto.setEstadoDevolucion("APROBADA");
            dto.setMontoDevolucion(montoDevolucion);
            dto.setPlazoAcreditacion(5);

            notificacionService.enviarDevolucion(evento.getIdCarrito(), dto);

            log.info("Correo de devolución enviado: carrito={}", evento.getIdCarrito());

        } catch (Exception e) {
            log.error("Error procesando devolución: carrito={}, error={}",
                    evento.getIdCarrito(), e.getMessage());
            throw e;
        }
    }
}