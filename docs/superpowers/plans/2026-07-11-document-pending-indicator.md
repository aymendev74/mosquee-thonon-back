# Indicateur "document en cours de (re)génération" — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Afficher un indicateur visuel sur le bouton PDF des écrans admin Adhésion/Inscriptions (desktop) quand une demande de génération de document est `PENDING` pour la ligne affichée.

**Architecture:** Deux repos siblings sous `C:\Projets dev\` : `mosquee-thonon-back` (Spring Boot/Liquibase/PostgreSQL) et `mosquee-thonon-front-2` (React/TypeScript/antd). Le calcul du statut PENDING se fait entièrement en SQL, via une colonne booléenne `documentpending` ajoutée aux vues `moth.v_adhesion_light`/`moth.v_inscription_light` (sous-requête `EXISTS` sur `moth.document_request`), qui remonte automatiquement jusqu'au DTO REST via le mapping JPA/MapStruct existant (aucune requête Java supplémentaire). Le front ajoute une 3e branche au rendu du bouton PDF déjà existant.

**Tech Stack:** Java 18, Spring Boot, Spring Data JPA, MapStruct, Liquibase 4.27, PostgreSQL (prod), H2 en mode PostgreSQL (tests, `ddl-auto: create-drop` + fixture SQL manuelle car les vues ne sont pas des `@Entity` générables), JUnit 5 + MockMvc (back) ; React 18, TypeScript, antd (front, pas de tests automatisés existants sur ces écrans).

## Global Constraints

- Le bouton PDF se désactive pendant **toute** demande `PENDING`, y compris quand un ancien document existe déjà (pas de téléchargement de version périmée pendant la régénération).
- Un seul état "en cours" (`documentPending=true`) couvre à la fois la 1ère génération et la régénération — pas de distinction visuelle entre les deux.
- Périmètre desktop uniquement (`AdhesionDesktopView.tsx`, `InscriptionDesktopView.tsx`) — pas de changement sur les vues mobiles.
- Pas de polling / rafraîchissement automatique ajouté à ces écrans.
- La sous-requête doit utiliser `EXISTS`, jamais un `LEFT JOIN` classique sur `document_request`, pour ne jamais risquer de dupliquer une ligne adhésion/inscription si plusieurs demandes `PENDING` existaient un jour pour le même `(type, businessId)`.
- Spec de référence : `docs/superpowers/specs/2026-07-11-document-pending-indicator-design.md` (repo `mosquee-thonon-back`).

---

## Task 1: Vue SQL de production — changelog Liquibase

**Files:**
- Create: `C:\Projets dev\mosquee-thonon-back\src\main\resources\db\changelog\2026\061-updateView-adhesion-inscription-add-documentpending.yml`

**Interfaces:**
- Consumes: rien (changement de schéma pur).
- Produces: colonne `documentpending` (BOOLEAN) sur `moth.v_adhesion_light` et `moth.v_inscription_light`, consommée par `AdhesionLightEntity.documentPending` / `InscriptionLightEntity.documentPending` (Task 2 et Task 3).

Ce changelog n'est exécuté par aucun test automatisé de ce repo (les tests tournent sur H2 avec `spring.liquibase.enabled: false`, cf. `src/test/resources/application-test.yml:6-7` — c'est `after-init.sql`, mis à jour en Task 2/3, qui sert de garde-fou pour la logique SQL). Sa validation réelle nécessite une base PostgreSQL configurée (identifiants `DB_LIQ_USERNAME`/`DB_LIQ_PASSWORD`), pas disponible dans cet environnement — à valider par toi sur une base de staging avant merge, comme pour le script de reprise SQL précédent.

- [ ] **Step 1: Créer le changelog**

```yaml
databaseChangeLog:
  - changeSet:
      id: 061-updateView-adhesion-inscription-add-documentpending
      author: Aymen
      changes:
        - sql:
            sql: |
              DROP VIEW IF EXISTS moth.v_adhesion_light CASCADE;
              DROP VIEW IF EXISTS moth.v_inscription_light CASCADE;

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

- [ ] **Step 2: Vérifier que le module compile toujours (le changelog est chargé au runtime, pas au build, mais on vérifie qu'on n'a rien cassé ailleurs)**

