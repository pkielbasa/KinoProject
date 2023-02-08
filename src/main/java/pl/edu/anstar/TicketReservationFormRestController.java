package pl.edu.anstar;

import io.camunda.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/")
public class TicketReservationFormRestController {

  private static final Logger LOG = LoggerFactory.getLogger(TicketReservationFormRestController.class);

  @Autowired
  private ZeebeClient client;

  @PostMapping("/start")
  public void startProcessInstance(@RequestBody Map<String, Object> variables) {

    LOG.info("Starting process " + ProcessConstants.BPMN_PROCESS_ID + " with variables: " + variables);

    //final ProcessInstanceEvent event =
    client
            .newCreateInstanceCommand()
            .bpmnProcessId(ProcessConstants.BPMN_PROCESS_ID)
            .latestVersion()
            .variables(variables)
            .send();

  }
}
