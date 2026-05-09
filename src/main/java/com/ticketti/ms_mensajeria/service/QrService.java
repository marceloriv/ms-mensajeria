package com.ticketti.ms_mensajeria.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ticketti.ms_mensajeria.exception.NotificacionException;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Genera imágenes QR en memoria usando ZXing.
 * El QR contiene un código único por entrada que
 * el sistema de acceso al evento puede escanear.
 */
@Service
public class QrService {

    private static final int QR_WIDTH  = 300;
    private static final int QR_HEIGHT = 300;
    private static final String FORMAT  = "PNG";

    /**
     * Genera el QR como array de bytes PNG.
     *
     * @param contenido texto a codificar (ej: "TICKETTI-123-42")
     * @return imagen PNG en bytes, lista para adjuntar al correo
     */
    public byte[] generarQr(String contenido) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(
                    contenido,
                    BarcodeFormat.QR_CODE,
                    QR_WIDTH,
                    QR_HEIGHT);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, FORMAT, stream);
            return stream.toByteArray();

        } catch (WriterException | IOException e) {
            throw new NotificacionException(
                    "Error generando QR: " + e.getMessage());
        }
    }
}