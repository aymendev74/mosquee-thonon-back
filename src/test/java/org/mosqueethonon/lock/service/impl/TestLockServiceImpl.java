package org.mosqueethonon.lock.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mosqueethonon.common.config.TimeConfiguration;
import org.mosqueethonon.common.security.ApplicationConfiguration;
import org.mosqueethonon.lock.entity.LockEntity;
import org.mosqueethonon.lock.enums.ResourceTypeEnum;
import org.mosqueethonon.lock.exception.ResourceLockedException;
import org.mosqueethonon.lock.repository.LockRepository;
import org.mosqueethonon.lock.v1.dto.LockResultDto;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TestLockServiceImpl {

    private static final ResourceTypeEnum TYPE = ResourceTypeEnum.INSCRIPTION;
    private static final Long ID = 1L;
    private static final String PROPRIETAIRE = "alice";
    private static final String AUTRE = "bob";

    /**
     * Horloge figée sur le fuseau de l'application, injectée dans le service par {@code @InjectMocks}.
     * Les verrous manipulés ici s'expriment tous en écart par rapport à « maintenant » : le service et
     * les fixtures doivent lire la même horloge, sinon un verrou actif peut paraître expiré du simple
     * fait du fuseau de la machine qui exécute les tests.
     */
    private static final Clock HORLOGE_FIGEE = Clock.fixed(
            LocalDateTime.of(2026, Month.MARCH, 15, 10, 0).atZone(TimeConfiguration.ZONE_APPLICATION).toInstant(),
            TimeConfiguration.ZONE_APPLICATION);

    /** Ce que le service obtient lorsqu'il appelle {@code LocalDateTime.now(clock)}. */
    private static final LocalDateTime MAINTENANT = LocalDateTime.now(HORLOGE_FIGEE);

    @Mock
    private LockRepository lockRepository;

    @Mock
    private ApplicationConfiguration applicationConfiguration;

    @Spy
    private Clock clock = HORLOGE_FIGEE;

    @InjectMocks
    private LockServiceImpl underTest;

    @BeforeEach
    public void setUp() {
        Mockito.lenient().when(this.applicationConfiguration.getResourceLockTimeout()).thenReturn(30L);
    }

    private LockEntity verrou(String proprietaire, LocalDateTime expiration) {
        return LockEntity.builder().resourceType(TYPE).resourceId(ID)
                .lockedBy(proprietaire).lockedAt(MAINTENANT).expiresAt(expiration).build();
    }

    private LockEntity verrouActif(String proprietaire) {
        return verrou(proprietaire, MAINTENANT.plusMinutes(10));
    }

    private LockEntity verrouExpire(String proprietaire) {
        return verrou(proprietaire, MAINTENANT.minusMinutes(1));
    }

    private void givenVerrou(LockEntity lock) {
        when(this.lockRepository.findByResourceTypeAndResourceId(TYPE, ID))
                .thenReturn(Optional.ofNullable(lock));
    }

    @Nested
    class QuandOnAcquiertUnVerrou {

        @Test
        public void testCreeUnVerrouQuandLaRessourceEstLibre() {
            // GIVEN
            givenVerrou(null);

            // WHEN
            LockResultDto result = underTest.acquireLock(TYPE, ID, PROPRIETAIRE);

            // THEN
            assertTrue(result.isAcquired());
            assertEquals(PROPRIETAIRE, result.getUsername());
            ArgumentCaptor<LockEntity> captor = ArgumentCaptor.forClass(LockEntity.class);
            verify(lockRepository).save(captor.capture());
            LockEntity saved = captor.getValue();
            assertEquals(TYPE, saved.getResourceType());
            assertEquals(ID, saved.getResourceId());
            assertTrue(saved.getExpiresAt().isAfter(MAINTENANT.plusMinutes(29)),
                    "l'expiration doit suivre le timeout configuré");
        }

        @Test
        public void testPurgeLesVerrousExpiresAvantTouteAcquisition() {
            // GIVEN
            givenVerrou(null);

            // WHEN
            underTest.acquireLock(TYPE, ID, PROPRIETAIRE);

            // THEN
            verify(lockRepository).deleteExpiredLocks(Mockito.any(LocalDateTime.class));
        }

        @Test
        public void testProlongeLeVerrouQuandLeMemeUtilisateurLeRedemande() {
            // GIVEN
            LockEntity existant = verrouActif(PROPRIETAIRE);
            LocalDateTime expirationInitiale = existant.getExpiresAt();
            givenVerrou(existant);

            // WHEN
            LockResultDto result = underTest.acquireLock(TYPE, ID, PROPRIETAIRE);

            // THEN
            assertTrue(result.isAcquired());
            assertTrue(existant.getExpiresAt().isAfter(expirationInitiale), "le verrou doit être prolongé");
            verify(lockRepository).save(existant);
            verify(lockRepository, never()).delete(Mockito.any());
        }

        @Test
        public void testRefuseQuandLaRessourceEstVerrouilleeParQuelquunDAutre() {
            // GIVEN
            givenVerrou(verrouActif(AUTRE));

            // WHEN
            ResourceLockedException exception = assertThrows(ResourceLockedException.class,
                    () -> underTest.acquireLock(TYPE, ID, PROPRIETAIRE));

            // THEN — l'exception porte l'identité du détenteur, pour l'afficher à l'utilisateur
            assertFalse(exception.getLockResult().isAcquired());
            assertEquals(AUTRE, exception.getLockResult().getUsername());
            verify(lockRepository, never()).save(Mockito.any());
        }

        @Test
        public void testRemplaceUnVerrouExpireAppartenantAUnAutreUtilisateur() {
            // GIVEN
            LockEntity expire = verrouExpire(AUTRE);
            givenVerrou(expire);

            // WHEN
            LockResultDto result = underTest.acquireLock(TYPE, ID, PROPRIETAIRE);

            // THEN
            assertTrue(result.isAcquired());
            assertEquals(PROPRIETAIRE, result.getUsername());
            verify(lockRepository).delete(expire);
            verify(lockRepository).save(Mockito.any(LockEntity.class));
        }

        @Test
        public void testRendLaMainAuGagnantEnCasDeCourseEtQueCestNous() {
            // GIVEN — deux requêtes simultanées : l'insert échoue, mais le verrou trouvé est le nôtre
            LockEntity concurrent = verrouActif(PROPRIETAIRE);
            when(lockRepository.findByResourceTypeAndResourceId(TYPE, ID))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(concurrent));
            when(lockRepository.save(Mockito.any(LockEntity.class)))
                    .thenThrow(new DataIntegrityViolationException("contrainte d'unicité"));

            // WHEN
            LockResultDto result = underTest.acquireLock(TYPE, ID, PROPRIETAIRE);

            // THEN
            assertTrue(result.isAcquired());
            assertEquals(PROPRIETAIRE, result.getUsername());
        }

        @Test
        public void testRefuseEnCasDeCourseGagneeParUnAutre() {
            // GIVEN
            when(lockRepository.findByResourceTypeAndResourceId(TYPE, ID))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(verrouActif(AUTRE)));
            when(lockRepository.save(Mockito.any(LockEntity.class)))
                    .thenThrow(new DataIntegrityViolationException("contrainte d'unicité"));

            // WHEN
            ResourceLockedException exception = assertThrows(ResourceLockedException.class,
                    () -> underTest.acquireLock(TYPE, ID, PROPRIETAIRE));

            // THEN
            assertEquals(AUTRE, exception.getLockResult().getUsername());
        }

        @Test
        public void testRepropageLErreurDIntegriteSiAucunVerrouConcurrentNexiste() {
            // GIVEN — l'insert échoue mais rien n'explique pourquoi : on ne masque pas l'erreur
            DataIntegrityViolationException cause = new DataIntegrityViolationException("autre contrainte");
            when(lockRepository.findByResourceTypeAndResourceId(TYPE, ID)).thenReturn(Optional.empty());
            when(lockRepository.save(Mockito.any(LockEntity.class))).thenThrow(cause);

            // WHEN
            DataIntegrityViolationException thrown = assertThrows(DataIntegrityViolationException.class,
                    () -> underTest.acquireLock(TYPE, ID, PROPRIETAIRE));

            // THEN
            assertSame(cause, thrown);
        }
    }

    @Nested
    class QuandOnVerifieUnVerrou {

        @Test
        public void testNeLevePasDErreurQuandLUtilisateurEstProprietaire() {
            // GIVEN
            givenVerrou(verrouActif(PROPRIETAIRE));

            // WHEN / THEN — aucune exception
            underTest.verifyLock(TYPE, ID, PROPRIETAIRE);
            verify(lockRepository, never()).delete(Mockito.any());
        }

        @Test
        public void testReacquiertLeVerrouQuandIlADisparu() {
            // GIVEN — plus de verrou en base : on le reprend au lieu d'échouer
            when(lockRepository.findByResourceTypeAndResourceId(TYPE, ID)).thenReturn(Optional.empty());

            // WHEN
            underTest.verifyLock(TYPE, ID, PROPRIETAIRE);

            // THEN
            verify(lockRepository).save(Mockito.any(LockEntity.class));
        }

        @Test
        public void testReacquiertLeVerrouQuandIlEstExpire() {
            // GIVEN
            LockEntity expire = verrouExpire(PROPRIETAIRE);
            when(lockRepository.findByResourceTypeAndResourceId(TYPE, ID))
                    .thenReturn(Optional.of(expire))
                    .thenReturn(Optional.empty());

            // WHEN
            underTest.verifyLock(TYPE, ID, PROPRIETAIRE);

            // THEN
            verify(lockRepository).delete(expire);
            verify(lockRepository).save(Mockito.any(LockEntity.class));
        }

        @Test
        public void testRefuseQuandLeVerrouAppartientAUnAutre() {
            // GIVEN
            givenVerrou(verrouActif(AUTRE));

            // WHEN
            ResourceLockedException exception = assertThrows(ResourceLockedException.class,
                    () -> underTest.verifyLock(TYPE, ID, PROPRIETAIRE));

            // THEN
            assertEquals(AUTRE, exception.getLockResult().getUsername());
            assertFalse(exception.getLockResult().isAcquired());
        }
    }

    @Nested
    class QuandOnLibereUnVerrou {

        @Test
        public void testSupprimeLeVerrouDeSonProprietaire() {
            // GIVEN
            LockEntity lock = verrouActif(PROPRIETAIRE);
            givenVerrou(lock);

            // WHEN
            underTest.releaseLock(TYPE, ID, PROPRIETAIRE);

            // THEN
            verify(lockRepository).delete(lock);
        }

        @Test
        public void testIgnoreLaDemandeDunNonProprietaire() {
            // GIVEN
            givenVerrou(verrouActif(AUTRE));

            // WHEN — silencieux, pas d'exception : on ne libère simplement pas
            underTest.releaseLock(TYPE, ID, PROPRIETAIRE);

            // THEN
            verify(lockRepository, never()).delete(Mockito.any());
        }

        @Test
        public void testNeFaitRienQuandAucunVerrouNexiste() {
            // GIVEN
            givenVerrou(null);

            // WHEN
            underTest.releaseLock(TYPE, ID, PROPRIETAIRE);

            // THEN
            verify(lockRepository, never()).delete(Mockito.any());
        }
    }
}
