package pl.edu.anstar;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@Service
@Component
public class ReservationAdapter {

    private static final String email_sender = "kkinoo188@gmail.com";

    private static Logger LOG = LoggerFactory.getLogger(ReservationAdapter.class);

    private final JavaMailSender javaMailSender;

    public ReservationAdapter(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @JobWorker(type = "requestClientRegistration")
    public Map<String, Object> addToDatabase(final JobClient client, final ActivatedJob job1) {
        HashMap<String, Object> jobResultVariables1 = new HashMap<>();
        Reservation reservation =
                Reservation.builder()
                    .first_name((String) job1.getVariablesAsMap().get("firstname"))
                    .last_name((String)job1.getVariablesAsMap().get("lastname"))
                    .e_mail((String)job1.getVariablesAsMap().get("email"))
                    .phone_number((String) job1.getVariablesAsMap().get("phone"))
                    .movie((String) job1.getVariablesAsMap().get("movie"))
                     .town((String) job1.getVariablesAsMap().get("town"))
                    .date((String) job1.getVariablesAsMap().get("date"))
                    .time((String) job1.getVariablesAsMap().get("time"))
                    .build();
        int applicationId = Database.addClients(reservation);
        if( applicationId == 1 ) {
            jobResultVariables1.put("decision", true);
            jobResultVariables1.put("mail_data", null);
            jobResultVariables1.put("mail_message", null);
        } else {
            jobResultVariables1.put("mail_data", reservation);
            jobResultVariables1.put("mail_message", 3);
            jobResultVariables1.put("decision", false);
        }

        return jobResultVariables1;
    }


    @JobWorker(type = "registerClient")
    public Map<String, Object> isTicketAvailable(final JobClient client, final ActivatedJob job1) {
        HashMap<String, Object> jobResultVariables1 = new HashMap<>();
        LOG.info("Queue is being check for pending tickets to register");
        int result;
        // My code here
        ArrayList<PendingTicketEntity> pendingTickets = Database.fetchQueuedTickets();
        if( pendingTickets.size() == 0 ){
            LOG.info("Queue is empty");
            jobResultVariables1.put("register",1);
            jobResultVariables1.put("mail_data", null);
            jobResultVariables1.put("mail_message", null);
            return jobResultVariables1;
        } else {
            PendingTicketEntity ticket =  pendingTickets.get(0);
            ApprovedTicketDto ticketToApprove =
                    ApprovedTicketDto.builder()
                        .id(ticket.getReservation_id())
                        .first_name(ticket.getFirst_name())
                        .last_name(ticket.getLast_name())
                        .e_mail(ticket.getE_mail())
                        .phone_number(ticket.getPhone_number())
                        .movie(ticket.getMovie())
                        .town(ticket.getTown())
                        .date(ticket.getDate())
                        .time(ticket.getTime())
                        .build();
            result = Database.AddApprovedTicket(
                    ticketToApprove
            );

            if( result == 1 ) {
                LOG.info("registration approved");
                jobResultVariables1.put("mail_data",ticketToApprove);
                jobResultVariables1.put("mail_message",1);
                jobResultVariables1.put("register", 2);
            } else {
                LOG.info("registration not approved");
                jobResultVariables1.put("mail_data",ticketToApprove);
                jobResultVariables1.put("mail_message",2);
                jobResultVariables1.put("register", 3);
            }

        }

        return jobResultVariables1;
    }

    @JobWorker(type = "sendPositiveMail")
    public Map<String, Object> sendPositiveMail(final JobClient client, final ActivatedJob job1) {
        HashMap<String, Object> jobResultVariables1 = new HashMap<>();

        LOG.info("Job registerApplication is started.");
        final Map<String, Object> jobVariables1 = job1.getVariablesAsMap();
        for (Map.Entry<String, Object> entry : jobVariables1.entrySet()) {
            LOG.info("Job variable (process variable & inputed variable): " + entry.getKey() + " : " + entry.getValue());
        }

        LOG.info("To jest zmienna dla maila: " + jobVariables1.get("mail_data"));
        LOG.info("To jest zmienna dla maila: " + jobVariables1.get("mail_message"));

        LinkedHashMap mail_data = (LinkedHashMap)jobVariables1.get("mail_data");
        MimeMessage message2Send = javaMailSender.createMimeMessage();
        try{
            String message = "<html>\n" +
                    "<head>\n" +
                    "<style>\n" +
                    "body {font-weight: bold;}\n" +
                    "</style>\n" +
                    "</head>\n" +
                    "<body>\n" +
                    "\n" +
                    "<h1 style=\"color:orange;\">Potwierdzenie rezerwacji biletu do kina</h1>\n" +
                    "<p>Rezerwacja biletu na imie i nazwisko " + mail_data.get("first_name") + " " + mail_data.get("last_name") +" została pomyślnie przeprowadzona.</p>\n" +
                    "<p>Numer telefonu: "+ mail_data.get("phone_number") + "</p>\n" +
                    "<p>Film: "+ mail_data.get("movie") + "</p>\n" +
                    "<p>Miejsce: "+ mail_data.get("town") + "</p>\n" +
                    "<p>Data: " + mail_data.get("date") +"</p>\n" +
                    "<p>Godzina: " + mail_data.get("time") +"</p>\n" +
                    "\n" +
                    "</body>\n" +
                    "</html>";

            message2Send.setFrom(new InternetAddress(ReservationAdapter.email_sender));
            message2Send.setRecipient(Message.RecipientType.TO, new InternetAddress((String)mail_data.get("e_mail")));
            message2Send.setSubject("Potwierdzenie rezerwacji biletu");
            message2Send.setContent(message, "text/html; charset=utf-8");
            System.out.println("nadawca  " + email_sender);
            System.out.println("odbiorca  " + mail_data.get("e_mail"));
            javaMailSender.send(message2Send);
            LOG.info("Sending mail succeeded.");
            jobResultVariables1.put("mailSent", true);

        }
        catch (Exception e){
            LOG.error("Sending mail failed." + e);
            jobResultVariables1.put("mailSent", false);
        }



        return jobResultVariables1;
    }


    @JobWorker(type = "sendNegativeMail")
    public Map<String, Object> sendEmail(final JobClient client, final ActivatedJob job) {
        HashMap<String, Object> jobResultVariables = new HashMap<>();

        LOG.info("Job sendEmail is started.");
        final Map<String, Object> jobVariables = job.getVariablesAsMap();
        for (Map.Entry<String, Object> entry : jobVariables.entrySet()) {
            LOG.info("Job variables (process & task input): {}", entry.getKey() + " : " + entry.getValue());
        }
        LOG.info("To jest zmienna dla maila: " + jobVariables.get("mail_data"));
        LOG.info("To jest zmienna dla maila: " + jobVariables.get("mail_message"));
        LinkedHashMap mail_data = (LinkedHashMap)jobVariables.get("mail_data");
        MimeMessage message2Send = javaMailSender.createMimeMessage();
        try {


            message2Send.setFrom(new InternetAddress(ReservationAdapter.email_sender));
            message2Send.setRecipient(Message.RecipientType.TO, new InternetAddress((String)mail_data.get("e_mail")));

            if((int)jobVariables.get("mail_message") == 2){
                message2Send.setSubject("Odmowa rezerwacji biletu");

                String message = "<html>\n" +
                        "<head>\n" +
                        "<style>\n" +
                        "body {font-weight: bold;}\n" +
                        "</style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "\n" +
                        "<h1 style=\"color:red;\">Odmowa rezerwacji biletu</h1>\n" +
                        "<p>Nie udało się zarezerwować biletu na nazwisko " + mail_data.get("first_name") + " " + mail_data.get("last_name")+" ze względu na brak wolnych miejsc" + ".</p>\n" +
                        "</body>\n" +
                        "</html>";
                message2Send.setContent(message, "text/html; charset=utf-8");


            }
            else if((int)jobVariables.get("mail_message") == 3){


                message2Send.setSubject("Błąd rezerwacji biletu");

                String message = "<html>\n" +
                        "<head>\n" +
                        "<style>\n" +
                        "body {font-weight: bold;}\n" +
                        "</style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "\n" +
                        "<h1 style=\"color:red;\">Odmowa rezerwacji biletu</h1>\n" +
                        "<p>Wystąpił problem podczas rezerwacji biletu" + ".</p>\n" +
                        "</body>\n" +
                        "</html>";
                message2Send.setContent(message, "text/html; charset=utf-8");
            }

            javaMailSender.send(message2Send);
            LOG.info("Sending mail succeeded.");
            jobResultVariables.put("mailSendingResult", true);
        } catch (Exception e) {
            LOG.error("Sending mail failed.");
            jobResultVariables.put("mailSendingResult", false);
        }

        return jobResultVariables;
    }

}
