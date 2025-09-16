package org.connected_sources.notification.service;

import org.connected_sources.notification.service.CuratorContact;
import java.util.List;
import java.util.Optional;

public interface ContactInformationRepo {
    Optional<String> findPrimaryEmail(long userId);
    List<CuratorContact> curatorsPrimaryEmail();
}
