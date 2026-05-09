package com.ticketti.ms_mensajeria.controller;

import com.ticketti.ms_mensajeria.dto.*;
import com.ticketti.ms_mensajeria.service.NotificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones",
        description = "Gestión de correos y notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    // POST /api/notificaciones/enviar-ticket
    @PostMapping("/enviar-ticket")
    @Operation(summary = "Enviar correo de confirmación con QR")
    public ResponseEntity<NotificacionResponseDTO> enviarTicket(
            @Valid @RequestBody EnviarTicketRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificacionService.enviarTicket(dto));
    }

    // POST /api/notificaciones/enviar-recomendacion
    @PostMapping("/enviar-recomendacion")
    @Operation(summary = "Enviar recomendaciones (requiere consentimiento)")
    public ResponseEntity<NotificacionResponseDTO> enviarRecomendacion(
            @Valid @RequestBody EnviarRecomendacionRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificacionService.enviarRecomendacion(dto));
    }

    // POST /api/notificaciones/enviar-devolucion/{idDevolucion}
    @PostMapping("/enviar-devolucion/{idDevolucion}")
    @Operation(summary = "Enviar notificación de devolución")
    public ResponseEntity<NotificacionResponseDTO> enviarDevolucion(
            @PathVariable Long idDevolucion,
            @Valid @RequestBody EnviarDevolucionRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificacionService
                        .enviarDevolucion(idDevolucion, dto));
    }

    // POST /api/notificaciones/recordatorio
    @PostMapping("/recordatorio")
    @Operation(summary = "Enviar recordatorio de evento próximo")
    public ResponseEntity<NotificacionResponseDTO> enviarRecordatorio(
            @Valid @RequestBody RecordatorioRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificacionService.enviarRecordatorio(dto));
    }

    // GET /api/notificaciones/historial/{idUsuario}
    @GetMapping("/historial/{idUsuario}")
    @Operation(summary = "Historial de notificaciones de un usuario")
    public ResponseEntity<List<NotificacionResponseDTO>> historial(
            @PathVariable Long idUsuario) {
        return ResponseEntity.ok(
                notificacionService.historial(idUsuario));
    }

    // GET /api/notificaciones/obtener/{id}
    @GetMapping("/obtener/{id}")
    @Operation(summary = "Obtener notificación por ID")
    public ResponseEntity<NotificacionResponseDTO> obtener(
            @PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.obtener(id));
    }

    // DELETE /api/notificaciones/cancelar/{id}
    @DeleteMapping("/cancelar/{id}")
    @Operation(summary = "Cancelar notificación pendiente")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        notificacionService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
