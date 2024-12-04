package com.example.fingerprint_service.fingerprint;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.example.fingerprint_service.model.Resident;
import com.example.fingerprint_service.model.StaffAuth;
import com.example.fingerprint_service.service.DatabaseService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class FingerprintWebSocketHandler extends TextWebSocketHandler {
  private static final Logger logger = LoggerFactory.getLogger(FingerprintWebSocketHandler.class);

  private final Selection selection;
  private final DatabaseService databaseService;
  private Reader reader;
  private final ObjectMapper objectMapper;

  public FingerprintWebSocketHandler(Selection selection, DatabaseService databaseService) {
    this.selection = selection;
    this.databaseService = databaseService;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    logger.info("WebSocket connection established");
    reader = selection.getFirstAvailableReader();
    reader.Open(Reader.Priority.EXCLUSIVE);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    String payload = message.getPayload();
    logger.info("Received message: {}", payload);

    if ("capture".equals(payload)) {
      captureAndSend(session);
    } else if ("identify".equals(payload)) {
      identifyAndSend(session);
    } else if ("identify_staff".equals(payload)) { // Add this case
      identifyStaffAndSend(session);
    } else {
      try {
        JsonNode jsonNode = objectMapper.readTree(payload);
        if (jsonNode.has("verify") && jsonNode.has("auth_id")) {
          Integer authId = jsonNode.get("auth_id").asInt();
          verifyStaffFingerprint(session, authId);
        }
      } catch (Exception e) {
        logger.error("Error processing verification message", e);
        session.sendMessage(new TextMessage("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
      }
    }
  }

  // Function to handle capturing and sending fingerprint in registration
  private void captureAndSend(WebSocketSession session) throws IOException {
    try {
      Reader.CaptureResult result = captureFingerprint();
      if (result != null && result.image != null) {
        Fid fid = result.image;

        // Create FMD for future comparisons
        Engine engine = UareUGlobal.GetEngine();
        Fmd fmd = engine.CreateFmd(fid, Fmd.Format.ANSI_378_2004);

        // Generate a secure representation of the FMD
        String secureData = generateSecureFmdData(fmd);

        // Convert image to base64 for display
        String base64Image = convertToBase64(fid);

        String jsonResponse = String.format("{\"status\":\"success\",\"image\":\"%s\",\"fmd\":\"%s\"}", base64Image,
            secureData);
        session.sendMessage(new TextMessage(jsonResponse));
      } else {
        session.sendMessage(new TextMessage("{\"status\":\"failed\",\"message\":\"Capture failed or low quality\"}"));
      }
    } catch (UareUException e) {
      logger.error("Error during capture", e);
      session.sendMessage(new TextMessage("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
    }
  }

  private StaffAuth identifyStaff() throws UareUException {
    logger.info("Starting staff identification process");

    Reader.CaptureResult captureResult = captureFingerprint();
    if (captureResult == null || captureResult.image == null) {
      logger.error("Failed to capture fingerprint");
      return null;
    }

    List<StaffAuth> staffList = databaseService.getAllStaffFingerprints();
    logger.info("Fetched {} staff from the database", staffList.size());

    Engine engine = UareUGlobal.GetEngine();
    Fmd capturedFmd = engine.CreateFmd(captureResult.image, Fmd.Format.ANSI_378_2004);

    for (StaffAuth staff : staffList) {
      try {
        Fmd storedFmd = createFmdFromStoredData(staff.getFingerprintBase64());
        if (storedFmd == null) {
          logger.warn("Invalid stored fingerprint data for staff: {}", staff.getAuthId());
          continue;
        }

        int falsematch_rate = engine.Compare(capturedFmd, 0, storedFmd, 0);
        int target_falsematch_rate = Engine.PROBABILITY_ONE / 100000;

        if (falsematch_rate < target_falsematch_rate) {
          logger.info("Staff match found! Auth ID: {}, Username: {}",
              staff.getAuthId(), staff.getUsername());
          return staff;
        }
      } catch (Exception e) {
        logger.error("Error comparing fingerprints for staff {}: {}",
            staff.getAuthId(), e.getMessage());
      }
    }

    logger.info("No matching staff found");
    return null;
  }

  // Function to handle identifying user
  private void identifyAndSend(WebSocketSession session) throws IOException {
    try {
      Resident identifiedResident = identifyUser();
      if (identifiedResident != null) {
        String jsonResponse = objectMapper.writeValueAsString(identifiedResident);
        session.sendMessage(new TextMessage("{\"status\":\"success\",\"resident\":" + jsonResponse + "}"));
      } else {
        session.sendMessage(new TextMessage("{\"status\":\"not_found\",\"message\":\"No matching user found\"}"));
      }
    } catch (UareUException e) {
      logger.error("Error during identification", e);
      session.sendMessage(new TextMessage("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
    }
  }

  private void identifyStaffAndSend(WebSocketSession session) throws IOException {
    try {
      StaffAuth identifiedStaff = identifyStaff();
      if (identifiedStaff != null) {
        // Need to send data in the same format as resident data
        String jsonResponse = objectMapper.writeValueAsString(Map.of(
            "status", "success",
            "resident", Map.of( // Changed from "staff" to "resident" to match existing client code
                "residentId", identifiedStaff.getResidentId(),
                "username", identifiedStaff.getUsername(),
                "role", identifiedStaff.getRole(),
                "authId", identifiedStaff.getAuthId(),
                "isStaff", true)));
        session.sendMessage(new TextMessage(jsonResponse));
      } else {
        session.sendMessage(new TextMessage("{\"status\":\"not_found\",\"message\":\"No matching staff found\"}"));
      }
    } catch (UareUException e) {
      logger.error("Error during staff identification", e);
      session.sendMessage(new TextMessage("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
    }
  }

  // Main function use to capture fingerprint
  private Reader.CaptureResult captureFingerprint() throws UareUException {
    logger.info("Capturing fingerprint...");
    Reader.CaptureResult result = reader.Capture(Fid.Format.ANSI_381_2004,
        Reader.ImageProcessing.IMG_PROC_DEFAULT,
        reader.GetCapabilities().resolutions[0], -1);

    if (result.image != null && result.quality == Reader.CaptureQuality.GOOD) {
      logger.info("Fingerprint captured successfully");
      return result;
    } else {
      logger.warn("Capture failed or quality is not good. Quality: {}", result.quality);
      return null;
    }
  }

  private String convertToBase64(Fid fid) throws IOException {
    Fid.Fiv view = fid.getViews()[0];
    BufferedImage bufferedImage = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    bufferedImage.getRaster().setDataElements(0, 0, view.getWidth(), view.getHeight(), view.getImageData());
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(bufferedImage, "png", baos);
    return Base64.getEncoder().encodeToString(baos.toByteArray());
  }

  // Main function use to identify user in the database based on fingerprint
  private Resident identifyUser() throws UareUException {
    logger.info("Starting user identification process");

    Reader.CaptureResult captureResult = captureFingerprint();
    if (captureResult == null || captureResult.image == null) {
      logger.error("Failed to capture fingerprint");
      return null;
    }

    List<Resident> residents = databaseService.getAllResidents();
    logger.info("Fetched {} residents from the database", residents.size());

    Engine engine = UareUGlobal.GetEngine();

    Fmd capturedFmd = engine.CreateFmd(captureResult.image, Fmd.Format.ANSI_378_2004);

    for (Resident resident : residents) {
      try {
        Fmd storedFmd = createFmdFromStoredData(resident.getFingerprintBase64());
        if (storedFmd == null) {
          logger.warn("Invalid stored fingerprint data for resident: {}", resident.getResidentId());
          continue;
        }

        int falsematch_rate = engine.Compare(capturedFmd, 0, storedFmd, 0);
        int target_falsematch_rate = Engine.PROBABILITY_ONE / 100000; // Adjust this value as needed

        logger.info("Comparison result for resident {}: falsematch_rate = {}, target = {}",
            resident.getResidentId(), falsematch_rate, target_falsematch_rate);

        if (falsematch_rate < target_falsematch_rate) {
          logger.info("Match found! Resident ID: {}, Full Name: {}", resident.getResidentId(),
              resident.getFullName());
          return resident;
        }
      } catch (Exception e) {
        logger.error("Error comparing fingerprints for resident {}: {}", resident.getResidentId(), e.getMessage());
      }
    }

    logger.info("No matching resident found");
    return null;
  }

  private void verifyStaffFingerprint(WebSocketSession session, Integer authId) throws IOException {
    try {
      // Get stored fingerprint data from database
      String storedFmdString = databaseService.getStaffFingerprintByAuthId(authId);
      if (storedFmdString == null) {
        String response = objectMapper.writeValueAsString(Map.of(
            "status", "error",
            "message", "No fingerprint found"));
        session.sendMessage(new TextMessage(response));
        return;
      }

      // Capture current fingerprint
      Reader.CaptureResult captureResult = captureFingerprint();
      if (captureResult == null || captureResult.image == null) {
        String response = objectMapper.writeValueAsString(Map.of(
            "status", "error",
            "message", "Failed to capture fingerprint"));
        session.sendMessage(new TextMessage(response));
        return;
      }

      try {
        Engine engine = UareUGlobal.GetEngine();
        // Create FMD from captured fingerprint
        Fmd capturedFmd = engine.CreateFmd(captureResult.image, Fmd.Format.ANSI_378_2004);
        // Import stored FMD
        Fmd storedFmd = UareUGlobal.GetImporter().ImportFmd(
            Base64.getDecoder().decode(storedFmdString),
            Fmd.Format.ANSI_378_2004,
            Fmd.Format.ANSI_378_2004);

        int falsematch_rate = engine.Compare(capturedFmd, 0, storedFmd, 0);
        int target_falsematch_rate = Engine.PROBABILITY_ONE / 100000;

        boolean isMatch = falsematch_rate < target_falsematch_rate;

        String message = isMatch ? "Fingerprint matched successfully" : "Fingerprint does not match";

        // Send response
        String response = objectMapper.writeValueAsString(Map.of(
            "status", "success",
            "verified", isMatch,
            "message", message));
        session.sendMessage(new TextMessage(response));

      } catch (Exception e) {
        logger.error("Error comparing fingerprints: ", e);
        String response = objectMapper.writeValueAsString(Map.of(
            "status", "error",
            "message", "Error comparing fingerprints"));
        session.sendMessage(new TextMessage(response));
      }

    } catch (UareUException e) {
      logger.error("Error during verification: ", e);
      String response = objectMapper.writeValueAsString(Map.of(
          "status", "error",
          "message", "Error during verification"));
      session.sendMessage(new TextMessage(response));
    }
  }

  private String generateSecureFmdData(Fmd fmd) {
    try {
      byte[] fmdData = fmd.getData();
      return Base64.getEncoder().encodeToString(fmdData);
    } catch (Exception e) {
      throw new RuntimeException("Error generating secure FMD data", e);
    }
  }

  private Fmd createFmdFromStoredData(String storedFmdData) {
    try {
      byte[] fmdData = Base64.getDecoder().decode(storedFmdData);
      return UareUGlobal.GetImporter().ImportFmd(fmdData, Fmd.Format.ANSI_378_2004, Fmd.Format.ANSI_378_2004);
    } catch (Exception e) {
      logger.error("Error creating FMD from stored data: {}", e.getMessage());
      return null;
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status)
      throws Exception {
    if (reader != null) {
      reader.Close();
    }
  }
}
