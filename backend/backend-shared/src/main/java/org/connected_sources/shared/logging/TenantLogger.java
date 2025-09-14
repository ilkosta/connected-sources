package org.connected_sources.shared.logging;

/**
 * tiny SPI to enable separate logging implementations
 */
public interface TenantLogger {
  enum Category { SECURITY, AUDIT, BUSINESS_EVENT, ACCESS, DIAGNOSTIC }

  enum Level {
    TRACE, DEBUG, INFO, WARN, ERROR;

//    public static Level fromString(String value) {
//      if (value == null) return INFO;
//      try {
//        return Level.valueOf(value.trim().toUpperCase());
//      } catch (IllegalArgumentException e) {
//        return INFO; // default fallback
//      }
//    }
  }

  void log(Category category, Level level, String message, java.util.Map<String,Object> data);
}

