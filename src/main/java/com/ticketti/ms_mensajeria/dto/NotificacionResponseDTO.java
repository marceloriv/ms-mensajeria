package com.ticketti.ms_mensajeria.dto;

import com.ticketti.ms_mensajeria.enums.EstadoNotificacion;
import com.ticketti.ms_mensajeria.enums.TipoNotificacion;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificacionResponseDTO {
    private Long idNotificacion;
    private Long idUsuario;
    private Long idCompra;
    private Long idEvento;
    private String correoDestinatario;
    private TipoNotificacion tipo;
    private String asunto;
    private EstadoNotificacion estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;
}