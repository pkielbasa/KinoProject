package pl.edu.anstar;

import javax.mail.*;
import java.util.Hashtable;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailTemplate {

    private static Logger LOG = LoggerFactory.getLogger(MailTemplate.class);

    static String contentHeader =
            "<html><head>" +
                    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" +
                    "<style type=\"text/css\">" +
                    "<!--" +
                    "   body { font-family: arial, helvetica, sans-serif; font-size: 10pt; color: black; background: white;}" +
                    "  .header { border-bottom: 2px solid #515151; width: 100%; font-weight: bold; font-size: 11pt; font-family: verdana, arial, sans-serif; white-space: nowrap; color: #515151;}" +
                    "  .title { border-bottom: 1px solid #515151; width: 100%; font-size: 8pt; font-family: verdana, arial, sans-serif; white-space: nowrap; color: #515151;}" +
                    "  .footer { font-size: 8pt; font-family: verdana, arial, sans-serif; white-space: nowrap; color: #515151;}" +
                    "  .content1 { margin-top: 0; margin-bottom: 0; width: 100%; background: white;}" +
                    "  .content2 { margin-top: 0; margin-bottom: 0; width: 100%; background: #f6f5f3;}" +
                    "-->" +
                    "</style></head>" +
                    "<body><div class=\"header\">Szanowna Pani!<br />Szanowny Panie!</div>";
    static String contentFooter ="</body></html>";

    private int mailTemplate;
    private String recipientName;
    private String recipientEmail;
    private Hashtable parameters;
    private int id;

    public MailTemplate() {
    }

    public MailTemplate(int mailTemplate, String recipientName, String recipientEmail, Hashtable parameters, int id) {
        this.mailTemplate = mailTemplate;
        this.recipientName = recipientName;
        this.recipientEmail = recipientEmail;
        this.parameters = parameters;
        this.id = id;
    }

    public boolean sendMail() throws Exception {

        Mail mail2Send = new Mail();
        String subject = "";
        String contentBody = "";

        if (mailTemplate == 0) {

            subject = "Aplikowanie w systemie {" + id + "}";
            contentBody =
                    "<P>Aplikacja złożona do Akademii Tarnowskiej im. Hetmana Wielkiego Koronnego Jana Amora Tarnowskiego.</P>";
        }
        String content = contentHeader + contentBody + contentFooter;

        mail2Send.setSubject(subject);
        mail2Send.setContent(content);

        String recipientNameEncoded = "";
        try {
            recipientNameEncoded = MimeUtility.encodeText(recipientName, "UTF-8", "Q");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported encoding.");
            return false;
        }

        mail2Send.setReceiver(recipientNameEncoded + " <" + recipientEmail + ">");

        try {
            LOG.info("Sending mail started.");
            mail2Send.smtp();
            LOG.info("Sending mail ended.");
            return true;
        } catch (MessagingException me) {
            LOG.error("Exception: {}", me);
            throw new MessagingException("Mail MessagingException.");
        } catch (Exception e) {
            LOG.error("Exception: {}", e);
            throw new Exception("Mail Exception.");
        }
    }

    public int getMailTemplate() {
        return mailTemplate;
    }

    public void setMailTemplate(int mailTemplate) {
        this.mailTemplate = mailTemplate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Hashtable getParameters() {
        return parameters;
    }

    public void setParameters(Hashtable parameters) {
        this.parameters = parameters;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }
}