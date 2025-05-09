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

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Service
@Slf4j
public class ManualFloorUsageService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MonthlyKWHGenerationService monthlyKWHGenerationService;

    @Autowired
    private ReportHistoryService reportHistoryService;

    public byte[] generateManualFloorUsageReport(String fromDate, String toDate) {
        // Convert full dates to YYYY-MM format for monthly report generation
        String fromMonth = fromDate.substring(0, 7);
        String toMonth = toDate.substring(0, 7);

        // Fetch floors and their meter mappings
        List<Map<String, Object>> floors = jdbcTemplate.queryForList("SELECT id, floor_name FROM dbo.floors");
        List<Map<String, Object>> relations = jdbcTemplate.queryForList("SELECT floor_id, name FROM dbo.floor_to_energy_meter_relation");

        Map<Integer, List<String>> meterMap = new HashMap<>();
        for (Map<String, Object> row : relations) {
            int floorId = (int) row.get("floor_id");
            String meter = (String) row.get("name");
            meterMap.computeIfAbsent(floorId, k -> new ArrayList<>()).add(meter);
        }

        List<Map<String, Object>> usageData = new ArrayList<>();
        for (Map<String, Object> floor : floors) {
            int floorId = (int) floor.get("id");
            String floorName = (String) floor.get("floor_name");
            double totalKwh = 0;

            if (meterMap.containsKey(floorId)) {
                List<MonthlyKwhReportDTO> reports = monthlyKWHGenerationService.generateManualKwhReport(
                        meterMap.get(floorId), fromDate, toDate
                );

                for (MonthlyKwhReportDTO report : reports) {
                    totalKwh += report.getMonthlyKwh();
                }
            }

            Map<String, Object> row = new HashMap<>();
            row.put("floor_name", floorName);
            row.put("usage", round(totalKwh));
            usageData.add(row);
        }

        byte[] pdf = generatePdf(fromDate, toDate, usageData);

        // Save PDF to report_history table
        ReportHistoryDTO dto = new ReportHistoryDTO();
        dto.setReport_name("Manual Floor Usage Report - From " + fromDate + " To " + toDate);
        dto.setPeriods(fromDate + " to " + toDate);
        dto.setGenerated_date(LocalDateTime.now());
        dto.setPdf_content(pdf);
        reportHistoryService.addReportHistory(dto);

        return pdf;
    }

    private byte[] generatePdf(String fromDate, String toDate, List<Map<String, Object>> usageData) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

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
//            PdfFont font = PdfFontFactory.createFont("C:/Windows/Fonts/arial.ttf", PdfEncodings.IDENTITY_H);
            document.setFont(font);

            // Title
            document.add(new Paragraph("Floor Usage Report")
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            // Header
            Table singleLine = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginBottom(10);

// Left-aligned "From"
            singleLine.addCell(new Cell()
                    .add(new Paragraph("From: " + fromDate).setFontSize(10))
                    .setTextAlignment(TextAlignment.LEFT)
                    .setBorder(Border.NO_BORDER));

// Right-aligned "To"
            singleLine.addCell(new Cell()
                    .add(new Paragraph("To: " + toDate).setFontSize(10))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER));

            document.add(singleLine);

            document.add(new Paragraph("Generated On: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                    .setFontSize(10).setTextAlignment(TextAlignment.LEFT));

            // Table
            Table table = new Table(UnitValue.createPercentArray(new float[]{4, 2}))
                    .setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell(new Cell().add(new Paragraph("Floor Name"))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));
            table.addHeaderCell(new Cell().add(new Paragraph("Total Usage (kWh)"))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY).setTextAlignment(TextAlignment.CENTER));

            for (Map<String, Object> row : usageData) {
                table.addCell(new Cell().add(new Paragraph((String) row.get("floor_name"))));
                table.addCell(new Cell().add(new Paragraph(String.format("%.2f", row.get("usage"))))
                        .setTextAlignment(TextAlignment.RIGHT));
            }

            document.add(table);
            // Get page width and bottom margin
            float pageWidth = pdf.getDefaultPageSize().getWidth();
            float bottomMargin = 15;  // Space from the bottom of the page
            float rightMargin = 30;  // Space from the right edge

            PdfFont italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            // Create footer paragraph
            Paragraph footer = new Paragraph("Report generated by Neptune Control Pvt Ltd")
                    .setFont(italicFont)  // Set italic font
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.RIGHT) // Align text to the right
                    .setFixedPosition(pageWidth - rightMargin - 180, bottomMargin, 180);  // Adjusted for perfect alignment

            // Add the footer to the document
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
