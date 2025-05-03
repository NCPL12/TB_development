package ncpl.bms.reports.service;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import ncpl.bms.reports.model.dto.MonthlyKwhReportDTO;
import ncpl.bms.reports.model.dto.ReportHistoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
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
            Document doc = new Document(pdf);

            PdfFont font = PdfFontFactory.createFont("C:/Windows/Fonts/arial.ttf", PdfEncodings.IDENTITY_H);
            doc.setFont(font);

            // Title
            doc.add(new Paragraph("Floor Usage Report")
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            // Header
            doc.add(new Paragraph("From: " + fromDate + "    To: " + toDate)
                    .setFontSize(10).setTextAlignment(TextAlignment.LEFT));
            doc.add(new Paragraph("Generated On: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
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

            doc.add(table);
            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
