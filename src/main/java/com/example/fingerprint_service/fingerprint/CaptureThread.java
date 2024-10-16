package com.example.fingerprint_service.fingerprint;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;

@Component
public class CaptureThread {

    private static final Logger logger = LoggerFactory.getLogger(CaptureThread.class);

    public CompletableFuture<Reader.CaptureResult> capture(Reader reader, Fid.Format format,
            Reader.ImageProcessing imageProcessing) {
        logger.info("Starting capture thread for reader: {}", reader.GetDescription().name);
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Capturing image...");
                Reader.CaptureResult result = reader.Capture(format, imageProcessing,
                        reader.GetCapabilities().resolutions[0], -1);
                logger.info("Image captured. Quality: {}", result.quality);
                return result;
            } catch (UareUException e) {
                logger.error("Capture failed", e);
                throw new RuntimeException("Capture failed", e);
            }
        });
    }
}
