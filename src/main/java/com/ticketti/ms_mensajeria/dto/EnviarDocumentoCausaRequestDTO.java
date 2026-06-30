package com.ticketti.ms_mensajeria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * El organizador sube un PDF de respaldo de la causa social y, en vez de
 * almacenarlo (no hay disco persistente en ECS/Fargate), se reenvía por
 * correo al equipo Ticketti para validación manual.
 */
@Data
public class EnviarDocumentoCausaRequestDTO {

    @NotNull
    private Long idCausa;

    @NotBlank
    private String nombreCausa;

    @NotBlank
    private String nombreOrganizador;

    @NotBlank
    private String archivoBase64;

    @NotBlank
    private String nombreArchivo;
}
