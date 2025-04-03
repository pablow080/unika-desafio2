package com.example.service;

import com.example.model.Client;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {
    @Autowired
    private ClientService clientService;

    public byte[] generatePdfReport(Long clientId) throws JRException {
        Client client = clientService.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        // Load the report template
        InputStream template = getClass().getResourceAsStream("/reports/client_report.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(template);

        // Prepare data
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("CLIENT_ID", client.getId());
        parameters.put("REPORT_TITLE", "Client Details Report");

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(List.of(client));

        // Generate report
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    public byte[] generateExcelReport(List<Client> clients) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clients");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Type", "Name/Company", "Document", "Email", "Status"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Create data rows
            int rowNum = 1;
            for (Client client : clients) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(client.getId());
                row.createCell(1).setCellValue(client.getPersonType().toString());
                row.createCell(2).setCellValue(client.getPersonType() == Client.PersonType.INDIVIDUAL ? 
                    client.getName() : client.getCompanyName());
                row.createCell(3).setCellValue(client.getPersonType() == Client.PersonType.INDIVIDUAL ? 
                    client.getCpf() : client.getCnpj());
                row.createCell(4).setCellValue(client.getEmail());
                row.createCell(5).setCellValue(client.isActive() ? "Active" : "Inactive");
            }

            // Autosize columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public void importClientsFromExcel(InputStream excelFile) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                Client client = new Client();
                client.setPersonType(Client.PersonType.valueOf(row.getCell(1).getStringCellValue()));
                
                if (client.getPersonType() == Client.PersonType.INDIVIDUAL) {
                    client.setName(row.getCell(2).getStringCellValue());
                    client.setCpf(row.getCell(3).getStringCellValue());
                } else {
                    client.setCompanyName(row.getCell(2).getStringCellValue());
                    client.setCnpj(row.getCell(3).getStringCellValue());
                }
                
                client.setEmail(row.getCell(4).getStringCellValue());
                client.setActive(row.getCell(5).getStringCellValue().equals("Active"));

                clientService.save(client);
            }
        }
    }
}