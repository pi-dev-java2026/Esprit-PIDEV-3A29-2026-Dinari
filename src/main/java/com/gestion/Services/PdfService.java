package com.gestion.Services;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PdfService {

    private static final float MARGIN = 50;
    private static final float FONT_SIZE = 12;
    private static final float LEADING = 1.5f * FONT_SIZE;

    public void exportToPdf(String text, String filePath, boolean isArabic) throws Exception {
        String sanitizedText = sanitizeText(text);
        
        try (PDDocument document = new PDDocument()) {
            PDFont font = loadFont(document, isArabic);
            
            List<String> lines = wrapText(sanitizedText, font, FONT_SIZE, 
                PDRectangle.A4.getWidth() - 2 * MARGIN);
            
            addPages(document, lines, font);
            
            document.save(filePath);
        }
    }

    private String sanitizeText(String text) {
        if (text == null) {
            return "";
        }

        text = text.replace("🎯", "[Objectif]");
        text = text.replace("✨", "*");
        text = text.replace("📊", "[Graphique]");
        text = text.replace("💰", "[Argent]");
        text = text.replace("📈", "[Croissance]");
        text = text.replace("🎓", "[Education]");
        text = text.replace("✓", "✓"); // Keep checkmark if supported
        text = text.replace("🏆", "[Trophee]");
        text = text.replace("🔄", "[Actualiser]");
        text = text.replace("📥", "[Telecharger]");
        text = text.replace("📋", "[Document]");
        text = text.replace("📝", "[Note]");
        text = text.replace("⬅", "<-");
        text = text.replace("→", "->");

        StringBuilder sanitized = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int codePoint = text.codePointAt(i);
            
            // Keep these character ranges:
            // - Basic Latin (0x0000-0x007F)
            // - Latin-1 Supplement (0x0080-0x00FF)
            // - Latin Extended-A (0x0100-0x017F)
            // - Arabic (0x0600-0x06FF)
            // - Arabic Supplement (0x0750-0x077F)
            // - Whitespace and common punctuation
            if ((codePoint >= 0x0020 && codePoint <= 0x007E) ||  // Basic ASCII
                (codePoint >= 0x00A0 && codePoint <= 0x00FF) ||  // Latin-1 Supplement
                (codePoint >= 0x0100 && codePoint <= 0x017F) ||  // Latin Extended-A
                (codePoint >= 0x0600 && codePoint <= 0x06FF) ||  // Arabic
                (codePoint >= 0x0750 && codePoint <= 0x077F) ||  // Arabic Supplement
                codePoint == 0x000A ||  // Line feed
                codePoint == 0x000D ||  // Carriage return
                codePoint == 0x2022 ||  // Bullet point
                codePoint == 0x2013 ||  // En dash
                codePoint == 0x2014 ||  // Em dash
                codePoint == 0x2018 ||  // Left single quote
                codePoint == 0x2019 ||  // Right single quote
                codePoint == 0x201C ||  // Left double quote
                codePoint == 0x201D ||  // Right double quote
                codePoint == 0x2026) {  // Ellipsis
                sanitized.append(c);
            } else if (Character.isHighSurrogate(c)) {
                i++;
            }
        }

        return sanitized.toString();
    }

    private PDFont loadFont(PDDocument document, boolean isArabic) throws Exception {
        try {
            InputStream fontStream = getClass().getResourceAsStream("/fonts/NotoSansArabic-Regular.ttf");
            if (fontStream != null && isArabic) {
                System.out.println("Loading Arabic font from resources...");
                return PDType0Font.load(document, fontStream);
            }
        } catch (Exception e) {
            System.err.println("Could not load Arabic font: " + e.getMessage());
        }

        try {
            InputStream fontStream = getClass().getResourceAsStream("/fonts/NotoSans-Regular.ttf");
            if (fontStream != null) {
                System.out.println("Loading NotoSans font from resources...");
                return PDType0Font.load(document, fontStream);
            }
        } catch (Exception e) {
            System.err.println("Could not load NotoSans font: " + e.getMessage());
        }

        System.out.println("Using Helvetica font (fallback)");
        return PDType1Font.HELVETICA;
    }

    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) 
            throws Exception {
        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                lines.add("");
                continue;
            }

            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                
                float width;
                try {
                    width = font.getStringWidth(testLine) / 1000 * fontSize;
                } catch (Exception e) {
                    // If character not supported, skip to next word
                    System.err.println("Skipping unsupported character in word: " + word);
                    continue;
                }

                if (width > maxWidth && currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    currentLine = new StringBuilder(testLine);
                }
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }

        return lines;
    }

    private void addPages(PDDocument document, List<String> lines, PDFont font) throws Exception {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.setFont(font, FONT_SIZE);
        contentStream.newLineAtOffset(MARGIN, PDRectangle.A4.getHeight() - MARGIN);

        float yPosition = PDRectangle.A4.getHeight() - MARGIN;

        for (String line : lines) {
            if (yPosition < MARGIN + LEADING) {
                contentStream.endText();
                contentStream.close();

                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                contentStream.beginText();
                contentStream.setFont(font, FONT_SIZE);
                yPosition = PDRectangle.A4.getHeight() - MARGIN;
                contentStream.newLineAtOffset(MARGIN, yPosition);
            }

            try {
                contentStream.showText(line);
            } catch (Exception e) {
                // If line contains unsupported characters, try to show what we can
                System.err.println("Error showing line, attempting character-by-character: " + e.getMessage());
                StringBuilder safeLine = new StringBuilder();
                for (char c : line.toCharArray()) {
                    try {
                        font.getStringWidth(String.valueOf(c));
                        safeLine.append(c);
                    } catch (Exception ex) {
                        safeLine.append('?');
                    }
                }
                contentStream.showText(safeLine.toString());
            }
            
            contentStream.newLineAtOffset(0, -LEADING);
            yPosition -= LEADING;
        }

        contentStream.endText();
        contentStream.close();
    }
}
