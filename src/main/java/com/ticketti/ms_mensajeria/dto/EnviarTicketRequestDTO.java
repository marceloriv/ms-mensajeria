package com.ticketti.ms_mensajeria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EnviarTicketRequestDTO {

    @NotNull
    private Long idUsuario;

    @NotNull
    private Long idCompra;

    @NotNull
    private Long idEvento;

    @Email @NotBlank
    private String correoDestinatario;

    @NotBlank
    private String nombreDestinatario;

    @NotBlank
    private String nombreEvento;

    @NotBlank
    private String fechaEvento;

    @NotBlank
    private String lugarEvento;

    @NotNull
    private BigDecimal montoTotal;

    @NotNull
    private BigDecimal montoDonacion;

    @NotBlank
    private String nombreCausa;

    // Contenido del QR (ej: "TICKETTI-compra-123-evento-42")
    @NotBlank
    private String codigoQr;
}