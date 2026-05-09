package com.ticketti.ms_mensajeria.service;

import com.ticketti.ms_mensajeria.exception.NotificacionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Tests de QrService")
class QrServiceTest {

    private final QrService qrService = new QrService();

    @Test
    @DisplayName("Genera QR con contenido válido")
    void generarQr_contenidoValido_retornaBytes() {
        byte[] resultado = qrService.generarQr("TICKETTI-100-10");

        assertThat(resultado).isNotNull();
        assertThat(resultado.length).isGreaterThan(0);
    }

    @Test
    @DisplayName("QR generado es imagen PNG válida")
    void generarQr_retornaPng() {
        byte[] resultado = qrService.generarQr("TEST-QR-CODE");

        // PNG empieza con estos bytes: 137 80 78 71
        assertThat(resultado[0]).isEqualTo((byte) 137);
        assertThat(resultado[1]).isEqualTo((byte) 80);  // P
        assertThat(resultado[2]).isEqualTo((byte) 78);  // N
        assertThat(resultado[3]).isEqualTo((byte) 71);  // G
    }

    @Test
    @DisplayName("Contenido vacío lanza excepción")
    void generarQr_contenidoVacio_lanzaExcepcion() {
        assertThatThrownBy(() -> qrService.generarQr(""))
                .isInstanceOf(NotificacionException.class);
    }
}
