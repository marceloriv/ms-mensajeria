package com.ticketti.ms_mensajeria.messaging;

import com.ticketti.ms_mensajeria.dto.EnviarTicketRequestDTO;
import com.ticketti.ms_mensajeria.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de MensajeriaConsumer")
class MensajeriaConsumerTest {

    @Mock private NotificacionService notificacionService;

    @InjectMocks
    private MensajeriaConsumer consumer;

    private CompraConfirmadaEvent evento;

    @BeforeEach
    void setUp() {
        evento = new CompraConfirmadaEvent();
        evento.setIdCarrito(100L);
        evento.setUsuarioId(5L);
        evento.setEventoId(10L);
        evento.setCorreoUsuario("pame@test.com");
        evento.setNombreUsuario("Pamela");
        evento.setNombreEvento("SKA-P Viña");
        evento.setFechaEvento("08-05-2026");
        evento.setLugarEvento("Viña del Mar");
        evento.setTotal(new BigDecimal("20000"));
        evento.setMontoDonacion(new BigDecimal("2000"));
        evento.setNombreCausa("Sanos y Salvos");
    }

    @Test
    @DisplayName("Evento válido delega al NotificacionService")
    void procesarEvento_valido_delegaAlService() {
        consumer.procesarCompraConfirmada(evento);

        verify(notificacionService, times(1))
                .enviarTicket(any(EnviarTicketRequestDTO.class));
    }

    @Test
    @DisplayName("Sin codigoQr genera código automático")
    void procesarEvento_sinCodigoQr_generaAuto() {
        evento.setCodigoQr(null);

        ArgumentCaptor<EnviarTicketRequestDTO> captor =
                ArgumentCaptor.forClass(EnviarTicketRequestDTO.class);

        consumer.procesarCompraConfirmada(evento);

        verify(notificacionService).enviarTicket(captor.capture());
        assertThat(captor.getValue().getCodigoQr())
                .isEqualTo("TICKETTI-100-10");
    }

    @Test
    @DisplayName("Error en service relanza excepción para reintento")
    void procesarEvento_errorService_relanzaExcepcion() {
        doThrow(new RuntimeException("SMTP caído"))
                .when(notificacionService)
                .enviarTicket(any());

        assertThatThrownBy(() ->
                consumer.procesarCompraConfirmada(evento))
                .isInstanceOf(RuntimeException.class);
    }
}
