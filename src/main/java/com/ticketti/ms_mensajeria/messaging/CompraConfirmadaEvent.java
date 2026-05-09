package com.ticketti.ms_mensajeria.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Payload del evento publicado por MSCarrito.
 * Mapea los campos del CarritoDeCompras serializado.
 * @JsonIgnoreProperties permite campos extra sin romper
 * la deserialización.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CompraConfirmadaEvent {

    private Long idCarrito;          // = idCompra
    private Long usuarioId;
    private BigDecimal total;
    private BigDecimal montoDonacion;
    private Long causaSocialId;

    // Datos del comprador para el correo
    private String correoUsuario;
    private String nombreUsuario;

    // Detalles del evento para el correo
    private Long eventoId;
    private String nombreEvento;
    private String fechaEvento;
    private String lugarEvento;

    // Código único para el QR
    // Formato: "TICKETTI-{idCarrito}-{eventoId}"
    private String codigoQr;

    // Nombre de la causa elegida (para mostrar en correo)
    private String nombreCausa;
}