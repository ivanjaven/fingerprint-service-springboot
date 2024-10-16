package com.example.fingerprint_service.fingerprint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fingerprint")
public class FingerprintController {

  private static final Logger logger = LoggerFactory.getLogger(FingerprintController.class);
  private final FingerprintService fingerprintService;

  public FingerprintController(FingerprintService fingerprintService) {
    this.fingerprintService = fingerprintService;
    logger.info("FingerprintController initialized");
  }

  @GetMapping("/capture")
  public ResponseEntity<String> captureFingerprint() {
    logger.info("Received request to capture fingerprint");
    try {
      String base64Image = fingerprintService.captureFingerprint();
      logger.info("Fingerprint captured successfully");
      return ResponseEntity.ok(base64Image);
    } catch (Exception e) {
      logger.error("Failed to capture fingerprint", e);
      return ResponseEntity.badRequest().body("Failed to capture fingerprint: " + e.getMessage());
    }
  }

  @GetMapping("/reader-info")
  public ResponseEntity<String> getReaderInfo() {
    logger.info("Received request to get reader info");
    try {
      String readerInfo = fingerprintService.getReaderInfo();
      logger.info("Reader info retrieved successfully: {}", readerInfo);
      return ResponseEntity.ok(readerInfo);
    } catch (Exception e) {
      logger.error("Failed to get reader info", e);
      return ResponseEntity.badRequest().body("Failed to get reader info: " + e.getMessage());
    }
  }
}
