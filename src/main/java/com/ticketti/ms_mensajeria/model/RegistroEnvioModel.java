package com.ticketti.ms_mensajeria.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Registra cada intento de envío de una notificación.
 * Permite saber si falló el primer intento y cuándo
 * se reintentó exitosamente.
 */
@Entity
@Table(name = "registro_envio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroEnvioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Long idRegistro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_notificacion", nullable = false)
    private NotificacionModel notificacion;

    @Column(name = "fecha_intento", nullable = false)
    private LocalDateTime fechaIntento;

    @Column(name = "exitoso", nullable = false)
    private boolean exitoso;

    // Mensaje de error si falló (null si exitoso)
    @Column(name = "detalle_error", length = 500)
    private String detalleError;

    @PrePersist
    protected void onCreate() {
        this.fechaIntento = LocalDateTime.now();
    }
}