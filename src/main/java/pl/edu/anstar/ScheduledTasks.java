package pl.edu.anstar;

import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ScheduledTasks {

    @Autowired
    private ZeebeClient client;
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Scheduled(fixedRate = 60000)
    public void reportCurrentTime() {
        log.info("Starting process " + ProcessConstants.BPMN_REGISTRATION_PROCESS_ID + " time: " + dateFormat.format(new Date()));

        client
                .newCreateInstanceCommand()
                .bpmnProcessId(ProcessConstants.BPMN_REGISTRATION_PROCESS_ID)
                .latestVersion()
                .send();
    }
}
