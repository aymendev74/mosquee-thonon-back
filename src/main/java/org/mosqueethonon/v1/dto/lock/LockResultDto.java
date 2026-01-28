package org.mosqueethonon.v1.dto.lock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockResultDto {
    
    private boolean acquired;
    private LocalDateTime expiresAt;
    private String username;
    
}
