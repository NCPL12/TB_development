    package ncpl.bms.reports.service;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.mail.javamail.JavaMailSender;
    import org.springframework.mail.javamail.MimeMessageHelper;
    import org.springframework.stereotype.Service;
    import org.springframework.core.io.ByteArrayResource;
    import org.springframework.core.io.InputStreamSource;

    import jakarta.mail.MessagingException;
    import jakarta.mail.internet.MimeMessage;

    @Service
    public class EmailService {

        @Autowired
        private JavaMailSender mailSender;

        public void sendBillEmail(String to, String subject, String text, byte[] pdfContent, String fileName) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(text);

                // ✅ Correctly attach PDF without requiring javax.activation.DataSource
                InputStreamSource attachment = new ByteArrayResource(pdfContent);
                helper.addAttachment(fileName, attachment, "application/pdf");

                mailSender.send(message);
                System.out.println("✅ Email sent successfully to: " + to);
            } catch (MessagingException e) {
                System.err.println("❌ Failed to send email to " + to + ": " + e.getMessage());
            }
        }
        public void sendReportEmail(String to, String subject, String body, byte[] attachment, String filename) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true);

                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(body);
                helper.addAttachment(filename, new ByteArrayResource(attachment));

                mailSender.send(message);
            } catch (Exception e) {
//                log.error("❌ Failed to send report email to {}: {}", to, e.getMessage(), e);
            }
        }

    }
