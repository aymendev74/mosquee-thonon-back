# Inscription aux cours adultes

Documentation fonctionnelle du parcours d'inscription des adultes aux cours d'arabe. Public visé : administrateurs et enseignants. Le parcours adulte partage plusieurs mécanismes avec le parcours enfant (compte utilisateur, numéro d'inscription, e-mails), mais s'en distingue sur des points importants décrits ici.

## Ouverture et fermeture des inscriptions

Les inscriptions adultes sont pilotées par une **date d'ouverture** dédiée (paramètre `A partir du`), indépendante de celle des enfants.

- Les inscriptions sont **ouvertes** dès que cette date est renseignée et atteinte (inférieure ou égale à aujourd'hui).
- Si la date est vide ou dans le futur, le formulaire publique de création d'une inscription adulte n'est **pas accessible** (Un message apparaît indiquant « Les inscriptions sont actuellement fermées »).

## Ce qu'une inscription adulte contient

Une inscription adulte représente **une seule personne** : l'adulte lui-même. Elle comporte :

- l'**identité et les coordonnées** de l'adulte (nom, prénom, date de naissance, sexe, e-mail, téléphone) ;
- son **statut professionnel** (étudiant, avec activité, sans activité) — qui détermine le tarif ;
- la **liste des matières** choisies ;
- éventuellement un **niveau interne** (débutant, intermédiaire, avancé).

Contrairement aux enfants (où une inscription peut regrouper plusieurs enfants d'une même famille), une inscription adulte correspond à un seul inscrit.

## Choix des matières

L'adulte sélectionne une ou plusieurs **matières** parmi l'offre de cours adultes (par exemple Tajwid, Tafsir du Coran, Nouraniya, Tafsir des noms d'Allah, Fiqh, Langue arabe).

- Le **nombre de matières est librerement choisi par le futur élève** : aucune limite maximale, mais une matière au minimum doit être sélectionné
- Les matières sélectionnées servent surtout à informer l'association des centres d'intérêts des futurs élèves. 

## Statut d'une inscription adulte

Le parcours adulte est **plus simple** que celui des enfants : il n'y a **ni capacité, ni liste d'attente, ni position d'attente**.

- Une **nouvelle inscription** est **toujours créée en statut Provisoire**, en attente de validation par un administrateur.
- Les autres changements de statut (validation, etc.) se font manuellement par un administrateur.

Il n'existe pas de statut « liste d'attente » pour les adultes.

## Déroulé d'une nouvelle inscription

1. **Saisie de l'inscription** par l'adulte intéressé.
2. **Gestion du compte utilisateur** à partir de l'e-mail renseginé :
    - si un compte existe déjà avec cet e-mail, l'inscription en cours y est rattachée ;
    - sinon un compte est créé (identifiant = e-mail), avec le rôle utilisateur, non activé dans un premier temps.
3. **Calcul du tarif** selon le statut professionnel (voir Tarifs).
4. Statut fixé à **Provisoire**, attribution d'un **numéro d'inscription** au format `AMC-<numéro>`.
5. **Génération du document PDF** et **création d'un e-mail** de confirmation à destination de l'élève inscrit.

## Tarifs

Le tarif adulte est un **forfait**, déterminé uniquement par le **statut professionnel** de l'inscrit :

- **Étudiant**, **avec activité**, ou **sans activité** : chacun correspond à un tarif forfaitaire distinct, défini dans l'écran **Tarifs**.

Points importants :

- Le prix **ne dépend pas du nombre de matières** choisies, ni du nombre d'inscrits.
- Il n'y a **ni notion d'adhérent, ni dégressivité** pour les adultes (contrairement aux enfants).

## E-mails envoyés

Le fonctionnement des e-mails est **mutualisé** avec le parcours enfant :

- **À la création** : un e-mail de confirmation est préparé (et envoyé avec le document PDF joint dès que ce dernier est généré).
- **À la modification par un administrateur** : un e-mail n'est envoyé que si le statut est Provisoire ou Validée **et** que l'envoi de confirmation a été demandé.
- **À la réinscription** : un e-mail est envoyé.

Comme pour les enfants, l'envoi effectif est soumis à l'**interrupteur global** d'envoi des e-mails (paramètre `Activer l'envoi des e-mails`).

## Paramètres liés à ce parcours

| Paramètre            | Rôle |
|----------------------|------|
| `A partir du`        | Date d'ouverture des inscriptions adultes. |
| `Activer l'envoi des e-mails` | Interrupteur global d'envoi des e-mails. |
