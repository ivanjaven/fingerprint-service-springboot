package com.example.fingerprint_service.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.fingerprint_service.model.Resident;
import com.example.fingerprint_service.model.StaffAuth;

@Service
public class DatabaseService {
  private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public List<Resident> getAllResidents() {
    logger.info("Fetching all residents from the database");
    String sql = "SELECT resident_id, full_name, fingerprint_base64, image_base64 FROM residents";
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      Resident resident = new Resident();
      resident.setResidentId(rs.getLong("resident_id"));
      resident.setFullName(rs.getString("full_name"));
      resident.setFingerprintBase64(rs.getString("fingerprint_base64"));
      resident.setImageBase64(rs.getString("image_base64"));
      return resident;
    });
  }

  public List<StaffAuth> getAllStaffFingerprints() {
    logger.info("Fetching all staff fingerprints from the database");
    String sql = """
            SELECT a.auth_id, a.resident_id, a.username, a.role,
                   r.full_name, r.fingerprint_base64
            FROM auth a
            JOIN residents r ON a.resident_id = r.resident_id
            WHERE r.fingerprint_base64 IS NOT NULL
        """;

    return jdbcTemplate.query(sql, (rs, rowNum) -> {
      StaffAuth staff = new StaffAuth();
      staff.setAuthId(rs.getLong("auth_id"));
      staff.setResidentId(rs.getLong("resident_id"));
      staff.setUsername(rs.getString("username"));
      staff.setRole(rs.getString("role"));
      staff.setFullName(rs.getString("full_name"));
      staff.setFingerprintBase64(rs.getString("fingerprint_base64"));
      return staff;
    });
  }

  public String getStaffFingerprintByAuthId(Integer authId) {
    logger.info("Fetching staff fingerprint for auth_id: {}", authId);
    String sql = """
            SELECT r.fingerprint_base64
            FROM auth a
            JOIN residents r ON a.resident_id = r.resident_id
            WHERE a.auth_id = ?
        """;

    try {
      return jdbcTemplate.queryForObject(sql, String.class, authId);
    } catch (Exception e) {
      logger.error("Error fetching fingerprint for auth_id {}: {}", authId, e.getMessage());
      return null;
    }
  }
}
