# Indicateur "document en cours de (re)génération" — Design

**Repos concernés** : `mosquee-thonon-back` (branche `feature/ged-v2`) et `mosquee-thonon-front-2` (branche `feature/ged-v2`).

## Contexte

Le système de génération asynchrone de documents (`moth.document_request`, `DocumentRequestType` = `ADHESION`/`INSCRIPTION_ENFANT`/`INSCRIPTION_ADULTE`/`BULLETIN`, `DocumentRequestStatut` = `PENDING`/`COMPLETED`/`ERROR`) crée une nouvelle demande PENDING chaque fois qu'une adhésion ou une inscription est créée/modifiée. Un job planifié (toutes les minutes) traite ces demandes et régénère le PDF si les données métier ont changé (hash).

Aujourd'hui, l'admin n'a aucune visibilité sur ce statut : le bouton de téléchargement PDF (`AdminAdhesion.tsx` → `AdhesionDesktopView.tsx`, `AdminCoursArabes.tsx` → `InscriptionDesktopView.tsx`) ne regarde que `idDocument` (présent ou non), sans savoir qu'une régénération est en cours suite à une modification récente.

## Objectif

Afficher un indicateur visuel sur le bouton PDF (vues desktop uniquement) quand une demande `PENDING` existe pour l'adhésion/inscription affichée, qu'il s'agisse d'une première génération ou d'une régénération.

## Hors scope (décidé explicitement)

- Pas de changement sur les vues mobiles (`AdhesionMobileView`/`InscriptionMobileView`) : elles n'ont aujourd'hui aucun bouton PDF, on ne l'y introduit pas dans ce lot.
- Pas de polling / rafraîchissement automatique de l'écran : l'indicateur se met à jour au prochain refresh naturel (nouvelle recherche, ou après une action de validation/suppression qui refetch déjà les données).
- Pas de modification du mécanisme `document_metadata`/`document` existant (résolution de `iddocument`) — seule une colonne `documentpending` est ajoutée aux vues, en plus de l'existant.
- Le bouton PDF se désactive pendant toute demande `PENDING`, y compris quand un ancien document existe déjà (pas de téléchargement de version périmée pendant la régénération).

## Data flow

Les vues `v_adhesion_light`/`v_inscription_light` exposent déjà `iddocument` via un `LEFT JOIN document_metadata + document` (changelog `2026/058-updateView-adhesion-inscription-add-iddocument.yml`). On applique le même principe pour `documentpending`, en étendant ces mêmes vues plutôt qu'en ajoutant une requête séparée côté Java — cohérent avec le pattern déjà en place, une seule requête (celle de la recherche elle-même), et pas de logique de correspondance d'id dupliquée entre SQL et Java.

