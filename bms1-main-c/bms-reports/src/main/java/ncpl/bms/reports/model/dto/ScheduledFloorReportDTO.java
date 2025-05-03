package ncpl.bms.reports.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledFloorReportDTO {
    private int id;
    private String reportName;
    private String type;
    private String periods;
    private LocalDateTime generatedDate;
}
