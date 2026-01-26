package org.mosqueethonon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.enums.ResourceTypeEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "lock_verrou", schema = "moth")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idlove")
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "cdloveresourcetype", nullable = false, length = 50)
    private ResourceTypeEnum resourceType;
    
    @Column(name = "cdloveresourceid", nullable = false)
    private Long resourceId;
    
    @Column(name = "cdlovelockedby", nullable = false, length = 255)
    private String lockedBy;
    
    @Column(name = "cdlovelockedat", nullable = false)
    private LocalDateTime lockedAt;
    
    @Column(name = "cdloveexpiresat", nullable = false)
    private LocalDateTime expiresAt;
    
}
