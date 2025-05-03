package ncpl.bms.reports.service;

import lombok.extern.slf4j.Slf4j;
import ncpl.bms.reports.model.dto.ScheduledFloorReportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
public class ScheduledFloorReportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FloorReportService floorReportService;

    @Autowired
    private EmailService emailService;


    public void generateAndProcessMonthlyFloorReport() {
        boolean isScheduled = isFloorReportScheduled();
        boolean isSendMail = isSendMailEnabled();

        if (!isScheduled && !isSendMail) {
            log.info("❌ Neither scheduling nor email enabled. Skipping floor report generation.");
            return;
        }

        log.info("✅ Starting floor report generation...");

        LocalDate previousMonth = LocalDate.now().minusMonths(1);
        String month = String.format("%02d", previousMonth.getMonthValue());
        String year = String.valueOf(previousMonth.getYear());
        String monthYear = previousMonth.format(DateTimeFormatter.ofPattern("MMMM-yyyy"));

        try {
            byte[] pdfContent = floorReportService.generateMonthlyReportPdf(month, year);

            if (isScheduled) {
                storeFloorReportInDatabase(monthYear, pdfContent);
            }

            if (isSendMail) {
                String email = getBuildingEmail();
                if (email != null && !email.isEmpty()) {
                    sendReportByEmail(email, monthYear, pdfContent);
                } else {
                    log.warn("⚠️ Email not found for building.");
                }
            }

        } catch (Exception e) {
            log.error("❌ Error generating floor report for {}: {}", monthYear, e.getMessage(), e);
        }
    }

    private void storeFloorReportInDatabase(String billPeriod, byte[] pdfContent) {
        String insertSql = "INSERT INTO kwh_data_ai_generated.dbo.scheduled_report " +
                "(report_name, type, periods, pdf_content, generated_date) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(insertSql,
                "Monthly Floor kWh Report - " + billPeriod,
                "FLOOR_KWH",
                billPeriod,
                pdfContent,
                LocalDateTime.now());

        log.info("✅ Floor report stored in DB for period: {}", billPeriod);
    }

    private boolean isFloorReportScheduled() {
        String sql = "SELECT schedule FROM building_details WHERE id = 1";
        try {
            Integer status = jdbcTemplate.queryForObject(sql, Integer.class);
            return status != null && status == 1;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private boolean isSendMailEnabled() {
        String sql = "SELECT send_mail FROM building_details WHERE id = 1";
        try {
            Integer status = jdbcTemplate.queryForObject(sql, Integer.class);
            return status != null && status == 1;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    private String getBuildingEmail() {
        String sql = "SELECT email FROM building_details WHERE id = 1";
        try {
            return jdbcTemplate.queryForObject(sql, String.class);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private void sendReportByEmail(String email, String billPeriod, byte[] pdfContent) {
        String subject = "Monthly Floor kWh Report - " + billPeriod;
        String body = "Dear Valued Tenant,\n\n"
                + "Greetings of the day!\n\n"
                + "We hope this email finds you well.\n"
                + "Attached is your monthly floor kWh report for the period of " + billPeriod + ".\n\n"
                + "Best regards,\n"
                + "Billing Team";

        emailService.sendReportEmail(
                email,
                subject,
                body,
                pdfContent,
                "Floor_Report_" + billPeriod + ".pdf"
        );

        log.info("✅ Email sent to building: {}", email);
    }

    @Scheduled(cron = "0 * * * * ?") // Every 1st of the month at 7 AM
    public void scheduleMonthlyFloorReport() {
        generateAndProcessMonthlyFloorReport();
    }

    public byte[] getReportPdfById(int id) {
        String sql = "SELECT pdf_content FROM kwh_data_ai_generated.dbo.scheduled_report WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, byte[].class);
        } catch (EmptyResultDataAccessException e) {
            log.warn("⚠️ No PDF report found for ID {}", id);
            return null;
        } catch (Exception e) {
            log.error("❌ Error retrieving report PDF by ID {}: {}", id, e.getMessage(), e);
            return null;
        }
    }

    public List<ScheduledFloorReportDTO> getAllScheduledFloorReports() {
        String sql = "SELECT id, report_name, type, periods, generated_date FROM scheduled_report WHERE type = 'FLOOR_KWH' ORDER BY generated_date DESC";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(ScheduledFloorReportDTO.class));
    }
}
