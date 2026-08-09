# Paiements des inscriptions

Documentation fonctionnelle du suivi des règlements des inscriptions aux cours (enfants et adultes). Décrit ce qu'un administrateur peut saisir, les contrôles appliqués et la restitution dans les écrans d'administration. Public visé : administrateurs.

## À quoi sert le suivi des paiements

Au moment de la finalisation des inscriptions, les familles se présentent au bureau de l'association pour régler et valider leur inscription. Le suivi des paiements permet d'**enregistrer ces encaissements dans l'application** plutôt que sur un support externe, et de savoir à tout moment quelles inscriptions restent à encaisser.

Le suivi porte sur les inscriptions aux **cours enfants** et aux **cours adultes**. Les adhésions ne sont pas concernées à ce stade : leur montant est saisi sur l'adhésion elle-même, sans détail des règlements.

## Montant dû, montant réglé, reste à payer

Le **montant dû** d'une inscription est calculé automatiquement par l'application au moment de l'inscription, en fonction du paramétrage effectué par les administrateurs chaque année.  
Pour les inscriptions enfants, il dépend de plusieurs paramètres, par exemple si le responsable légal est adhérent ou non, ou encore du nombre d'enfants inscrits.Pour les inscriptions adultes, il dépend uniquement de son statut professionnel.

Le **montant réglé** est la somme des paiements enregistrés sur l'inscription et non annulés.

Le **reste à payer** est la différence entre les deux. Il n'est jamais stocké en base de données, il est simplement déduit : il est calculé à l'affichage.

## Enregistrer un paiement

Une inscription peut être réglée **en une ou plusieurs fois**. Chaque règlement est enregistré séparément, avec :

- le **montant** ;
- la **date du paiement** ;
- le **mode de paiement** ;
- une **référence** facultative (typiquement le numéro de chèque) ;
- un **commentaire** libre, facultatif.

L'enregistrement se fait depuis l'écran de gestion des inscriptions, ou depuis l'inscription elle-même lorsqu'un administrateur la consulte.

## Modes de paiement

Quatre modes sont proposés à la saisie :

- **Espèces**
- **Carte**
- **Chèque**
- **Virement**

Un cinquième mode, **paiement en ligne**, est réservé à un futur règlement par le site : il ne peut pas être choisi à la saisie manuelle.

La référence reste facultative quel que soit le mode. Elle est simplement mise en avant pour un chèque, où elle sert à noter le numéro.

## Contrôles à la saisie

L'application refuse un paiement dans les cas suivants :

- le **montant est nul ou négatif** ;
- le **montant dépasse le reste à payer** de l'inscription — il n'est donc pas possible d'encaisser plus que le montant dû ;
- la **date de paiement est absente ou postérieure à la date du jour**.

Lors de la modification d'un paiement existant, le contrôle sur le reste à payer tient compte du fait que ce paiement est déjà comptabilisé : modifier un règlement de 120 € en 150 € est accepté si le reste à payer le permet une fois les 120 € d'origine réintégrés.

## Modifier ou annuler un paiement

Un paiement enregistré peut être **modifié** tant qu'il est valide : tous ses champs sont éditables et les contrôles de saisie s'appliquent à nouveau.

Un paiement n'est **jamais supprimé**. En cas d'erreur, il est **annulé** : il reste visible dans l'historique de l'inscription, affiché comme annulé, et cesse d'être compté dans le montant réglé. Le reste à payer remonte donc automatiquement.

Un paiement annulé est **définitif** : il ne peut être ni modifié ni réactivé. Pour corriger, on enregistre un nouveau paiement.

Ce choix garantit la **traçabilité** : l'historique complet des opérations reste consultable, y compris les erreurs de saisie et leur correction.

## État de règlement d'une inscription

Chaque inscription se voit attribuer un état de règlement, calculé automatiquement :

| État | Signification |
|---|---|
| **Non réglé** | Aucun paiement enregistré |
| **Partiel** | Une partie du montant dû a été encaissée |
| **Soldé** | Le montant dû a été intégralement encaissé |
| **Trop-perçu** | Le montant encaissé dépasse le montant dû |

L'état **Trop-perçu** ne peut pas résulter d'une saisie, puisque le sur-paiement est refusé. Il ne peut apparaître que si le tarif d'une inscription est revu **à la baisse** après un encaissement. Il est affiché explicitement pour que la situation soit traitée, et non masquée.

Une inscription dont le montant dû est nul est considérée comme **soldée**.

## Restitution dans les écrans d'administration

Dans la liste des inscriptions (cours enfants comme cours adultes), une colonne **Paiement** affiche l'état de règlement de chaque inscription. Elle est filtrable, ce qui permet d'isoler d'un coup d'œil les inscriptions non soldées.

Attention : la liste affiche une **ligne par élève**. Une inscription regroupant plusieurs enfants d'une même famille apparaît donc sur plusieurs lignes, qui portent toutes le **même état de règlement** — le paiement concerne l'inscription dans son ensemble.

Le détail des règlements d'une inscription (situation et historique) est accessible depuis la liste, et depuis l'inscription elle-même.

## Qui peut faire quoi

La saisie, la modification, l'annulation et la consultation des paiements sont réservées exclusivement aux **administrateurs** de l'application (rôle ADMIN).

Les familles n'ont pour le moment **aucune visibilité** sur les paiements enregistrés depuis leur espace personnel.
