-- ============================================================================
-- Script de préparation : création des entrées dans utilisateur_account_action
-- pour les comptes utilisateurs non actifs (loutilenabled = false)
-- ============================================================================
-- A exécuter APRÈS le script migration-comptes-utilisateurs.sql
-- Les entrées sont créées avec le statut NOT_READY afin que le job
-- MailActivationUtilisateurJob ne les traite pas immédiatement.
--
-- Quand le responsable donne le GO, il suffira d'exécuter :
--   UPDATE moth.utilisateur_account_action
--   SET cduaacstatut = 'PENDING'
--   WHERE cduaacstatut = 'NOT_READY' AND cduaactype = 'ACTIVATION';
-- ============================================================================

DO $$
DECLARE
    v_rec RECORD;
    v_token TEXT;
    v_count INT := 0;
BEGIN

    RAISE NOTICE '=== Préparation des entrées d''activation (statut NOT_READY) ===';

    FOR v_rec IN
        SELECT u.txutiluser AS username
        FROM moth.utilisateur u
        WHERE u.loutilenabled = false
          -- Exclure les comptes qui ont déjà une action d'activation en cours
          AND NOT EXISTS (
              SELECT 1 FROM moth.utilisateur_account_action uaa
              WHERE uaa.txuaacuser = u.txutiluser
                AND uaa.cduaactype = 'ACTIVATION'
          )
        ORDER BY u.txutiluser
    LOOP
        -- Générer un token unique (43 caractères, URL-safe, sans pgcrypto)
        v_token := translate(
            rtrim(encode(decode(
                md5(random()::text || clock_timestamp()::text || v_rec.username)
                || md5(random()::text || clock_timestamp()::text),
            'hex'), 'base64'), '='),
            '+/', '-_'
        );

        INSERT INTO moth.utilisateur_account_action
            (txuaacuser, txuaactoken, cduaacstatut, cduaactype, oh_version, oh_date_cre, oh_vis_cre)
        VALUES
            (v_rec.username, v_token, 'NOT_READY', 'ACTIVATION', 0, NOW(), 'SQL');

        v_count := v_count + 1;
        RAISE NOTICE '  Entrée créée pour : % (token=%)', v_rec.username, v_token;
    END LOOP;

    RAISE NOTICE '=== Terminé : % entrées créées avec statut NOT_READY ===', v_count;

END $$;
