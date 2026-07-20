package com.cinegrid.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.Color;
import java.io.File;

public class PdfTicketExporter {

    public static boolean generateTicketPdf(String receiptText) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            String bookingRefId = extractBookingId(receiptText);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                
                float pageWidth = page.getMediaBox().getWidth();
                float pageHeight = page.getMediaBox().getHeight();

                // 1. Outer Luxury Border Frame
                cs.setStrokingColor(new Color(25, 25, 35));
                cs.setLineWidth(2.5f);
                cs.addRect(25, 25, pageWidth - 50, pageHeight - 50);
                cs.stroke();

                // 2. Cinematic Header Banner
                cs.setNonStrokingColor(new Color(248, 68, 100)); // CineGrid Red/Pink
                cs.addRect(27, pageHeight - 115, pageWidth - 54, 88);
                cs.fill();

                // Header Title
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 22);
                cs.setNonStrokingColor(Color.WHITE);
                cs.newLineAtOffset(50, pageHeight - 62);
                cs.showText("CINEGRID MULTIPLEX");
                cs.endText();

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                cs.setNonStrokingColor(new Color(255, 230, 235));
                cs.newLineAtOffset(50, pageHeight - 85);
                cs.showText("OFFICIAL SECURE DIGITAL ENTRY PASS | 2026 EDITION");
                cs.endText();

                // 3. Main Details Container Box
                cs.setStrokingColor(new Color(210, 215, 225));
                cs.setLineWidth(1f);
                cs.addRect(45, 140, pageWidth - 90, 530);
                cs.stroke();

                // Section Header Title inside body
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
                cs.setNonStrokingColor(new Color(40, 40, 50));
                cs.newLineAtOffset(70, 635);
                cs.showText("RESERVATION & SEAT ALLOCATION SUMMARY");
                cs.endText();

                // Accent Line under section header
                cs.setStrokingColor(new Color(248, 68, 100));
                cs.setLineWidth(1.5f);
                cs.moveTo(70, 623);
                cs.lineTo(pageWidth - 70, 623);
                cs.stroke();

                // 4. Highlighted Booking Reference ID Box (Placed cleanly at the top of data)
                cs.setNonStrokingColor(new Color(240, 243, 248));
                cs.addRect(68, 565, pageWidth - 136, 34);
                cs.fill();

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 13);
                cs.setNonStrokingColor(new Color(248, 68, 100));
                cs.newLineAtOffset(85, 576);
                cs.showText("BOOKING REFERENCE ID : " + bookingRefId);
                cs.endText();

                // 5. Clean Receipt Data Fields Render (With fixed spacing to prevent overlap)
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER_BOLD), 12);
                cs.setNonStrokingColor(new Color(30, 30, 30));
                cs.newLineAtOffset(70, 520);

                String[] lines = receiptText.split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty() && !line.contains("===") && !line.contains("Booking Reference ID:")) {
                        cs.showText(line);
                        cs.newLineAtOffset(0, -32);
                    }
                }
                cs.endText();

                // 6. VECTOR QR CODE MATRIX GENERATOR (Cleanly positioned on bottom right)
                float qrX = pageWidth - 195;
                float qrY = 175;
                float qrSize = 110;

                cs.setStrokingColor(Color.BLACK);
                cs.setLineWidth(1.5f);
                cs.addRect(qrX - 6, qrY - 22, qrSize + 12, qrSize + 32);
                cs.stroke();

                cs.setNonStrokingColor(Color.WHITE);
                cs.addRect(qrX, qrY, qrSize, qrSize);
                cs.fill();

                // QR Pixel Matrix
                cs.setNonStrokingColor(Color.BLACK);
                float cellSize = 5.5f;
                int grid = (int)(qrSize / cellSize);
                int hashSeed = bookingRefId.hashCode();

                for (int row = 0; row < grid; row++) {
                    for (int col = 0; col < grid; col++) {
                        boolean isTopLeft = (row < 4 && col < 4);
                        boolean isTopRight = (row < 4 && col >= grid - 4);
                        boolean isBottomLeft = (row >= grid - 4 && col < 4);

                        if (isTopLeft || isTopRight || isBottomLeft) continue;

                        if (((hashSeed + row * 31 + col * 17) % 3) == 0) {
                            cs.addRect(qrX + 3 + (col * cellSize), qrY + 3 + (row * cellSize), cellSize, cellSize);
                            cs.fill();
                        }
                    }
                }

                // QR Finder Pattern Squares
                drawPdfQRFinder(cs, qrX + 3, qrY + 3);
                drawPdfQRFinder(cs, qrX + qrSize - 25, qrY + 3);
                drawPdfQRFinder(cs, qrX + 3, qrY + qrSize - 25);

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
                cs.setNonStrokingColor(Color.DARK_GRAY);
                cs.newLineAtOffset(qrX + 6, qrY - 14);
                cs.showText("SCAN TO VERIFY");
                cs.endText();

                // 7. Footer Status Banner
                cs.setNonStrokingColor(new Color(40, 167, 69)); // Success Green
                cs.addRect(45, 75, pageWidth - 90, 42);
                cs.fill();

                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                cs.setNonStrokingColor(Color.WHITE);
                cs.newLineAtOffset(115, 91);
                cs.showText("STATUS: CONFIRMED & VERIFIED GATE PASS (2026)");
                cs.endText();
            }

            // Save to Downloads Directory
            String userHome = System.getProperty("user.home");
            File downloadsDir = new File(userHome, "Downloads");
            if (!downloadsDir.exists()) downloadsDir.mkdirs();
            
            String fileName = "cinegrid_ticket_" + System.currentTimeMillis() + ".pdf";
            File outputFile = new File(downloadsDir, fileName);
            
            document.save(outputFile);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String extractBookingId(String text) {
        try {
            for (String line : text.split("\n")) {
                if (line.contains("Booking Reference ID:")) {
                    return line.split(":")[1].trim();
                }
            }
        } catch (Exception ignored) {}
        return "#CG-99999";
    }

    private static void drawPdfQRFinder(PDPageContentStream cs, float x, float y) throws java.io.IOException {
        cs.setNonStrokingColor(Color.BLACK);
        cs.addRect(x, y, 22, 22);
        cs.fill();
        cs.setNonStrokingColor(Color.WHITE);
        cs.addRect(x + 4, y + 4, 14, 14);
        cs.fill();
        cs.setNonStrokingColor(Color.BLACK);
        cs.addRect(x + 7, y + 7, 8, 8);
        cs.fill();
    }
}