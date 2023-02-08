package pl.edu.anstar;

import java.util.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mail {

//    195.150.230.215
//    powiadomienia_erk
//    powiadomieniaERK#@!
    private static Logger LOG = LoggerFactory.getLogger(Mail.class);

    private final static String CONTENT_TYPE = "text/html; charset=UTF-8";
    private final static String SMTP_HOST = "";
    private final static String USER = "";
    private final static String PASSWORD = "";
    private final static String SENDER = "\"Akademia Tarnowska\" <powiadomienia_erk@pwsztar.edu.pl>";
    private final static String COPY_RECEIPENT = "powiadomienia.erk@pwsztar.edu.pl";
    private String smtpHost = SMTP_HOST;
    private String user = USER;
    private String password = PASSWORD;
    private String sender = SENDER;
    private String receiver = null;
    private String copyReceiver = null;
    private String hiddenReceiver = COPY_RECEIPENT;
    private String subject = null;
    private String content = null;
    private String affixPath = null;

    public void smtp() throws MessagingException {
        if (smtpHost == null) {
            throw new MessagingException("Server SMTP not found!");
        }
        if (user == null) {
            throw new MessagingException("No account!");
        }
        if (password == null) {
            throw new MessagingException("Incorrect password!");
        }
        if (receiver == null && copyReceiver == null && hiddenReceiver == null) {
            throw new MessagingException("Incorrect recipient!");
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(properties, new Authenticator() {

            @Override public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        MimeMessage mimeMsg = new MimeMessage(session);

        if (sender != null) {
            mimeMsg.setFrom(new InternetAddress(sender));
        }
        if (receiver != null) {
            mimeMsg.setRecipients(Message.RecipientType.TO, parse(receiver));
        }
        if (copyReceiver != null) {
            mimeMsg.setRecipients(Message.RecipientType.CC, parse(copyReceiver));
        }
        if (hiddenReceiver != null) {
            mimeMsg.setRecipients(Message.RecipientType.BCC, parse(hiddenReceiver));
        }
        if (subject != null) {
            mimeMsg.setSubject(subject, "UTF-8");
        }

        MimeBodyPart part = new MimeBodyPart();
        part.setText(content == null ? "" : content, "UTF-8");
        part.setHeader("Content-Type", CONTENT_TYPE);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(part);

        if (affixPath != null) {
            ArrayList parts = parsePath(affixPath);
            for (int i = 0; i < parts.size(); i++) {
                multipart.addBodyPart((MimeBodyPart) parts.get(i));
            }
        }

        mimeMsg.setContent(multipart);
        mimeMsg.setSentDate(new Date());

        LOG.info("Mail transport started.");
        try {
            Transport.send(mimeMsg);
        } catch (Exception e) {
            LOG.error("Mail transport failed. Exception: {}", e);
            throw new MessagingException("Mail transport failed.");
        }
        LOG.info("Mail transport ended.");
    }

    private InternetAddress[] parse(String addressSet) throws AddressException {
        ArrayList list = new ArrayList();

        StringTokenizer tokens = new StringTokenizer(addressSet, ";");
        while (tokens.hasMoreTokens()) {
            list.add(new InternetAddress(tokens.nextToken().trim()));
        }

        InternetAddress[] addressArray = new InternetAddress[list.size()];
        list.toArray(addressArray);
        return addressArray;
    }

    private ArrayList parsePath(String affixPath) throws MessagingException {
        ArrayList list = new ArrayList();

        StringTokenizer tokens = new StringTokenizer(affixPath, ";");
        while (tokens.hasMoreTokens()) {
            MimeBodyPart part = new MimeBodyPart();
            FileDataSource source = new FileDataSource(tokens.nextToken().trim());
            part.setDataHandler(new DataHandler(source));
            part.setFileName(source.getName());
            list.add(part);
        }
        return list;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setCopyReceiver(String receiver) {
        this.copyReceiver = receiver;
    }

    public String getCopyReceiver() {
        return copyReceiver;
    }

    public void setHiddenReceiver(String receiver) {
        this.hiddenReceiver = receiver;
    }

    public String getHiddenReceiver() {
        return hiddenReceiver;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setAffixPath(String affixPath) {
        this.affixPath = affixPath;
    }

    public String getAffixPath() {
        return affixPath;
    }
}