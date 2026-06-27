package com.ticketti.ms_mensajeria.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionContactoRequest {

    private String nombre;
    private String correo;
    private String asunto;
    private String mensaje;


}
