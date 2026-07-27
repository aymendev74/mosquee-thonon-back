package org.mosqueethonon.lock.service;

import org.mosqueethonon.lock.entity.LockEntity;
import org.mosqueethonon.lock.enums.ResourceTypeEnum;
import org.mosqueethonon.lock.v1.dto.LockResultDto;

public interface LockService {

    LockResultDto acquireLock(ResourceTypeEnum resourceType, Long resourceId, String username);

    void verifyLock(ResourceTypeEnum resourceType, Long resourceId, String username);

    void releaseLock(ResourceTypeEnum resourceType, Long resourceId, String username);

    void refreshLock(LockEntity lock);

    void cleanExpiredLocks();

}
