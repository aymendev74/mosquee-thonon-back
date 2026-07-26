# Inscription aux cours enfants

Documentation fonctionnelle du parcours d'inscription des enfants aux cours d'arabe. Décrit les règles telles qu'appliquées par l'application (ouverture des inscriptions, statuts, liste d'attente, réinscription prioritaire, tarifs, e-mails). Public visé : administrateurs et enseignants.

## Ouverture et fermeture des inscriptions

Les inscriptions enfants ne sont pas ouvertes en permanence. Leur ouverture est pilotée par un paramètre de l'application : une **date d'ouverture** (paramètre `A partir du`).

- Les inscriptions sont **ouvertes** et le formulaire publique est accessible dès que cette date est atteinte (date renseignée et antérieure ou égale à aujourd'hui).
- Si la date n'est pas renseignée, ou si elle est dans le futur, les inscriptions sont **fermées**, et le formulaire publique n'est pas accessible (un message apparaît à la place indiquant que les inscriptions sont fermées)
- Cas particulier : pendant la **fenêtre de réinscription prioritaire** (voir plus bas), les nouvelles inscriptions ne sont pas autorisés et le formulaire est également inaccessible — seul le parcours de réinscription est autorisé.

Ces réglages se gèrent dans l'écran **Paramètres** de l'application.

## Statuts d'une inscription

Une inscription enfant peut se trouver dans l'un des quatre statuts suivants :

- **Provisoire** : inscription normale acceptée, en attente de validation par un administrateur. C'est le statut par défaut d'une nouvelle inscription quand il reste de la place.
- **Validée** : inscription confirmée par un administrateur. C'est aussi le résultat d'un parcours de réinscription prioritaire réussie.
- **Liste d'attente** : la capacité de la période est atteinte ; l'inscription reçoit un **numéro de position** dans la file d'attente.
- **Refusée** : Action uniquement effectuée manuellement par un administrateur de l'application

## Déroulé d'une nouvelle inscription

Lorsqu'un parent (ou un administrateur) enregistre une inscription, l'application effectue les étapes suivantes :

1. **Vérification de l'ouverture** des inscriptions (voir plus haut).
2. **Gestion du compte utilisateur** à partir de l'e-mail du responsable légal :
   - si un compte existe déjà avec cet e-mail, il est réutilisé ;
   - sinon un compte est créé (identifiant = e-mail), avec le rôle utilisateur, non activé dans un premier temps.
3. **Calcul du tarif** (voir la section Tarifs) et détermination de la disponibilité de place.
4. **Détermination du statut** (provisoire, liste d'attente ou validée selon le contexte).
5. **Attribution d'un numéro d'inscription** au format `AMC-<numéro>` et enregistrement de la date d'inscription.
6. **Génération d'un document PDF** de l'inscription (pour les statuts provisoire et validée) et **création d'un e-mail** à destination du responsable légal.

Il n'y a **pas de nombre minimum ou maximum d'enfants** imposé par inscription : le nombre d'enfants sert uniquement au calcul du tarif et de la capacité.

### Comment le statut est décidé

- **En période normale** (hors réinscription prioritaire) :
  - s'il reste de la place → statut **Provisoire** ;
  - si la capacité est atteinte → statut **Liste d'attente** avec un numéro de position.
- **Pendant la fenêtre de réinscription prioritaire** : l'inscription est automatiquement **Validée**.

## Capacité et liste d'attente

Chaque année scolaire correspond à une **période** de cours enfants qui définit une **capacité maximale** d'élèves.

- Le nombre d'élèves déjà inscrits pris en compte pour la capacité correspond aux inscriptions en statut **Validée ou Provisoire**.
- Si le nombre d'enfants d'une nouvelle inscription (en cours d'enregistrement dans le système) ferait dépasser la capacité, l'inscription entière passe en **liste d'attente**.
- Le **numéro de position** en liste d'attente est attribué dans l'ordre d'arrivée (le premier en attente reçoit la position 1, puis 2, etc.).

### Promotion depuis la liste d'attente

Quand des places se libèrent (par exemple après la suppression d'une inscription), la liste d'attente est réévaluée automatiquement :

- les inscriptions en attente sont promues **dans l'ordre de leur numéro de position (ordre d'enregistrement, premier arrivé premier servi)** ;
- une inscription n'est promue que si **tous ses enfants** tiennent dans les places restantes (une inscription est traitée globalement, jamais scindée) ;
- la promotion s'arrête dès qu'il n'y a plus de place disponible.

À noter : lors d'une validation manuelle par un administrateur, si le nombre réel d'élèves inscrits dépasse la capacité définie sur la période, la capacité de cette période est automatiquement relevée pour rester cohérente.

## Réinscription prioritaire

La réinscription prioritaire permet aux familles déjà inscrites l'année précédente de réinscrire leurs enfants en priorité, avant l'ouverture des inscriptions au public.

### Conditions d'ouverture

La fenêtre de réinscription prioritaire est active lorsque **deux conditions** sont réunies :

- les inscriptions enfants sont ouvertes (date d'ouverture atteinte) ;
- **et** le paramètre `Seulement les réinscriptions` (réinscription prioritaire) est activé.

Pendant cette fenêtre, les inscriptions « nouvelles » classiques sont bloquées : seul le parcours de réinscription, réservé aux utilisateurs connectés, est autorisé.

### Fonctionnement
- L'utilisateur (responsable légal) doit être **connecté** à son espace.
- Un bouton intitulé « **Me réinscrire pour l'année prochaine** » apparait 
- Une **nouvelle inscription** est créée pour l'année prochaine, reprenant les enfants sélectionnés avec le niveau scolaire déclaré par le parent.

### Progression de niveau interne

Lors d'une réinscription réussie, le niveau interne de l'enfant est recalculé à partir de l'année précédente :

- si l'enfant avait **acquis** son niveau → il passe au **niveau supérieur** ;
- si le niveau était **non acquis** → il **redouble** (reste au même niveau) ;
- si l'information de résultat est absente → le niveau n'est pas déterminé automatiquement.

## Tarifs

Le tarif d'une inscription enfant dépend de **deux critères** : le fait que le responsable légal soit **adhérent** ou non, et le **nombre d'enfants** inscrits.

Le montant total se calcule ainsi :

> **Montant total = tarif de base + (tarif par enfant × nombre d'enfants)**, arrondi à l'euro.

- Le **tarif de base** est un montant fixe par inscription (famille).
- Le **tarif par enfant** s'ajoute pour chaque enfant.
- Ces montants sont **définis par palier** : il existe des tarifs distincts selon le nombre d'enfants et selon le statut adhérent / non-adhérent. C'est ainsi qu'est appliquée la **dégressivité** (le tarif par enfant peut diminuer à mesure que le nombre d'enfants augmente). Ces montants se règlent dans l'écran **Tarifs**.
- Si aucun tarif n'est défini pour la combinaison demandée (période, adhérent, nombre d'enfants), aucun montant ne peut être calculé et l'inscription ne peut pas être enregistrée.

### Lien avec l'adhésion

Le fait d'être **adhérent** ne sert qu'à sélectionner la grille tarifaire adhérent ou non-adhérent : c'est une **information déclarative**. L'application **ne vérifie pas** qu'une adhésion a réellement été souscrite ou payée avant d'autoriser une inscription. L'adhésion est gérée dans un domaine séparé.

## E-mails envoyés

Un e-mail est adressé au responsable légal aux moments clés du parcours :

- **À la création** d'une inscription : un e-mail est toujours envoyé.
- **À la modification** : un e-mail de confirmation n'est envoyé que si le statut est Provisoire ou Validée **et** que l'envoi de confirmation a été demandé.
- **À la réinscription** : un e-mail est toujours envoyé.

Le **contenu de l'e-mail dépend du statut** : il existe des messages distincts pour une inscription provisoire (confirmation), validée, en liste d'attente, ou refusée.

Points importants :

- Si un document PDF doit être joint (statuts Provisoire et Validée), l'e-mail **attend que le PDF soit généré** avant d'être envoyé.
- Un **interrupteur global** d'envoi d'e-mails existe (paramètre `Activer l'envoi des e-mails`) : s'il est désactivé, aucun e-mail n'est réellement envoyé (les demandes sont ignorées).

## Paramètres liés à ce parcours

| Paramètre                      | Rôle |
|--------------------------------|------|
| `A partir du`                  | Date d'ouverture des inscriptions enfants. Conditionne l'ouverture et le calcul du tarif. |
| `Seulement les réinscriptions` | Active la fenêtre de réinscription prioritaire. |
| `Activer l'envoi des e-mails`           | Interrupteur global d'envoi des e-mails. |

L'**année scolaire** et la **capacité maximale** d'une période ne sont pas des paramètres généraux de l'application, ils sont redéfinis à chaque nouvelle année scolaire par les administrateurs. 
Les paramètres qui influent sur **les tarifs** sont le statut adhérent/non adhérent et le nombre d'enfants à inscrire.

## Restrictions

Pour pouvoir inscrire un enfant aux cours, il faut qu'il soit âgé d'au moins 6 ans au 1er octobre de l'année scolaire (à la rentrée)  