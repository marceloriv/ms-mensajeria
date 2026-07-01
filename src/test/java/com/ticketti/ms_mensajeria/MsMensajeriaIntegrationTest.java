package com.ticketti.ms_mensajeria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketti.ms_mensajeria.dto.EnviarDocumentoCausaRequestDTO;
import com.ticketti.ms_mensajeria.dto.NotificacionContactoRequest;
import com.ticketti.ms_mensajeria.enums.TipoNotificacion;
import com.ticketti.ms_mensajeria.model.NotificacionModel;
import com.ticketti.ms_mensajeria.repository.NotificacionRepository;
import com.ticketti.ms_mensajeria.service.MailAsyncSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true"
})
@WebAppConfiguration
@Transactional
class MsMensajeriaIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Mockear ConnectionFactory y el sender: sin broker ni SMTP real en tests.
    @MockitoBean
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private MailAsyncSender mailAsyncSender;

    /**
     * Reemplaza la factory de RabbitMQConfig por una con autoStartup=false:
     * los listeners se registran pero no intentan conectarse al broker.
     */
    @TestConfiguration
    static class RabbitTestConfig {
        @Bean
        @Primary
        SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
                ConnectionFactory connectionFactory) {
            SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
            factory.setConnectionFactory(connectionFactory);
            factory.setAutoStartup(false);
            return factory;
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        notificacionRepository.deleteAll();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private EnviarDocumentoCausaRequestDTO dtoDocumentoCausa(Long idCausa) {
        EnviarDocumentoCausaRequestDTO dto = new EnviarDocumentoCausaRequestDTO();
        dto.setIdCausa(idCausa);
        dto.setNombreCausa("Causa Test");
        dto.setNombreOrganizador("Organizador Test");
        dto.setArchivoBase64(Base64.getEncoder().encodeToString("contenido-pdf-test".getBytes()));
        dto.setNombreArchivo("respaldo.pdf");
        return dto;
    }

    // ── Tests: Documento de causa ─────────────────────────────────────────────

    @Test
    void enviarDocumentoCausa_datosValidos_retorna200YPersiste() throws Exception {
        mockMvc.perform(post("/api/v1/notificaciones/causa-documento")
                        .header("X-Usuario-Id", "1")
                        .header("X-Rol-Usuario-Id", "ORGANIZADOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoDocumentoCausa(1L))))
                .andExpect(status().isOk());

        List<NotificacionModel> notifs = notificacionRepository.findAll();
        assertThat(notifs).hasSize(1);
        assertThat(notifs.get(0).getTipo()).isEqualTo(TipoNotificacion.DOCUMENTO_CAUSA);
    }

    @Test
    void enviarDocumentoCausa_sinCamposObligatorios_retorna400() throws Exception {
        EnviarDocumentoCausaRequestDTO dto = new EnviarDocumentoCausaRequestDTO();
        dto.setIdCausa(null);

        mockMvc.perform(post("/api/v1/notificaciones/causa-documento")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void enviarDocumentoCausa_dosVecesMismaCausa_guardaDosSiTiposDistintos() throws Exception {
        // El UNIQUE está en (id_compra, tipo) — causa-documento usa id_compra=null
        // (no es una compra), así que pueden existir múltiples DOCUMENTO_CAUSA para
        // distintas causas sin violar el constraint.
        mockMvc.perform(post("/api/v1/notificaciones/causa-documento")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoDocumentoCausa(1L))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/notificaciones/causa-documento")
                        .header("X-Usuario-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoDocumentoCausa(2L))))
                .andExpect(status().isOk());

        assertThat(notificacionRepository.count()).isEqualTo(2);
    }

    // ── Tests: Formulario de contacto (endpoint público) ──────────────────────

    @Test
    void enviarContacto_datosValidos_retorna200YPersiste() throws Exception {
        NotificacionContactoRequest dto = new NotificacionContactoRequest();
        dto.setNombre("Juan Pérez");
        dto.setCorreo("juan@test.cl");
        dto.setMensaje("Consulta de prueba para el test de integración");

        mockMvc.perform(post("/api/v1/notificaciones/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        List<NotificacionModel> notifs = notificacionRepository.findAll();
        assertThat(notifs).hasSize(1);
        assertThat(notifs.get(0).getTipo()).isEqualTo(TipoNotificacion.CONTACTO);
    }

    @Test
    void enviarContacto_sinEmail_retorna400() throws Exception {
        NotificacionContactoRequest dto = new NotificacionContactoRequest();
        dto.setNombre("Sin Email");
        dto.setMensaje("Mensaje sin email");

        mockMvc.perform(post("/api/v1/notificaciones/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void enviarContacto_sinMensaje_retorna400() throws Exception {
        NotificacionContactoRequest dto = new NotificacionContactoRequest();
        dto.setNombre("Sin Mensaje");
        dto.setCorreo("test@test.cl");

        mockMvc.perform(post("/api/v1/notificaciones/contacto")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ── Tests: Historial de notificaciones ────────────────────────────────────

    @Test
    void historial_usuarioSinNotificaciones_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/v1/notificaciones/historial/999")
                        .header("X-Usuario-Id", "999")
                        .header("X-Rol-Usuario-Id", "CLIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void historial_usuarioConNotificacion_retornaHistorial() throws Exception {
        // causa-documento usa idUsuario=0L (notificación interna al equipo).
        // Para el historial de un comprador usamos enviarTicket que sí recibe idUsuario del DTO.
        com.ticketti.ms_mensajeria.dto.EnviarTicketRequestDTO ticketDto =
                new com.ticketti.ms_mensajeria.dto.EnviarTicketRequestDTO();
        ticketDto.setIdCompra(777L);
        ticketDto.setIdUsuario(5L);
        ticketDto.setIdEvento(10L);
        ticketDto.setCorreoDestinatario("user5@test.cl");
        ticketDto.setNombreDestinatario("Usuario Cinco");
        ticketDto.setNombreEvento("Evento Historial");
        ticketDto.setFechaEvento("2026-12-01");
        ticketDto.setLugarEvento("Lugar Test");
        ticketDto.setMontoTotal(new java.math.BigDecimal("50000"));
        ticketDto.setMontoDonacion(new java.math.BigDecimal("5000"));
        ticketDto.setNombreCausa("Causa Test");
        ticketDto.setCodigoQr("TICKETTI-777-10");

        mockMvc.perform(post("/api/v1/notificaciones/enviar-ticket")
                        .header("X-Usuario-Id", "5")
                        .header("X-Rol-Usuario-Id", "ADMINPLATAFORMA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ticketDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/notificaciones/historial/5")
                        .header("X-Usuario-Id", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("CONFIRMACION_COMPRA"));
    }

    // ── Tests: Constraint de unicidad (dedup de tickets) ─────────────────────

    @Test
    void enviarTicket_dosVecesParaMismaCompra_segundaLanzaBusinessException() throws Exception {
        // Este test verifica el UNIQUE(id_compra, tipo) para CONFIRMACION_COMPRA.
        // Se simula el escenario de duplicidad que puede ocurrir si RabbitMQ
        // reintenta la entrega del mismo evento de compra.
        com.ticketti.ms_mensajeria.dto.EnviarTicketRequestDTO dto =
                new com.ticketti.ms_mensajeria.dto.EnviarTicketRequestDTO();
        dto.setIdCompra(100L);
        dto.setIdUsuario(1L);
        dto.setIdEvento(10L);
        dto.setCorreoDestinatario("comprador@test.cl");
        dto.setNombreDestinatario("Comprador Test");
        dto.setNombreEvento("Evento Test");
        dto.setFechaEvento("2026-12-01");
        dto.setLugarEvento("Estadio Test");
        dto.setMontoTotal(new java.math.BigDecimal("50000"));
        dto.setMontoDonacion(new java.math.BigDecimal("5000"));
        dto.setNombreCausa("Causa Test");
        dto.setCodigoQr("TICKETTI-100-10");

        // Primera llamada: persiste notificación (201 Created)
        mockMvc.perform(post("/api/v1/notificaciones/enviar-ticket")
                        .header("X-Usuario-Id", "1")
                        .header("X-Rol-Usuario-Id", "ADMINPLATAFORMA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Segunda llamada para la misma compra: debe rechazarse (dedup)
        mockMvc.perform(post("/api/v1/notificaciones/enviar-ticket")
                        .header("X-Usuario-Id", "1")
                        .header("X-Rol-Usuario-Id", "ADMINPLATAFORMA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        // Solo debe existir 1 notificación en BD
        assertThat(notificacionRepository.count()).isEqualTo(1);
    }
}
