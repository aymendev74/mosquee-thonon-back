drop table if exists moth.v_adhesion_light;
drop table if exists moth.v_inscription_light;

CREATE VIEW moth.v_adhesion_light AS
SELECT
    a.idadhe AS id,
    a.txadhenom AS nom,
    a.txadheprenom AS prenom,
    a.txadheville AS ville,
    a.cdadhestatut AS statut,
    COALESCE(a.mtadheautre, t.mttari) AS montant,
    a.dtadheinscription AS dateinscription,
    d.iddocu AS iddocument
FROM moth.adhesion a
         INNER JOIN moth.tarif t ON t.idtari = a.idtari
         LEFT JOIN (moth.document_metadata dm
    JOIN moth.document d ON d.iddocu = dm.iddocu)
                   ON (dm.cddomecle = 'ID_ADHESION' AND dm.txdomevaleur = CAST(a.idadhe AS VARCHAR))
;

CREATE VIEW moth.v_inscription_light AS
SELECT
    e.idelev AS id,
    i.idinsc AS idinscription,
    i.dtinscinscription AS dateinscription,
    i.cdinscstatut AS statut,
    e.txelevnom AS nom,
    e.txelevprenom AS prenom,
    e.dtelevnaissance AS datenaissance,
    e.cdelevniveau AS niveau,
    e.cdelevniveauinterne AS niveauinterne,
    r.txrespnom AS nomresponsablelegal,
    r.txrespprenom AS prenomresponsablelegal,
    r.txrespmobile AS mobile,
    r.txrespnomautre AS nomcontacturgence,
    r.txrespprenomautre AS prenomcontacturgence,
    r.txrespphoneautre AS mobilecontacturgence,
    r.txrespville AS ville,
    r.txrespphoneautre AS mobileautre,
    r.lorespautonomie AS autorisationautonomie,
    r.lorespmedia AS autorisationmedia,
    i.noinscinscription AS noinscription,
    p.idperi AS idperiode,
    r.txrespemail AS email,
    i.cdinsctype AS type,
    d.iddocu AS iddocument
FROM ((((moth.inscription i
    JOIN moth.eleve e ON (e.idinsc = i.idinsc))
    JOIN moth.resplegal r ON (i.idresp = r.idresp))
    JOIN moth.tarif t ON (t.idtari = i.idtari))
    JOIN moth.periode p ON (p.idperi = t.idperi))
         LEFT JOIN (moth.document_metadata dm
    JOIN moth.document d ON d.iddocu = dm.iddocu)
                   ON (dm.cddomecle = 'ID_INSCRIPTION' AND dm.txdomevaleur = CAST(i.idinsc AS VARCHAR))
;