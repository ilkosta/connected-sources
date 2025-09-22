package org.connected_sources.core.user;

import java.util.Optional;

public interface UserContactService {
  Optional<String> getTelegramChatId(Long userId);
}

