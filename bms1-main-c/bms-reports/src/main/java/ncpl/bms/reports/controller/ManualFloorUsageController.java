package ncpl.bms.reports.controller;

import ncpl.bms.reports.service.ManualFloorUsageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/manual-floor-usage")
@CrossOrigin(origins = "http://localhost:4200") // update your allowed frontend origin here
public class ManualFloorUsageController {

    @Autowired
    private ManualFloorUsageService manualFloorUsageService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportManualFloorUsageReport(@RequestParam String fromDate,
                                                               @RequestParam String toDate) {
        byte[] pdf = manualFloorUsageService.generateManualFloorUsageReport(fromDate, toDate);

        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=floor_usage_" + fromDate + "_to_" + toDate + ".pdf")
                .body(pdf);
    }
}
