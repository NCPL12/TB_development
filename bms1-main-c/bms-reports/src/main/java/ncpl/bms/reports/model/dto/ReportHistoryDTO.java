package ncpl.bms.reports.model.dto;

import java.time.LocalDateTime;

public class ReportHistoryDTO {
    private int id;
    private String report_name;
    private LocalDateTime generated_date;
    private String periods;
    private byte[] pdf_content;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReport_name() { return report_name; }
    public void setReport_name(String report_name) { this.report_name = report_name; }

    public LocalDateTime getGenerated_date() { return generated_date; }
    public void setGenerated_date(LocalDateTime generated_date) { this.generated_date = generated_date; }

    public String getPeriods() { return periods; }
    public void setPeriods(String periods) { this.periods = periods; }

    public byte[] getPdf_content() { return pdf_content; }
    public void setPdf_content(byte[] pdf_content) { this.pdf_content = pdf_content; }
}
