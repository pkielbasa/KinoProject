package pl.edu.anstar;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableZeebeClient
@Deployment(resources = "classpath*:/model/*.*")
@EnableScheduling
public class TicketReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketReservationApplication.class, args);
    }
}