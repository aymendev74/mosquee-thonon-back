package org.mosqueethonon.classe.v1.mapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.mosqueethonon.classe.entity.ClasseEntity;
import org.mosqueethonon.classe.entity.LienClasseEnseignantEntity;

import java.util.ArrayList;
import java.util.List;

public class TestClasseMapper {

    private final ClasseMapper underTest = new ClasseMapperImpl();

    @Test
    public void testPropageLIdDeLaClasseAuxLiensEnseignants() {
        LienClasseEnseignantEntity premier = LienClasseEnseignantEntity.builder().idUtilisateur(1L).build();
        LienClasseEnseignantEntity second = LienClasseEnseignantEntity.builder().idUtilisateur(2L).build();
        ClasseEntity classe = ClasseEntity.builder().id(9L)
                .liensClasseEnseignants(new ArrayList<>(List.of(premier, second)))
                .build();

        underTest.setIdClasseInLiensEnseignants(classe);

        assertEquals(9L, premier.getIdClasse());
        assertEquals(9L, second.getIdClasse());
    }

    @Test
    public void testSansLienEnseignant() {
        ClasseEntity classe = ClasseEntity.builder().id(9L).build();

        assertDoesNotThrow(() -> underTest.setIdClasseInLiensEnseignants(classe));
    }
}
