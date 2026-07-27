package org.mosqueethonon.lock.exception;

import lombok.Getter;
import org.mosqueethonon.lock.v1.dto.LockResultDto;

@Getter
public class ResourceLockedException extends RuntimeException {

    private final LockResultDto lockResult;

    public ResourceLockedException(String message, LockResultDto lockResult) {
        super(message);
        this.lockResult = lockResult;
    }

}
