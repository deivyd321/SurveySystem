import sun.net.smtp.SmtpClient;
import sun.security.util.Password;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * Created by arturas on 2017-04-04.
 */
@Stateless
public class EmailSession
{
    private String port = "587";
    private String host = "mail.inbox.lt";
    private String from = "surveysystem@inbox.lt";
    private String username = "surveysystem";
    private String password = "apklausa";

    public void sendInvitation(String to, String link)
    {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator()
                {
                    protected PasswordAuthentication getPasswordAuthentication()
                    {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try
        {
            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));

            // Set Subject: header field
            message.setSubject("Registracijos į apklausų sistemą užbaigimas");

            // Now set the actual message
            message.setText("Norėdami prisiregistruoti, spauskite šią nuorodą: " + link);

            // Send message
            Transport.send(message);

            System.out.println("Sent message successfully....");

        } catch (MessagingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
