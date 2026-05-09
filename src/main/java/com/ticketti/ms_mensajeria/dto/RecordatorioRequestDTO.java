package com.ticketti.ms_mensajeria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecordatorioRequestDTO {
    @NotNull  private Long idUsuario;
    @NotNull  private Long idEvento;
    @Email @NotBlank private String correoDestinatario;
    @NotBlank private String nombreDestinatario;
    @NotBlank private String nombreEvento;
    @NotBlank private String fechaEvento;
    @NotBlank private String lugarEvento;
}
