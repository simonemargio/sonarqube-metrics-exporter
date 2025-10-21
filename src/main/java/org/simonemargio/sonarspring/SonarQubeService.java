package org.simonemargio.sonarspring;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SonarQubeService {

    public void extractDataAndCreateExcelFile( Map<String, List<String>> sonarData) throws IOException {
        List<ContainerSonarDataDto> formattedSonarData = extractDataFromSonarJson(sonarData);
        createFileExcel(formattedSonarData);
    }

    private List<ContainerSonarDataDto> extractDataFromSonarJson(Map<String, List<String>> sonarData) {
        ObjectMapper mapper = new ObjectMapper();
        List<ContainerSonarDataDto> containerSonarDataDtoList = new ArrayList<>();

        for(Map.Entry<String, List<String>> entry : sonarData.entrySet()) {
            ContainerSonarDataDto containerSonarDataDto = new ContainerSonarDataDto();

            try {
                String qualityGate = entry.getValue().get(0);
                String allInfo = entry.getValue().get(1);

                JsonNode rootQualityNode = mapper.readTree(qualityGate);
                JsonNode rootMeasuresAllInfoNode = mapper.readTree(allInfo).get("component").get("measures");

                containerSonarDataDto.setContainerName(entry.getKey());
                containerSonarDataDto.setQualityGate(rootQualityNode.path("projectStatus").path("status").asText());

                for(JsonNode objNode : rootMeasuresAllInfoNode) {
                    String metric = objNode.path("metric").asText();
                    String metricKeyValue = "value";
                    SonarQubeApi apiMetric = SonarQubeApi.fromMetric(metric);

                    switch (apiMetric) {
                        case COVERAGE:
                            containerSonarDataDto.setCoverage(objNode.path(metricKeyValue).asText());
                            break;
                        case BUGS:
                            containerSonarDataDto.setBugs(objNode.path(metricKeyValue).asText());
                            break;
                        case VULNERABILITIES:
                            containerSonarDataDto.setVulnerabilities(objNode.path(metricKeyValue).asText());
                            break;
                        case SECURITY_HOTSPOTS:
                            containerSonarDataDto.setSecurityHotspots(objNode.path(metricKeyValue).asText());
                            break;
                        case DUPLICATED_LINES:
                            containerSonarDataDto.setDuplications(objNode.path(metricKeyValue).asText());
                            break;
                        case LINES_OF_CODE:
                            containerSonarDataDto.setLinesOfCode(objNode.path(metricKeyValue).asText());
                            break;
                        case CODE_SMELLS:
                            containerSonarDataDto.setCodeSmells(objNode.path(metricKeyValue).asText());
                            break;
                        default:
                            break;
                    }
                }
                containerSonarDataDtoList.add(containerSonarDataDto);
            } catch (Exception ignored) {}
        }
        return containerSonarDataDtoList;
    }


    private void createFileExcel(List<ContainerSonarDataDto> formattedSonarData)  throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Data");

        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "Container", "Quality Gate Status", "Lines of code", "Bugs", "Vulnerabilities",
                "Code Smell", "Security Hotspot", "Coverage", "Duplications"
        };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        CellStyle evenRowStyle = setCellStyleColor(workbook,"grey", "low");
        CellStyle oddRowStyle = setCellStyleColor(workbook, "white", "low");

        int rowNum = 1;
        for(ContainerSonarDataDto data : formattedSonarData) {
            Row row = sheet.createRow(rowNum++);
            CellStyle rowStyle = (rowNum % 2 == 0) ? evenRowStyle : oddRowStyle;
            rowStyle.setBorderTop(BorderStyle.THIN);
            rowStyle.setBorderBottom(BorderStyle.THIN);
            rowStyle.setBorderLeft(BorderStyle.THIN);
            rowStyle.setBorderRight(BorderStyle.THIN);

            setContainerName(data, row, rowStyle, workbook);
            setQualityGate(data, row, rowStyle, workbook);
            setLineOfCode(data, row, rowStyle, workbook);
            setBugs(data, row, rowStyle, workbook);
            setVulnerabilities(data, row, rowStyle, workbook);
            setCodeSmell(data, row, rowStyle, workbook);
            setSecurityHotspot(data, row, rowStyle, workbook);
            setCoverage(data, row, rowStyle, workbook);
            setDuplications(data, row, rowStyle, workbook);
        }

        int lastRow = sheet.getPhysicalNumberOfRows();
        String range = "A1:I" + lastRow;
        AreaReference areaReference = new AreaReference(range, workbook.getSpreadsheetVersion());
        XSSFTable table = sheet.createTable(areaReference);

        if (table.getCTTable().getTableStyleInfo() == null) table.getCTTable().addNewTableStyleInfo();

        table.getCTTable().getTableStyleInfo().setName("TableStyleMedium1");

        for (int i = 1; i < headers.length; i++) {
            int currentColumnWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, (currentColumnWidth + 2500));
        }
        sheet.autoSizeColumn(0);

        try (FileOutputStream fileOut = new FileOutputStream(new File("CheckSonar-" + getCurrentDateTime() + ".xlsx"))) {
            workbook.write(fileOut);
        }

        workbook.close();
    }


    private void setContainerName(ContainerSonarDataDto data, Row row, CellStyle rowStyle, XSSFWorkbook workbook) {
        Cell cell = row.createCell(0);
        cell.setCellStyle(rowStyle);
        cell.setCellValue(data.getContainerName());
    }

    private void setQualityGate(ContainerSonarDataDto data, Row row, CellStyle rowStyle, XSSFWorkbook workbook) {
        Cell cell = row.createCell(1);
        cell.setCellStyle(rowStyle);

        if(data.getQualityGate().equals("OK")) {
            cell.setCellStyle(setCellStyleColor(workbook, "green", "low"));
            cell.setCellValue("Passed");
        } else {
            cell.setCellStyle(setCellStyleColor(workbook, "red", "high"));
            cell.setCellValue("Failed");
        }
    }

    private void setLineOfCode(ContainerSonarDataDto data, Row row, CellStyle rowStyle, XSSFWorkbook workbook) {
        Cell cell = row.createCell(2);
        cell.setCellStyle(rowStyle);
        cell.setCellValue(data.getLinesOfCode());
    }

    private void setBugs(ContainerSonarDataDto data, Row row, CellStyle rowStyle, XSSFWorkbook workbook) {
        Cell cell = row.createCell(3);
        cell.setCellStyle(rowStyle);

        String bugs = data.getBugs().replace(",","");
        cell.setCellStyle(setCellStyleFromRangeValue(workbook, rowStyle, bugs, 100, 200));
        cell.setCellValue(data.getBugs());
    }

    private void setVulnerabilities(ContainerSonarDataDto data, Row row, CellStyle rowStyle, XSSFWorkbook workbook) {
        Cell cell = row.createCell(4);
        cell.setCellStyle(rowStyle);
        cell.setCellStyle(setCellStyleFromRangeValue(workbook, rowStyle, data.getVulnerabilities(), 0, 100));
        cell.setCellValue(data.getVulnerabilities());
    }

    private void setCodeSmell(ContainerSonarDataDto data, Row row, CellStyle rowStyle, XSSFWorkbook workbook) {
        Cell cell = row.createCell(5);
        cell.setCellStyle(rowStyle);
        cell.setCellStyle(setCellStyleFromRangeValue(workbook, rowStyle, data.getCodeSmells(), 10, 100));
        cell.setCellValue(data.getCodeSmells());
    }

    private void setSecurityHotspot(ContainerSonarDataDto data, Row row, CellStyle rowStyle, XSSFWorkbook workbook) {
        Cell cell = row.createCell(6);
        cell.setCellStyle(rowStyle);
        cell.setCellValue(data.getSecurityHotspots());
    }

    private void setCoverage(ContainerSonarDataDto data, Row row, CellStyle rowStyle, XSSFWorkbook workbook) {
        Cell cell = row.createCell(7);
        cell.setCellStyle(setCellStyleCoverageFromRangeValue(workbook, rowStyle, data.getCoverage()));
        cell.setCellValue(data.getCoverage().equals("-") ? data.getCoverage() : data.getCoverage() + "%");
    }

    private void setDuplications(ContainerSonarDataDto data, Row row, CellStyle rowStyle, XSSFWorkbook workbook) {
        Cell cell = row.createCell(8);
        cell.setCellStyle(rowStyle);
        cell.setCellValue(data.getDuplications());
    }

    private CellStyle setCellStyleFromRangeValue(Workbook workbook, CellStyle rowStyle, String value, int lowEnd, int mediumEnd) {

        try {
            int parsedValue = Integer.parseInt(value);

            if(parsedValue <= lowEnd) return setCellStyleColor(workbook,"green", "low");
            else if(parsedValue <= mediumEnd) return setCellStyleColor(workbook,"yellow", "medium");
            else return setCellStyleColor(workbook,"red", "low");

        } catch (NumberFormatException e) {
            return rowStyle;
        }

    }

    private CellStyle setCellStyleCoverageFromRangeValue(Workbook workbook, CellStyle rowStyle, String value) {

        try {
            double parsedValue = Double.parseDouble(value);

            if(parsedValue <= 80) return setCellStyleColor(workbook,"red", "high");
            else if(parsedValue <= 82) return setCellStyleColor(workbook,"yellow", "high");
            else if(parsedValue <= 85) return setCellStyleColor(workbook,"yellow", "low");
            else return setCellStyleColor(workbook,"green", "low");

        } catch (NumberFormatException e) {
            return rowStyle;
        }

    }

    private CellStyle setCellStyleColor(Workbook workbook, String color, String intensity) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(new XSSFColor(getColor(color, intensity)));
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    public byte[] getColor(String name, String intensity) {
        if (name == null || intensity == null) return null;

        return switch (name.toLowerCase()) {
            case "green" -> greenColor(intensity);
            case "yellow" -> yellowColor(intensity);
            case "red" -> redColor(intensity);
            case "blue" -> blueColor(intensity);
            case "white" -> whiteColor(intensity);
            case "grey" -> greyColor(intensity);
            default -> null;
        };
    }

    private byte[] greenColor(String intensity) {
        return switch (intensity.toLowerCase()) {
            case "low" -> new byte[]{(byte) 218, (byte) 242, (byte) 208};
            case "medium" -> new byte[]{(byte) 144, (byte) 238, (byte) 144};
            case "high" -> new byte[]{(byte) 0, (byte) 128, (byte) 0};
            default -> null;
        };
    }

    private byte[] yellowColor(String intensity) {
        return switch (intensity.toLowerCase()) {
            case "low" -> new byte[]{(byte) 255, (byte) 249, (byte) 196};
            case "medium" -> new byte[]{(byte) 255, (byte) 235, (byte) 59};
            case "high" -> new byte[]{(byte) 255, (byte) 193, (byte) 7};
            default -> null;
        };
    }

    private byte[] redColor(String intensity) {
        return switch (intensity.toLowerCase()) {
            case "low" -> new byte[]{(byte) 255, (byte) 205, (byte) 210};
            case "medium" -> new byte[]{(byte) 239, (byte) 83, (byte) 80};
            case "high" -> new byte[]{(byte) 255, (byte) 0, (byte) 0};
            default -> null;
        };
    }

    private byte[] blueColor(String intensity) {
        return switch (intensity.toLowerCase()) {
            case "low" -> new byte[]{(byte) 187, (byte) 222, (byte) 251};
            case "medium" -> new byte[]{(byte) 66, (byte) 165, (byte) 245};
            case "high" -> new byte[]{(byte) 13, (byte) 71, (byte) 161};
            default -> null;
        };
    }

    private byte[] whiteColor(String intensity) {
        return switch (intensity.toLowerCase()) {
            case "low", "medium", "high" -> new byte[]{(byte) 255, (byte) 255, (byte) 255};
            default -> null;
        };
    }

    private byte[] greyColor(String intensity) {
        return switch (intensity.toLowerCase()) {
            case "low", "medium", "high" -> new byte[]{(byte) 217, (byte) 217, (byte) 217};
            default -> null;
        };
    }

    private String getCurrentDateTime() {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        return myDateObj.format(myFormatObj);
    }

}
