# SonarQube Metrics Exporter

## Overview
This project retrieves SonarQube metrics for every component (container/project/repository) registered in SonarQube and exports them into an Excel file. 

Metrics collected include: `ncloc`, `bugs`, `vulnerabilities`, `code_smells`, `security_hotspots`, `coverage`, `duplicated_lines_density`. The exporter uses SonarQube REST APIs and generates a detailed Excel report using Apache POI with color coding and metric-based highlights.

## Features
- Query SonarQube API to fetch measures for each component
- Metrics retrieved:
    - `ncloc` (Lines of Code)
    - `bugs`
    - `vulnerabilities`
    - `code_smells`
    - `security_hotspots`
    - `coverage`
    - `duplicated_lines_density`
- Produce an Excel workbook (.xlsx) summarizing metrics per component
- Conditional formatting/colors to highlight thresholds and trends
- Configurable SonarQube URL and authentication via environment variable or properties

## How it works
1. The service enumerates components associated with your SonarQube instance.
2. For each component it requests the defined metric keys via SonarQube's `/api/measures/component` endpoint.
3. Retrieved values are aggregated and written to an Excel file using Apache POI. Each metric is placed in its own column and cells are colored based on configurable thresholds.

## Requirements
- Java 17+ (or project-specified JVM)
- Maven (recommended: use the included Maven Wrapper)
- Access to a SonarQube server with an API token that has permission to read project measures

## Configuration

Configuration values are currently defined in code; please replace hardcoded values or (recommended) provide them via environment variables.

Recommended environment variables:

- `COMPONENT_LINK` — SonarQube base URL
- `USER_TOKEN` — SonarQube API token
- `QUALITY_GATE_LINK` — SonarQube Quality Gate URL for reference in reports

## Contributing
- Open issues or PRs for bugs and improvements.
- Ensure no secrets are added in PRs.

## License

This project is licensed under the GNU AFFERO GENERAL PUBLIC LICENSE Version 3 (19 November 2007).