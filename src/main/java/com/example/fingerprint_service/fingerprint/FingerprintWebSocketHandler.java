package com.example.fingerprint_service.fingerprint;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;

@Component
public class FingerprintWebSocketHandler extends TextWebSocketHandler {

  private static final Logger logger = LoggerFactory.getLogger(FingerprintWebSocketHandler.class);
  private final Selection selection;
  private Reader reader;

  public FingerprintWebSocketHandler(Selection selection) {
    this.selection = selection;
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    logger.info("WebSocket connection established");
    reader = selection.getFirstAvailableReader();
    reader.Open(Reader.Priority.EXCLUSIVE);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    if ("capture".equals(message.getPayload())) {
      captureAndSend(session);
    }
  }

  private void captureAndSend(WebSocketSession session) throws IOException {
    try {
      logger.info("Capturing fingerprint...");
      Reader.CaptureResult result = reader.Capture(Fid.Format.ANSI_381_2004, Reader.ImageProcessing.IMG_PROC_DEFAULT,
          reader.GetCapabilities().resolutions[0], -1);

      if (result.image != null && result.quality == Reader.CaptureQuality.GOOD) {
        logger.info("Fingerprint captured successfully");
        Fid.Fiv view = result.image.getViews()[0];
        BufferedImage image = new BufferedImage(view.getWidth(), view.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        image.getRaster().setDataElements(0, 0, view.getWidth(), view.getHeight(), view.getImageData());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        String jsonResponse = String.format("{\"status\":\"success\",\"image\":\"%s\"}", base64Image);
        session.sendMessage(new TextMessage(jsonResponse));
        logger.info("Image sent to client. Size: {} bytes", imageBytes.length);
      } else {
        logger.warn("Capture failed or quality is not good. Quality: {}", result.quality);
        session.sendMessage(
            new TextMessage("{\"status\":\"failed\",\"message\":\"Capture failed: " + result.quality + "\"}"));
      }
    } catch (UareUException e) {
      logger.error("Error during capture", e);
      session.sendMessage(new TextMessage("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}"));
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
