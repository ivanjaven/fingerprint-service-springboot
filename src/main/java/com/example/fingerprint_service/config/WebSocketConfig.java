package com.example.fingerprint_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.example.fingerprint_service.fingerprint.FingerprintWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

  private final FingerprintWebSocketHandler fingerprintWebSocketHandler;

  public WebSocketConfig(FingerprintWebSocketHandler fingerprintWebSocketHandler) {
    this.fingerprintWebSocketHandler = fingerprintWebSocketHandler;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(fingerprintWebSocketHandler, "/fingerprint-ws").setAllowedOrigins("*");
  }
}
