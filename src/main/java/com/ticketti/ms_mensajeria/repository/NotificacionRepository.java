package com.ticketti.ms_mensajeria.repository;

import com.ticketti.ms_mensajeria.enums.EstadoNotificacion;
import com.ticketti.ms_mensajeria.enums.TipoNotificacion;
import com.ticketti.ms_mensajeria.model.NotificacionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificacionRepository
        extends JpaRepository<NotificacionModel, Long> {

    // GET /historial/{id} — todos los correos de un usuario
    List<NotificacionModel> findByIdUsuario(Long idUsuario);

    // Filtrar por tipo para reportes
    List<NotificacionModel> findByIdUsuarioAndTipo(
            Long idUsuario, TipoNotificacion tipo);

    // Para el DELETE /cancelar/{id} — verificar estado antes
    List<NotificacionModel> findByIdEventoAndEstado(
            Long idEvento, EstadoNotificacion estado);

    // Verificar si ya se envió confirmación para una compra
    boolean existsByIdCompraAndTipo(
            Long idCompra, TipoNotificacion tipo);
}