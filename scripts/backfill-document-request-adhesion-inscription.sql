-- ============================================================================
-- Script de reprise : génération des documents manquants pour les adhésions
-- et les inscriptions (enfant/adulte) existantes
-- ============================================================================
-- A exécuter en one-shot sur la base PostgreSQL, après mise en prod de la
-- fonctionnalité de génération de documents.
--
-- Principe : pour chaque adhésion / inscription enfant / inscription adulte
-- qui n'a pas encore de document généré (absence d'entrée dans
-- moth.document_metadata pour la clé ID_ADHESION / ID_INSCRIPTION
-- correspondante), on insère une demande de génération (moth.document_request,
-- statut PENDING) qui sera traitée par le job asynchrone existant
-- (DocumentRequestJobService).
--
-- Périmètre :
--   - ADHESION            : moth.adhesion, sans filtre de statut (comme le
--                            comportement de AdhesionServiceImpl à la création)
--   - INSCRIPTION_ENFANT  : moth.inscription (cdinsctype = 'ENFANT'),
--                            uniquement cdinscstatut IN ('PROVISOIRE','VALIDEE')
--   - INSCRIPTION_ADULTE  : moth.inscription (cdinsctype = 'ADULTE'),
--                            uniquement cdinscstatut IN ('PROVISOIRE','VALIDEE')
--   - BULLETIN            : hors périmètre, non traité par ce script
--
-- Le script est idempotent : une ligne n'est reprise que si elle n'a ni
-- document généré (document_metadata) ni demande déjà existante
-- (document_request), donc une ré-exécution ne crée pas de doublons.
-- ============================================================================

DO $$
DECLARE
    v_count_adhesion INT := 0;
    v_count_inscription_enfant INT := 0;
    v_count_inscription_adulte INT := 0;
BEGIN

    RAISE NOTICE '=== Début de la reprise des demandes de génération de documents ===';

    -- ========================================================================
    -- ETAPE 1 : Adhésions sans document généré
    -- ========================================================================
    RAISE NOTICE 'Etape 1 : Adhésions...';

    INSERT INTO moth.document_request (cddoretype, iddorebusi, cddorestatut, oh_version, oh_date_cre, oh_vis_cre)
    SELECT 'ADHESION', a.idadhe, 'PENDING', 0, NOW(), 'BACKFILL'
    FROM moth.adhesion a
    WHERE NOT EXISTS (
              SELECT 1
              FROM moth.document_metadata dm
              JOIN moth.document d ON d.iddocu = dm.iddocu
              WHERE dm.cddomecle = 'ID_ADHESION'
                AND dm.txdomevaleur = a.idadhe::text
          )
      AND NOT EXISTS (
              SELECT 1
              FROM moth.document_request dr
              WHERE dr.cddoretype = 'ADHESION'
                AND dr.iddorebusi = a.idadhe
          );

    GET DIAGNOSTICS v_count_adhesion = ROW_COUNT;
    RAISE NOTICE 'Etape 1 terminée : % demandes créées pour les adhésions', v_count_adhesion;

    -- ========================================================================
    -- ETAPE 2 : Inscriptions enfant (PROVISOIRE/VALIDEE) sans document généré
    -- ========================================================================
    RAISE NOTICE 'Etape 2 : Inscriptions enfant...';

    INSERT INTO moth.document_request (cddoretype, iddorebusi, cddorestatut, oh_version, oh_date_cre, oh_vis_cre)
    SELECT 'INSCRIPTION_ENFANT', i.idinsc, 'PENDING', 0, NOW(), 'BACKFILL'
    FROM moth.inscription i
    WHERE i.cdinsctype = 'ENFANT'
      AND i.cdinscstatut IN ('PROVISOIRE', 'VALIDEE')
      AND NOT EXISTS (
              SELECT 1
              FROM moth.document_metadata dm
              JOIN moth.document d ON d.iddocu = dm.iddocu
              WHERE dm.cddomecle = 'ID_INSCRIPTION'
                AND dm.txdomevaleur = i.idinsc::text
          )
      AND NOT EXISTS (
              SELECT 1
              FROM moth.document_request dr
              WHERE dr.cddoretype = 'INSCRIPTION_ENFANT'
                AND dr.iddorebusi = i.idinsc
          );

    GET DIAGNOSTICS v_count_inscription_enfant = ROW_COUNT;
    RAISE NOTICE 'Etape 2 terminée : % demandes créées pour les inscriptions enfant', v_count_inscription_enfant;

    -- ========================================================================
    -- ETAPE 3 : Inscriptions adulte (PROVISOIRE/VALIDEE) sans document généré
    -- ========================================================================
    RAISE NOTICE 'Etape 3 : Inscriptions adulte...';

    INSERT INTO moth.document_request (cddoretype, iddorebusi, cddorestatut, oh_version, oh_date_cre, oh_vis_cre)
    SELECT 'INSCRIPTION_ADULTE', i.idinsc, 'PENDING', 0, NOW(), 'BACKFILL'
    FROM moth.inscription i
    WHERE i.cdinsctype = 'ADULTE'
      AND i.cdinscstatut IN ('PROVISOIRE', 'VALIDEE')
      AND NOT EXISTS (
              SELECT 1
              FROM moth.document_metadata dm
              JOIN moth.document d ON d.iddocu = dm.iddocu
              WHERE dm.cddomecle = 'ID_INSCRIPTION'
                AND dm.txdomevaleur = i.idinsc::text
          )
      AND NOT EXISTS (
              SELECT 1
              FROM moth.document_request dr
              WHERE dr.cddoretype = 'INSCRIPTION_ADULTE'
                AND dr.iddorebusi = i.idinsc
          );

    GET DIAGNOSTICS v_count_inscription_adulte = ROW_COUNT;
    RAISE NOTICE 'Etape 3 terminée : % demandes créées pour les inscriptions adulte', v_count_inscription_adulte;

    -- ========================================================================
    -- RESUME
    -- ========================================================================
    RAISE NOTICE '=== Reprise terminée ===';
    RAISE NOTICE '  Adhésions             : % demandes créées', v_count_adhesion;
    RAISE NOTICE '  Inscriptions enfant   : % demandes créées', v_count_inscription_enfant;
    RAISE NOTICE '  Inscriptions adulte   : % demandes créées', v_count_inscription_adulte;
    RAISE NOTICE '  Total                 : % demandes créées', v_count_adhesion + v_count_inscription_enfant + v_count_inscription_adulte;

END $$;
