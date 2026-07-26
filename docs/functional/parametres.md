# Paramètres de l'application

Documentation fonctionnelle des paramètres généraux de l'application, réglables dans l'écran **Paramètres**. Ces paramètres pilotent l'ouverture des inscriptions et l'envoi des e-mails. Public visé : administrateurs.

## Vue d'ensemble

L'application possède un petit nombre de paramètres globaux. Chaque paramètre a un **type** (booléen oui/non, ou date) et contrôle un comportement précis du système. Les dates utilisent le format **jour.mois.année** (ex. `01.09.2026`).

Il existe actuellement **quatre paramètres** :

| Paramètre | Type | Rôle en une phrase |
|-----------|------|--------------------|
| Réinscription prioritaire | Oui/Non | Active la fenêtre de réinscription prioritaire des anciens élèves. |
| Date d'ouverture des inscriptions enfants | Date | Date à partir de laquelle les inscriptions enfants sont ouvertes. |
| Date d'ouverture des inscriptions adultes | Date | Date à partir de laquelle les inscriptions adultes sont ouvertes. |
| Envoi des e-mails | Oui/Non | Interrupteur global : autorise ou bloque tout envoi d'e-mail. |

## Réinscription prioritaire

Paramètre **oui/non** (nom technique `REINSCRIPTION_ENABLED`) qui active la période de **réinscription prioritaire** : les familles déjà inscrites l'année précédente réinscrivent leurs enfants avant l'ouverture des inscriptions au public.

Effets concrets quand ce paramètre est **activé** :

- Les **nouvelles inscriptions enfants classiques sont bloquées** (message « Les inscriptions sont actuellement fermées »). Seul le parcours de réinscription est autorisé pendant cette fenêtre.
- Le parcours de **réinscription** enfant devient accessible (il exige que ce paramètre soit activé **et** que les inscriptions enfants soient ouvertes).

## Date d'ouverture des inscriptions enfants

Paramètre **date** (`A partir du`) : les inscriptions enfants sont **ouvertes dès que cette date est atteinte**.

- Les inscriptions sont ouvertes si la date est **renseignée et inférieure ou égale à aujourd'hui** (le jour exact de la date est inclus).
- Si la date est **vide** ou **dans le futur**, les inscriptions enfants sont **fermées**.

Astuce : on peut **programmer une date future**. L'ouverture devient alors automatique le jour venu, à minuit, sans nouvelle intervention de l'administrateur.

## Date d'ouverture des inscriptions adultes

Paramètre **date** (`A partir du`), symétrique du paramètre enfant : les inscriptions adultes sont **ouvertes dès que cette date est atteinte**.

- Si la date est vide ou dans le futur, la création d'une inscription adulte est refusée (« Les inscriptions sont actuellement fermées »), de même que la réinscription adulte.
-  Si la date est vide ou dans le futur, les inscriptions adultes sont fermées.

## Envoi des e-mails

Paramètre **oui/non** (nom technique `Activer l'envoi des e-mails`) qui sert d'**interrupteur global** d'envoi des e-mails. Pratique pour l'environnement de test (pré-production) où l'on veut éviter les envois de mails par accident.

Quand ce paramètre est **désactivé** :

- Aucun e-mail d'inscription ou d'adhésion n'est envoyé (les demandes sont marquées comme ignorées).
- Aucun e-mail de **réinitialisation de mot de passe** ni d'**activation de compte** n'est envoyé.

## Modification des paramètres

La **Modification** des paramètres de l'application est réservée exclusivement aux **administrateurs** (rôle ADMIN). 
Les autres rôles ne peuvent pas modifier ces paramètres.
