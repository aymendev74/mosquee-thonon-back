# Rôles et accès

Documentation fonctionnelle des rôles utilisateurs et de ce que chacun peut faire. Couvre aussi la création des comptes, l'activation et la réinitialisation de mot de passe. Public visé : administrateurs.

## Les quatre rôles

L'application définit quatre rôles :

- **Administrateur** (ADMIN) : accès total à l'application.
- **Trésorier** (TRESORIER) : gestion des adhésions.
- **Enseignant** (ENSEIGNANT) : gestion des classes, présences et bulletins.
- **Utilisateur** (UTILISATEUR) : rôle de base des parents/inscrits ; correspond à un compte créé automatiquement lors d'une inscription.

Le rôle **utilisateur** ne doit pas être affecté manuellement via l'écran de gestion des utilisateurs, ce rôle est affecté automatiquement lors d'une inscription.

### Hiérarchie

- L'**administrateur hérite** des droits du trésorier **et** de l'enseignant : il peut tout faire.
- **Utilisateur** ne donne accès à aucune fonction d'administration ; il permet seulement à la personne de consulter ses propres inscriptions et documents.

Un même compte peut cumuler plusieurs rôles (par exemple enseignant **et** trésorier).

## Qui peut faire quoi

| Domaine | Action                              | Accès |
|---------|-------------------------------------|-------|
| **Inscriptions cours** | Créer une inscription (enfant/adulte) | Public (formulaire ouvert à tous) |
| | Voir ses propres inscriptions       | Connecté |
| | Réinscription enfant                | Connecté |
| | Gérer/valider/modifier les inscriptions | Administrateur |
| **Adhésions** | Créer une adhésion                  | Public |
| | Consulter, valider, modifier, supprimer | Trésorier (et administrateur) |
| **Tarifs** | Créer/modifier les tarifs et périodes | Administrateur |
| **Paramètres** | Consulter/Modifier | Administrateur |
| **Classes** | Consulter ses classes               | Enseignant (et administrateur) |
| **Présences** | Saisir les présences d'une classe   | Enseignant (et administrateur) |
| | Modifier / supprimer des présences  | Enseignant (et administrateur) |
| **Bulletins & élèves** | Consulter et gérer                  | Enseignant (et administrateur) |
| | Consulter les bulletins de son enfant | Connecté (le parent) |
| **Utilisateurs** | Créer / modifier / supprimer des comptes | Administrateur |
| **Documents** | Consulter un document               | Le propriétaire, ou le rôle concerné par le type de document |

### Accès aux documents

Un document (PDF d'inscription, d'adhésion, bulletin) est accessible :

- **toujours** à la personne propriétaire du document ;
- et également selon le type : l'**attestation d'adhésion** est réservée au trésorier/administrateur ; les **documents d'inscription** et les **bulletins** sont réservés à l'enseignant/parent/administrateur.

## Création des comptes

Il y a actuellement deux façons de créer un compte utilisateur :

1. **Automatiquement, lors d'une inscription** : quand un parent s'inscrit (cours enfant ou adulte), un compte **Utilisateur** est créé à partir de son e-mail s'il n'en a pas déjà un. Le compte est d'abord **non activé** et un e-mail d'activation lui est envoyé.
2. **Manuellement, par un administrateur** : seul un **administrateur** peut créer des comptes avec des rôles particuliers (enseignant, trésorier…), les modifier, les supprimer, ou relancer un e-mail d'activation. Le ou les rôles sont attribués à la création du compte.

## Activation d'un compte

1. À la création, le compte est **non activé** ; un e-mail d'activation contenant un lien est envoyé.
2. Via ce lien, la personne accède à un écran qui affiche ses informations, puis **choisit son mot de passe** pour activer le compte.
3. Un administrateur peut **renvoyer l'e-mail d'activation** si nécessaire depuis l'écran de gestion des utilisateurs (uniquement si le compte n'a pas encore été activé).

## Mot de passe oublié

Le parcours de réinitialisation de mot de passe est ouvert à tous :

1. La personne demande une réinitialisation à partir de son identifiant (e-mail en général) sur l'écran de connexion de l'application (lien intitulé **mot de passe oublié**).
2. Si l'e-mail renseigné est bien lié à un compte existant, un lien de réinitialisation lui est envoyé.
3. Via le lien, la personne définit un nouveau mot de passe.

Un utilisateur **connecté** peut aussi changer son mot de passe directement via un menu dédié (l'ancien mot de passe est vérifié).

## Connexion

La connexion se fait par **identifiant (e-mail) et mot de passe**. L'application gère la session de manière sécurisée (jeton d'authentification déposé dans un cookie sécurisé, valable une heure et renouvelé de façon transparente). Un utilisateur non connecté qui tente d'accéder à une fonction protégée est invité à se connecter.

À noter : la suppression d'un compte **Utilisateur** supprime toutes les données qui lui sont liées (inscriptions, adhésions, bulletins...etc).
