package org.mosqueethonon.chatbot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.enums.ChatbotRoleEnum;
import org.mosqueethonon.chatbot.service.ChatbotGenerationService;
import org.mosqueethonon.chatbot.service.ChatbotTurn;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Appelle l'API Google Generative Language (modèle gemini-2.5-flash par défaut) pour générer
 * la réponse du chatbot RAG, via {@link RestClient}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiGenerationServiceImpl implements ChatbotGenerationService {

    static final String SYSTEM_INSTRUCTION = """
            Tu es un assistant factuel intégré à l'application de gestion de l'association (mosquée AMC), \
            destiné aux administrateurs et enseignants qui utilisent l'application.

            Tu réponds UNIQUEMENT à partir des extraits de documentation fournis dans le CONTEXTE ci-dessous. \
            Tu ne dois JAMAIS inventer, deviner ou compléter avec des connaissances extérieures à ce CONTEXTE.

            GARDE-FOU IMPÉRATIF : si le CONTEXTE ne contient pas l'information nécessaire pour répondre à la \
            question, réponds EXACTEMENT : "Je n'ai pas cette information dans la documentation."

            Réponds en français, de manière claire, concise et directement utilisable par un administrateur \
            ou un enseignant. Va droit au but, sans préambule du type "D'après la documentation".

            FORMAT DE RÉPONSE : ta réponse est affichée dans une fenêtre de discussion étroite. \
            Tu ne peux utiliser QUE du gras (**terme**) pour les termes clés, des listes à puces \
            commençant par "- " pour les énumérations, et des paragraphes séparés par une ligne vide. \
            N'utilise JAMAIS de titres (#), de tableaux (|), de blocs de code (```), de liens \
            ([texte](url)) ni de citations (>). Reste court : quelques phrases, ou une liste de 3 à \
            5 puces. La phrase du garde-fou ci-dessus doit être renvoyée telle quelle, sans aucun \
            formatage.""";

    private static final String NO_CONTEXT_PLACEHOLDER = "(aucun extrait de documentation pertinent trouvé)";

    private final RestClient geminiRestClient;

    private final ChatbotProperties chatbotProperties;

    @Override
    public String generate(List<ChatbotTurn> history, String context, String question) {
        String rawModel = this.chatbotProperties.getGemini().getGenerationModel();

        List<GeminiContentEntry> contents = history.stream()
                .map(turn -> new GeminiContentEntry(toGeminiRole(turn.role()), List.of(new GeminiPart(turn.content()))))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        String userTurn = "CONTEXTE:\n" + (context == null || context.isBlank() ? NO_CONTEXT_PLACEHOLDER : context)
                + "\n\nQUESTION: " + question;
        contents.add(new GeminiContentEntry("user", List.of(new GeminiPart(userTurn))));

        GeminiGenerateRequest request = new GeminiGenerateRequest(
                new GeminiSystemInstruction(List.of(new GeminiPart(SYSTEM_INSTRUCTION))),
                contents,
                new GeminiGenerationConfig(this.chatbotProperties.getGemini().getTemperature())
        );

        GeminiGenerateResponse response = this.geminiRestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/models/{model}:generateContent").build(rawModel))
                .body(request)
                .retrieve()
                .body(GeminiGenerateResponse.class);

        if (response == null || response.candidates() == null || response.candidates().isEmpty()
                || response.candidates().get(0).content() == null
                || response.candidates().get(0).content().parts() == null
                || response.candidates().get(0).content().parts().isEmpty()) {
            log.error("Réponse vide ou inattendue de l'API Gemini generateContent");
            throw new IllegalStateException("Réponse vide de l'API Gemini generateContent");
        }

        return response.candidates().get(0).content().parts().get(0).text();
    }

    private String toGeminiRole(ChatbotRoleEnum role) {
        return role == ChatbotRoleEnum.ASSISTANT ? "model" : "user";
    }

    private record GeminiPart(String text) {
    }

    private record GeminiSystemInstruction(List<GeminiPart> parts) {
    }

    private record GeminiContentEntry(String role, List<GeminiPart> parts) {
    }

    private record GeminiGenerationConfig(Double temperature) {
    }

    private record GeminiGenerateRequest(GeminiSystemInstruction systemInstruction, List<GeminiContentEntry> contents,
                                         GeminiGenerationConfig generationConfig) {
    }

    private record GeminiCandidateContent(List<GeminiPart> parts) {
    }

    private record GeminiCandidate(GeminiCandidateContent content) {
    }

    private record GeminiGenerateResponse(List<GeminiCandidate> candidates) {
    }

}
