package org.mosqueethonon.v1.mapper.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.v1.dto.user.UserDto;

import static org.junit.jupiter.api.Assertions.*;

public class TestUserMapperImpl {
    private UserMapper userMapper;

    @BeforeEach
    public void setUp() {
        userMapper = Mappers.getMapper(UserMapper.class);
    }

    @Test
    public void testFromEntityToDto() {
        UtilisateurEntity entity = new UtilisateurEntity();
        entity.setId(1L);
        entity.setUsername("testuser");
        entity.setNom("Doe");
        entity.setPrenom("John");
        entity.setEmail("john.doe@email.com");
        entity.setEnabled(true);

        UserDto dto = userMapper.fromEntityToDto(entity);
        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getUsername(), dto.getUsername());
        assertEquals(entity.getNom(), dto.getNom());
        assertEquals(entity.getPrenom(), dto.getPrenom());
        assertEquals(entity.getEmail(), dto.getEmail());
        assertEquals(entity.isEnabled(), dto.isEnabled());
    }

    @Test
    public void testFromDtoToEntity() {
        UserDto dto = new UserDto();
        dto.setId(2L);
        dto.setUsername("anotheruser");
        dto.setNom("Smith");
        dto.setPrenom("Jane");
        dto.setEmail("jane.smith@email.com");
        dto.setEnabled(false);
        // ... set other fields as needed

        UtilisateurEntity entity = userMapper.fromDtoToEntity(dto);
        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getUsername(), entity.getUsername());
        assertEquals(dto.getNom(), entity.getNom());
        assertEquals(dto.getPrenom(), entity.getPrenom());
        assertEquals(dto.getEmail(), entity.getEmail());
        assertEquals(dto.isEnabled(), entity.isEnabled());
    }

    @Test
    public void testUpdateUserEntityFromDto() {
        UtilisateurEntity entity = new UtilisateurEntity();
        entity.setId(5L);
        entity.setUsername("original");
        entity.setNom("Original");
        entity.setPrenom("User");
        entity.setEmail("original@email.com");
        entity.setEnabled(true);

        UserDto dto = new UserDto();
        dto.setUsername("updated");
        dto.setNom("Updated");
        dto.setPrenom("User");
        dto.setEmail("updated@email.com");
        dto.setEnabled(false);

        userMapper.updateUserEntityFromDto(dto, entity);
        // id et enabled doivent rester inchangés (selon l'annotation @Mapping)
        assertEquals(5L, entity.getId());
        assertEquals("updated", entity.getUsername());
        assertEquals("Updated", entity.getNom());
        assertEquals("User", entity.getPrenom());
        assertEquals("updated@email.com", entity.getEmail());
        assertTrue(entity.isEnabled()); // enabled ignoré lors du mapping
    }
}
