# Saisie des paiements sur les inscriptions — Design

**Repos concernés** : `mosquee-thonon-back` et `mosquee-thonon-front-2`.

## Contexte

Chaque année en septembre, les familles inscrites viennent au bureau de l'association
finaliser leur inscription et la régler — en espèces, par carte ou par chèque, en une ou
plusieurs fois. **L'application ne garde aujourd'hui aucune trace de ces encaissements.**

Le montant dû est pourtant déjà calculé et stocké : `InscriptionEntity.montantTotal`
(colonne `mtinsctotal`), alimenté par `InscriptionEnfantServiceImpl.doCalculTarifInscription`
et `InscriptionAdulteServiceImpl.calculTarif` à chaque création et à chaque mise à jour de
l'inscription. Mais rien n'indique ce qui a été payé : le suivi des encaissements se fait
intégralement hors système.

Un point du modèle existant simplifie beaucoup les choses : **les inscriptions enfants et
adultes partagent la même table** `moth.inscription`, distinguées par le discriminateur
`cdinsctype` (`InscriptionEntity` est `@Inheritance(SINGLE_TABLE)`). Une seule référence
suffit donc à couvrir les deux populations.

## Objectif

Permettre à un administrateur de saisir les règlements sur une inscription (enfant comme
adulte), avec les contrôles nécessaires sur le montant, et voir d'un coup d'œil dans la
liste d'administration ce qui reste à encaisser.

**Contrainte structurante** : le modèle doit rendre l'ajout du paiement en ligne (Stripe ou
équivalent) quasi gratuit plus tard — un simple mode de paiement supplémentaire (`WEB`)
avec un identifiant de transaction, sans refonte du domaine.

## Décisions

| # | Décision | Raison |
|---|---|---|
| 1 | Cible **polymorphe** `(cdpaietypecible, idpaiecible)` plutôt qu'une FK `idinsc` | Rend les paiements d'adhésion gratuits plus tard pour le coût d'une colonne. Précédent dans le codebase : `document_request` (`cddoretype` / `iddorebusi`). Contrepartie assumée : pas de contrainte d'intégrité référentielle en base |
| 2 | **ADMIN uniquement** | Le rôle trésorier sera étudié plus tard. C'est le comportement par défaut, cf. § Sécurité |
| 3 | Modes `ESPECE`, `CARTE`, `CHEQUE`, `VIREMENT`, et `WEB` réservé | `WEB` est déclaré dès maintenant pour figer le contrat, mais refusé en saisie manuelle |
| 4 | **Aucune suppression physique** : annulation par passage en `ANNULE` | Traçabilité. Un paiement annulé reste visible dans l'historique et devient figé |
| 5 | Modification autorisée tant que le paiement est `VALIDE` | Les colonnes d'audit tracent qui et quand ; la valeur précédente n'est pas conservée (cf. Hors scope) |
| 6 | Sur-paiement **bloqué** côté serveur et côté client, mais un reste négatif est **affiché** comme « Trop-perçu » | Ne peut survenir que si un tarif est recalculé à la baisse après encaissement. Ne doit pas être masqué |

## Hors scope (décidé explicitement)

- **Écran / dashboard dédié aux paiements** (inscriptions non soldées, totaux encaissés par
  mode, export comptable). Reporté à un lot ultérieur.
- **Historique des modifications d'un paiement** : les colonnes d'audit (`oh_vis_mod`,
  `oh_date_mod`) tracent *qui* a modifié et *quand*, pas la valeur précédente. Conserver
  l'ancien montant demanderait une table `paiement_historique`, non justifiée en V1.
- **Paiement en ligne** : le modèle est prêt (mode `WEB`, colonne `txpaiereference` pour le
  `transactionId`, statuts `EN_ATTENTE` / `ECHOUE` / `REMBOURSE` à ajouter à l'enum — sans
  migration, la colonne est un `VARCHAR`), le flux ne l'est pas.
- **Paiements sur les adhésions** : le modèle est prêt (`TypeCiblePaiementEnum.ADHESION`),
  ni le service ni l'UI ne le câblent.
