package org.simonemargio.sonarspring;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ContainerNameData {

    public List<String> getContainerNames() {
        List<String> container = new ArrayList<>(Arrays.asList(
           "container-name-1",
           "container-name-2",
           "...",
           "container-name-n"
        ));

        return deleteCrtContainerName(container);
    }

    private List<String> deleteCrtContainerName(List<String> containerListNames) {
        containerListNames.removeIf(s -> s.toLowerCase().contains("exclude-specific-word"));
        return containerListNames;
    }
}