Run: `cd "C:\Projets dev\mosquee-thonon-back" && mvn -q compile`
Expected: succès, aucune sortie d'erreur.

- [ ] **Step 3: Commit**

```bash
cd "C:/Projets dev/mosquee-thonon-back"
git add src/main/resources/db/changelog/2026/061-updateView-adhesion-inscription-add-documentpending.yml
git commit -m "feat: ajoute documentpending aux vues adhesion/inscription light"
```

---

## Task 2: Adhésion — entité, DTO, fixture de test H2, tests

**Files:**
- Modify: `C:\Projets dev\mosquee-thonon-back\src\test\resources\after-init.sql`
- Modify: `C:\Projets dev\mosquee-thonon-back\src\main\java\org\mosqueethonon\entity\adhesion\AdhesionLightEntity.java`
- Modify: `C:\Projets dev\mosquee-thonon-back\src\main\java\org\mosqueethonon\v1\dto\adhesion\AdhesionLightDto.java`
- Modify: `C:\Projets dev\mosquee-thonon-back\src\test\java\org\mosqueethonon\v1\controller\TestAdhesionController.java`

**Interfaces:**
- Consumes: `AdhesionLightRepository` (existant, `JpaRepository<AdhesionLightEntity, Long>`), `DocumentRequestRepository.findByTypeAndBusinessIdAndStatut(DocumentRequestType, Long, DocumentRequestStatut) : Optional<DocumentRequestEntity>` (existant), `DocumentRequestEntity.setStatut(DocumentRequestStatut)` (existant).
- Produces: `AdhesionLightEntity.getDocumentPending() : Boolean`, `AdhesionLightDto.getDocumentPending() : Boolean` (consommés par le front en Task 4/5).