Pour éviter tout risque de duplication de ligne (si jamais plusieurs `document_request` PENDING existaient un jour pour le même `(type, businessId)` — rien ne l'empêche au niveau base, seule la dédup applicative de `AsyncDocumentServiceImpl` l'évite aujourd'hui), on utilise une sous-requête `EXISTS` et non un `LEFT JOIN` classique : un `EXISTS` reste un booléen quel que soit le nombre de lignes PENDING correspondantes, un `JOIN` dupliquerait la ligne adhésion/inscription en cas de fan-out.

## Backend

### Changelog Liquibase

Nouveau changelog (suivant le même schéma que `058-...`), `DROP`/`CREATE OR REPLACE` des deux vues avec une colonne `documentpending` en plus :

```sql
CREATE OR REPLACE VIEW moth.v_adhesion_light AS
SELECT a.idadhe AS id,
    a.txadhenom AS nom,
    a.txadheprenom AS prenom,
    a.txadheville AS ville,
    a.cdadhestatut AS statut,
    COALESCE((a.mtadheautre)::numeric, t.mttari) AS montant,
    a.dtadheinscription AS dateinscription,
    d.iddocu AS iddocument,
    EXISTS (
        SELECT 1 FROM moth.document_request dr
        WHERE dr.cddoretype = 'ADHESION'
          AND dr.iddorebusi = a.idadhe
          AND dr.cddorestatut = 'PENDING'
    ) AS documentpending
FROM (moth.adhesion a
     JOIN moth.tarif t ON ((t.idtari = a.idtari)))
     LEFT JOIN (moth.document_metadata dm
         JOIN moth.document d ON d.iddocu = dm.iddocu)
     ON (dm.cddomecle = 'ID_ADHESION' AND dm.txdomevaleur = a.idadhe::text);

CREATE OR REPLACE VIEW moth.v_inscription_light AS
SELECT e.idelev AS id,
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
     d.iddocu AS iddocument,
     EXISTS (
         SELECT 1 FROM moth.document_request dr
         WHERE dr.cddoretype = 'INSCRIPTION_' || i.cdinsctype
           AND dr.iddorebusi = i.idinsc
           AND dr.cddorestatut = 'PENDING'
     ) AS documentpending
FROM ((((moth.inscription i
     JOIN moth.eleve e ON ((e.idinsc = i.idinsc)))
     JOIN moth.resplegal r ON ((i.idresp = r.idresp)))
     JOIN moth.tarif t ON ((t.idtari = i.idtari)))
     JOIN moth.periode p ON ((p.idperi = t.idperi)))
     LEFT JOIN (moth.document_metadata dm
         JOIN moth.document d ON d.iddocu = dm.iddocu)
     ON (dm.cddomecle = 'ID_INSCRIPTION' AND dm.txdomevaleur = i.idinsc::text);
```

`dr.cddoretype = 'INSCRIPTION_' || i.cdinsctype` fonctionne car `cdinsctype` vaut `'ENFANT'`/`'ADULTE'`, ce qui reconstitue exactement `INSCRIPTION_ENFANT`/`INSCRIPTION_ADULTE` (les valeurs de l'enum `DocumentRequestType`).

### Entités / DTOs

`AdhesionLightEntity` / `InscriptionLightEntity` : nouveau champ mappé directement sur la colonne de vue :
```java
@Column(name = "documentpending")
private Boolean documentPending;
```

`AdhesionLightDto` / `InscriptionLightDto` : nouveau champ `Boolean documentPending`, mappé automatiquement par MapStruct (même nom de propriété des deux côtés) — **aucun changement dans les mappers, ni dans les services** (`AdhesionLightServiceImpl`/`InscriptionLightServiceImpl` restent inchangés, la requête via `Specification` sur la vue ramène directement la colonne).

## Frontend

### Types (`src/services/adhesion.ts`, `src/services/inscription.ts`)

Ajout de `documentPending?: boolean` sur `AdhesionLight` et `InscriptionLight`.

### `AdhesionDesktopView.tsx` / `InscriptionDesktopView.tsx`

Le bloc du bouton PDF passe de 2 à 3 branches :

```tsx
{item.documentPending ? (
    <Tooltip title="Document en cours de génération, réessayez dans quelques instants" color="orange">
        <Button icon={<SyncOutlined spin />} size="small" type="primary" disabled />
    </Tooltip>
) : item.idDocument ? (
    <Tooltip title="Télécharger PDF" color="geekblue">
        <Button icon={<FilePdfTwoTone />} size="small" type="primary" onClick={() => { /* inchangé */ }} />
    </Tooltip>
) : (
    <Tooltip title="Le document n'est pas encore disponible" color="orange">
        <Button icon={<FilePdfTwoTone />} size="small" type="primary" disabled />
    </Tooltip>
)}
```

Comportement unifié : que ce soit une première génération (pas encore de `idDocument`) ou une régénération (`idDocument` déjà présent), `documentPending=true` affiche systématiquement le même indicateur "en cours" et désactive le bouton — un seul état à gérer côté UI.

## Testing

- Backend : test d'intégration (base réelle/Testcontainers selon ce qui existe déjà dans le projet) sur les vues `v_adhesion_light`/`v_inscription_light` vérifiant que `documentpending` est `true` uniquement quand une ligne `document_request` PENDING correspondante existe pour le bon `(type, businessId)`, et `false` sinon (y compris quand un `document_request` `COMPLETED`/`ERROR` existe, ou pour le mauvais type — ex. une demande `INSCRIPTION_ADULTE` PENDING ne doit pas marquer une inscription `ENFANT` comme pending).
- Backend : vérifier qu'une inscription et une adhésion partageant le même id numérique (ids indépendants entre `adhesion`/`inscription`) ne se contaminent pas (le typage `cddoretype` dans le `EXISTS` isole bien les deux).
- Frontend : vérifier manuellement dans le navigateur les 3 états du bouton PDF (aucun document, document dispo, PENDING) sur les écrans Adhésion et Inscriptions (desktop).
