package ncpl.bms.reports.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ncpl.bms.reports.service.FloorReportService;

@RestController
@RequestMapping("v1/floor-reports")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class FloorReportController {

    @Autowired
    private FloorReportService floorReportService;

    @GetMapping("/export-monthly-report")
    public ResponseEntity<byte[]> exportMonthlyReport(@RequestParam("month") String month,
                                                      @RequestParam("year") String year) {
        byte[] pdfContent = floorReportService.generateMonthlyReportPdf(month, year);
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=floor_report.pdf")
                .body(pdfContent);
    }
}
