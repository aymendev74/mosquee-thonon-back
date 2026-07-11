# Migration du formulaire de login vers le frontend React

## Contexte

Le backend (`mosquee-thonon-back`) est son propre serveur d'autorisation OAuth2/OIDC
(`spring-security-oauth2-authorization-server`), avec un unique client public enregistré
(`moth-react-app`, PKCE, `ClientAuthenticationMethod.NONE`). Le frontend
(`mosquee-thonon-front-2`) implémente déjà entièrement le flux PKCE côté client
(génération `code_verifier`/`code_challenge`, échange du code, cookie httpOnly
`MOTH-TOKEN`) dans `src/hooks/AuthContext.tsx`.

Aujourd'hui, quand un utilisateur non authentifié atteint `/oauth2/authorize`, Spring
Security redirige vers `GET /login`, servi par `AuthentificationController` via un
template Thymeleaf (`templates/login.html`, Bootstrap CDN, sans branding). Cette page
ne correspond pas visuellement au reste de l'application React (thème antd) et donne une
impression peu professionnelle.

**Objectif** : afficher un formulaire de connexion entièrement React (composants antd,
thème `antdTheme.ts`), sans rechargement de page ni redirection intermédiaire moche,
tout en conservant le flux OAuth2/PKCE existant inchangé pour l'échange du code contre
le JWT.

**Confirmé avec l'utilisateur** :
- Aucun autre client n'utilise `/login` (Thymeleaf) que ce frontend React → on peut le
  supprimer sans impact ailleurs.
- Pas de rate limiting/anti-bruteforce dans le périmètre de cette évolution (même niveau
  de protection qu'aujourd'hui).
- En cas d'échec de connexion, un message générique unique ("Identifiants incorrects")
  est affiché, sans distinguer compte inexistant / mot de passe erroné / compte
  désactivé (évite l'énumération de comptes).

## Architecture cible

```
Utilisateur clique "Se connecter" (header) ou accède à /oauth2/authorize sans session
  │
  ▼
Frontend : navigate("/login")  (page React, plus de redirection immédiate vers l'IDP)
  │
  ▼
Formulaire antd (username/password) → submit
  │
  ▼
POST /login (JSON) — nouvel endpoint backend
  - authentifie via AuthenticationManager (bean déjà existant)
  - persiste le SecurityContext en session (cookie JSESSIONID)
  - échec → 401 générique / succès → 200
  │
  ▼ (succès)
Frontend : login()  (flux PKCE existant, inchangé)
  → redirection vers /oauth2/authorize
  → session déjà authentifiée → code renvoyé immédiatement
  │
  ▼
Callback existant (déjà implémenté, inchangé) échange le code contre le JWT,
pose le cookie httpOnly MOTH-TOKEN
```

Le flux d'échange de code (`POST /token`) et le cookie `MOTH-TOKEN` ne changent pas.
Seule l'étape de saisie des identifiants change de support (React au lieu de
Thymeleaf) et de mécanisme (endpoint JSON custom au lieu du `formLogin` par défaut de
Spring Security).

## Backend (`mosquee-thonon-back`)

### 1. Nouvel endpoint `POST /login` (JSON)

