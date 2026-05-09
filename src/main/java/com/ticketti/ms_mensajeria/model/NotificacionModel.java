package com.ticketti.ms_mensajeria.model;

import com.ticketti.ms_mensajeria.enums.EstadoNotificacion;
import com.ticketti.ms_mensajeria.enums.TipoNotificacion;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "notificacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long idNotificacion;

    // IDs lógicos de otros microservicios (NO son FK)
    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "id_compra")
    private Long idCompra;   // null para recordatorios/recomendaciones

    @Column(name = "id_evento")
    private Long idEvento;

    @Column(name = "id_devolucion")
    private Long idDevolucion;  // solo para tipo DEVOLUCION

    // Datos del destinatario (se guardan para el historial)
    @Column(name = "correo_destinatario", nullable = false, length = 100)
    private String correoDestinatario;

    @Column(name = "nombre_destinatario", length = 150)
    private String nombreDestinatario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoNotificacion tipo;

    @Column(name = "asunto", nullable = false, length = 200)
    private String asunto;

    // Contenido del cuerpo del correo (HTML)
    @Column(name = "contenido", columnDefinition = "TEXT")
    private String contenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoNotificacion estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    // Relación con los intentos de envío
    @OneToMany(mappedBy = "notificacion",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<RegistroEnvioModel> registros;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoNotificacion.PENDIENTE;
        }
    }
}
