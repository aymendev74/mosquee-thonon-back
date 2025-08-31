package org.mosqueethonon.service.impl.bulletin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.entity.bulletin.BulletinEntity;
import org.mosqueethonon.exception.ResourceNotFoundException;
import org.mosqueethonon.repository.BulletinRepository;
import org.mosqueethonon.service.inscription.EleveService;
import org.mosqueethonon.v1.dto.bulletin.BulletinDto;
import org.mosqueethonon.v1.dto.inscription.EleveDto;
import org.mosqueethonon.v1.mapper.bulletin.BulletinMapper;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TestBulletinServiceImpl {

    @Mock
    private BulletinRepository bulletinRepository;

    @Mock
    private BulletinMapper bulletinMapper;

    @Mock
    private EleveService eleveService;

    @InjectMocks
    private BulletinServiceImpl bulletinService;

    @Test
    public void testCreateBulletin() {
        // GIVEN
        BulletinDto bulletinDto = new BulletinDto();
        BulletinEntity bulletinEntity = new BulletinEntity();
        when(this.bulletinMapper.fromDtoToEntity(any())).thenReturn(bulletinEntity);
        when(this.bulletinRepository.save(any())).thenReturn(bulletinEntity);
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(bulletinDto);

        // WHEN
        bulletinDto = this.bulletinService.createBulletin(bulletinDto);

        // THEN
        assertNotNull(bulletinDto);
    }

    @Test
    public void testUpdateBulletinExpectThrowsResourceNotFoundExceptionWhenResourceNotFound() {
        // GIVEN
        BulletinDto bulletinDto = new BulletinDto();
        when(this.bulletinRepository.findById(eq(1L))).thenReturn(Optional.empty());

        // WHEN THEN
        assertThrows(ResourceNotFoundException.class, () -> this.bulletinService.updateBulletin(1L, bulletinDto));
    }

    @Test
    public void testUpdateBulletin() {
        // GIVEN
        BulletinDto bulletinDto = new BulletinDto();
        BulletinEntity bulletinEntity = new BulletinEntity();
        when(this.bulletinRepository.findById(eq(1L))).thenReturn(Optional.of(bulletinEntity));
        when(this.bulletinRepository.save(any())).thenReturn(bulletinEntity);
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(bulletinDto);

        // WHEN
        bulletinDto = this.bulletinService.updateBulletin(1L, bulletinDto);

        // THEN
        assertNotNull(bulletinDto);
        verify(this.bulletinMapper, times(1)).updateBulletinEntity(any(), any());
    }

    @Test
    public void testFindBulletinsByIdEleveThrowsResourceNotFoundExceptionWhenResourceNotFound() {
        // GIVEN
        when(this.eleveService.findEleveById(anyLong())).thenReturn(null);

        // WHEN THEN
        assertThrows(ResourceNotFoundException.class, () -> this.bulletinService.findBulletinsByIdEleve(1L));
    }

    @Test
    public void testFindBulletinsByIdEleve() {
        // GIVEN
        BulletinEntity bulletinEntity = new BulletinEntity();
        BulletinEntity bulletinEntity2 = new BulletinEntity();
        when(this.eleveService.findEleveById(anyLong())).thenReturn(new EleveDto());
        when(this.bulletinRepository.findByIdEleve(anyLong())).thenReturn(List.of(bulletinEntity, bulletinEntity2));
        when(this.bulletinMapper.fromEntityToDto(any())).thenReturn(new BulletinDto());

        // WHEN
        List<BulletinDto> bulletinDtos = this.bulletinService.findBulletinsByIdEleve(1L);

        // THEN
        assertNotNull(bulletinDtos);
        assertEquals(2, bulletinDtos.size());
        verify(this.bulletinMapper, times(2)).fromEntityToDto(any());
    }

}
