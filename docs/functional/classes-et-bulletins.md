# Classes, présences et bulletins

Documentation fonctionnelle du domaine enseignant : constitution des classes, feuilles de présence et bulletins. Public visé : enseignants et administrateurs.

## Classes

### Ce qu'est une classe

Une classe regroupe des élèves d'un même **niveau** pour une **année scolaire** donnée. Elle est caractérisée par :

- un **niveau** interne (par exemple P1, P2, N1_1… pour les enfants) ;
- l'**année scolaire** (année de début et de fin) ;
- un ou plusieurs **enseignants** ;
- une liste d'**élèves**;
- un **jour d'activité** (par exemple samedi matin, dimanche matin, dimanche après-midi, mercredi après-midi) ; en pratique une classe a lieu une fois par semaine ;
- éventuellement une classe a un **libellé** saisi par l'administrateur lors de sa création et permet de l'identifier plus facilement dans le système.

>> **Attention**: Un utilisateur de l'application n'apparaitra dans la liste des enseignants sélectionnables que s'il a bien un rôle **enseignant** qui lui est affecté.  

### Règles d'affectation des élèves

- **Un élève n'appartient qu'à une seule classe pour une année donnée** : l'affecter à une classe le retire automatiquement de toute autre classe de la même année.
- Un élève **sans niveau interne** hérite du **niveau de la classe** à laquelle on l'affecte.
- Quand on modifie l'effectif d'une classe, les **feuilles de présence existantes sont réconciliées** : les élèves retirés de la classe sont supprimés des feuilles, et les nouveaux élèves y sont ajoutés en **« absent »** par défaut.

### Qui peut faire quoi

- **Créer, modifier, supprimer** une classe (y compris la génération automatique) : **administrateur** uniquement.
- **Gérer une classe** : les enseignants (et administrateurs). Un enseignant ne peut gérer cependant **que les classes qui lui ont été affectées** ; un administrateur peut gérer toutes les classes.

## Feuilles de présence

### Fonctionnement

Une **feuille de présence** correspond à **une séance à une date donnée**, rattachée à une classe. Pour chaque élève, la présence est un simple **présent / absent** (il n'y a aucun autre état intermédiaire comme « retard » ou « excusé »).

- On crée une feuille pour une classe à une date donnée, avec l'état de présence de chaque élève.
- Les feuilles d'une classe sont consultables et modifiables.

### Qui peut faire quoi

Les enseignants peuvent effectuer **toutes les actions (création, modification, suppression)** sur les feuilles de présences des classes qui leur sont affectées. 
Les administrateurs peuvent effectuer **toutes les actions** sur les feuilles de présences de **toutes les classes**.

## Bulletins

### Ce qu'est un bulletin

Un **bulletin** concerne **un élève, pour un mois et une année** (bulletin mensuel). Il contient :

- une **appréciation** générale ;
- un **nombre d'absences** ;
- une **note par matière** ;
- une **remarque** libre par matière.

Les **notes** possibles par matière sont : **A** (Acquis), **EA** (En cours d'acquisition), **NA** (Non acquis).

Les matières évaluées côté enfants sont (liste non exhaustive) : expression orale, dictée, lecture, écriture, Coran, éducation islamique, assiduité/comportement.

### Complétude et génération du PDF

Lors de l'édition d'un bulletin, celui-ci est est considéré **complet** lorsque :

- l'appréciation est renseignée ;
- le nombre d'absences, le mois, l'année et la date sont renseignés ;
- **une note est saisie pour chacune des matières requises**.

Dès qu'un bulletin devient complet, lors de la sauvegarde, l'utilisateur est averti et le **document PDF du bulletin est généré automatiquement** (de façon asynchrone) et devient téléchargeable. 
Un bulletin **n'est pas envoyé par e-mail** : il est seulement généré puis mis à disposition des enseignants, administrateurs et également du parent responsable dans son espace personnel.

### Qui peut faire quoi

- **La création des bulletins** : les **enseignants** (et administrateurs).
- **La modification et suppression d'un bulletin pas enore complet** : les **enseignants** (et administrateurs).
- **La modification et suppression des bulletins déjà complets et dont le document PDF a été généré** : uniquement les **administrateurs**.

## Résultat annuel et progression de niveau

À ne pas confondre avec les notes du bulletin : chaque élève porte un **résultat annuel** distinct, **Acquis** ou **Non acquis**, saisi séparément (ce n'est pas calculé automatiquement à partir des bulletins).

C'est ce **résultat annuel** qui pilote la **progression de niveau lors de la réinscription** de l'année suivante :

- **Acquis** → l'élève passe au **niveau supérieur** ;
- **Non acquis** → l'élève **reste au même niveau** (redouble) ;
- résultat ou niveau manquant → la progression n'est pas déterminée automatiquement.

Tant que tous les résultats annuels de tous les élèves ne sont pas renseignés, le taux de réussite globale de la classe n'est pas calculé.