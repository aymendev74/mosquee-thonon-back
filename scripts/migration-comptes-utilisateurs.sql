-- ============================================================================
-- Script de migration : création des comptes utilisateurs pour les inscriptions
-- de l'année 2025/2026 et rattachement des inscriptions 2024/2025 existantes
-- ============================================================================
-- A exécuter en one-shot sur la base PostgreSQL
-- Le mot de passe généré est un BCrypt temporaire
-- Les comptes sont créés enabled=false, locked=false
-- ============================================================================

DO $$
DECLARE
    v_rec RECORD;
    v_util_id BIGINT;
    v_password TEXT := '$2a$10$dXJ3SW6G7P50lGmMQoeGbOwS2GFv/ADhDBaThqkbLyQoc/2cMfHcy';
    v_count_users INT := 0;
    v_count_roles INT := 0;
    v_count_roles_existing INT := 0;
    v_count_rattach_current INT := 0;
    v_count_rattach_previous INT := 0;
BEGIN

    RAISE NOTICE '=== Début de la migration des comptes utilisateurs ===';

    -- ========================================================================
    -- ETAPE 1 : Créer les comptes utilisateurs pour les inscriptions 2025/2026
    --           (un compte par email unique de responsable légal)
    -- ========================================================================
    RAISE NOTICE 'Etape 1 : Création des comptes utilisateurs...';

    FOR v_rec IN
        SELECT DISTINCT ON (LOWER(TRIM(r.txrespemail)))
            LOWER(TRIM(r.txrespemail)) AS email,
            TRIM(r.txrespnom) AS nom,
            TRIM(r.txrespprenom) AS prenom,
            TRIM(r.txrespmobile) AS mobile
        FROM moth.inscription i
        JOIN moth.resplegal r ON r.idresp = i.idresp
        JOIN moth.tarif t ON t.idtari = i.idtari
        JOIN moth.periode p ON p.idperi = t.idperi
        WHERE p.noperianneedebut = 2025 AND p.noperianneefin = 2026
          AND i.cdinscstatut = 'VALIDEE'
          AND r.txrespemail IS NOT NULL
          AND TRIM(r.txrespemail) <> ''
          AND i.idutil IS NULL  -- pas déjà rattaché
          AND NOT EXISTS (
              SELECT 1 FROM moth.utilisateur u
              WHERE LOWER(u.txutilemail) = LOWER(TRIM(r.txrespemail))
          )
        ORDER BY LOWER(TRIM(r.txrespemail)), i.dtinscinscription DESC
    LOOP
        -- Créer l'utilisateur
        INSERT INTO moth.utilisateur (txutiluser, txutilpassword, txutilnom, txutilprenom, txutilemail, txutilmobile,
                                      loutilenabled, loutillocked, oh_version, oh_date_cre, oh_vis_cre)
        VALUES (v_rec.email, v_password, v_rec.nom, v_rec.prenom, v_rec.email, v_rec.mobile,
                false, false, 0, NOW(), 'SQL')
        RETURNING idutil INTO v_util_id;

        v_count_users := v_count_users + 1;

        -- Attribuer le rôle ROLE_UTILISATEUR
        INSERT INTO moth.utilisateur_roles (txutiluser, cdutrorole, oh_version, oh_date_cre, oh_vis_cre)
        VALUES (v_rec.email, 'ROLE_UTILISATEUR', 0, NOW(), 'SQL');

        v_count_roles := v_count_roles + 1;

        RAISE NOTICE '  Utilisateur créé : % (id=%)', v_rec.email, v_util_id;
    END LOOP;

    RAISE NOTICE 'Etape 1 terminée : % comptes créés, % rôles attribués', v_count_users, v_count_roles;

    -- ========================================================================
    -- ETAPE 1bis : Pour les comptes existants (ex: enseignants), ajouter
    --              ROLE_UTILISATEUR s'ils ont une inscription 2025/2026
    -- ========================================================================
    RAISE NOTICE 'Etape 1bis : Ajout du rôle UTILISATEUR aux comptes existants...';

    FOR v_rec IN
        SELECT DISTINCT u.txutiluser AS username
        FROM moth.utilisateur u
        JOIN moth.inscription i ON i.idutil IS NULL
        JOIN moth.resplegal r ON r.idresp = i.idresp
        JOIN moth.tarif t ON t.idtari = i.idtari
        JOIN moth.periode p ON p.idperi = t.idperi
        WHERE p.noperianneedebut = 2025 AND p.noperianneefin = 2026
          AND i.cdinscstatut = 'VALIDEE'
          AND LOWER(u.txutilemail) = LOWER(TRIM(r.txrespemail))
          AND NOT EXISTS (
              SELECT 1 FROM moth.utilisateur_roles ur
              WHERE ur.txutiluser = u.txutiluser
                AND ur.cdutrorole = 'ROLE_UTILISATEUR'
          )
    LOOP
        INSERT INTO moth.utilisateur_roles (txutiluser, cdutrorole, oh_version, oh_date_cre, oh_vis_cre)
        VALUES (v_rec.username, 'ROLE_UTILISATEUR', 0, NOW(), 'SQL');

        v_count_roles_existing := v_count_roles_existing + 1;
        RAISE NOTICE '  Rôle UTILISATEUR ajouté au compte existant : %', v_rec.username;
    END LOOP;

    RAISE NOTICE 'Etape 1bis terminée : % rôles ajoutés à des comptes existants', v_count_roles_existing;

    -- ========================================================================
    -- ETAPE 2 : Rattacher les inscriptions 2025/2026 à leur utilisateur
    --           (matching sur email du responsable légal = username utilisateur)
    -- ========================================================================
    RAISE NOTICE 'Etape 2 : Rattachement des inscriptions 2025/2026...';

    UPDATE moth.inscription i
    SET idutil = u.idutil
    FROM moth.resplegal r, moth.tarif t, moth.periode p, moth.utilisateur u
    WHERE r.idresp = i.idresp
      AND t.idtari = i.idtari
      AND p.idperi = t.idperi
      AND p.noperianneedebut = 2025 AND p.noperianneefin = 2026
      AND LOWER(u.txutilemail) = LOWER(TRIM(r.txrespemail))
      AND i.idutil IS NULL
      AND i.cdinscstatut = 'VALIDEE'
      ;

    GET DIAGNOSTICS v_count_rattach_current = ROW_COUNT;
    RAISE NOTICE 'Etape 2 terminée : % inscriptions 2025/2026 rattachées', v_count_rattach_current;

    -- ========================================================================
    -- ETAPE 3 : Rattacher les inscriptions 2024/2025 aux utilisateurs existants
    --           (uniquement si un compte existe déjà, créé à l'étape 1)
    -- ========================================================================
    RAISE NOTICE 'Etape 3 : Rattachement des inscriptions 2024/2025...';

    UPDATE moth.inscription i
    SET idutil = u.idutil
    FROM moth.resplegal r, moth.tarif t, moth.periode p, moth.utilisateur u
    WHERE r.idresp = i.idresp
      AND t.idtari = i.idtari
      AND p.idperi = t.idperi
      AND p.noperianneedebut = 2024 AND p.noperianneefin = 2025
      AND LOWER(u.txutilemail) = LOWER(TRIM(r.txrespemail))
      AND i.idutil IS NULL
      AND i.cdinscstatut = 'VALIDEE'
      ;

    GET DIAGNOSTICS v_count_rattach_previous = ROW_COUNT;
    RAISE NOTICE 'Etape 3 terminée : % inscriptions 2024/2025 rattachées', v_count_rattach_previous;

    -- ========================================================================
    -- RESUME
    -- ========================================================================
    RAISE NOTICE '=== Migration terminée ===';
    RAISE NOTICE '  Comptes créés              : %', v_count_users;
    RAISE NOTICE '  Rôles attribués (nouveaux) : %', v_count_roles;
    RAISE NOTICE '  Rôles ajoutés (existants)  : %', v_count_roles_existing;
    RAISE NOTICE '  Inscriptions 2025/2026     : % rattachées', v_count_rattach_current;
    RAISE NOTICE '  Inscriptions 2024/2025     : % rattachées', v_count_rattach_previous;

END $$;
