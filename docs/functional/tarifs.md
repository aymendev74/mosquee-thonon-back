# Tarifs et périodes

Documentation fonctionnelle transverse du système de tarifs et des périodes (années scolaires). Explique comment les tarifs sont structurés et administrés. Le détail de leur **utilisation** figure dans les fiches Inscription cours enfant, Inscription cours adulte et Adhésions. Public visé : administrateurs.

## Notion de période (année scolaire)

Toute la tarification et les inscriptions s'organisent autour de **périodes**. Une période représente une **année scolaire** pour un domaine donné, et porte :

- une **fenêtre de dates** (date de début / date de fin) pendant laquelle elle est active ;
- l'**année scolaire** (année de début / année de fin, ex. 2025–2026) ;
- une **capacité maximale d'inscriptions** (utilisée pour la liste d'attente des cours enfants) ;
- le **domaine concerné** : cours enfant, cours adulte, ou adhésion ;

Règles importantes :

- Il y a **un domaine par période** (une période est soit « cours enfant », soit « cours adulte », soit « adhésion »).
- Les périodes d'un même domaine **ne peuvent pas se chevaucher** dans le temps : à une date donnée, une seule période est active par domaine. Plusieurs périodes d'un même domaine coexistent seulement pour des **années différentes**.

## Notion de tarif

Un **tarif** est une **case de prix** : un montant en euros, rattaché à une période et à une combinaison de critères. Un tarif porte :

- le **montant** ;
- le **type** (voir ci-dessous) ;
- l'indicateur **adhérent** (oui/non) — utilisé uniquement pour les cours enfants ;
- le **nombre d'enfants** (palier de 1 à 4) — cours enfants uniquement ;

## Types de tarifs par domaine

Selon le domaine, les types de tarifs disponibles diffèrent :

### Cours enfants

Deux types combinés pour former le prix :

- **Base** : part fixe par inscription (famille).
- **Enfant** : part ajoutée par enfant.

Chacun est décliné selon le statut **adhérent / non-adhérent** et selon le **nombre d'enfants** (1 à 4). Cela forme une **grille complète** (Base et Enfant × adhérent oui/non × 1 à 4 enfants). C'est ce qui permet la **dégressivité** et le tarif préférentiel adhérent. Le prix final d'une inscription = part Base + (part Enfant × nombre d'enfants).

### Cours adultes

Un **tarif forfaitaire** par **statut professionnel** :

- **Étudiant**, **avec activité**, **sans activité**.

Pas de notion d'adhérent ni de dégressivité pour les adultes.

### Adhésions

- **Montant fixe** et **montant libre** (l'adhérent peut choisir un autre montant).

## Comment un tarif est sélectionné

Pour retrouver le bon tarif à un instant donné, l'application filtre sur :

- le **domaine** (via la période) ;
- la **date** : la période doit couvrir cette date (date de début ≤ date ≤ date de fin, bornes incluses) ;
- le **type** de tarif ;
- pour les cours enfants : l'indicateur **adhérent** et le **nombre d'enfants**.

C'est ce mécanisme qui garantit qu'une inscription se voit appliquer la grille tarifaire de la bonne année.

## Administration des périodes et des tarifs

La configuration se fait dans les écrans d'administration, **réservés aux administrateurs** (rôle ADMIN).

### Mise en place d'une nouvelle année

1. **Créer la période** de l'année (domaine, dates de début/fin, année, capacité maximale). Un contrôle empêche tout chevauchement avec une période existante du même domaine.
2. Ouvrir l'écran **Tarifs** pour cette période : la grille est vide la première fois.
3. **Saisir les montants** puis enregistrer. Au premier enregistrement, l'ensemble des cases de tarifs (toute la grille enfant, ou les tarifs adultes) est **créé automatiquement** à partir d'un modèle, et les montants saisis y sont inscrits.

### Modification et suppression

- Les tarifs se modifient en ré-enregistrant la grille de la période concernée.
- Dès lors qu'une inscription / adhésion a été enregistrée sur la période, il n'est plus autorisé de modifier les tarifs, car il n'existe pas de mécanisme d'historisation des tarif.
- La suppression d'une période **supprime aussi ses tarifs**.
- La validation d'une période effectue des contrôles (chevauchement, inscriptions hors période, capacité) avant son enregistrement.

## Qui peut faire quoi

**L'Administration des périodes et des tarifs** est réservée exclusivement aux **administrateurs** de l'application (rôle ADMIN).
