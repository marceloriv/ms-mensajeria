package com.ticketti.ms_mensajeria.service;

import com.ticketti.ms_mensajeria.config.MailConfig;
import com.ticketti.ms_mensajeria.dto.EnviarTicketRequestDTO;
import com.ticketti.ms_mensajeria.enums.EstadoNotificacion;
import com.ticketti.ms_mensajeria.enums.TipoNotificacion;
import com.ticketti.ms_mensajeria.exception.BusinessException;
import com.ticketti.ms_mensajeria.factory.NotificationFactory;
import com.ticketti.ms_mensajeria.model.NotificacionModel;
import com.ticketti.ms_mensajeria.repository.NotificacionRepository;
import com.ticketti.ms_mensajeria.repository.RegistroEnvioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de NotificacionService")
class NotificacionServiceTest {

    @Mock private NotificacionRepository notificacionRepository;
    @Mock private RegistroEnvioRepository registroEnvioRepository;
    @Mock private NotificationFactory factory;
    @Mock private QrService qrService;
    @Mock private JavaMailSender mailSender;
    @Mock private MailConfig mailConfig;

    @InjectMocks
    private NotificacionService notificacionService;

    private EnviarTicketRequestDTO ticketDto;
    private NotificacionModel notificacion;

    @BeforeEach
    void setUp() {
        ticketDto = new EnviarTicketRequestDTO();
        ticketDto.setIdUsuario(1L);
        ticketDto.setIdCompra(100L);
        ticketDto.setIdEvento(10L);
        ticketDto.setCorreoDestinatario("test@test.com");
        ticketDto.setNombreDestinatario("Test User");
        ticketDto.setNombreEvento("SKA-P Viña");
        ticketDto.setFechaEvento("08-05-2026");
        ticketDto.setLugarEvento("Viña del Mar");
        ticketDto.setMontoTotal(new BigDecimal("20000"));
        ticketDto.setMontoDonacion(new BigDecimal("2000"));
        ticketDto.setNombreCausa("Sanos y Salvos");
        ticketDto.setCodigoQr("TICKETTI-100-10");

        notificacion = NotificacionModel.builder()
                .idNotificacion(1L)
                .idUsuario(1L)
                .idCompra(100L)
                .correoDestinatario("test@test.com")
                .tipo(TipoNotificacion.CONFIRMACION_COMPRA)
                .asunto("Tu entrada para SKA-P Viña — Ticketti")
                .estado(EstadoNotificacion.PENDIENTE)
                .build();
    }

    @Test
    @DisplayName("Enviar ticket duplicado lanza BusinessException")
    void enviarTicket_duplicado_lanzaExcepcion() {
        when(notificacionRepository.existsByIdCompraAndTipo(
                100L, TipoNotificacion.CONFIRMACION_COMPRA))
                .thenReturn(true);

        assertThatThrownBy(() ->
                notificacionService.enviarTicket(ticketDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("100");

        verify(factory, never())
                .crearConfirmacionCompra(any());
    }

    @Test
    @DisplayName("Cancelar notificación enviada lanza BusinessException")
    void cancelar_notificacionEnviada_lanzaExcepcion() {
        notificacion.setEstado(EstadoNotificacion.ENVIADO);
        when(notificacionRepository.findById(1L))
                .thenReturn(java.util.Optional.of(notificacion));

        assertThatThrownBy(() ->
                notificacionService.cancelar(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("Cancelar notificación pendiente cambia estado")
    void cancelar_pendiente_exitoso() {
        when(notificacionRepository.findById(1L))
                .thenReturn(java.util.Optional.of(notificacion));
        when(notificacionRepository.save(any()))
                .thenReturn(notificacion);

        notificacionService.cancelar(1L);

        assertThat(notificacion.getEstado())
                .isEqualTo(EstadoNotificacion.CANCELADO);
        verify(notificacionRepository).save(notificacion);
    }

    @Test
    @DisplayName("Historial retorna lista de notificaciones")
    void historial_retornaLista() {
        when(notificacionRepository.findByIdUsuario(1L))
                .thenReturn(java.util.List.of(notificacion));

        var resultado = notificacionService.historial(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getIdUsuario()).isEqualTo(1L);
    }
}