- [ ] **Step 1: Écrire les tests (ils ne compileront pas encore — `getDocumentPending()` n'existe pas)**

Ajouter dans `TestAdhesionController.java`, avec ces imports supplémentaires en haut du fichier :

```java
import org.mosqueethonon.entity.adhesion.AdhesionLightEntity;
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.repository.AdhesionLightRepository;
import org.mosqueethonon.repository.DocumentRequestRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
```

Ajouter ce champ autowired à côté de `adhesionRepository` (ligne 37) :

```java
    @Autowired
    protected AdhesionLightRepository adhesionLightRepository;

    @Autowired
    protected DocumentRequestRepository documentRequestRepository;
```

Ajouter ces deux tests à la fin de la classe, avant la dernière accolade :

```java
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdhesionLight_DocumentPendingTrueAfterCreate() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/v1/adhesions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAdhesion)))
                .andReturn().getResponse().getContentAsString();
        AdhesionDto createdAdhesion = objectMapper.readValue(response, AdhesionDto.class);

        AdhesionLightEntity light = adhesionLightRepository.findById(createdAdhesion.getId()).orElseThrow();
        assertTrue(light.getDocumentPending());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAdhesionLight_DocumentPendingFalseWhenRequestCompleted() throws Exception {
        String response = mockMvc.perform(MockMvcRequestBuilders.post("/v1/adhesions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAdhesion)))
                .andReturn().getResponse().getContentAsString();
        AdhesionDto createdAdhesion = objectMapper.readValue(response, AdhesionDto.class);

        DocumentRequestEntity request = documentRequestRepository
                .findByTypeAndBusinessIdAndStatut(DocumentRequestType.ADHESION, createdAdhesion.getId(), DocumentRequestStatut.PENDING)
                .orElseThrow();
        request.setStatut(DocumentRequestStatut.COMPLETED);
        documentRequestRepository.save(request);

        AdhesionLightEntity light = adhesionLightRepository.findById(createdAdhesion.getId()).orElseThrow();
        assertFalse(light.getDocumentPending());
    }
```

- [ ] **Step 2: Lancer les tests pour vérifier qu'ils échouent (à la compilation)**

Run: `cd "C:\Projets dev\mosquee-thonon-back" && mvn test -Dtest=TestAdhesionController`
Expected: `COMPILATION ERROR` — `cannot find symbol: method getDocumentPending()` sur `AdhesionLightEntity`.

- [ ] **Step 3: Mettre à jour la fixture H2 (`after-init.sql`)**

Dans `after-init.sql`, remplacer le bloc `v_adhesion_light` :

```sql
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
```

par :

```sql
CREATE VIEW moth.v_adhesion_light AS
SELECT
    a.idadhe AS id,
    a.txadhenom AS nom,
    a.txadheprenom AS prenom,
    a.txadheville AS ville,
    a.cdadhestatut AS statut,
    COALESCE(a.mtadheautre, t.mttari) AS montant,
    a.dtadheinscription AS dateinscription,
    d.iddocu AS iddocument,
    EXISTS (
        SELECT 1 FROM moth.document_request dr
        WHERE dr.cddoretype = 'ADHESION'
          AND dr.iddorebusi = a.idadhe
          AND dr.cddorestatut = 'PENDING'
    ) AS documentpending
FROM moth.adhesion a
         INNER JOIN moth.tarif t ON t.idtari = a.idtari
         LEFT JOIN (moth.document_metadata dm
    JOIN moth.document d ON d.iddocu = dm.iddocu)
                   ON (dm.cddomecle = 'ID_ADHESION' AND dm.txdomevaleur = CAST(a.idadhe AS VARCHAR))
;
```

- [ ] **Step 4: Ajouter le champ à `AdhesionLightEntity`**

Dans `AdhesionLightEntity.java`, ajouter après `idDocument` (ligne 32) :

```java
    @Column(name = "documentpending")
    private Boolean documentPending;
```

- [ ] **Step 5: Ajouter le champ à `AdhesionLightDto`**

Dans `AdhesionLightDto.java`, ajouter après `idDocument` (ligne 21) :

```java
    private Boolean documentPending;
```

- [ ] **Step 6: Relancer les tests pour vérifier qu'ils passent**

Run: `cd "C:\Projets dev\mosquee-thonon-back" && mvn test -Dtest=TestAdhesionController`
Expected: `BUILD SUCCESS`, tous les tests de `TestAdhesionController` passent (y compris les 2 nouveaux).

- [ ] **Step 7: Commit**

```bash
cd "C:/Projets dev/mosquee-thonon-back"
git add src/test/resources/after-init.sql \
        src/main/java/org/mosqueethonon/entity/adhesion/AdhesionLightEntity.java \
        src/main/java/org/mosqueethonon/v1/dto/adhesion/AdhesionLightDto.java \
        src/test/java/org/mosqueethonon/v1/controller/TestAdhesionController.java
git commit -m "feat: expose documentPending sur AdhesionLight"
```

---

## Task 3: Inscription (enfant + adulte) — entité, DTO, fixture de test H2, tests

**Files:**
- Modify: `C:\Projets dev\mosquee-thonon-back\src\test\resources\after-init.sql`
- Modify: `C:\Projets dev\mosquee-thonon-back\src\main\java\org\mosqueethonon\entity\inscription\InscriptionLightEntity.java`
- Modify: `C:\Projets dev\mosquee-thonon-back\src\main\java\org\mosqueethonon\v1\dto\inscription\InscriptionLightDto.java`
- Modify: `C:\Projets dev\mosquee-thonon-back\src\test\java\org\mosqueethonon\v1\controller\TestInscriptionEnfantController.java`
- Modify: `C:\Projets dev\mosquee-thonon-back\src\test\java\org\mosqueethonon\v1\controller\TestInscriptionAdulteController.java`

**Interfaces:**
- Consumes: `InscriptionLightRepository` (existant, `JpaRepository<InscriptionLightEntity, Long>` — attention, `id` = idelev, PAS idinsc), `DocumentRequestRepository.findByTypeAndBusinessIdAndStatut(...)` (existant, Task 2), `InscriptionEnfantRepository.findAll()`/`InscriptionAdulteRepository.findAll()` (existants).
- Produces: `InscriptionLightEntity.getDocumentPending() : Boolean`, `InscriptionLightDto.getDocumentPending() : Boolean` (consommés par le front en Task 4/6).

- [ ] **Step 1: Écrire le test enfant (ne compilera pas encore)**

Ajouter dans `TestInscriptionEnfantController.java`, imports supplémentaires :

```java
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.entity.inscription.InscriptionLightEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.repository.InscriptionLightRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
```

Champs autowired à côté de `inscriptionEnfantRepository` (ligne 49) :

```java
    @Autowired
    protected InscriptionLightRepository inscriptionLightRepository;

    @Autowired
    protected DocumentRequestRepository documentRequestRepository;
```

Tests à ajouter en fin de classe :

```java
    @Test
    @WithMockUser(username = "anonymous")
    public void testInscriptionEnfantLight_DocumentPendingTrueAfterCreate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/inscriptions-enfants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(this.createInscription()))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Long idInscription = this.inscriptionEnfantRepository.findAll().get(0).getId();
        InscriptionLightEntity light = inscriptionLightRepository.findAll().stream()
                .filter(l -> idInscription.equals(l.getIdInscription()))
                .findFirst().orElseThrow();
        assertTrue(light.getDocumentPending());
    }

    @Test
    @WithMockUser(username = "anonymous")
    public void testInscriptionEnfantLight_DocumentPendingFalseWhenRequestCompleted() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/inscriptions-enfants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(this.createInscription()))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Long idInscription = this.inscriptionEnfantRepository.findAll().get(0).getId();
        DocumentRequestEntity request = documentRequestRepository
                .findByTypeAndBusinessIdAndStatut(DocumentRequestType.INSCRIPTION_ENFANT, idInscription, DocumentRequestStatut.PENDING)
                .orElseThrow();
        request.setStatut(DocumentRequestStatut.COMPLETED);
        documentRequestRepository.save(request);

        InscriptionLightEntity light = inscriptionLightRepository.findAll().stream()
                .filter(l -> idInscription.equals(l.getIdInscription()))
                .findFirst().orElseThrow();
        assertFalse(light.getDocumentPending());
    }
```

- [ ] **Step 2: Écrire le test adulte (même principe)**

Ajouter dans `TestInscriptionAdulteController.java`, imports supplémentaires :

```java
import org.mosqueethonon.entity.document.DocumentRequestEntity;
import org.mosqueethonon.entity.inscription.InscriptionLightEntity;
import org.mosqueethonon.enums.DocumentRequestStatut;
import org.mosqueethonon.enums.DocumentRequestType;
import org.mosqueethonon.repository.DocumentRequestRepository;
import org.mosqueethonon.repository.InscriptionLightRepository;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
```

Champs autowired à côté de `inscriptionAdulteRepository` (ligne 49) :

```java
    @Autowired
    protected InscriptionLightRepository inscriptionLightRepository;

    @Autowired
    protected DocumentRequestRepository documentRequestRepository;
```

Tests à ajouter en fin de classe :

```java
    @Test
    @WithMockUser(username = "anonymous")
    public void testInscriptionAdulteLight_DocumentPendingTrueAfterCreate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/inscriptions-adultes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(this.createInscriptionAdulte()))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Long idInscription = this.inscriptionAdulteRepository.findAll().get(0).getId();
        InscriptionLightEntity light = inscriptionLightRepository.findAll().stream()
                .filter(l -> idInscription.equals(l.getIdInscription()))
                .findFirst().orElseThrow();
        assertTrue(light.getDocumentPending());
    }

    @Test
    @WithMockUser(username = "anonymous")
    public void testInscriptionAdulteLight_DocumentPendingFalseWhenRequestCompleted() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/inscriptions-adultes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.writeValueAsString(this.createInscriptionAdulte()))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Long idInscription = this.inscriptionAdulteRepository.findAll().get(0).getId();
        DocumentRequestEntity request = documentRequestRepository
                .findByTypeAndBusinessIdAndStatut(DocumentRequestType.INSCRIPTION_ADULTE, idInscription, DocumentRequestStatut.PENDING)
                .orElseThrow();
        request.setStatut(DocumentRequestStatut.COMPLETED);
        documentRequestRepository.save(request);

        InscriptionLightEntity light = inscriptionLightRepository.findAll().stream()
                .filter(l -> idInscription.equals(l.getIdInscription()))
                .findFirst().orElseThrow();
        assertFalse(light.getDocumentPending());
    }
```

- [ ] **Step 3: Lancer les deux classes pour vérifier qu'elles échouent (à la compilation)**

Run: `cd "C:\Projets dev\mosquee-thonon-back" && mvn test -Dtest=TestInscriptionEnfantController,TestInscriptionAdulteController`
Expected: `COMPILATION ERROR` — `cannot find symbol: method getDocumentPending()` sur `InscriptionLightEntity`.

- [ ] **Step 4: Mettre à jour la fixture H2 (`after-init.sql`)**

Dans `after-init.sql`, remplacer :

```sql
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
```

par :

```sql
    i.cdinsctype AS type,
    d.iddocu AS iddocument,
    EXISTS (
        SELECT 1 FROM moth.document_request dr
        WHERE dr.cddoretype = 'INSCRIPTION_' || i.cdinsctype
          AND dr.iddorebusi = i.idinsc
          AND dr.cddorestatut = 'PENDING'
    ) AS documentpending
FROM ((((moth.inscription i
    JOIN moth.eleve e ON (e.idinsc = i.idinsc))
    JOIN moth.resplegal r ON (i.idresp = r.idresp))
    JOIN moth.tarif t ON (t.idtari = i.idtari))
    JOIN moth.periode p ON (p.idperi = t.idperi))
         LEFT JOIN (moth.document_metadata dm
    JOIN moth.document d ON d.iddocu = dm.iddocu)
                   ON (dm.cddomecle = 'ID_INSCRIPTION' AND dm.txdomevaleur = CAST(i.idinsc AS VARCHAR))
;
```

- [ ] **Step 5: Ajouter le champ à `InscriptionLightEntity`**

Dans `InscriptionLightEntity.java`, ajouter après `idDocument` (ligne 65) :

```java
    @Column(name = "documentpending")
    private Boolean documentPending;
```

- [ ] **Step 6: Ajouter le champ à `InscriptionLightDto`**

Dans `InscriptionLightDto.java`, ajouter après `idDocument` (ligne 36) :

```java
    private Boolean documentPending;
```

- [ ] **Step 7: Relancer les tests pour vérifier qu'ils passent**

Run: `cd "C:\Projets dev\mosquee-thonon-back" && mvn test -Dtest=TestInscriptionEnfantController,TestInscriptionAdulteController`
Expected: `BUILD SUCCESS`, tous les tests passent (y compris les 4 nouveaux).

- [ ] **Step 8: Lancer toute la suite de tests backend pour vérifier l'absence de régression**

Run: `cd "C:\Projets dev\mosquee-thonon-back" && mvn test`
Expected: `BUILD SUCCESS`.

- [ ] **Step 9: Commit**

```bash
cd "C:/Projets dev/mosquee-thonon-back"
git add src/test/resources/after-init.sql \
        src/main/java/org/mosqueethonon/entity/inscription/InscriptionLightEntity.java \
        src/main/java/org/mosqueethonon/v1/dto/inscription/InscriptionLightDto.java \
        src/test/java/org/mosqueethonon/v1/controller/TestInscriptionEnfantController.java \
        src/test/java/org/mosqueethonon/v1/controller/TestInscriptionAdulteController.java
git commit -m "feat: expose documentPending sur InscriptionLight (enfant + adulte)"
```

---

## Task 4: Front — types TypeScript

**Files:**
- Modify: `C:\Projets dev\mosquee-thonon-front-2\src\services\adhesion.ts:25-34`
- Modify: `C:\Projets dev\mosquee-thonon-front-2\src\services\inscription.ts:33-55`

**Interfaces:**
- Consumes: réponse JSON de `GET /v1/adhesions` / `GET /v1/inscriptions`, qui inclura désormais `documentPending` (Task 2, Task 3).
- Produces: `AdhesionLight.documentPending?: boolean`, `InscriptionLight.documentPending?: boolean`, consommés en Task 5/6.

- [ ] **Step 1: Ajouter le champ à `AdhesionLight`**

Dans `src/services/adhesion.ts`, remplacer :

```ts
export type AdhesionLight = {
    id: number;
    nom: string;
    prenom: string;
    ville: string;
    montant: number;
    statut: StatutInscription | boolean;
    dateInscription: Dayjs | string;
    idDocument?: number;
}
```

par :

```ts
export type AdhesionLight = {
    id: number;
    nom: string;
    prenom: string;
    ville: string;
    montant: number;
    statut: StatutInscription | boolean;
    dateInscription: Dayjs | string;
    idDocument?: number;
    documentPending?: boolean;
}
```

- [ ] **Step 2: Ajouter le champ à `InscriptionLight`**

Dans `src/services/inscription.ts`, remplacer :

```ts
export type InscriptionLight = {
    id: number;
    idInscription: number;
    nom: string;
    prenom: string;
    nomResponsableLegal: string;
    prenomResponsableLegal: string;
    nomContactUrgence: string;
    prenomContactUrgence: string;
    dateNaissance: string;
    niveau: string;
    niveauInterne: string;
    mobile: string;
    mobileContactUrgence: string
    autorisationAutonomie: boolean,
    autorisationMedia: boolean,
    statut: StatutInscription;
    ville: string;
    dateInscription: string;
    noInscription: string;
    email: string;
    idDocument?: number;
}
```

par :

```ts
export type InscriptionLight = {
    id: number;
    idInscription: number;
    nom: string;
    prenom: string;
    nomResponsableLegal: string;
    prenomResponsableLegal: string;
    nomContactUrgence: string;
    prenomContactUrgence: string;
    dateNaissance: string;
    niveau: string;
    niveauInterne: string;
    mobile: string;
    mobileContactUrgence: string
    autorisationAutonomie: boolean,
    autorisationMedia: boolean,
    statut: StatutInscription;
    ville: string;
    dateInscription: string;
    noInscription: string;
    email: string;
    idDocument?: number;
    documentPending?: boolean;
}
```

- [ ] **Step 3: Vérifier la compilation TypeScript**

Run: `cd "C:\Projets dev\mosquee-thonon-front-2" && npx tsc --noEmit`
Expected: aucune erreur (ajout de champ optionnel, non-breaking).

- [ ] **Step 4: Commit**

```bash
cd "C:/Projets dev/mosquee-thonon-front-2"
git add src/services/adhesion.ts src/services/inscription.ts
git commit -m "feat: ajoute documentPending aux types AdhesionLight/InscriptionLight"
```

---

## Task 5: Front — bouton PDF Adhésion

**Files:**
- Modify: `C:\Projets dev\mosquee-thonon-front-2\src\routes\admin\adhesion\AdhesionDesktopView.tsx:3` (import icône)
- Modify: `C:\Projets dev\mosquee-thonon-front-2\src\routes\admin\adhesion\AdhesionDesktopView.tsx:130-146` (rendu bouton)

**Interfaces:**
- Consumes: `AdhesionLight.documentPending` (Task 4).
- Produces: rien (feuille de l'arbre de dépendances).

- [ ] **Step 1: Ajouter l'import de l'icône**

Remplacer la ligne 3 :

```tsx
import { CheckCircleOutlined, CheckCircleTwoTone, DeleteOutlined, EditOutlined, EyeOutlined, FileExcelOutlined, FilePdfTwoTone, PauseCircleTwoTone } from "@ant-design/icons";
```

par :

```tsx
import { CheckCircleOutlined, CheckCircleTwoTone, DeleteOutlined, EditOutlined, EyeOutlined, FileExcelOutlined, FilePdfTwoTone, PauseCircleTwoTone, SyncOutlined } from "@ant-design/icons";
```

- [ ] **Step 2: Ajouter la branche PENDING au rendu du bouton PDF**

Remplacer (lignes 130-146) :

```tsx
                    {adhesion.idDocument ? (
                        <Tooltip title="Télécharger PDF" color="geekblue">
                            <Button
                                icon={<FilePdfTwoTone />}
                                size="small"
                                type="primary"
                                onClick={() => {
                                    const url = buildUrlWithParams(DOCUMENT_CONTENT_ENDPOINT, { idDocument: adhesion.idDocument });
                                    window.open(`${process.env.REACT_APP_BASE_URL_API_V1}${url}`, '_blank');
                                }}
                            />
                        </Tooltip>
                    ) : (
                        <Tooltip title="Le document n'est pas encore disponible" color="orange">
                            <Button icon={<FilePdfTwoTone />} size="small" type="primary" disabled />
                        </Tooltip>
                    )}
```

par :

```tsx
                    {adhesion.documentPending ? (
                        <Tooltip title="Document en cours de génération, réessayez dans quelques instants" color="orange">
                            <Button icon={<SyncOutlined spin />} size="small" type="primary" disabled />
                        </Tooltip>
                    ) : adhesion.idDocument ? (
                        <Tooltip title="Télécharger PDF" color="geekblue">
                            <Button
                                icon={<FilePdfTwoTone />}
                                size="small"
                                type="primary"
                                onClick={() => {
                                    const url = buildUrlWithParams(DOCUMENT_CONTENT_ENDPOINT, { idDocument: adhesion.idDocument });
                                    window.open(`${process.env.REACT_APP_BASE_URL_API_V1}${url}`, '_blank');
                                }}
                            />
                        </Tooltip>
                    ) : (
                        <Tooltip title="Le document n'est pas encore disponible" color="orange">
                            <Button icon={<FilePdfTwoTone />} size="small" type="primary" disabled />
                        </Tooltip>
                    )}
```

- [ ] **Step 3: Vérifier la compilation TypeScript**

Run: `cd "C:\Projets dev\mosquee-thonon-front-2" && npx tsc --noEmit`
Expected: aucune erreur.

- [ ] **Step 4: Commit**

```bash
cd "C:/Projets dev/mosquee-thonon-front-2"
git add src/routes/admin/adhesion/AdhesionDesktopView.tsx
git commit -m "feat: indicateur documentPending sur le bouton PDF adhesion"
```

---

## Task 6: Front — bouton PDF Inscriptions

**Files:**
- Modify: `C:\Projets dev\mosquee-thonon-front-2\src\routes\admin\cours\InscriptionDesktopView.tsx:3` (import icône)
- Modify: `C:\Projets dev\mosquee-thonon-front-2\src\routes\admin\cours\InscriptionDesktopView.tsx:194-210` (rendu bouton)

**Interfaces:**
- Consumes: `InscriptionLight.documentPending` (Task 4).
- Produces: rien (feuille de l'arbre de dépendances).

- [ ] **Step 1: Ajouter l'import de l'icône**

Remplacer la ligne 3 :

```tsx
import { CheckCircleOutlined, CheckCircleTwoTone, DeleteOutlined, EditOutlined, EyeOutlined, FileExcelOutlined, FilePdfTwoTone, PauseCircleTwoTone, StopOutlined, WarningOutlined } from "@ant-design/icons";
```

par :

```tsx
import { CheckCircleOutlined, CheckCircleTwoTone, DeleteOutlined, EditOutlined, EyeOutlined, FileExcelOutlined, FilePdfTwoTone, PauseCircleTwoTone, StopOutlined, SyncOutlined, WarningOutlined } from "@ant-design/icons";
```

- [ ] **Step 2: Ajouter la branche PENDING au rendu du bouton PDF**

Remplacer (lignes 194-210) :

```tsx
                    {inscription.idDocument ? (
                        <Tooltip title="Télécharger PDF" color="geekblue">
                            <Button
                                icon={<FilePdfTwoTone />}
                                size="small"
                                type="primary"
                                onClick={() => {
                                    const url = buildUrlWithParams(DOCUMENT_CONTENT_ENDPOINT, { idDocument: inscription.idDocument });
                                    window.open(`${process.env.REACT_APP_BASE_URL_API_V1}${url}`, '_blank');
                                }}
                            />
                        </Tooltip>
                    ) : (
                        <Tooltip title="Le document n'est pas encore disponible" color="orange">
                            <Button icon={<FilePdfTwoTone />} size="small" type="primary" disabled />
                        </Tooltip>
                    )}
```

par :

```tsx
                    {inscription.documentPending ? (
                        <Tooltip title="Document en cours de génération, réessayez dans quelques instants" color="orange">
                            <Button icon={<SyncOutlined spin />} size="small" type="primary" disabled />
                        </Tooltip>
                    ) : inscription.idDocument ? (
                        <Tooltip title="Télécharger PDF" color="geekblue">
                            <Button
                                icon={<FilePdfTwoTone />}
                                size="small"
                                type="primary"
                                onClick={() => {
                                    const url = buildUrlWithParams(DOCUMENT_CONTENT_ENDPOINT, { idDocument: inscription.idDocument });
                                    window.open(`${process.env.REACT_APP_BASE_URL_API_V1}${url}`, '_blank');
                                }}
                            />
                        </Tooltip>
                    ) : (
                        <Tooltip title="Le document n'est pas encore disponible" color="orange">
                            <Button icon={<FilePdfTwoTone />} size="small" type="primary" disabled />
                        </Tooltip>
                    )}
```

- [ ] **Step 3: Vérifier la compilation TypeScript**

Run: `cd "C:\Projets dev\mosquee-thonon-front-2" && npx tsc --noEmit`
Expected: aucune erreur.

- [ ] **Step 4: Commit**

```bash
cd "C:/Projets dev/mosquee-thonon-front-2"
git add src/routes/admin/cours/InscriptionDesktopView.tsx
git commit -m "feat: indicateur documentPending sur le bouton PDF inscriptions"
```

---

## Task 7: Vérification manuelle bout-en-bout

**Files:** aucun (vérification uniquement, pas de code).

**Interfaces:**
- Consumes: back (Task 1-3) + front (Task 4-6) déployés/lancés localement.
- Produces: confirmation visuelle que la feature fonctionne, condition pour merger sur master.

Rappel : aucun test automatisé front n'existe dans ce repo (`react-scripts test` n'a aucun fichier `*.test.tsx` à date) — cette étape est la seule couverture du rendu réel des 3 états du bouton.

- [ ] **Step 1: Lancer le backend et le frontend en local**

Run backend (nécessite une base PostgreSQL configurée avec les changelogs 057-061 appliqués) : `cd "C:\Projets dev\mosquee-thonon-back" && mvn spring-boot:run`
Run frontend : `cd "C:\Projets dev\mosquee-thonon-front-2" && npm start`

- [ ] **Step 2: Vérifier l'état "document disponible" (inchangé)**

Sur `/admin/adhesions` ou `/admin/coursArabes`, repérer une ligne avec un document déjà généré (bouton PDF actif, tooltip "Télécharger PDF"). Cliquer dessus, vérifier que le PDF s'ouvre bien dans un nouvel onglet (non régression).

- [ ] **Step 3: Vérifier l'état "en cours de génération" (nouveau)**

Modifier une adhésion ou une inscription existante (bouton "Modifier" puis sauvegarder sans forcément changer de valeur, ou changer le statut). Recharger immédiatement la recherche : le bouton PDF de cette ligne doit passer en désactivé, icône `SyncOutlined` animée, tooltip "Document en cours de génération, réessayez dans quelques instants". Attendre ~1 minute (le job planifié tourne toutes les minutes) puis relancer la recherche : le bouton doit redevenir actif (ou repasser à "pas encore disponible" si la génération a échoué — vérifier les logs backend dans ce cas).

- [ ] **Step 4: Vérifier l'état "pas encore de document" (inchangé)**

Créer une nouvelle adhésion ou inscription. Immédiatement après création (avant le prochain passage du job), le bouton doit afficher soit "en cours de génération" (si la recherche est relancée tout de suite après la requête PENDING créée à la création) soit, une fois le job passé sans qu'aucun document n'ait pu être généré (cas d'erreur), l'état désactivé "pas encore disponible" — confirmer qu'aucun des deux états ne casse l'affichage.

- [ ] **Step 5: Vérifier le scope desktop uniquement**

Réduire la fenêtre du navigateur (ou utiliser les devtools en mode mobile) pour passer sur `InscriptionMobileView`/`AdhesionMobileView` : confirmer qu'il n'y a toujours aucun bouton PDF (comportement inchangé, hors scope de cette feature).

---

## Self-Review

**Couverture de la spec** : Vue SQL (`EXISTS`, pas `LEFT JOIN`) → Task 1. Propagation entité/DTO sans code de service supplémentaire → Task 2/3. Type front → Task 4. Rendu bouton 3 états, désactivé pendant PENDING même si `idDocument` existe → Task 5/6. Scope desktop only, pas de polling → Task 7 (vérifié, rien à coder). Tout couvert.

**Cohérence des types/noms** : `documentPending` (Java/TS) ↔ `documentpending` (colonne SQL) — cohérent sur toutes les tasks. `getDocumentPending()`/`getIdInscription()` utilisés identiquement en Task 2/3. Pas de divergence détectée.

**Placeholders** : aucun "TBD"/"TODO" — tous les steps contiennent du code réel ou une commande exacte avec sortie attendue.
