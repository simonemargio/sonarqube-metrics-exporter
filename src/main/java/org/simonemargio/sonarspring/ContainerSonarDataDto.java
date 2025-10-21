package org.simonemargio.sonarspring;

import lombok.Data;

import java.lang.reflect.Field;

@Data
public class ContainerSonarDataDto {
    private String containerName;
    private String qualityGate;
    private String linesOfCode;
    private String bugs;
    private String vulnerabilities;
    private String codeSmells;
    private String securityHotspots;
    private String coverage;
    private String duplications;

    {
        for(Field field : this.getClass().getDeclaredFields()) {
            if(field.getType().equals(String.class)) {
                field.setAccessible(true);
                try {
                    field.set(this, "-");
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