- **Filtre de recherche serveur sur l'état de règlement** : inutile, `GET /v1/inscriptions`
  renvoie la liste complète et antd pagine en mémoire. Le filtre est purement client.

## Modèle de données

Nouvelle table `moth.paiement`, code 4 lettres `paie`, préfixes de colonnes selon la
convention observée partout dans le schéma (`id` / `tx` / `dt` / `cd` / `mt`).

| Colonne | Type | Champ Java |
|---|---|---|
| `idpaie` | BIGINT PK autoincrement | `id` |
| `cdpaietypecible` | VARCHAR(20) NOT NULL | `typeCible` (`TypeCiblePaiementEnum`) |
| `idpaiecible` | BIGINT NOT NULL | `idCible` |
| `mtpaie` | NUMERIC(10,2) NOT NULL | `montant` (`BigDecimal`) |
| `dtpaie` | DATE NOT NULL | `datePaiement` (`LocalDate`) |
| `cdpaiemode` | VARCHAR(20) NOT NULL | `mode` (`ModePaiementEnum`) |
| `cdpaiestatut` | VARCHAR(20) NOT NULL | `statut` (`StatutPaiementEnum`) |
| `txpaiereference` | VARCHAR(100) NULL | `reference` |
| `txpaiecommentaire` | VARCHAR(500) NULL | `commentaire` |
| `oh_date_cre`, `oh_vis_cre`, `oh_date_mod`, `oh_vis_mod` | | `@Embedded Signature` |
| `oh_version` | BIGINT | `@Version Long version` |

Index sur `(cdpaietypecible, idpaiecible)` — c'est l'unique axe de lecture.

**`txpaiereference` porte le numéro de chèque aujourd'hui et portera l'identifiant de
transaction Stripe demain.** C'est délibéré : les deux jouent le même rôle métier
(référence externe permettant de retrouver le règlement), et une seule colonne évite d'en
ajouter une à chaque nouveau moyen de paiement.

Enums, volontairement minimalistes en V1 :

- `ModePaiementEnum` : `ESPECE`, `CARTE`, `CHEQUE`, `VIREMENT`, `WEB`
- `StatutPaiementEnum` : `VALIDE`, `ANNULE`
- `TypeCiblePaiementEnum` : `INSCRIPTION`

### Le reste à payer n'est jamais stocké

`resteAPayer = montantTotal − Σ(montant des paiements VALIDE)`, calculé à la demande.