Ajouté dans `AuthentificationController` (converti de `@Controller` à
`@RestController`, puisqu'il ne rend plus aucune vue) :

```java
@PostMapping("/login")
public ResponseEntity<Void> login(@RequestBody LoginRequestDto request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
    securityContextRepository.saveContext(context, httpRequest, httpResponse);

    userService.saveLoginHistory(request.getUsername());

    return ResponseEntity.ok().build();
}
```

- `LoginRequestDto` : nouveau DTO simple `{ username, password }`.
- `AuthenticationException` (bad credentials, compte désactivé, etc.) → interceptée
  (handler dédié ou géré par le mécanisme d'exception global existant) → **401, corps
  vide ou message générique** — jamais de détail sur la cause.
- `securityContextRepository` : bean `HttpSessionSecurityContextRepository` (ou le
  `DelegatingSecurityContextRepository` déjà utilisé par Spring Security par défaut,
  à réutiliser/injecter tel quel).
- Reprend le comportement de l'ancien `loginSuccessHandler()`
  (`userService.saveLoginHistory(username)`).

### 2. `AuthorizationServerConfig` (chaîne `@Order(1)`, endpoints OAuth2 uniquement)

C'est **cette** chaîne — et non `SecurityConfig` — qui redirige aujourd'hui vers
`/login` quand une requête non authentifiée arrive sur `/oauth2/authorize` (via son
propre `.formLogin(Customizer.withDefaults())`). À modifier :

```java
.formLogin(form -> form.loginPage(applicationConfiguration.getLoginRedirectUri()))
```

`getLoginRedirectUri()` correspond à l'URL de la page `/login` du frontend React (déjà
utilisée par ailleurs comme `redirectUri` du client OAuth2 enregistré). Spring Security
sauvegarde la requête d'origine (avec le `code_challenge`/`state` du flux PKCE) en
session indépendamment du fait que la loginPage soit interne ou externe — ce mécanisme
n'est pas affecté par ce changement.

### 3. `SecurityConfig` (chaîne `@Order(2)`)

- **Supprimer entièrement** `.formLogin(...)` et la méthode `loginSuccessHandler()`.
  Nécessaire : si on la laisse, le `UsernamePasswordAuthenticationFilter` par défaut de
  Spring intercepterait `POST /login` avant qu'il n'atteigne notre contrôleur (il
  attend des paramètres form-encoded, pas du JSON), empêchant le nouvel endpoint
  d'être jamais exécuté.
- Whitelist `authorizeHttpRequests` : retirer `GET /login` (n'existe plus), ajouter
  `POST /login permitAll`.
- CSRF : déjà désactivé globalement (`http.csrf(AbstractHttpConfigurer::disable)`),
  rien à changer.
- CORS : déjà configuré avec `allowCredentials(true)` sur les origines autorisées,
  rien à changer — le nouvel appel `axios.post(..., { withCredentials: true })`
  fonctionnera tel quel.

### 4. Suppression

- `templates/login.html` (Thymeleaf) — supprimé. **Ne pas** toucher à la dépendance
  Thymeleaf elle-même : elle sert aussi à générer d'autres documents
  (`templates/documents/*.html`, PDF d'inscription/adhésion/bulletins).
- Méthode `GET /login` du contrôleur — supprimée.
- La propriété `RESET_PASSWORD_URI` / `ApplicationConfiguration.getResetPasswordUri()`
  **reste** : elle est aussi utilisée par `MailResetPasswordJob` pour l'email de
  réinitialisation de mot de passe. Seul son usage dans le modèle de la vue Thymeleaf
  du login disparaît.

## Frontend (`mosquee-thonon-front-2`)

### `src/hooks/AuthContext.tsx`

- Nouvelle fonction `authenticate(username: string, password: string): Promise<void>` :
  `axios.post(baseUrl + "/login", { username, password }, { withCredentials: true })`,
  propage l'erreur (401) pour que le formulaire l'affiche.
- `login` accepte un paramètre optionnel `login(fromOverride?: string)` : utilise
  `fromOverride ?? getState()`. Nécessaire car `getState()` renvoie `null` quand
  `pathname === "/login"` — or après le nouveau flux, on est justement sur `/login` au
  moment d'appeler `login()`, ce qui casserait la redirection vers la page d'origine
  sans cet ajustement.

### `src/routes/admin/SignIn.tsx`

- Supprime l'appel automatique à `login()` quand il n'y a pas de `code` et pas
  d'utilisateur connecté.
- Affiche un formulaire antd (`Form`, `Input`, `Input.Password`, `Button`) : username,
  password, soumission.
- Utilise `useLocation()` pour récupérer un éventuel `from` transmis via
  `navigate("/login", { state: { from: ... } })`.
- Soumission : `await authenticate(username, password)` →
  - succès → `login(from)` (relance le flux PKCE existant, inchangé)
  - échec (401) → `Alert` antd générique "Identifiants incorrects"
- Conserve le lien "mot de passe oublié" (présent aujourd'hui dans `login.html` via
  `resetPasswordUri`) : simple lien React Router vers la route existante
  `/resetPassword` (`src/routes/public/ResetPassword.tsx`), déjà fonctionnelle côté
  frontend. Aucun changement backend nécessaire pour ce lien — seule sa présence
  visuelle sur le nouveau formulaire est à ne pas oublier.

### `src/App.tsx` (ou fichier du header)

- Le bouton "Se connecter", qui appelle aujourd'hui `login()` directement, doit faire
  `navigate("/login")` pour amener l'utilisateur sur le nouveau formulaire au lieu de
  rediriger immédiatement vers le backend.

### Filet de sécurité

Si une session expire et qu'une requête protégée déclenche `/oauth2/authorize` sans
session valide, Spring redirige automatiquement (cf. `AuthorizationServerConfig`
ci-dessus) vers cette même page React `/login` — aucun autre point d'entrée à modifier.

### Style

Formulaire construit avec les composants antd déjà utilisés dans le reste de
l'application, dans le même layout (header/footer) que les autres routes — plus de
CSS Bootstrap isolé ni de page visuellement déconnectée du reste du site.

## Tests

- **Backend** : test du nouveau endpoint `POST /login` — succès (credentials valides →
  200, `SecurityContext` persisté en session), échec (401 générique, aucune fuite
  d'information sur la cause). Test d'intégration vérifiant qu'un appel `/oauth2/authorize`
  dans la même session après un `POST /login` réussi renvoie bien un code
  d'autorisation sans re-demander les identifiants.
- **Frontend** : test du formulaire `SignIn` — soumission réussie déclenche `login()`,
  échec affiche l'alerte générique, `authenticate` appelle l'endpoint avec
  `withCredentials: true`.
- **Test manuel end-to-end** (prioritaire vu la nature transverse du flux) : connexion
  complète depuis le nouveau formulaire jusqu'au dashboard ; session expirée →
  redirection vers `/login` React (plus l'ancienne page Thymeleaf) ; déconnexion ;
  mauvais mot de passe → message générique.

## Hors périmètre

- Rate limiting / anti-bruteforce sur `POST /login`.
- Messages d'erreur différenciés par type d'échec.
- Toute modification du flux de réinitialisation de mot de passe (`/v1/users/resetPassword/*`),
  qui reste tel quel.
