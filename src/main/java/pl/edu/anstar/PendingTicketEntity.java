package pl.edu.anstar;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PendingTicketEntity {
    private int reservation_id;
    private String first_name;
    private String last_name;
    private String e_mail;
    private String phone_number;
    private String movie;
    private String town;
    private String date;
    private String time;
}
