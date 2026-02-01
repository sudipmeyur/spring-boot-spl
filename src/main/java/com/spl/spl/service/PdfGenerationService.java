package com.spl.spl.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.spl.spl.entity.PlayerTeam;
import com.spl.spl.entity.TeamSeason;
import com.spl.spl.entity.TeamSeasonPlayerLevel;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    public byte[] generateTeamSquadPdf(TeamSeason teamSeason) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Header
            document.add(new Paragraph(teamSeason.getTeam().getName())
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("Season: " + teamSeason.getSeason().getCode() + 
                    " | Budget: " + formatAmount(teamSeason.getSeason().getBudgetLimit()) +
                    " | Spent: " + formatAmount(teamSeason.getTotalAmountSpent()))
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));

            // Manager Section
            PlayerTeam manager = teamSeason.getPlayerTeams().stream()
                    .filter(PlayerTeam::getIsManager)
                    .findFirst()
                    .orElse(null);

            if (manager != null) {
                document.add(new Paragraph("\nTeam Manager").setFontSize(14).setBold());
                Table managerTable = new Table(UnitValue.createPercentArray(new float[]{3, 2}));
                managerTable.setWidth(UnitValue.createPercentValue(100));
                
                managerTable.addHeaderCell(new Cell().add(new Paragraph("Name").setBold()));
                managerTable.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));
                
                managerTable.addCell(manager.getPlayer().getName());
                managerTable.addCell(formatAmount(manager.getSoldAmount()));
                
                document.add(managerTable);
            }

            // Players by Level
            for (TeamSeasonPlayerLevel level : teamSeason.getTeamSeasonPlayerLevels()) {
                List<PlayerTeam> levelPlayers = teamSeason.getPlayerTeams().stream()
                        .filter(pt -> !pt.getIsManager() && 
                                pt.getPlayer().getPlayerLevel().getCode().equals(level.getPlayerLevel().getCode()))
                        .toList();

                if (!levelPlayers.isEmpty()) {
                    document.add(new Paragraph("\n" + level.getPlayerLevel().getCode().toUpperCase() + 
                            " Level Players (Total: " + formatAmount(level.getTotalAmountSpent()) + ")")
                            .setFontSize(14).setBold());

                    Table playerTable = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2}));
                    playerTable.setWidth(UnitValue.createPercentValue(100));
                    
                    playerTable.addHeaderCell(new Cell().add(new Paragraph("Player Name").setBold()));
                    playerTable.addHeaderCell(new Cell().add(new Paragraph("Category").setBold()));
                    playerTable.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));

                    for (PlayerTeam playerTeam : levelPlayers) {
                        playerTable.addCell(playerTeam.getPlayer().getName());
                        playerTable.addCell(playerTeam.getPlayer().getCategory() != null ? 
                                playerTeam.getPlayer().getCategory().getName() : "N/A");
                        playerTable.addCell(formatAmount(playerTeam.getSoldAmount()));
                    }
                    
                    document.add(playerTable);
                }
            }

            // Grand Total
            document.add(new Paragraph("\nGrand Total: " + formatAmount(teamSeason.getTotalAmountSpent()))
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "₹0";
        
        long value = amount.longValue();
        if (value >= 10000000) {
            return "₹" + String.format("%.1f", value / 10000000.0) + "Cr";
        } else if (value >= 100000) {
            return "₹" + String.format("%.1f", value / 100000.0) + "L";
        } else {
            return "₹" + String.format("%,d", value);
        }
    }
}