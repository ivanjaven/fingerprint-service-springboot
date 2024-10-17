// package com.example.fingerprint_service.fingerprint;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.stereotype.Service;

// import com.digitalpersona.uareu.Reader;
// import com.digitalpersona.uareu.UareUException;
// import com.example.fingerprint_service.model.Resident;
// import com.example.fingerprint_service.service.IdentificationService;

// @Service
// public class FingerprintService {
//   private static final Logger logger = LoggerFactory.getLogger(FingerprintService.class);

//   private final Capture capture;
//   private final Selection selection;
//   private final IdentificationService identificationService;

//   public FingerprintService(Capture capture, Selection selection, IdentificationService identificationService) {
//     this.capture = capture;
//     this.selection = selection;
//     this.identificationService = identificationService;
//     logger.info("FingerprintService initialized");
//   }

//   public String captureFingerprint() throws UareUException {
//     logger.info("Starting fingerprint capture process");
//     Reader reader = selection.getFirstAvailableReader();
//     try {
//       logger.info("Reader selected: {}", reader.GetDescription().name);
//       String result = capture.captureFingerprint(reader);
//       logger.info("Fingerprint captured successfully");
//       return result;
//     } catch (Exception e) {
//       logger.error("Error during fingerprint capture", e);
//       throw new RuntimeException("Failed to capture fingerprint", e);
//     }
//   }

//   public String getReaderInfo() {
//     logger.info("Getting reader info");
//     try {
//       String readerInfo = selection.getFirstAvailableReader().GetDescription().name;
//       logger.info("Reader info retrieved: {}", readerInfo);
//       return readerInfo;
//     } catch (Exception e) {
//       logger.error("Error getting reader info", e);
//       return "No reader available";
//     }
//   }

//   public Resident identifyUser() {
//     logger.info("Starting user identification process");
//     try {
//       Resident identifiedResident = identificationService.identifyUser();
//       if (identifiedResident != null) {
//         logger.info("User identified: Resident ID {}, Full Name: {}",
//             identifiedResident.getResidentId(), identifiedResident.getFullName());
//         return identifiedResident;
//       } else {
//         logger.info("No matching user found");
//         return null;
//       }
//     } catch (UareUException e) {
//       logger.error("Error during user identification", e);
//       throw new RuntimeException("Failed to identify user", e);
//     }
//   }
// }
