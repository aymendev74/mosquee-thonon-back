package org.mosqueethonon.chatbot.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Découpe un document markdown en sections, une section = un titre {@code ##} ou {@code ###}
 * et tout le contenu jusqu'au prochain titre {@code ##}/{@code ###} (ou la fin du fichier).
 * Le titre est inclus dans le contenu de la section. Le contenu avant le premier titre
 * {@code ##}/{@code ###} (typiquement le H1 et le chapeau introductif) n'est pas indexé.
 */
public final class MarkdownChunker {

    // ## ou ### suivi d'un espace, mais pas #### (négation via lookahead)
    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{2,3})(?!#)\\s+(.+?)\\s*$");

    private MarkdownChunker() {
    }

    public static List<MarkdownSection> split(String markdown) {
        List<MarkdownSection> sections = new ArrayList<>();
        if (markdown == null || markdown.isBlank()) {
            return sections;
        }

        String[] lines = markdown.split("\n", -1);

        String currentTitle = null;
        StringBuilder currentContent = null;

        for (String line : lines) {
            Matcher matcher = HEADING_PATTERN.matcher(line);
            if (matcher.matches()) {
                if (currentTitle != null) {
                    sections.add(new MarkdownSection(currentTitle, currentContent.toString().strip()));
                }
                currentTitle = matcher.group(2).strip();
                currentContent = new StringBuilder(line.strip());
            } else if (currentContent != null) {
                currentContent.append("\n").append(line);
            }
            // lignes avant le premier titre ##/### : ignorées (H1 + chapeau introductif)
        }

        if (currentTitle != null) {
            sections.add(new MarkdownSection(currentTitle, currentContent.toString().strip()));
        }

        return sections;
    }

}
