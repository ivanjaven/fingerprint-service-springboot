// package com.example.fingerprint_service.service;

// import java.util.List;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.digitalpersona.uareu.Engine;
// import com.digitalpersona.uareu.Fid;
// import com.digitalpersona.uareu.Fmd;
// import com.digitalpersona.uareu.Reader;
// import com.digitalpersona.uareu.UareUException;
// import com.digitalpersona.uareu.UareUGlobal;
// import com.example.fingerprint_service.fingerprint.Selection;
// import com.example.fingerprint_service.model.Resident;

// @Service
// public class IdentificationService {
//     private static final Logger logger = LoggerFactory.getLogger(IdentificationService.class);

//     @Autowired
//     private DatabaseService databaseService;

//     @Autowired
//     private Selection selection;

//     public Resident identifyUser() throws UareUException {
//         logger.info("Starting user identification process");

//         // Capture the fingerprint
//         Fid capturedFid = captureFingerprint();
//         if (capturedFid == null) {
//             logger.error("Failed to capture fingerprint");
//             return null;
//         }

//         List<Resident> residents = databaseService.getAllResidents();
//         logger.info("Fetched {} residents from the database", residents.size());

//         Engine engine = UareUGlobal.GetEngine();

//         Fmd capturedFmd = engine.CreateFmd(capturedFid, Fmd.Format.ANSI_378_2004);

//         for (Resident resident : residents) {
//             try {
//                 Fmd storedFmd = createFmdFromBase64(engine, resident.getFingerprintBase64());
//                 int falsematch_rate = engine.Compare(capturedFmd, 0, storedFmd, 0);
//                 int target_falsematch_rate = Engine.PROBABILITY_ONE / 100000; // Target rate of 0.00001

//                 if (falsematch_rate < target_falsematch_rate) {
//                     logger.info("Match found! Resident ID: {}, Full Name: {}", resident.getResidentId(),
//                             resident.getFullName());
//                     return resident;
//                 }
//             } catch (UareUException e) {
//                 logger.error("Error comparing fingerprints", e);
//             }
//         }

//         logger.info("No matching resident found");
//         return null;
//     }

//     private Fid captureFingerprint() throws UareUException {
//         logger.info("Capturing fingerprint");
//         Reader reader = selection.getFirstAvailableReader();
//         reader.Open(Reader.Priority.COOPERATIVE);
//         try {
//             Reader.CaptureResult result = reader.Capture(Fid.Format.ANSI_381_2004,
//                     Reader.ImageProcessing.IMG_PROC_DEFAULT,
//                     reader.GetCapabilities().resolutions[0], -1);

//             if (result.image != null && result.quality == Reader.CaptureQuality.GOOD) {
//                 return result.image;
//             } else {
//                 logger.warn("Capture failed or quality is not good. Quality: {}", result.quality);
//                 return null;
//             }
//         } finally {
//             reader.Close();
//         }
//     }

//     private Fmd createFmdFromBase64(Engine engine, String base64Fingerprint) throws UareUException {
//         logger.info("Decoding fingerprint");
//         byte[] fingerprintData = java.util.Base64.getDecoder().decode(base64Fingerprint);
//         Fid fid = UareUGlobal.GetImporter().ImportFid(fingerprintData, Fid.Format.ANSI_381_2004);
//         logger.info("Converted to fmd");
//         return engine.CreateFmd(fid, Fmd.Format.ANSI_378_2004);
//     }
// }
