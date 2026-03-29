-- ============================================================================
-- Script de ROLLBACK : annule la migration des comptes utilisateurs
-- ============================================================================
-- 1. Détache toutes les inscriptions de leur utilisateur (idutil = NULL)
-- 2. Supprime le rôle ROLE_UTILISATEUR de tous les utilisateurs
-- 3. Supprime les utilisateurs qui n'ont plus aucun rôle
-- ============================================================================

DO $$
DECLARE
    v_count_detach INT := 0;
    v_count_roles INT := 0;
    v_count_users INT := 0;
BEGIN

    RAISE NOTICE '=== Début du rollback de la migration ===';

    -- ========================================================================
    -- ETAPE 1 : Détacher toutes les inscriptions de leur utilisateur
    -- ========================================================================
    RAISE NOTICE 'Etape 1 : Détachement des inscriptions...';

    UPDATE moth.inscription
    SET idutil = NULL
    WHERE idutil IS NOT NULL;

    GET DIAGNOSTICS v_count_detach = ROW_COUNT;
    RAISE NOTICE 'Etape 1 terminée : % inscriptions détachées', v_count_detach;

    -- ========================================================================
    -- ETAPE 2 : Supprimer le rôle ROLE_UTILISATEUR de tous les utilisateurs
    -- ========================================================================
    RAISE NOTICE 'Etape 2 : Suppression du rôle ROLE_UTILISATEUR...';

    DELETE FROM moth.utilisateur_roles
    WHERE cdutrorole = 'ROLE_UTILISATEUR';

    GET DIAGNOSTICS v_count_roles = ROW_COUNT;
    RAISE NOTICE 'Etape 2 terminée : % rôles ROLE_UTILISATEUR supprimés', v_count_roles;

    -- ========================================================================
    -- ETAPE 3 : Supprimer les utilisateurs qui n'ont plus aucun rôle
    -- ========================================================================
    RAISE NOTICE 'Etape 3 : Suppression des utilisateurs sans rôle...';

    DELETE FROM moth.utilisateur u
    WHERE NOT EXISTS (
        SELECT 1 FROM moth.utilisateur_roles ur
        WHERE ur.txutiluser = u.txutiluser
    );

    GET DIAGNOSTICS v_count_users = ROW_COUNT;
    RAISE NOTICE 'Etape 3 terminée : % utilisateurs supprimés', v_count_users;

    -- ========================================================================
    -- RESUME
    -- ========================================================================
    RAISE NOTICE '=== Rollback terminé ===';
    RAISE NOTICE '  Inscriptions détachées       : %', v_count_detach;
    RAISE NOTICE '  Rôles UTILISATEUR supprimés  : %', v_count_roles;
    RAISE NOTICE '  Utilisateurs supprimés       : %', v_count_users;

END $$;
