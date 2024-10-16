package com.example.fingerprint_service.fingerprint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;

@Component
public class Selection {

  private static final Logger logger = LoggerFactory.getLogger(Selection.class);
  private ReaderCollection readerCollection;

  public Selection() {
    logger.info("Initializing Selection");
    try {
      readerCollection = UareUGlobal.GetReaderCollection();
      logger.info("Reader collection initialized");
    } catch (UareUException e) {
      logger.error("Failed to get reader collection", e);
      throw new RuntimeException("Failed to get reader collection", e);
    }
  }

  public Reader getFirstAvailableReader() throws UareUException {
    logger.info("Getting first available reader");
    readerCollection.GetReaders();
    if (readerCollection.isEmpty()) {
      logger.warn("No readers available");
      throw new RuntimeException("No readers available");
    }
    Reader reader = readerCollection.get(0);
    logger.info("First available reader: {}", reader.GetDescription().name);
    return reader;
  }
}
