package com.example.fingerprint_service.model;

public class StaffAuth {
  private Long residentId;
  private Long authId;
  private String username;
  private String role;
  private String fullName;
  private String fingerprintBase64;

  // Default constructor
  public StaffAuth() {
  }

  // Constructor with all fields
  public StaffAuth(Long residentId, Long authId, String username, String role, String fullName,
      String fingerprintBase64) {
    this.residentId = residentId;
    this.authId = authId;
    this.username = username;
    this.role = role;
    this.fullName = fullName;
    this.fingerprintBase64 = fingerprintBase64;
  }

  // Getters and Setters
  public Long getResidentId() {
    return residentId;
  }

  public void setResidentId(Long residentId) {
    this.residentId = residentId;
  }

  public Long getAuthId() {
    return authId;
  }

  public void setAuthId(Long authId) {
    this.authId = authId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getFingerprintBase64() {
    return fingerprintBase64;
  }

  public void setFingerprintBase64(String fingerprintBase64) {
    this.fingerprintBase64 = fingerprintBase64;
  }
}
