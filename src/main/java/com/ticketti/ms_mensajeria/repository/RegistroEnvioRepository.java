package com.ticketti.ms_mensajeria.repository;

import com.ticketti.ms_mensajeria.model.RegistroEnvioModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistroEnvioRepository
        extends JpaRepository<RegistroEnvioModel, Long> {

    List<RegistroEnvioModel> findByNotificacion_IdNotificacion(
            Long idNotificacion);

    // Cuántos intentos fallidos tuvo una notificación
    long countByNotificacion_IdNotificacionAndExitosoFalse(
            Long idNotificacion);
}