Aucune colonne dénormalisée, donc **aucune dérive possible** — ce qui compte ici car
`montantTotal` est recalculé à chaque `updateInscription`. Une colonne « déjà réglé »
maintenue à la main se serait désynchronisée au premier changement de tarif. Les volumes en
jeu (quelques centaines d'inscriptions par an) rendent le coût du calcul négligeable.

## Backend

### Domaine

Nouveau domaine de premier niveau `org.mosqueethonon.paiement`, conforme au gabarit de
`docs/CONVENTIONS.md` :

```
paiement/
├── entity/PaiementEntity.java
├── enums/{ModePaiementEnum, StatutPaiementEnum, TypeCiblePaiementEnum, StatutReglementEnum}.java
├── exception/PaiementValidationException.java
├── repository/PaiementRepository.java
├── service/PaiementService.java              (interface seule)
│   └── impl/PaiementServiceImpl.java
└── v1/
    ├── controller/PaiementController.java
    ├── dto/{PaiementDto, SituationPaiementDto}.java
    └── mapper/PaiementMapper.java
```

`PaiementEntity` implémente `Auditable`, porte `@EntityListeners(EntityListener.class)` et
`@Table(name = "paiement", schema = "moth")`, sur la recette de `InscriptionEntity` et
`TarifEntity`.

### Service

```java
public interface PaiementService {
    SituationPaiementDto getSituation(TypeCiblePaiementEnum typeCible, Long idCible);
    PaiementDto creer(PaiementDto paiement);
    PaiementDto modifier(Long id, PaiementDto paiement);
    PaiementDto annuler(Long id);
}
```

`SituationPaiementDto` porte `typeCible`, `idCible`, `montantTotal`, `montantRegle`,
`resteAPayer`, `statutReglement`, et `List<PaiementDto> paiements` — **paiements annulés
compris**, pour la traçabilité ; le front les grise.

`statutReglement` (`StatutReglementEnum`, dérivé, jamais persisté) :

| Valeur | Condition |
|---|---|
| `SOLDE` | `montantRegle >= montantTotal` (couvre le cas `montantTotal == 0`) |
| `NON_REGLE` | `montantRegle == 0` et `montantTotal > 0` |
| `PARTIEL` | `0 < montantRegle < montantTotal` |
| `TROP_PERCU` | `montantRegle > montantTotal` |

`TROP_PERCU` est évalué avant `SOLDE`.

Le montant dû est lu via `InscriptionRepository`. Le `switch` sur `typeCible` qui le
résout est **le seul endroit à étendre** pour couvrir les adhésions plus tard.

### Règles de validation

Toutes côté serveur — le front les rejoue pour le confort de saisie, mais le serveur fait
foi. Chacune lève une `PaiementValidationException` porteuse d'un code.

| Code | Règle |
|---|---|
| `CIBLE_INTROUVABLE` | l'inscription visée doit exister |
| `MONTANT_INVALIDE` | `montant > 0` |
| `MONTANT_SUPERIEUR_RESTE` | `montant ≤ resteAPayer`, **en excluant le paiement courant** lors d'une modification |
| `DATE_OBLIGATOIRE` | date de paiement renseignée |
| `DATE_FUTURE` | date `≤ aujourd'hui` |
| `MODE_WEB_NON_AUTORISE` | le mode `WEB` est refusé en saisie manuelle |
| `PAIEMENT_ANNULE_NON_MODIFIABLE` | un paiement `ANNULE` ne peut être ni modifié ni ré-annulé |

La date « du jour » se lit via le bean `Clock` injecté (`common/config/TimeConfiguration`),
jamais via `LocalDate.now()` : surefire force `-Duser.timezone=UTC`, un appel direct rendrait
les tests dépendants du fuseau.

`txpaiereference` reste **facultative quel que soit le mode**, y compris `CHEQUE` — le front
la met en avant pour un chèque sans l'imposer.

### REST

| Verbe | Chemin | Retour |
|---|---|---|
| `GET` | `/v1/paiements?typeCible=INSCRIPTION&idCible={id}` | `SituationPaiementDto` |
| `POST` | `/v1/paiements` | `SituationPaiementDto` |
| `PUT` | `/v1/paiements/{id}` | `SituationPaiementDto` |
| `POST` | `/v1/paiements/{id}/annulation` | `SituationPaiementDto` |

**Les trois mutations renvoient la situation complète, pas le seul paiement créé ou
modifié.** Toute mutation change mécaniquement le montant réglé, le reste à payer et l'état
de règlement : les renvoyer dans la réponse évite un rechargement systématique derrière
chaque enregistrement. Le composant front se limite alors à un chargement initial et zéro
rechargement.

L'annulation est un endpoint explicite plutôt qu'un `DELETE` : le verbe `DELETE` suggérerait
une suppression, or la ligne est conservée.

Dates des DTO en `@JsonFormat(shape = STRING, pattern = APIDateFormats.DATE_FORMAT)`, comme
`InscriptionLightDto`. Mapper MapStruct simple (`@Mapper(componentModel = "spring")`).

### La situation est aussi embarquée dans l'inscription

`InscriptionEnfantDto` et `InscriptionAdulteDto` gagnent un champ
`SituationPaiementDto situationPaiement`, peuplé par `InscriptionEnfantServiceImpl.findInscriptionById`
et `InscriptionAdulteServiceImpl.findInscriptionById`.

Raison : quand l'administrateur ouvre une inscription, celle-ci est déjà chargée — un appel
supplémentaire à `/v1/paiements` serait du gaspillage. Le bloc de paiements de l'étape
« Tarif » se sert donc directement de ce qu'il a déjà.

Cela ne rend pas `GET /v1/paiements` redondant : **la modale ouverte depuis la liste
d'administration ne dispose que de `idInscription` et du type**. Sans endpoint dédié, il
faudrait appeler `/v1/inscriptions-enfants/{id}` ou `/v1/inscriptions-adultes/{id}` selon le
type — donc charger le responsable légal, les élèves et les matières pour lire trois lignes
de paiement — et faire brancher `PaiementsInscription` sur la forme de l'inscription, ce qui
lui ferait perdre son autonomie. Les deux usages sont complémentaires.

Trois points de vigilance sur cet ajout :

- **Aucun risque d'exposition aux familles** : `GET /v1/inscriptions-enfants/{id}` et
  `/v1/inscriptions-adultes/{id}` ne figurent dans aucun `requestMatcher` de `SecurityConfig`
  et relèvent donc de `anyRequest().hasRole("ADMIN")`. L'espace personnel passe par
  `GET /v1/inscriptions/mes-inscriptions`, qui renvoie un DTO distinct.
- **Ces DTO servent aussi de corps de requête** en `POST`/`PUT`. `situationPaiement` est
  strictement en lecture et doit être ignoré à l'entrée par le mapper — c'est déjà le régime
  de `montantTotal` et `anneeScolaire`.
- **`InscriptionAdulteDto implements IMailObject`** : le champ restera `null` dans le flux
  mail, ce DTO y étant construit à la création et non via `findInscriptionById`. Ne pas l'y
  alimenter — les montants réglés n'ont rien à faire dans le modèle de rendu des mails.

Les deux `findInscriptionById` renvoyant un DTO ne sont appelés que par leur contrôleur
respectif (vérifié) : aucun effet de bord sur la génération de documents ou de mails.

### Sécurité

**Aucune ligne à ajouter dans `SecurityConfig`** : la règle terminale
`anyRequest().hasRole("ADMIN")` couvre déjà toute route non listée explicitement. C'est le
comportement voulu, mais il est implicite — un test d'intégration vérifiant le 403 pour un
rôle non-ADMIN le fige et documente l'intention.

### Gestion des erreurs

Les handlers existants de `CustomExceptionHandler` renvoient tous un **corps vide**
(`ResponseEntity.status(...).build()`), ce qui empêcherait le front d'afficher un message
utile — or « le montant dépasse le reste à payer » doit être lisible par l'utilisateur.

On ajoute donc un handler pour `PaiementValidationException` renvoyant **400 avec un corps**
`{ "code": "...", "message": "..." }`. C'est le seul ajout hors du domaine `paiement`.

### Migrations Liquibase

Dans `src/main/resources/db/changelog/2026/` (déjà couvert par l'`includeAll` du master) :

- `066-createTable-paiement.yml` — sur le modèle de `057-createTable-document-request.yml`.
- `067-updateView-inscription-light-paiement.yml` — sur le modèle de
  `062-addColumn-reinscription-inscription-and-updateView.yml` (`DROP VIEW IF EXISTS ...
  CASCADE` puis recréation). Deux colonnes ajoutées à `moth.v_inscription_light` :

```sql
i.mtinsctotal AS montanttotal,
COALESCE((SELECT SUM(p.mtpaie) FROM moth.paiement p
          WHERE p.cdpaietypecible = 'INSCRIPTION'
            AND p.idpaiecible = i.idinsc
            AND p.cdpaiestatut = 'VALIDE'), 0) AS montantregle
```

La sous-requête corrélée est préférée à un `LEFT JOIN ... GROUP BY` : la vue produit une
ligne **par élève**, un `GROUP BY` obligerait à agréger toutes les autres colonnes.

> **Piège** : Liquibase est désactivé en test (`spring.liquibase.enabled: false`). La vue est
> recréée à la main pour H2 dans `src/test/resources/after-init.sql` et **doit être mise à
> jour dans le même commit** — sinon tous les `@SpringBootTest` échouent au démarrage du
> contexte.

`InscriptionLightEntity` et `InscriptionLightDto` gagnent `montantTotal` et `montantRegle`.
MapStruct les mappe automatiquement (même nom de propriété des deux côtés) : ni les mappers
ni `InscriptionLightServiceImpl` ne changent.

Le `statutReglement` n'est **pas** ajouté à la vue — il se dérive côté front à partir des
deux montants, avec exactement la même logique que côté serveur.

## Frontend

### Service et types

- `src/services/services.ts` : `PAIEMENTS_ENDPOINT`, `PAIEMENT_ENDPOINT`,
  `PAIEMENT_ANNULATION_ENDPOINT` (avec le helper `buildUrlWithParams` déjà présent).
- `src/services/paiement.ts` (nouveau) : types et schémas **zod**, suivant la convention
  récente de `src/services/mesInscriptions.ts`. Pattern générique `Dayjs` (front) vs `string`
  au format `DD.MM.YYYY` (back), comme `InscriptionEnfant<T, U>`.
- `src/services/inscription.ts` : `InscriptionLight` gagne `montantTotal` et `montantRegle` ;
  `InscriptionEnfant<T, U>` et `InscriptionAdulte<T>` gagnent `situationPaiement?`.

Tous les appels passent par `useApi().execute` — pas de react-query, pas d'en-tête d'auth
(session par cookie, `withCredentials`).

### Composants — `src/components/paiements/`

- **`paiementUtils.ts`** — `getStatutReglement(montantTotal, montantRegle)` et
  `formatMontant(n)`. Il n'existe aujourd'hui **aucun utilitaire de formatage monétaire**
  dans le repo (chaque écran fait son `${value} €` ou son `.toFixed(2)`) ; ce helper devient
  la référence.
- **`PaiementBadge.tsx`** — le `<Tag>` `Soldé` / `Partiel` / `Non réglé` / `Trop-perçu`,
  calqué sur le couple `STATUT_CONFIG` + `StatutBadge` de `InscriptionDesktopView.tsx`.
- **`PaiementsInscription.tsx`** — **le composant central, réutilisé partout** : bandeau de
  situation (total / réglé / reste), tableau de l'historique (paiements annulés grisés, avec
  actions Modifier et Annuler), formulaire d'ajout.
  Props : `{ idInscription: number; situationInitiale?: SituationPaiementDto; onChange?: (s: SituationPaiementDto) => void; }`.
  Il n'appelle `GET /v1/paiements` **que si `situationInitiale` est absente** — le formulaire
  d'inscription la lui passe depuis l'inscription déjà chargée, la modale de la liste le
  laisse charger lui-même. Ensuite, chaque mutation renvoyant la situation complète, il n'y a
  plus aucun rechargement.
- **`ModalPaiements.tsx`** — enveloppe modale autour du composant précédent, props
  `{ open, setOpen, idInscription }` selon la convention universelle des modales du repo
  (pas d'`onClose`, footer custom) — modèle : `ModalPeriode.tsx`.

Le formulaire réutilise les wrappers existants (`InputNumberFormItem` avec `addonAfter="€"`,
`DatePickerFormItem`, `SelectFormItem`, `InputFormItem`) et l'idiome e2e du repo
`{...({ testid: "..." } as any)}`. La validation du montant passe par un validateur
`validateMontantPaiement` posé dans `src/utils/FormUtils.ts`, à côté de
`validateMontantMinAdhesion`.

### Intégration dans les écrans

| Écran | Modification |
|---|---|
| `routes/admin/cours/InscriptionDesktopView.tsx` | Colonne « Paiement » avant `Actions` (`PaiementBadge` + `filters`/`onFilter` client) et bouton `<EuroCircleOutlined />` dans `RowActions` ouvrant `ModalPaiements` |
| `routes/admin/cours/InscriptionMobileView.tsx` | Une `adhesion-card-row` supplémentaire et l'entrée dans les actions de carte |
| `routes/admin/AdminCoursArabes.tsx` | `montantTotal` et `montantRegle` dans `excelColumnHeaders` |
| `components/inscriptions/Tarif.tsx` | `<PaiementsInscription>` dans le bloc `{isAdmin && ...}` existant, **seulement si l'inscription a déjà un id** |
| `routes/public/CoursArabesAdulteForm.tsx` | Même insertion dans son bloc `{isAdmin && ...}` (ce formulaire n'utilise pas `Tarif.tsx`, le bloc y est écrit en dur) |

> **Identité des lignes** : la table d'administration produit une ligne **par élève**
> (`rowKey = record.id`) alors que le paiement porte sur `record.idInscription`. Une
> inscription à trois enfants affiche donc trois fois le même badge — c'est le comportement
> attendu, mais toute action doit se faire sur `idInscription`, comme le fait déjà
> `getSelectedInscriptionDistinctIds()`.

> **Quirk existant à ne pas amplifier** : `useInscriptionManagement` recharge la liste par
> `await loadInscriptions()` **sans paramètres** après chaque mutation, ce qui perd les
> critères de recherche courants. Après enregistrement d'un paiement on rafraîchit la
> situation localement (`onChange`) plutôt que la liste entière.

## Testing

### Backend

- `paiement/service/impl/TestPaiementServiceImpl` — unitaire Mockito, sur le modèle de
  `TestInscriptionEnfantServiceImpl` (`@ExtendWith(MockitoExtension.class)`, `Clock` figé,
  commentaires GIVEN / WHEN / THEN en français). Couvre chaque code de validation, le
  paiement partiel puis soldant, l'exclusion du paiement courant lors d'une modification, le
  refus de modifier un paiement annulé, `montantTotal == 0`, et `TROP_PERCU`.
- `paiement/v1/controller/TestPaiementController` — `extends TestController`, MockMvc : 403
  avec `@WithMockUser(roles = "ENSEIGNANT")`, 200 avec `roles = "ADMIN"`, et un cycle complet
  création → modification → annulation → situation. Jeu de données monté comme dans
  `TestAdhesionController`.
- **Non-régression** : l'ensemble des `@SpringBootTest` existants doit rester vert. Un
  `after-init.sql` désynchronisé de la vue les fait tous échouer d'un coup — c'est le
  principal risque de ce lot.

### Frontend

Jest + React Testing Library via `react-scripts test`.

- `paiementUtils.test.ts` — dérivation du statut, dont `montantTotal === 0` et le trop-perçu.
- `PaiementBadge.test.tsx` — les quatre rendus.
- `PaiementsInscription.test.tsx` — chargement, saisie, blocage du sur-paiement, annulation.

> Aucun test n'existe aujourd'hui sur les écrans d'administration et **rien ne mocke
> `useApi`**. Ce lot pose ce pattern (`jest.mock('../../hooks/useApi')` renvoyant un `execute`
> factice), sur le style de `src/routes/admin/HomeAdmin.test.tsx`.

### Recette manuelle

Back et front lancés, connecté en ADMIN :

1. Ouvrir « Inscriptions cours enfants » : la colonne **Paiement** affiche « Non réglé »
   partout.
2. Sur une inscription à 200 €, saisir 120 € en espèces → `120 € réglés / 80 € restants`,
   badge **Partiel**.
3. Retenter 150 € → refusé avec un message explicite ; vérifier que le serveur refuse aussi
   (appel direct à `POST /v1/paiements`, 400 attendu).
4. Saisir 80 € par chèque avec un numéro → badge **Soldé**, reste à 0 €.
5. Modifier ce chèque à 50 € → badge **Partiel**, reste 30 €.
6. Annuler le paiement en espèces → il reste visible et grisé, le reste à payer remonte, et
   il n'est plus ni modifiable ni annulable.
7. Vérifier le rendu **mobile** (< 768 px) et l'**export Excel**.
8. Ouvrir l'inscription en consultation (`/coursEnfants/:id`) : le bloc paiements apparaît à
   l'étape Tarif. Refaire le test sur une inscription **adulte**.
9. Se reconnecter avec un rôle non-ADMIN : `GET /v1/paiements` renvoie **403** et aucun bloc
   paiement n'est visible.
