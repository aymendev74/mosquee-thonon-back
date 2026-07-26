# Conventions de rangement du code

## La règle

**Au premier niveau sous `org.mosqueethonon`, il n'y a que des domaines métier,
`common`, et `Application.java`.** Rien d'autre.

```
org.mosqueethonon
├── Application.java
├── common/                     transverse — ne dépend d'aucun domaine
└── adhesion/ bulletin/ chatbot/ classe/ document/ inscription/
    lock/ mail/ param/ referentiel/ tarif/ utilisateur/
```

Le découpage est **par domaine d'abord, par couche technique ensuite** — et non
l'inverse. Une évolution « inscription » doit se lire dans un seul dossier.

## Gabarit d'un domaine

Tous les sous-packages sont optionnels ; on ne crée que ceux dont on a besoin.

```
<domaine>/
├── annotation/     annotations propres au domaine
├── criteria/       critères de recherche service-level
├── entity/         entités JPA (+ converter/ si besoin)
├── enums/
├── exception/
├── repository/     (+ specification/ pour les Specifications JPA)
├── scheduled/      jobs @Scheduled
├── service/        INTERFACES uniquement, + les value objects du domaine
│   └── impl/       implémentations @Service
├── util/
└── v1/             la surface REST, versionnée
    ├── controller/
    ├── dto/
    └── mapper/     mappers MapStruct
```

### Points d'attention

- **`service/` ne contient que des interfaces**, `service/impl/` que les
  implémentations. Les value objects du domaine (`ChatbotTurn`, `GroupeEleves`,
  `Incoherences`) vivent à côté des interfaces, dans `service/`.
- **Le versionnement d'API est sous le domaine**, pas au-dessus :
  `inscription/v1/dto`, et non `v1/dto/inscription`. Une v2 s'écrira
  `inscription/v2/`, par ressource.
- **Exception unique à la règle « DTO sous v1 »** : `mail/dto` contient
  `MailDto`, `MailAttachmentDto` et `IMailObject`, qui sont des contrats
  service-level jamais exposés en REST.

## À quel domaine appartient un enum ?

**Au domaine dont l'entité le persiste.** Ce critère est mécanique et tranche
les cas apparemment partagés :

- `NiveauInterneEnum` est persisté par `NiveauEntity` → `referentiel`
- `NiveauScolaireEnum` est persisté par `EleveEntity` → `inscription`
- `ResultatEnum` est porté par `EleveEntity` → `inscription`, malgré sa
  parenté sémantique avec `bulletin`

Consommer l'enum d'un autre domaine est normal et n'appelle aucun déplacement.
**Il n'y a délibérément pas de `common/enums`** : ce serait la poubelle par
défaut, et cela recréerait le découpage par couche qu'on a supprimé.

## Ce qui va dans `common`

Uniquement ce qui est vraiment transverse, et qui **ne dépend d'aucun domaine** :

| Package | Contenu |
|---|---|
| `common/config` | configuration Spring générale (formats de date, Hibernate) |
| `common/web` | filtres de log HTTP, `CustomExceptionHandler` |
| `common/security` | plomberie Spring Security et OAuth (+ `context/`) |
| `common/audit` | `Auditable`, `EntityListener`, `Signature` |
| `common/exception` | exceptions génériques, `ErrorConstantes` |
| `common/util` | utilitaires sans état (`DateUtils`, `StringUtils`, `HashUtils`) |

`SecurityContext` est dans `common/security/context` et non dans `utilisateur` :
il est appelé par 6 domaines et par `common/audit`. Le descendre dans
`utilisateur` ferait dépendre tous les domaines de `utilisateur`.

Symétriquement, une configuration qui ne sert qu'à un domaine appartient au
domaine : `LockWebMvcConfig` est dans `lock/config`, pas dans `common`.

## Suffixes de classe

`*Entity`, `*Repository`, `*Service` / `*ServiceImpl`, `*Dto`, `*Mapper`,
`*Enum`, `*Criteria`, `*Properties`, `*Config`, `*Exception`.

Pas de préfixe `I` sur les interfaces.

## Tests

`src/test/java` est un **miroir exact** de `src/main/java`, et les classes de
test sont **préfixées** `Test` (`TestInscriptionServiceImpl`) — pas suffixées.
Le `pom.xml` ne configure pas surefire, on dépend donc de ses includes par
défaut, qui reconnaissent `Test*`.

## Références en dur au nom des packages

Deux fichiers hors Java désignent une classe par son **nom pleinement qualifié,
dans une chaîne**. Aucun compilateur ne les vérifie : les déplacer sans les
mettre à jour casse l'application au démarrage, silencieusement.

| Fichier | Classe référencée | Symptôme si désynchronisé |
|---|---|---|
| `src/test/resources/schema.sql` | `common.util.FunctionSQLMocks` | tous les `@SpringBootTest` échouent au démarrage du contexte H2 |
| `src/main/resources/META-INF/spring.factories` | `common.config.RequiredEnvironmentVariablesValidator` | la validation des variables d'environnement ne s'exécute plus, l'application démarre avec une config incomplète |

Déplacer l'une de ces deux classes et modifier le fichier correspondant doivent
se faire **dans le même commit**.

Le reste est sûr : `application.yml`, Liquibase, `logback-spring.xml`, le
`Dockerfile` et la CI ne contiennent aucun nom de package Java, et MapStruct
n'utilise que des littéraux `X.class`. Seul `pom.xml` cite
`org.mosqueethonon.Application` dans `<start-class>`, qui ne doit pas bouger de
la racine.

## Dette connue

- `inscription/service/impl/CommonInscriptionService` est un `@Service` concret
  sans interface, dans `impl/`. À renommer `InscriptionCommonServiceImpl` avec
  extraction d'une interface.
- Aucun test ArchUnit ne fige ces règles pour l'instant : ajouter des assertions
  du type « `**.service` ne contient que des interfaces », « `common` ne dépend
  d'aucun domaine » éviterait que la structure re-dérive.
