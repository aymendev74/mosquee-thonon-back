# Adhésions

Documentation fonctionnelle du parcours d'adhésion à l'association. Public visé : administrateurs et trésoriers. L'adhésion est un parcours **autonome**, distinct des inscriptions aux cours.

## Ce qu'est une adhésion

Une adhésion enregistre l'appartenance d'une personne à l'association. Elle contient :

- l'**identité** (civilité, nom, prénom, date de naissance) ;
- les **coordonnées** (e-mail, téléphone, adresse : numéro et rue, code postal, ville) ;
- un **montant** ;
- un **statut** (provisoire, validée) ;
- éventuellement un **numéro de membre** ;
- la **date d'inscription**.

## Création d'une adhésion

- La demande d'adhésion se fait via un **formulaire public** : toute personne peut soumettre une demande, sans être connectée.
- À la création, l'adhésion est enregistrée en statut **Provisoire**, un **document PDF** (formulaire d'adhésion) est généré, et un **e-mail d'accusé de réception** est préparé.
- **Aucun compte utilisateur n'est créé** lors d'une adhésion (contrairement aux inscriptions aux cours).
- Il n'y a **pas de contrôle de doublon** : une même personne peut faire plusieurs demandes ; c'est au trésorier de gérer.
- Il faut être agé d'au minimum 18 ans au moment de la date d'adhésion pour pouvoir adhérer à l'association.

## Montant

Le montant d'une adhésion repose sur deux notions :

- des **montants de référence** (tarif d'adhésion fixe, par exemple 15 €), défini dans l'écran des **Tarifs** pour l'année en cours.
- un **montant libre** que l'adhérent peut saisir s'il souhaite donner un montant différent (esprit don).

Un montant minimum de 15 € est imposé par l'application. Selon le contexte, c'est le montant fixe de référence ou le montant libre saisi qui est retenu.

Actuellement, les tarifs des adhésions ne sont pas redéfinis d'année en année contrairement aux tarifs des cours.
Une fois qu'une adhésion est saisie, l'adhérent n'est pas autonome pour la gérer, il doit forcément passer par un administrateur pour la modifier ou la supprimer.

## Cycle de vie et validation

- Une adhésion nouvellement créée est **Provisoire**.
- Le **trésorier** (ou un administrateur) la **valide** ensuite : le passage de Provisoire à **Validée** déclenche la régénération du document (formulaire) et l'envoi d'un e-mail de confirmation.
- Le **numéro de membre** n'est **pas attribué automatiquement** : c'est le trésorier qui le renseigne manuellement en modifiant l'adhésion.
- Les opérations de modification / suppression prennent un **verrou** sur l'adhésion pour éviter les modifications concurrentes ; la suppression nettoie aussi les documents et e-mails associés.

## Qui peut consulter et gérer les adhésions

- **Création** : publique (formulaire ouvert à tous).
- **Consultation, modification, validation, suppression** : réservées au rôle **Trésorier**. Un **administrateur** en hérite (l'administrateur domine le trésorier dans la hiérarchie des rôles). Les **enseignants** n'y ont pas accès.

## E-mails et documents

- **À la création** : un e-mail d'**accusé de réception** est envoyé (« Nous vous remercions pour votre demande d'adhésion, vous serez recontacté… »).
- **À la validation** : un e-mail de **confirmation** est envoyé, avec en pièce jointe le **RIB de l'association** (pour le règlement de la cotisation).
- Un **document PDF** (formulaire/attestation d'adhésion) est généré automatiquement et joint à l'e-mail dès qu'il est prêt.
- Comme partout, l'envoi effectif des e-mails dépend de l'**interrupteur global** (paramètre `Activer l'envoi des e-mails`) : s'il est désactivé, aucun e-mail n'est envoyé.

## Lien avec les inscriptions aux cours

L'adhésion et les inscriptions aux cours sont **deux domaines séparés**. 
Lors d'une inscription enfant, la case « adhérent » sert **uniquement à choisir la grille tarifaire appropriée** (les adhérents bénéficient d'un tarif différent), cependant c'est une **information déclarative**. 
L'application **ne vérifie pas** qu'une adhésion a réellement été souscrite ou payée. Il n'existe aucun contrôle automatique liant une inscription à une adhésion.

## Paramètres liés

Le seul paramètre qui influe sur les adhésions est l'**interrupteur global d'envoi des e-mails** (`Activer l'envoi des e-mails`). Il n'existe **aucun paramètre d'ouverture** propre aux adhésions.
