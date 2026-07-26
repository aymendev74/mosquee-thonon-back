package org.mosqueethonon.chatbot.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mosqueethonon.chatbot.config.ChatbotProperties;
import org.mosqueethonon.chatbot.entity.ChatbotDocumentChunkEntity;
import org.mosqueethonon.chatbot.repository.ChatbotDocumentChunkRepository;
import org.mosqueethonon.chatbot.repository.ChatbotIndexStateRepository;
import org.mosqueethonon.chatbot.service.ChatbotIndexingService;
import org.mosqueethonon.chatbot.service.EmbeddingService;
import org.mosqueethonon.chatbot.util.MarkdownChunker;
import org.mosqueethonon.chatbot.util.MarkdownSection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotIndexingServiceImpl implements ChatbotIndexingService {

    private static final String README_FILE_NAME = "README.md";

    private static final String SIGNATURE_ALGORITHM = "SHA-256";

    /**
     * Filet de sécurité en secondes. La protection principale reste le read-timeout HTTP
     * (spring.http.client.read-timeout) : un timeout de transaction JDBC ne peut pas interrompre un
     * appel réseau bloqué, il n'agit qu'à la prochaine instruction SQL.
     */
    private static final int TRANSACTION_TIMEOUT_SECONDS = 180;

    /** Séparateur non imprimable, absent du markdown, pour rendre la sérialisation non ambiguë. */
    private static final String SIGNATURE_SEPARATOR = Character.toString(0);

    private final EmbeddingService embeddingService;

    private final ChatbotDocumentChunkRepository chatbotDocumentChunkRepository;

    private final ChatbotIndexStateRepository chatbotIndexStateRepository;

    private final ChatbotProperties chatbotProperties;

    /**
     * La transaction couvre l'appel d'embedding en plus des écritures : c'est ce qui garantit que la
     * purge et la réinsertion sont atomiques (sans quoi un incident entre les deux laisserait un
     * index vide), et que le verrou pris sur la ligne d'état sérialise deux instances qui
     * démarreraient simultanément.
     */
    @Override
    @Transactional(timeout = TRANSACTION_TIMEOUT_SECONDS)
    public int reindex() {
        IndexingContext context = prepareIndexing();
        return index(context.chunks(), context.signature());
    }

    @Override
    @Transactional(timeout = TRANSACTION_TIMEOUT_SECONDS)
    public OptionalInt reindexIfOutdated() {
        IndexingContext context = prepareIndexing();

        if (context.signature().equals(context.storedSignature())) {
            log.info("Indexation chatbot : index déjà à jour (signature {}), rien à faire", context.signature());
            return OptionalInt.empty();
        }

        log.info("Indexation chatbot : signature obsolète (stockée={}, calculée={}), réindexation",
                context.storedSignature().isEmpty() ? "<aucune>" : context.storedSignature(), context.signature());
        return OptionalInt.of(index(context.chunks(), context.signature()));
    }

    /**
     * Lit et découpe la documentation, calcule sa signature, puis verrouille la ligne d'état. L'ordre
     * compte et est volontairement centralisé ici : la lecture des fichiers a lieu avant la prise du
     * verrou pour le tenir le moins longtemps possible, et le verrou est pris avant toute
     * comparaison, sans quoi deux instances pourraient conclure simultanément qu'il faut réindexer.
     */
    private IndexingContext prepareIndexing() {
        List<ChatbotDocumentChunkEntity> chunks = buildAllChunks();
        String signature = computeSignature(chunks);
        String storedSignature = this.chatbotIndexStateRepository.findSignatureForUpdate();
        return new IndexingContext(chunks, signature, storedSignature);
    }

    private record IndexingContext(List<ChatbotDocumentChunkEntity> chunks, String signature,
                                   String storedSignature) {
    }

    private int index(List<ChatbotDocumentChunkEntity> chunks, String signature) {
        if (chunks.isEmpty()) {
            log.warn("Indexation chatbot : aucun chunk à indexer");
            this.chatbotDocumentChunkRepository.deleteAll();
            this.chatbotIndexStateRepository.updateSignature(signature, 0);
            return 0;
        }

        // L'appel Gemini a lieu avant toute écriture : un échec réseau laisse l'index précédent intact.
        List<float[]> embeddings = this.embeddingService.embedBatch(
                chunks.stream().map(ChatbotDocumentChunkEntity::getContent).toList());
        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).setEmbedding(embeddings.get(i));
        }

        this.chatbotDocumentChunkRepository.deleteAll();
        this.chatbotDocumentChunkRepository.insertAll(chunks);
        this.chatbotIndexStateRepository.updateSignature(signature, chunks.size());

        log.info("Indexation chatbot terminée : {} chunk(s) indexé(s)", chunks.size());
        return chunks.size();
    }

    private List<ChatbotDocumentChunkEntity> buildAllChunks() {
        Path docsPath = Paths.get(this.chatbotProperties.getDocsPath());
        List<Path> markdownFiles = listMarkdownFiles(docsPath);

        log.info("Indexation chatbot : {} fichier(s) markdown trouvé(s) dans {}", markdownFiles.size(), docsPath);

        List<ChatbotDocumentChunkEntity> chunks = new ArrayList<>();
        for (Path file : markdownFiles) {
            chunks.addAll(buildChunksForFile(file));
        }
        return chunks;
    }

    /**
     * Empreinte de tout ce qui rend l'index valide ou non : le contenu des chunks, mais aussi le
     * modèle d'embedding et la dimension demandée. Changer de modèle sans toucher à la documentation
     * rend les vecteurs stockés incomparables aux nouvelles requêtes ; hacher les chunks plutôt que
     * les octets bruts des fichiers couvre en plus un changement de découpage du MarkdownChunker.
     */
    private String computeSignature(List<ChatbotDocumentChunkEntity> chunks) {
        ChatbotProperties.Gemini gemini = this.chatbotProperties.getGemini();
        StringBuilder payload = new StringBuilder()
                .append(gemini.getEmbeddingModel()).append(SIGNATURE_SEPARATOR)
                .append(gemini.getEmbeddingDimension()).append(SIGNATURE_SEPARATOR);
        for (ChatbotDocumentChunkEntity chunk : chunks) {
            payload.append(chunk.getTheme()).append(SIGNATURE_SEPARATOR)
                    .append(chunk.getSectionTitle()).append(SIGNATURE_SEPARATOR)
                    .append(chunk.getContent()).append(SIGNATURE_SEPARATOR);
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(SIGNATURE_ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(payload.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 est garanti présent sur toute JVM conforme.
            throw new IllegalStateException("Algorithme " + SIGNATURE_ALGORITHM + " indisponible", e);
        }
    }

    private List<ChatbotDocumentChunkEntity> buildChunksForFile(Path file) {
        String fileName = file.getFileName().toString();
        String theme = fileName.substring(0, fileName.length() - ".md".length());
        String content = readFile(file);

        List<MarkdownSection> sections = MarkdownChunker.split(content);
        LocalDateTime now = LocalDateTime.now();

        return sections.stream()
                .map(section -> ChatbotDocumentChunkEntity.builder()
                        .theme(theme)
                        .sourceFile(fileName)
                        .sectionTitle(section.title())
                        .content(section.content())
                        .createdAt(now)
                        .build())
                .toList();
    }

    private List<Path> listMarkdownFiles(Path docsPath) {
        try (Stream<Path> stream = Files.list(docsPath)) {
            return stream
                    .filter(path -> path.getFileName().toString().endsWith(".md"))
                    .filter(path -> !README_FILE_NAME.equalsIgnoreCase(path.getFileName().toString()))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de lister les fichiers markdown dans " + docsPath, e);
        }
    }

    private String readFile(Path file) {
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de lire le fichier " + file, e);
        }
    }

}
