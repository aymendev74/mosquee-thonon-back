package org.mosqueethonon.repository;

import org.mosqueethonon.entity.LockEntity;
import org.mosqueethonon.enums.ResourceTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LockRepository extends JpaRepository<LockEntity, Long> {

    Optional<LockEntity> findByResourceTypeAndResourceId(ResourceTypeEnum resourceType, Long resourceId);

    @Modifying
    @Query("DELETE FROM LockEntity l WHERE l.expiresAt < :now")
    void deleteExpiredLocks(@Param("now") LocalDateTime now);

}
