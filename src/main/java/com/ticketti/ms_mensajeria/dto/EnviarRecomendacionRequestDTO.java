package com.ticketti.ms_mensajeria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class EnviarRecomendacionRequestDTO {

    @NotNull
    private Long idUsuario;

    @Email @NotBlank
    private String correoDestinatario;

    @NotBlank
    private String nombreDestinatario;

    // Solo se envía si consentimiento = true
    @NotNull
    private Boolean tieneConsentimiento;

    private List<String> eventosRecomendados;
}