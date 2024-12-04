package com.example.fingerprint_service.model;

public class Resident {
  private Long residentId;
  private String fullName;
  private String fingerprintBase64;
  private String imageBase64;
  private Long authId;
  private String role;
  private String username;

  public Resident() {
  }

  public Resident(Long residentId, String fullName, String fingerprintBase64, String imageBase64,
      Long authId, String role, String username) {
    this.residentId = residentId;
    this.fullName = fullName;
    this.fingerprintBase64 = fingerprintBase64;
    this.imageBase64 = imageBase64;
    this.authId = authId;
    this.role = role;
    this.username = username;
  }

  public Long getResidentId() {
    return residentId;
  }

  public void setResidentId(Long residentId) {
    this.residentId = residentId;
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

  public String getImageBase64() {
    return imageBase64;
  }

  public void setImageBase64(String imageBase64) {
    this.imageBase64 = imageBase64;
  }

  public Long getAuthId() {
    return authId;
  }

  public void setAuthId(Long authId) {
    this.authId = authId;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
