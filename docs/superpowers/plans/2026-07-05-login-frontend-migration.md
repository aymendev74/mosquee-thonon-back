# Migration du login vers le frontend React — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remplacer la page de login Thymeleaf du backend par un formulaire React (antd), tout en conservant le flux OAuth2/PKCE existant inchangé pour l'échange du code contre le JWT.

**Architecture:** Un nouvel endpoint JSON `POST /login` (backend) authentifie l'utilisateur via l'`AuthenticationManager` existant et persiste le `SecurityContext` en session ; le formulaire React appelle cet endpoint puis relance le flux PKCE déjà implémenté (`login()`), qui aboutit immédiatement puisque la session est déjà authentifiée. `AuthorizationServerConfig` redirige les accès non authentifiés à `/oauth2/authorize` vers la page React `/login` au lieu de l'ancienne page Thymeleaf.

**Tech Stack:** Backend : Spring Boot 3, Spring Security (OAuth2 Authorization Server), JUnit 5 + MockMvc + H2. Frontend : React, TypeScript, antd, react-router-dom v6, axios, Jest + React Testing Library (déjà présents en dépendance, non encore utilisés dans ce repo).

## Global Constraints

- Spec de référence : `docs/superpowers/specs/2026-07-05-login-frontend-migration-design.md` (ce repo).
- Aucun autre client que `mosquee-thonon-front-2` n'utilise `/login` (confirmé) → suppression sans fallback nécessaire.
- CSRF est déjà désactivé globalement (`SecurityConfig`) — ne pas en ajouter.
- Message d'erreur de connexion : générique uniquement ("Identifiants incorrects"), jamais de détail sur la cause.
- Pas de rate limiting/anti-bruteforce dans ce périmètre.
- Ne pas toucher à la dépendance Thymeleaf (utilisée par ailleurs pour les documents PDF) ni à la propriété `RESET_PASSWORD_URI` (utilisée par `MailResetPasswordJob`).
- Conventions de nommage des tests backend : classes préfixées `Test...` (pas de suffixe), placées dans le même package que la classe testée, étendant `org.mosqueethonon.v1.controller.TestController` pour les tests d'intégration `MockMvc`.
- Repos concernés : backend = `C:\Projets dev\mosquee-thonon-back`, frontend = `C:\Projets dev\mosquee-thonon-front-2` (deux dépôts git indépendants — chaque commit se fait dans le bon repo).
- **Compromis accepté (validé avec l'utilisateur) :** en supprimant l'ancien `formLogin`/`SavedRequestAwareAuthenticationSuccessHandler`, on perd le rejeu de la requête `/oauth2/authorize` d'origine sauvegardée par Spring. Concrètement, un utilisateur dont la session expire en pleine navigation et qui se reconnecte via le nouveau formulaire atterrit sur son tableau de bord par défaut (role-based) plutôt que sur la page exacte où il se trouvait avant l'expiration. Ne pas tenter de corriger ce point dans ce plan (pas de `RequestCache`, pas de paramètre `from` à faire transiter) — c'est un choix assumé, pas un oubli.

---

### Task 1: Nouvel endpoint `POST /login` (JSON) et suppression du login Thymeleaf

**Files:**
- Create: `mosquee-thonon-back/src/main/java/org/mosqueethonon/dto/auth/LoginRequestDto.java`
- Create: `mosquee-thonon-back/src/test/java/org/mosqueethonon/controller/TestAuthentificationController.java`
- Modify: `mosquee-thonon-back/src/main/java/org/mosqueethonon/controller/AuthentificationController.java`
- Modify: `mosquee-thonon-back/src/main/java/org/mosqueethonon/configuration/security/SecurityConfig.java`
- Modify: `mosquee-thonon-back/src/main/java/org/mosqueethonon/configuration/exception/CustomExceptionHandler.java`
- Delete: `mosquee-thonon-back/src/main/resources/templates/login.html`

**Interfaces:**
- Consumes: `AuthenticationManager` bean (`SecurityConfig.authenticationManager(...)`, déjà existant), `UserService.saveLoginHistory(String username)` (déjà existant), `UserService` autowirable (déjà un bean Spring existant).
- Produces: `POST /login` — body `{ "username": string, "password": string }` → `200` (session authentifiée) ou `401` (échec, corps vide). `LoginRequestDto { username: String, password: String }` (Lombok `@Data`). Bean `SecurityContextRepository securityContextRepository()`.

- [ ] **Step 1: Créer le DTO de requête**

```java
package org.mosqueethonon.dto.auth;

import lombok.Data;

@Data
public class LoginRequestDto {

    private String username;
    private String password;

}
```

- [ ] **Step 2: Écrire les tests d'intégration (encore en échec)**

```java
package org.mosqueethonon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mosqueethonon.dto.auth.LoginRequestDto;
import org.mosqueethonon.entity.utilisateur.UtilisateurEntity;
import org.mosqueethonon.repository.UtilisateurRepository;
import org.mosqueethonon.v1.controller.TestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestAuthentificationController extends TestController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @BeforeEach
    public void initContext() {
        utilisateurRepository.deleteAll();

        UtilisateurEntity utilisateur = new UtilisateurEntity();
        utilisateur.setUsername("jane.d");
        utilisateur.setPassword(bCryptPasswordEncoder.encode("correct-password"));
        utilisateur.setEmail("jane.d@example.com");
        utilisateur.setEnabled(true);
        utilisateur.setRoles(new ArrayList<>());
        utilisateurRepository.save(utilisateur);
    }

    @Test
    public void testLogin_WithValidCredentials_ShouldReturnOkAndPersistSession() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("jane.d");
        request.setPassword("correct-password");

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/profile")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jane.d"));
    }

    @Test
    public void testLogin_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("jane.d");
        request.setPassword("wrong-password");

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

}
```

- [ ] **Step 3: Lancer les tests, vérifier l'échec**

Run: `mvn -pl . test -Dtest=TestAuthentificationController` (depuis `mosquee-thonon-back`)
Expected: FAIL — `POST /login` renvoie `404` ou `403` (aucun mapping applicatif ; l'ancien `formLogin` intercepte ou route non trouvée).

- [ ] **Step 4: Implémenter le nouvel endpoint et retirer le `formLogin` par défaut qui l'intercepterait**

Dans `SecurityConfig.java` :
- Ajouter les imports :
```java
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
```
- Retirer les imports devenus inutiles : `AuthenticationSuccessHandler`, `SavedRequestAwareAuthenticationSuccessHandler` (et `Authentication`, `ServletException`, `IOException` s'ils ne sont plus utilisés ailleurs dans la classe — vérifier avant suppression).
- Supprimer la ligne `.formLogin(login -> login.loginPage("/login").successHandler(loginSuccessHandler()).permitAll())` de `filterChain(...)`.

  **Pourquoi impératif à cette étape précise :** tant que `.formLogin(...)` reste configuré, le `UsernamePasswordAuthenticationFilter` par défaut de Spring intercepte `POST /login` avant qu'il n'atteigne le contrôleur (il attend des paramètres form-encoded, pas du JSON) — le nouvel endpoint ne serait jamais exécuté et les tests resteraient en échec.
- Supprimer la méthode privée `loginSuccessHandler()` (son comportement est repris dans le contrôleur, step suivant).
- Remplacer dans la whitelist `.requestMatchers(HttpMethod.GET, "/login").permitAll()` par `.requestMatchers(HttpMethod.POST, "/login").permitAll()`.
- Ajouter le bean :
```java
@Bean
public SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
}
```

Dans `AuthentificationController.java` :
- Remplacer `@Controller` par `@RestController` (la classe ne rend plus aucune vue).
- Supprimer le champ `applicationConfiguration` et son import (`ApplicationConfiguration`), devenu inutilisé après suppression de la méthode `GET /login` (step suivant).
- Supprimer la méthode `GET /login` (`login(Model model)`) et l'import `Model`.
- Ajouter les champs et imports nécessaires :
```java
import jakarta.servlet.http.HttpServletRequest;
import org.mosqueethonon.dto.auth.LoginRequestDto;
import org.mosqueethonon.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
```
```java
    private AuthenticationManager authenticationManager;

    private SecurityContextRepository securityContextRepository;

    private UserService userService;
```
- Ajouter la méthode :
```java
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequestDto request,
                                       HttpServletRequest httpRequest,
                                       HttpServletResponse httpResponse) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        httpRequest.changeSessionId(); // protection contre la fixation de session (rôle tenu avant par formLogin)

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        userService.saveLoginHistory(request.getUsername());

        return ResponseEntity.ok().build();
    }
```

Dans `CustomExceptionHandler.java` :
- Ajouter l'import `org.springframework.security.core.AuthenticationException`.
- Ajouter :
```java
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException e) {
        log.error("Authentication failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
```

- [ ] **Step 5: Lancer les tests, vérifier le succès**

Run: `mvn -pl . test -Dtest=TestAuthentificationController` (depuis `mosquee-thonon-back`)
Expected: PASS — les deux tests passent.

- [ ] **Step 6: Supprimer le template Thymeleaf devenu orphelin**

Supprimer le fichier `mosquee-thonon-back/src/main/resources/templates/login.html`.

- [ ] **Step 7: Relancer la suite de tests complète pour vérifier l'absence de régression**

Run: `mvn -pl . test` (depuis `mosquee-thonon-back`)
Expected: PASS — aucune régression (en particulier `TestUserController`, qui dépend aussi de la config de sécurité).

- [ ] **Step 8: Commit**

```bash
git add src/main/java/org/mosqueethonon/dto/auth/LoginRequestDto.java \
        src/main/java/org/mosqueethonon/controller/AuthentificationController.java \
        src/main/java/org/mosqueethonon/configuration/security/SecurityConfig.java \
        src/main/java/org/mosqueethonon/configuration/exception/CustomExceptionHandler.java \
        src/test/java/org/mosqueethonon/controller/TestAuthentificationController.java \
        src/main/resources/templates/login.html
git commit -m "feat: replace Thymeleaf login form with JSON POST /login endpoint"
```

---

### Task 2: Rediriger `/oauth2/authorize` non authentifié vers la page React `/login`

**Files:**
- Modify: `mosquee-thonon-back/src/main/java/org/mosqueethonon/configuration/oauth/AuthorizationServerConfig.java`
- Create: `mosquee-thonon-back/src/test/java/org/mosqueethonon/configuration/oauth/TestAuthorizationServerConfig.java`

**Interfaces:**
- Consumes: `ApplicationConfiguration.getLoginRedirectUri()` (déjà existant, déjà injecté dans `AuthorizationServerConfig`).
- Produces: comportement — `GET /oauth2/authorize` sans session authentifiée renvoie une redirection (`3xx`) vers `applicationConfiguration.getLoginRedirectUri()`.

- [ ] **Step 1: Écrire le test (encore en échec)**

```java
package org.mosqueethonon.configuration.oauth;

import org.junit.jupiter.api.Test;
import org.mosqueethonon.configuration.security.ApplicationConfiguration;
import org.mosqueethonon.v1.controller.TestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestAuthorizationServerConfig extends TestController {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationConfiguration applicationConfiguration;

    @Test
    public void testAuthorize_WhenUnauthenticated_ShouldRedirectToFrontendLoginPage() throws Exception {
        String redirectUri = applicationConfiguration.getLoginRedirectUri();

        mockMvc.perform(MockMvcRequestBuilders.get("/oauth2/authorize")
                        .accept(MediaType.TEXT_HTML)
                        .queryParam("response_type", "code")
                        .queryParam("client_id", "moth-react-app")
                        .queryParam("redirect_uri", redirectUri)
                        .queryParam("code_challenge", "E9melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM")
                        .queryParam("code_challenge_method", "S256"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", redirectUri));
    }

}
```

Import supplémentaire nécessaire : `org.springframework.http.MediaType`.

**Notes (découvertes lors de l'implémentation) :**
- `OAuth2AuthorizationEndpointFilter` valide les paramètres OAuth2 requis (`response_type`, `client_id`, `redirect_uri`, `code_challenge` — PKCE obligatoire pour un client public `ClientAuthenticationMethod.NONE`) *avant* de vérifier l'authentification. Une requête `GET /oauth2/authorize` sans aucun paramètre renvoie donc `400 Bad Request` ("invalid_request: response_type"), pas une redirection — il faut fournir une requête syntaxiquement valide (mais toujours non authentifiée) pour atteindre la vérification d'authentification et observer la redirection. `redirect_uri` doit correspondre exactement à celui enregistré pour le client (`applicationConfiguration.getLoginRedirectUri()`, le même déjà utilisé dans l'assertion).
- `MockHttpServletRequestBuilder.param(...)` ne peuple pas la query string brute que le validateur OAuth2 lit sur une requête `GET` — utiliser `.queryParam(...)` à la place.
- L'`AuthenticationEntryPoint` négocié par le contenu ne redirige que pour les requêtes `Accept: text/html` (la config OIDC câble aussi un entry point Bearer 401 pour les clients API) — ajouter `.accept(MediaType.TEXT_HTML)`.

- [ ] **Step 2: Lancer le test, vérifier l'échec**

Run: `mvn -pl . test -Dtest=TestAuthorizationServerConfig` (depuis `mosquee-thonon-back`)
Expected: FAIL — `Location` vaut `/login` (chemin relatif, page Spring par défaut), pas l'URL configurée.

- [ ] **Step 3: Modifier `AuthorizationServerConfig`**

Remplacer :
```java
.formLogin(Customizer.withDefaults())
```
par :
```java
.formLogin(form -> form.loginPage(applicationConfiguration.getLoginRedirectUri()))
```

- [ ] **Step 4: Lancer le test, vérifier le succès**

Run: `mvn -pl . test -Dtest=TestAuthorizationServerConfig` (depuis `mosquee-thonon-back`)
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/org/mosqueethonon/configuration/oauth/AuthorizationServerConfig.java \
        src/test/java/org/mosqueethonon/configuration/oauth/TestAuthorizationServerConfig.java
git commit -m "feat: redirect unauthenticated /oauth2/authorize to the React login page"
```

---

### Task 3: `AuthContext` — endpoint d'authentification JSON

**Files:**
- Modify: `mosquee-thonon-front-2/src/hooks/AuthContext.tsx`

**Interfaces:**
- Consumes: rien de nouveau (mêmes `axios`, mêmes variables d'environnement que le reste du fichier).
- Produces: `authenticate(username: string, password: string): Promise<void>` (rejette en cas d'échec, notamment `401`). `login()` n'est pas modifiée (cf. compromis accepté dans les Global Constraints : après une reconnexion depuis `/login`, `getState()` renvoie `null` et l'utilisateur atterrit sur le tableau de bord par défaut — comportement voulu, pas de paramètre `from` à ajouter).

Pas de test dédié pour cette tâche : `AuthContext` appelle `window.crypto.subtle` et `window.location.replace`, non fiables à mocker proprement dans cet environnement de test sans polyfills supplémentaires (aucune convention existante dans ce repo pour ça). La logique d'orchestration (`authenticate` puis `login`) est couverte par le test du composant `SignIn` (Task 4, avec `useAuth` mocké) ; le câblage réel est vérifié manuellement (Task 6).

- [ ] **Step 1: Ajouter l'endpoint et la fonction `authenticate`**

Dans `AuthProvider`, à côté des autres constantes d'endpoint (après la ligne définissant `profileEndpoint`) :
```ts
    const loginEndpoint = process.env.REACT_APP_BASE_URL_API + "/login";
```

Juste après la déclaration de `login` (avant `logout`) :
```ts
    const authenticate = useCallback(async (username: string, password: string) => {
        await axios.post(loginEndpoint, { username, password }, { withCredentials: true });
    }, []);
```

- [ ] **Step 2: Mettre à jour le type du contexte et la valeur exposée**

Dans `AuthContextType` :
```ts
    authenticate: (username: string, password: string) => Promise<void>;
```

Dans le `return` du provider :
```tsx
        <AuthContext.Provider value={{ handleAuthorizationCode, login, authenticate, logout, username, prenom, roles, requestProfileInformations }}>
```

- [ ] **Step 3: Vérifier la compilation TypeScript**

Run: `npx tsc --noEmit` (depuis `mosquee-thonon-front-2`)
Expected: aucune erreur.

- [ ] **Step 4: Commit**

```bash
git add src/hooks/AuthContext.tsx
git commit -m "feat: add JSON authenticate() to AuthContext"
```

---

### Task 4: Formulaire de connexion React (`SignIn`)

**Files:**
- Create: `mosquee-thonon-front-2/src/setupTests.ts`
- Create: `mosquee-thonon-front-2/src/routes/admin/SignIn.test.tsx`
- Modify: `mosquee-thonon-front-2/src/routes/admin/SignIn.tsx`

**Interfaces:**
- Consumes: `useAuth()` → `{ login(), authenticate(username, password), handleAuthorizationCode(code, state), username, roles }` (Task 3).
- Produces: composant `SignIn` (export nommé, inchangé), monté sur la route `/login` (déjà en place dans `App.tsx`).

- [ ] **Step 1: Créer `setupTests.ts` (absent du repo, nécessaire pour les matchers `jest-dom`)**

```ts
import '@testing-library/jest-dom';
```

- [ ] **Step 2: Écrire le test du formulaire (encore en échec)**

```tsx
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { SignIn } from './SignIn';
import { useAuth } from '../../hooks/AuthContext';

jest.mock('../../hooks/AuthContext');

const mockedUseAuth = useAuth as jest.Mock;

describe('SignIn', () => {
    const authenticate = jest.fn();
    const login = jest.fn();
    const handleAuthorizationCode = jest.fn();

    beforeEach(() => {
        jest.clearAllMocks();
        mockedUseAuth.mockReturnValue({
            authenticate,
            login,
            handleAuthorizationCode,
            username: null,
            roles: [],
        });
    });

    it('logs in and continues the OAuth flow on valid credentials', async () => {
        authenticate.mockResolvedValueOnce(undefined);
        render(<MemoryRouter initialEntries={['/login']}><SignIn /></MemoryRouter>);

        await userEvent.type(screen.getByLabelText("Nom d'utilisateur"), 'jane.d');
        await userEvent.type(screen.getByLabelText('Mot de passe'), 'correct-password');
        await userEvent.click(screen.getByRole('button', { name: 'Se connecter' }));

        await waitFor(() => expect(authenticate).toHaveBeenCalledWith('jane.d', 'correct-password'));
        expect(login).toHaveBeenCalled();
        expect(screen.queryByText('Identifiants incorrects')).not.toBeInTheDocument();
    });

    it('shows a generic error message on invalid credentials', async () => {
        authenticate.mockRejectedValueOnce({ response: { status: 401 } });
        render(<MemoryRouter initialEntries={['/login']}><SignIn /></MemoryRouter>);

        await userEvent.type(screen.getByLabelText("Nom d'utilisateur"), 'jane.d');
        await userEvent.type(screen.getByLabelText('Mot de passe'), 'wrong-password');
        await userEvent.click(screen.getByRole('button', { name: 'Se connecter' }));

        await waitFor(() => expect(screen.getByText('Identifiants incorrects')).toBeInTheDocument());
        expect(login).not.toHaveBeenCalled();
    });
});
```

- [ ] **Step 3: Lancer le test, vérifier l'échec**

Run: `CI=true npx react-scripts test SignIn --watchAll=false` (depuis `mosquee-thonon-front-2`)
Expected: FAIL — `SignIn` redirige encore automatiquement (`login()`) au lieu d'afficher un formulaire ; les champs `"Nom d'utilisateur"`/`"Mot de passe"` n'existent pas encore.

- [ ] **Step 4: Réécrire `SignIn.tsx`**

```tsx
import { FunctionComponent, useEffect, useState } from "react";
import { useAuth } from "../../hooks/AuthContext";
import { useNavigate } from "react-router-dom";
import { Alert, Button, Form, Input, Spin } from "antd";
import { ROLE_UTILISATEUR } from "../../services/user";

type FieldType = {
    username: string;
    password: string;
};

export const SignIn: FunctionComponent = () => {

    const { login, authenticate, handleAuthorizationCode, username, roles } = useAuth();
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    const [loginFailed, setLoginFailed] = useState(false);

    const getRedirectForRoles = (userRoles: string[]) => {
        if (userRoles.includes("ROLE_ADMIN") || userRoles.includes("ROLE_ENSEIGNANT") || userRoles.includes("ROLE_TRESORIER")) {
            return "/admin";
        }
        if (userRoles.includes(ROLE_UTILISATEUR)) {
            return "/dashboard";
        }
        return "/";
    };

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const code = params.get("code");
        const state = params.get("state");

        const init = async () => {
            if (code) {
                const result = await handleAuthorizationCode(code, state);
                if (result && result.from && result.from !== "/") {
                    navigate(result.from);
                } else {
                    navigate(getRedirectForRoles(result?.roles ?? []));
                }
            } else if (username) {
                navigate(getRedirectForRoles(roles));
            }
        };

        init();
    }, []);

    const onFinish = async (values: FieldType) => {
        setLoginFailed(false);
        setIsLoading(true);
        try {
            await authenticate(values.username, values.password);
            await login();
        } catch (error) {
            setLoginFailed(true);
            setIsLoading(false);
        }
    };

    return (
        <div className="centered-content">
            <Form<FieldType>
                name="login"
                layout="vertical"
                style={{ maxWidth: 400, width: "80%", marginTop: "100px", padding: "0 16px", boxSizing: "border-box" }}
                onFinish={onFinish}
                autoComplete="off"
            >
                <h2 className="user-activation-title">Connexion</h2>
                <Spin spinning={isLoading}>
                    {loginFailed && (
                        <Form.Item>
                            <Alert type="error" message="Identifiants incorrects" showIcon />
                        </Form.Item>
                    )}
                    <Form.Item<FieldType>
                        label="Nom d'utilisateur"
                        name="username"
                        rules={[{ required: true, message: "Veuillez saisir votre nom d'utilisateur" }]}
                    >
                        <Input />
                    </Form.Item>
                    <Form.Item<FieldType>
                        label="Mot de passe"
                        name="password"
                        rules={[{ required: true, message: "Veuillez saisir votre mot de passe" }]}
                    >
                        <Input.Password />
                    </Form.Item>
                    <Form.Item>
                        <Button type="primary" htmlType="submit">
                            Se connecter
                        </Button>
                    </Form.Item>
                    <Form.Item>
                        <Button type="link" onClick={() => navigate("/resetPassword")}>
                            Mot de passe oublié ?
                        </Button>
                    </Form.Item>
                </Spin>
            </Form>
        </div>
    );

}
```

- [ ] **Step 5: Lancer le test, vérifier le succès**

Run: `CI=true npx react-scripts test SignIn --watchAll=false` (depuis `mosquee-thonon-front-2`)
Expected: PASS — les deux tests passent.

- [ ] **Step 6: Commit**

```bash
git add src/setupTests.ts src/routes/admin/SignIn.tsx src/routes/admin/SignIn.test.tsx
git commit -m "feat: replace auto-redirect SignIn page with an antd login form"
```

---

### Task 5: Point d'entrée du header — naviguer vers `/login` au lieu de rediriger directement

**Files:**
- Modify: `mosquee-thonon-front-2/src/App.tsx`

**Interfaces:**
- Consumes: `navigate` (déjà disponible via `useNavigate()` dans `App.tsx`).
- Produces: aucun changement d'API — comportement UI uniquement.

- [ ] **Step 1: Retirer `login` de la destructuration `useAuth()` (devient inutilisé) et rediriger vers `/login`**

Remplacer :
```tsx
  const { username, prenom, logout, login, requestProfileInformations } = useAuth();
```
par :
```tsx
  const { username, prenom, logout, requestProfileInformations } = useAuth();
```

Remplacer :
```tsx
              <Button type="primary" onClick={login} icon={<LoginOutlined />}>
                Se connecter
              </Button>
```
par :
```tsx
              <Button type="primary" onClick={() => navigate("/login")} icon={<LoginOutlined />}>
                Se connecter
              </Button>
```

- [ ] **Step 2: Vérifier la compilation TypeScript**

Run: `npx tsc --noEmit` (depuis `mosquee-thonon-front-2`)
Expected: aucune erreur.

- [ ] **Step 3: Commit**

```bash
git add src/App.tsx
git commit -m "fix: navigate to the React /login page instead of redirecting straight to the IdP"
```

---

### Task 6: Vérification manuelle de bout en bout

Aucun fichier modifié — validation du câblage réel (OAuth2/PKCE, cookie httpOnly, session Spring) que les tests automatisés ne couvrent pas entièrement.

- [ ] **Step 1: Démarrer le backend**

Run (depuis `mosquee-thonon-back`) : `mvn spring-boot:run`
Expected: démarrage sans erreur, port `8080` (context-path `/api`).

- [ ] **Step 2: Démarrer le frontend**

Run (depuis `mosquee-thonon-front-2`) : `npm start`
Expected: démarrage sans erreur, ouverture sur `http://localhost:3000`.

- [ ] **Step 3: Connexion nominale**

Dans le navigateur : cliquer sur "Se connecter" dans le header → vérifier l'affichage du nouveau formulaire React (pas de flash Bootstrap) → saisir des identifiants valides → soumettre.
Expected: redirection vers `/admin` ou `/dashboard` selon le rôle, cookie `MOTH-TOKEN` présent (DevTools → Application → Cookies), utilisateur affiché dans le header.

- [ ] **Step 4: Mauvais mot de passe**

Depuis `/login`, saisir un mauvais mot de passe.
Expected: message "Identifiants incorrects" affiché inline, pas de redirection, pas de cookie posé.

- [ ] **Step 5: Lien "mot de passe oublié"**

Cliquer sur "Mot de passe oublié ?".
Expected: navigation vers `/resetPassword`, page fonctionnelle (déjà existante, non modifiée par ce plan).

- [ ] **Step 6: Accès direct à une page protégée sans session**

Ouvrir un onglet de navigation privée, naviguer directement vers une route protégée (ex. `/admin`).
Expected: redirection vers `/login` (page React, plus l'ancienne page Thymeleaf/Bootstrap).

- [ ] **Step 7: Déconnexion**

Se déconnecter via le menu utilisateur.
Expected: retour à l'état non authentifié, cookies `MOTH-TOKEN`/session supprimés.
