package ncpl.bms.reports.service;

import ncpl.bms.reports.model.dto.ReportHistoryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportHistoryService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public ReportHistoryDTO addReportHistory(ReportHistoryDTO report) {
        String sql = "INSERT INTO report_history " +
                "(report_name, generated_date, periods, pdf_content) " +
                "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                report.getReport_name(),
                report.getGenerated_date(),
                report.getPeriods(),
                report.getPdf_content());
        return report;
    }

    public ReportHistoryDTO getReportHistoryById(int id) {
        String sql = "SELECT * FROM report_history WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                ReportHistoryDTO r = new ReportHistoryDTO();
                r.setId(rs.getInt("id"));
                r.setReport_name(rs.getString("report_name"));
                r.setGenerated_date(rs.getTimestamp("generated_date").toLocalDateTime());
                r.setPeriods(rs.getString("periods"));
                r.setPdf_content(rs.getBytes("pdf_content"));
                return r;
            }, id);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Report not found with ID: " + id);
        }
    }

    public byte[] getReportPdfById(int id) {
        String sql = "SELECT pdf_content FROM report_history WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> rs.getBytes("pdf_content"), id);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("PDF not found for report ID: " + id);
        }
    }

    public List<ReportHistoryDTO> getAllReportHistories() {
        String sql = "SELECT id, report_name, generated_date, periods FROM report_history ORDER BY generated_date DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReportHistoryDTO r = new ReportHistoryDTO();
            r.setId(rs.getInt("id"));
            r.setReport_name(rs.getString("report_name"));
            r.setGenerated_date(rs.getTimestamp("generated_date").toLocalDateTime());
            r.setPeriods(rs.getString("periods"));
            return r;
        });
    }
}
