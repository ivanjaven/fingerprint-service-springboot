package com.example.fingerprint_service.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.fingerprint_service.model.Resident;

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
}
