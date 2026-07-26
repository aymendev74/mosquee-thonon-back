# Documentation fonctionnelle

Ce dossier contient la **documentation fonctionnelle** de l'application de gestion de la mosquée (association AMC), rédigée par thème. Elle décrit les **règles métier** telles qu'appliquées par l'application : ouverture des inscriptions, statuts, tarifs, adhésions, paramètres, rôles, classes et bulletins.

Ces fichiers ont un double usage :

1. **Documentation de référence** pour les administrateurs et enseignants.
2. **Base de connaissance du chatbot** : chaque fichier est découpé par section, transformé en embeddings et interrogé par le chatbot d'assistance (RAG). La justesse de ces fichiers conditionne directement la qualité des réponses du chatbot.

## Comment maintenir cette documentation

- Quand une **règle métier change**, mettez à jour le fichier de thème concerné.
- Gardez chaque **section (titre `##` / `###`) autonome et cohérente** : c'est l'unité utilisée par le chatbot pour retrouver l'information.
- Après toute modification, **relancez l'indexation** du chatbot pour que les changements soient pris en compte.

## Thèmes

| Fichier | Contenu |
|---------|---------|
| [inscription-cours-enfant.md](inscription-cours-enfant.md) | Inscription aux cours enfants : ouverture, statuts, capacité et liste d'attente, réinscription prioritaire, tarifs, e-mails. |
| [inscription-cours-adulte.md](inscription-cours-adulte.md) | Inscription aux cours adultes : matières, statut professionnel, tarif forfaitaire, réinscription. |
| [adhesions.md](adhesions.md) | Adhésions : formulaire public, montant fixe/libre, validation par le trésorier, documents et e-mails. |
| [tarifs.md](tarifs.md) | Tarifs et périodes : modèle de tarification, années scolaires, administration des grilles. |
| [parametres.md](parametres.md) | Paramètres de l'application : ouverture des inscriptions, envoi des e-mails, règles de saisie. |
| [classes-et-bulletins.md](classes-et-bulletins.md) | Domaine enseignant : classes, feuilles de présence, bulletins, progression de niveau. |
| [roles-et-acces.md](roles-et-acces.md) | Rôles (administrateur, trésorier, enseignant, utilisateur), permissions, gestion des comptes. |
