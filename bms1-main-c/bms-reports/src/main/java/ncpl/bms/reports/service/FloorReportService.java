package ncpl.bms.reports.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import ncpl.bms.reports.model.dto.MonthlyKwhReportDTO;
import ncpl.bms.reports.model.dto.ReportHistoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
@Slf4j
public class FloorReportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ReportHistoryService reportHistoryService;

    @Autowired
    private MonthlyKWHGenerationService monthlyKWHGenerationService;

    public byte[] generateMonthlyReportPdf(String month, String year) {
        String monthYear = year + "-" + month;
        log.info("Generating Floor Monthly kWh Report for {}", monthYear);

        // Fetch floors
        String floorSql = "SELECT id, floor_name FROM dbo.floors";
        List<Map<String, Object>> floors = jdbcTemplate.queryForList(floorSql);

        // Fetch floor-to-meter mapping
        String relationSql = "SELECT floor_id, name FROM dbo.floor_to_energy_meter_relation";
        List<Map<String, Object>> relations = jdbcTemplate.queryForList(relationSql);

        // Map floors to their energy meters
        Map<Integer, List<String>> floorMeterMapping = new HashMap<>();
        for (Map<String, Object> relation : relations) {
            int floorId = (int) relation.get("floor_id");
            String meterTable = (String) relation.get("name");
            floorMeterMapping.computeIfAbsent(floorId, k -> new ArrayList<>()).add(meterTable);
        }

        // Compute monthly kWh usage per floor
        List<Map<String, Object>> reportData = new ArrayList<>();
        for (Map<String, Object> floor : floors) {
            int floorId = (int) floor.get("id");
            String floorName = (String) floor.get("floor_name");
            double totalMonthlyUsage = 0.0;

            if (floorMeterMapping.containsKey(floorId)) {
                List<String> meterTables = floorMeterMapping.get(floorId);
                List<MonthlyKwhReportDTO> kwhReports = monthlyKWHGenerationService.generateMonthlyKwhReport(meterTables, monthYear, monthYear);

                for (MonthlyKwhReportDTO report : kwhReports) {
                    totalMonthlyUsage += report.getMonthlyKwh();
                }
            }

            Map<String, Object> floorReport = new HashMap<>();
            floorReport.put("floor_id", floorId);
            floorReport.put("floor_name", floorName);
            floorReport.put("monthly_usage", totalMonthlyUsage);
            reportData.add(floorReport);
        }

        // 1. Generate PDF
        byte[] pdfBytes = generatePdf(monthYear, reportData);

        // 2. Save to report_history
        ReportHistoryDTO dto = new ReportHistoryDTO();
        dto.setReport_name("Monthly Floor kWh Report - " + monthYear);  // <-- Dynamically set report name
        dto.setGenerated_date(LocalDateTime.now());
        dto.setPeriods(monthYear);
        dto.setPdf_content(pdfBytes);

        reportHistoryService.addReportHistory(dto);
        return pdfBytes;
    }

    private byte[] generatePdf(String monthYear, List<Map<String, Object>> reportData) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Company Name
            Paragraph companyName = new Paragraph("Vajram CMR One")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(10)
                    .setFixedPosition(30, pdf.getDefaultPageSize().getHeight() - 40, 200);
            document.add(companyName);

            PdfFont font = PdfFontFactory.createFont("C:/Windows/Fonts/arial.ttf", PdfEncodings.IDENTITY_H);
            document.setFont(font);

            // Load and add logo
            try {
                ImageData logoData = ImageDataFactory.create(new ClassPathResource("static/images/logo1.png").getURL());
                Image logo = new Image(logoData);
                logo.scaleToFit(100, 100);
                float pageWidth = pdf.getDefaultPageSize().getWidth();
                float pageHeight = pdf.getDefaultPageSize().getHeight();
                logo.setFixedPosition(pageWidth - logo.getImageScaledWidth() - 20, pageHeight - logo.getImageScaledHeight() - 10);
                document.add(logo);
            } catch (Exception e) {
                throw new RuntimeException("Error loading logo image", e);
            }

            // Title
            document.add(new Paragraph("Floor Monthly kWh Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(20));

            // Format month-year
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM-yyyy", Locale.ENGLISH);
            String formattedMonthYear = LocalDate.of(Integer.parseInt(monthYear.split("-")[0]), Integer.parseInt(monthYear.split("-")[1]), 1).format(monthFormatter);

            // Format generated date
            DateTimeFormatter generatedDateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedGeneratedOn = LocalDate.now().format(generatedDateFormatter);

            // Header Table
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth()
                    .setMarginTop(20)
                    .setMarginBottom(10)
                    .setBorder(Border.NO_BORDER);
            headerTable.addCell(new Cell().add(new Paragraph("Month-Year: " + formattedMonthYear)).setTextAlignment(TextAlignment.LEFT).setBorder(Border.NO_BORDER));
            headerTable.addCell(new Cell().add(new Paragraph("Generated on: " + formattedGeneratedOn)).setTextAlignment(TextAlignment.RIGHT).setBorder(Border.NO_BORDER));
            document.add(headerTable);

            // Report Table
            Table reportTable = new Table(UnitValue.createPercentArray(new float[]{6, 3}))
                    .setWidth(UnitValue.createPercentValue(100));

            reportTable.addHeaderCell(new Cell().add(new Paragraph("Floor Name")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));
            reportTable.addHeaderCell(new Cell().add(new Paragraph("Monthly Usage (kWh)")).setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));

            // Populate table data
            for (Map<String, Object> floorData : reportData) {
                reportTable.addCell(new Cell().add(new Paragraph((String) floorData.get("floor_name"))).setTextAlignment(TextAlignment.LEFT));
                reportTable.addCell(new Cell().add(new Paragraph(String.format("%.2f", (double) floorData.get("monthly_usage")))).setTextAlignment(TextAlignment.RIGHT));
            }
            document.add(reportTable);
            // Get page width and bottom margin
            float pageWidth = pdf.getDefaultPageSize().getWidth();
            float bottomMargin = 15;  // Space from the bottom of the page
            float rightMargin = 30;  // Space from the right edge

            PdfFont italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            // Create footer paragraph
            Paragraph footer = new Paragraph("Bill generated by Neptune Control Pvt Ltd")
                    .setFont(italicFont)  // Set italic font
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.RIGHT) // Align text to the right
                    .setFixedPosition(pageWidth - rightMargin - 180, bottomMargin, 180);  // Adjusted for perfect alignment

            // Add the footer to the document
            document.add(footer);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF: {}", e.getMessage());
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}
