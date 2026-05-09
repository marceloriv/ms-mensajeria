package com.ticketti.ms_mensajeria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EnviarDevolucionRequestDTO {

    @NotNull
    private Long idUsuario;

    @NotNull
    private Long idDevolucion;

    @Email @NotBlank
    private String correoDestinatario;

    @NotBlank
    private String nombreDestinatario;

    // APROBADA o RECHAZADA
    @NotBlank
    private String estadoDevolucion;

    @NotNull
    private BigDecimal montoDevolucion;

    // Días hábiles para acreditación
    private Integer plazoAcreditacion;

    private String motivoRechazo; // null si fue aprobada
}