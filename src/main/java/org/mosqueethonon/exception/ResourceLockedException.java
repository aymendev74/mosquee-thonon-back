package org.mosqueethonon.exception;

import lombok.Getter;
import org.mosqueethonon.v1.dto.lock.LockResultDto;

@Getter
public class ResourceLockedException extends RuntimeException {

    private final LockResultDto lockResult;

    public ResourceLockedException(String message, LockResultDto lockResult) {
        super(message);
        this.lockResult = lockResult;
    }

}
