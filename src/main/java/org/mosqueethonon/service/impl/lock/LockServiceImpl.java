package org.mosqueethonon.service.impl.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.entity.LockEntity;
import org.mosqueethonon.enums.ResourceTypeEnum;
import org.mosqueethonon.exception.ResourceLockedException;
import org.mosqueethonon.repository.LockRepository;
import org.mosqueethonon.service.lock.LockService;
import org.mosqueethonon.v1.dto.lock.LockResultDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockServiceImpl implements LockService {

    private final LockRepository lockRepository;

    private static final int DEFAULT_LOCK_DURATION_MINUTES = 30;

    @Transactional
    public LockResultDto acquireLock(ResourceTypeEnum resourceType, Long resourceId, String username) {
        cleanExpiredLocks();

        Optional<LockEntity> existingLock = lockRepository.findByResourceTypeAndResourceId(resourceType, resourceId);

        if (existingLock.isPresent()) {
            LockEntity lock = existingLock.get();
            if (lock.getExpiresAt().isAfter(LocalDateTime.now())) {
                if (!lock.getLockedBy().equals(username)) {
                    log.warn("Resource {}:{} is already locked by user {}", resourceType, resourceId, lock.getLockedBy());
                    LockResultDto lockResult = buildLockResultDto(lock, false);
                    throw new ResourceLockedException(
                            String.format("La ressource %s avec l'ID %d est déjà verrouillée par un autre utilisateur",
                                    resourceType, resourceId),
                            lockResult
                    );
                }
                log.debug("User {} already owns the lock for resource {}:{}", username, resourceType, resourceId);
                refreshLock(lock);
                return buildLockResultDto(lock, true);
            }
            lockRepository.delete(lock);
        }

        LockEntity newLock = LockEntity.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .lockedBy(username)
                .lockedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(DEFAULT_LOCK_DURATION_MINUTES))
                .build();

        try {
            lockRepository.save(newLock);
            log.info("Lock acquired for resource {}:{} by user {}", resourceType, resourceId, username);
            return buildLockResultDto(newLock, true);
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent lock acquisition detected for resource {}:{}, retrying...", resourceType, resourceId);
            Optional<LockEntity> concurrentLock = lockRepository.findByResourceTypeAndResourceId(resourceType, resourceId);
            if (concurrentLock.isPresent()) {
                LockEntity lock = concurrentLock.get();
                if (!lock.getLockedBy().equals(username)) {
                    LockResultDto lockResult = buildLockResultDto(lock, false);
                    throw new ResourceLockedException(
                            String.format("La ressource %s avec l'ID %d est déjà verrouillée par un autre utilisateur",
                                    resourceType, resourceId),
                            lockResult
                    );
                }
                return buildLockResultDto(lock, true);
            }
            throw e;
        }
    }

    @Transactional
    public void verifyLock(ResourceTypeEnum resourceType, Long resourceId, String username) {
        cleanExpiredLocks();

        Optional<LockEntity> lockOpt = lockRepository.findByResourceTypeAndResourceId(resourceType, resourceId);

        if (lockOpt.isEmpty()) {
            log.info("No lock found for resource {}:{}, attempting to reacquire for user {}", 
                    resourceType, resourceId, username);
            acquireLock(resourceType, resourceId, username);
            return;
        }

        LockEntity lock = lockOpt.get();

        if (lock.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("Lock expired for resource {}:{}, attempting to reacquire for user {}", 
                    resourceType, resourceId, username);
            lockRepository.delete(lock);
            acquireLock(resourceType, resourceId, username);
            return;
        }

        if (!lock.getLockedBy().equals(username)) {
            log.warn("User {} does not own the lock for resource {}:{}, owned by {}",
                    username, resourceType, resourceId, lock.getLockedBy());
            LockResultDto lockResult = buildLockResultDto(lock, false);
            throw new ResourceLockedException(
                    String.format("La ressource %s avec l'ID %d est verrouillée par un autre utilisateur",
                            resourceType, resourceId),
                    lockResult
            );
        }

        log.debug("Lock verified for resource {}:{} by user {}", resourceType, resourceId, username);
    }

    @Transactional
    public void releaseLock(ResourceTypeEnum resourceType, Long resourceId, String username) {
        Optional<LockEntity> lockOpt = lockRepository.findByResourceTypeAndResourceId(resourceType, resourceId);

        if (lockOpt.isPresent()) {
            LockEntity lock = lockOpt.get();
            if (lock.getLockedBy().equals(username)) {
                lockRepository.delete(lock);
                log.info("Lock released for resource {}:{} by user {}", resourceType, resourceId, username);
            } else {
                log.warn("User {} attempted to release lock owned by user {} for resource {}:{}",
                        username, lock.getLockedBy(), resourceType, resourceId);
            }
        }
    }

    @Transactional
    public void refreshLock(LockEntity lock) {
        lock.setExpiresAt(LocalDateTime.now().plusMinutes(DEFAULT_LOCK_DURATION_MINUTES));
        lockRepository.save(lock);
        log.debug("Lock refreshed for resource {}:{}", lock.getResourceType(), lock.getResourceId());
    }

    @Transactional
    public void cleanExpiredLocks() {
        lockRepository.deleteExpiredLocks(LocalDateTime.now());
    }

    private LockResultDto buildLockResultDto(LockEntity lock, boolean acquired) {
        return LockResultDto.builder()
                .acquired(acquired)
                .expiresAt(lock.getExpiresAt())
                .username(lock.getLockedBy())
                .build();
    }

}
