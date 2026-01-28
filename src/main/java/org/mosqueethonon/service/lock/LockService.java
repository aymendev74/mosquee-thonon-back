package org.mosqueethonon.service.lock;

import org.mosqueethonon.entity.LockEntity;
import org.mosqueethonon.enums.ResourceTypeEnum;
import org.mosqueethonon.v1.dto.lock.LockResultDto;

public interface LockService {

    LockResultDto acquireLock(ResourceTypeEnum resourceType, Long resourceId, String username);

    void verifyLock(ResourceTypeEnum resourceType, Long resourceId, String username);

    void releaseLock(ResourceTypeEnum resourceType, Long resourceId, String username);

    void refreshLock(LockEntity lock);

    void cleanExpiredLocks();

}
