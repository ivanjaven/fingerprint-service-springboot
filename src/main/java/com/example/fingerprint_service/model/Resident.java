package com.example.fingerprint_service.model;

public class Resident {
  private Long residentId;
  private String fullName;
  private String fingerprintBase64;
  private String imageBase64;

  public Resident() {
  }

  public Resident(Long residentId, String fullName, String fingerprintBase64, String imageBase64) {
    this.residentId = residentId;
    this.fullName = fullName;
    this.fingerprintBase64 = fingerprintBase64;
    this.imageBase64 = imageBase64;
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
}
