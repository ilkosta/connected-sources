package org.connected_sources.notification.incident;

import java.util.Optional;

/**
 * Resolves a Redmine category name given a permanent error code/type.
 */
public interface CategoryResolver {
  public  Optional<String> resolve(String errorCode);
}