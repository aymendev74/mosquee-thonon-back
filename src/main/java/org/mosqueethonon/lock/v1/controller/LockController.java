package org.mosqueethonon.lock.v1.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.lock.service.LockService;
import org.mosqueethonon.lock.v1.dto.LockRequestDto;
import org.mosqueethonon.lock.v1.dto.LockResultDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/locks")
@RequiredArgsConstructor
public class LockController {

    private final LockService lockService;

    @PostMapping
    public ResponseEntity<LockResultDto> acquireLock(@RequestBody LockRequestDto lockRequest) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        LockResultDto result = lockService.acquireLock(
                lockRequest.getResourceType(), 
                lockRequest.getResourceId(), 
                username
        );
        
        return ResponseEntity.ok(result);
    }

    @DeleteMapping
    public ResponseEntity<LockResultDto> releaseLock(@RequestBody LockRequestDto lockRequest) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        lockService.releaseLock(
                lockRequest.getResourceType(), 
                lockRequest.getResourceId(), 
                username
        );
        
        LockResultDto result = LockResultDto.builder()
                .acquired(false)
                .expiresAt(null)
                .username(username)
                .build();
        
        return ResponseEntity.ok(result);
    }
    
}
