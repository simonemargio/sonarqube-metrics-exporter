package org.simonemargio.sonarspring;

public enum SonarQubeApi {
    COMPONENT_LINK("http://sonarqube.example.com:8310/sonarqube/api/measures/component?component="),
    COMPONENT_METRIC_LINK("&metricKeys=ncloc,bugs,vulnerabilities,code_smells,security_hotspots,coverage,duplicated_lines_density"),
    QUALITY_GATE_LINK("http://sonarqube.example.com:8310/sonarqube/api/qualitygates/project_status?projectKey="),
    USER_TOKEN("REPLACE_WITH_YOUR_TOKEN"),
    COVERAGE("coverage"),
    BUGS("bugs"),
    VULNERABILITIES("vulnerabilities"),
    SECURITY_HOTSPOTS("security_hotspots"),
    DUPLICATED_LINES("duplicated_lines_density"),
    LINES_OF_CODE("ncloc"),
    CODE_SMELLS("code_smells"),
    VALUE("value"),
    UNKNOWN("");

    private final String value;

    SonarQubeApi(String value) { this.value = value; }

    public String getValue() { return value; }

    public static SonarQubeApi fromMetric(String metric) {
        for (SonarQubeApi api : values()) {
            if (api.getValue().equalsIgnoreCase(metric)) {
                return api;
            }
        }
        return UNKNOWN;
    }

}
