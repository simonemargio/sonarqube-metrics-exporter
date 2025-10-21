package org.simonemargio.sonarspring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.io.IOException;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
public class Controller {

    @Autowired
    private SonarQubeService sonarQubeService;

    @Autowired
    private ContainerNameData containerNameData;

    @GetMapping("/sonar")
    public void sonar() throws IOException {
        List<String> component = containerNameData.getContainerNames();
        Map<String, List<String>> sonarData = new HashMap<>();

        String basicAuth = Base64.getEncoder().encodeToString((SonarQubeApi.USER_TOKEN.getValue() + ":").getBytes());

        WebClient webClient = WebClient.builder()
                .defaultHeader("Authorization", "Basic " + basicAuth)
                .build();

        for(String single : component) {
            try {
                String allInfo = webClient.get()
                        .uri(SonarQubeApi.COMPONENT_LINK.getValue() + single + SonarQubeApi.COMPONENT_METRIC_LINK.getValue())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                String qualityGate = webClient.get()
                        .uri(SonarQubeApi.QUALITY_GATE_LINK.getValue() + single)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                List<String> dataList = new ArrayList<>();
                dataList.add(qualityGate);
                dataList.add(allInfo);
                sonarData.put(single, dataList);

                System.out.println("Info on: " + single + " " + qualityGate + " " + allInfo);
            } catch (Exception e) {
                System.err.println("Error" + single + ": " + e.getMessage());
            }

        }
        sonarQubeService.extractDataAndCreateExcelFile(sonarData);
    }


}
