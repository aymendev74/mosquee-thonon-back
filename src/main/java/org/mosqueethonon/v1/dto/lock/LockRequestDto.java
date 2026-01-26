package org.mosqueethonon.v1.dto.lock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.mosqueethonon.enums.ResourceTypeEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockRequestDto {
    
    private ResourceTypeEnum resourceType;
    private Long resourceId;
    
}
