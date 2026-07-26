package org.mosqueethonon.chatbot.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestMarkdownChunker {

    @Test
    public void testSplitH2Sections() {
        // GIVEN
        String markdown = """
                # Titre du document

                Chapeau introductif non indexé.

                ## Première section

                Contenu de la première section.

                ## Deuxième section

                Contenu de la deuxième section.
                """;

        // WHEN
        List<MarkdownSection> sections = MarkdownChunker.split(markdown);

        // THEN
        assertEquals(2, sections.size());
        assertEquals("Première section", sections.get(0).title());
        assertTrue(sections.get(0).content().startsWith("## Première section"));
        assertTrue(sections.get(0).content().contains("Contenu de la première section."));
        assertEquals("Deuxième section", sections.get(1).title());
        assertTrue(sections.get(1).content().contains("Contenu de la deuxième section."));
    }

    @Test
    public void testSplitH2AndH3SectionsSequentially() {
        // GIVEN
        String markdown = """
                # Titre

                ## Section A

                Texte A.

                ### Sous-section A.1

                Texte A.1.

                ### Sous-section A.2

                Texte A.2.

                ## Section B

                Texte B.
                """;

        // WHEN
        List<MarkdownSection> sections = MarkdownChunker.split(markdown);

        // THEN
        assertEquals(4, sections.size());
        assertEquals("Section A", sections.get(0).title());
        assertEquals("Sous-section A.1", sections.get(1).title());
        assertEquals("Sous-section A.2", sections.get(2).title());
        assertEquals("Section B", sections.get(3).title());
    }

    @Test
    public void testIgnoresH4AndDeeperHeadings() {
        // GIVEN
        String markdown = """
                ## Section

                Texte.

                #### Détail non indexé séparément

                Ce texte reste rattaché à la section ##.
                """;

        // WHEN
        List<MarkdownSection> sections = MarkdownChunker.split(markdown);

        // THEN
        assertEquals(1, sections.size());
        assertTrue(sections.get(0).content().contains("Détail non indexé séparément"));
    }

    @Test
    public void testNoHeadingReturnsEmptyList() {
        // GIVEN
        String markdown = "# Titre seul\n\nAucune section ##/### dans ce document.";

        // WHEN
        List<MarkdownSection> sections = MarkdownChunker.split(markdown);

        // THEN
        assertTrue(sections.isEmpty());
    }

    @Test
    public void testBlankInputReturnsEmptyList() {
        // WHEN
        List<MarkdownSection> sections = MarkdownChunker.split("   ");

        // THEN
        assertTrue(sections.isEmpty());
    }

}
