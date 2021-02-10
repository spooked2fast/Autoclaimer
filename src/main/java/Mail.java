import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Mail {
    private Settings settings;
    public Mail(Settings settings){
        this.settings = settings;
    }
    public void createMsg(String credentials, String gamertag){
        String to = settings.getRecipientEmail();
        String from = settings.getSenderEmail();
        String host = "smtp.gmail.com";
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(settings.getSenderEmail(), settings.getGmailAppPassword());
            }

        });
        session.setDebug(false);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Autoclaimed GT: " + gamertag);
            message.setText(gamertag + " | " + credentials);
            Transport.send(message);
        } catch (Exception mex) {
        }

    }
    public void alertUnauth(String ip){
        String to = settings.getRecipientEmail();
        String from = settings.getSenderEmail();
        String host = "smtp.gmail.com";
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(settings.getSenderEmail(), settings.getGmailAppPassword());
            }

        });
        session.setDebug(false);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Unauthorized Xbox Autoclaimer User Alert");
            message.setText("Someone has unauthorized access to Xbox Autoclaimer | IP: "  + ip);
            Transport.send(message);
        } catch (Exception mex) {
        }
    }
}

