package com.example.fingerprint_service.fingerprint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;

@Service
public class FingerprintService {

  private static final Logger logger = LoggerFactory.getLogger(FingerprintService.class);
  private final Capture capture;
  private final Selection selection;

  public FingerprintService(Capture capture, Selection selection) {
    this.capture = capture;
    this.selection = selection;
    logger.info("FingerprintService initialized");
  }

  public String captureFingerprint() throws UareUException {
    logger.info("Starting fingerprint capture process");
    Reader reader = selection.getFirstAvailableReader();
    try {
      logger.info("Reader selected: {}", reader.GetDescription().name);
      String result = capture.captureFingerprint(reader);
      logger.info("Fingerprint captured successfully");
      return result;
    } catch (Exception e) {
      logger.error("Error during fingerprint capture", e);
      throw new RuntimeException("Failed to capture fingerprint", e);
    }
  }

  public String getReaderInfo() {
    logger.info("Getting reader info");
    try {
      String readerInfo = selection.getFirstAvailableReader().GetDescription().name;
      logger.info("Reader info retrieved: {}", readerInfo);
      return readerInfo;
    } catch (Exception e) {
      logger.error("Error getting reader info", e);
      return "No reader available";
    }
  }
}
