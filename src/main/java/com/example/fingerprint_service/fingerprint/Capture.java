package com.example.fingerprint_service.fingerprint;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;

@Component
public class Capture {

  private static final Logger logger = LoggerFactory.getLogger(Capture.class);
  private final CaptureThread captureThread;

  public Capture(CaptureThread captureThread) {
    this.captureThread = captureThread;
    logger.info("Capture initialized");
  }

  public String captureFingerprint(Reader reader) {
    logger.info("Starting fingerprint capture for reader: {}", reader.GetDescription().name);
    try {
      reader.Open(Reader.Priority.COOPERATIVE);
      logger.info("Reader opened successfully");

      Reader.CaptureResult result = captureThread
          .capture(reader, Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT).get();
      logger.info("Capture completed. Quality: {}", result.quality);

      if (result.image != null && result.quality == Reader.CaptureQuality.GOOD) {
        byte[] imageData = result.image.getViews()[0].getImageData();
        String base64Image = Base64.getEncoder().encodeToString(imageData);
        logger.info("Fingerprint captured and converted to base64 successfully");
        return base64Image;
      } else {
        logger.warn("Capture failed or image quality is not good. Quality: {}", result.quality);
        throw new RuntimeException("Capture failed or image quality is not good");
      }
    } catch (Exception e) {
      logger.error("Failed to capture fingerprint", e);
      throw new RuntimeException("Failed to capture fingerprint", e);
    } finally {
      try {
        reader.Close();
        logger.info("Reader closed successfully");
      } catch (UareUException e) {
        logger.error("Error closing reader", e);
      }
    }
  }
}
