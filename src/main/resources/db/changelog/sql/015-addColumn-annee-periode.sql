UPDATE MOTH.PERIODE SET NOPERIANNEEDEBUT = 2024, NOPERIANNEEFIN = 2025 WHERE CDPERIAPPLICATION IN ('COURS_ADULTE', 'COURS_ENFANT');
DELETE FROM MOTH.PARAMS WHERE TXPARANAME = 'ANNEE_SCOLAIRE';