package ncpl.bms.reports.controller;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import ncpl.bms.reports.model.dto.ScheduledFloorReportDTO;
import ncpl.bms.reports.service.ScheduledFloorReportService;
import ncpl.bms.reports.service.ReportHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/scheduled-floor-reports")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class ScheduledFloorReportController {

    @Autowired
    private ScheduledFloorReportService scheduledFloorReportService;


    // ✅ Trigger report generation manually
    @PostMapping("/generate")
    public ResponseEntity<String> generateFloorReportManually() {
        try {
            scheduledFloorReportService.generateAndProcessMonthlyFloorReport();
            return ResponseEntity.ok("Floor report generation triggered successfully.");
        } catch (Exception e) {
            log.error("❌ Error during manual report generation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    // ✅ Get all floor report metadata
    @GetMapping("/report-all")
    public ResponseEntity<List<ScheduledFloorReportDTO>> getAllFloorReports() {
        List<ScheduledFloorReportDTO> reports = scheduledFloorReportService.getAllScheduledFloorReports();
        return ResponseEntity.ok(reports);
    }

    // ✅ Preview PDF inline in browser
    @GetMapping("/{id}/pdf-view")
    public void viewPdfInBrowser(@PathVariable int id, HttpServletResponse response) {
        try {
            byte[] pdfContent = scheduledFloorReportService.getReportPdfById(id);


            if (pdfContent == null || pdfContent.length == 0) {
                log.warn("⚠️ PDF content not found for ID: {}", id);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=floor-report-" + id + ".pdf");
            response.setContentLength(pdfContent.length);

            try (ServletOutputStream out = response.getOutputStream()) {
                out.write(pdfContent);
                out.flush();
            }
        } catch (Exception e) {
            log.error("❌ Error while viewing PDF in browser for ID {}: {}", id, e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // ✅ Download PDF as file
    @GetMapping("/{id}/pdf-download")
    public ResponseEntity<byte[]> downloadFloorReportPdf(@PathVariable int id) {
        try {
            byte[] pdfContent = scheduledFloorReportService.getReportPdfById(id);
            if (pdfContent == null || pdfContent.length == 0) {
                log.warn("⚠️ PDF content not found for download. ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=floor-report-" + id + ".pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfContent);
        } catch (Exception e) {
            log.error("❌ Error while downloading PDF for ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
