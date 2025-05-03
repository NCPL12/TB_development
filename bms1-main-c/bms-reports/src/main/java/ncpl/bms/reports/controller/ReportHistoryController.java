package ncpl.bms.reports.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ncpl.bms.reports.model.dto.ReportHistoryDTO;
import ncpl.bms.reports.service.ReportHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("v1")
@CrossOrigin(origins = "http://localhost:4200")
public class ReportHistoryController {

    @Autowired
    private ReportHistoryService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(value = "/add-report-history", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addReportHistory(
            @RequestPart("reportHistory") String reportJson,
            @RequestPart("file") MultipartFile file) {
        try {
            ReportHistoryDTO report = objectMapper.readValue(reportJson, ReportHistoryDTO.class);

            if (file != null && !file.isEmpty()) {
                report.setPdf_content(file.getBytes());
            }

            ReportHistoryDTO saved = reportService.addReportHistory(report);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/all-report-histories")
    public ResponseEntity<List<ReportHistoryDTO>> getAllReportHistories() {
        try {
            return ResponseEntity.ok(reportService.getAllReportHistories());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/report-history-by-id")
    public ResponseEntity<ReportHistoryDTO> getReportHistoryById(@RequestParam int id) {
        try {
            return ResponseEntity.ok(reportService.getReportHistoryById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/report-history-pdf/{id}")
    public ResponseEntity<ByteArrayResource> getReportPdf(@PathVariable int id) {
        try {
            byte[] pdfBytes = reportService.getReportPdfById(id);
            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=report-history-" + id + ".pdf");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(pdfBytes.length)
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
