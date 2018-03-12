package com.dreamhack.example.jasper.controller;

import com.dreamhack.example.jasper.dto.Employee;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("api/jasper")
public class JasperExampleController {

    @RequestMapping(value = "/pdf",
            method = RequestMethod.GET)
    public ResponseEntity<byte[]> pdf() {
        JasperPrint jasperPrint = getJasperPrint();
        if (jasperPrint != null) {

            byte[] content = exportPdf(jasperPrint);
            String filename = "example.pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/pdf"));
            headers.setContentDispositionFormData(filename, filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            return new ResponseEntity<>(content, headers, HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body(null);
    }

    @RequestMapping(value = "/html",
            method = RequestMethod.GET)
    public ResponseEntity<String> html() {
        JasperPrint jasperPrint = getJasperPrint();
        if (jasperPrint != null)
            return ResponseEntity.ok()
                    .contentType(
                            MediaType.TEXT_HTML)
                    .body(exportHtml(jasperPrint));

        return ResponseEntity.badRequest().body(null);
    }

    private JasperPrint getJasperPrint() {
        InputStream employeeReportStream
                = getClass().getResourceAsStream("/template2.jrxml");
        try {
            HashMap<String, Object> hmParams = new HashMap<String, Object>();
            hmParams.put("Year", 2018);
            hmParams.put("Title", "Jasper report example");

            List<Employee> employees = new ArrayList<>();
            employees.add(new Employee("Nguyen Van", "A", 200000));
            employees.add(new Employee("Tran Thi", "B", 300000));
            JRDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(employees);

            JasperReport jasperReport
                    = JasperCompileManager.compileReport(employeeReportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(
                    jasperReport, hmParams, beanCollectionDataSource);
            return jasperPrint;
        } catch (Exception ex) {
            return null;
        }
    }

    private byte[] exportPdf(JasperPrint jasperPrint) {
        JRPdfExporter exporter = new JRPdfExporter();
        OutputStream outputStream = new ByteArrayOutputStream();

        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(
                new SimpleOutputStreamExporterOutput(outputStream));

        SimplePdfReportConfiguration reportConfig
                = new SimplePdfReportConfiguration();
        reportConfig.setSizePageToContent(true);
        reportConfig.setForceLineBreakPolicy(false);

        SimplePdfExporterConfiguration exportConfig
                = new SimplePdfExporterConfiguration();
        exportConfig.setEncrypted(true);
        exportConfig.setAllowedPermissionsHint("PRINTING");

        exporter.setConfiguration(reportConfig);
        exporter.setConfiguration(exportConfig);

        try {
            exporter.exportReport();
            return ((ByteArrayOutputStream) outputStream).toByteArray();
        } catch (JRException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String exportHtml(JasperPrint jasperPrint) {
        HtmlExporter exporter = new HtmlExporter();
        StringBuffer stringBuffer = new StringBuffer();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleHtmlExporterOutput(stringBuffer));

        try {
            exporter.exportReport();
            return stringBuffer.toString();
        } catch (JRException e) {
            e.printStackTrace();
        }
        return "";
    }

    //Export pdf file
//    public void exportPdf(JasperPrint jasperPrint) {
//        JRPdfExporter exporter = new JRPdfExporter();
//
//        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
//        exporter.setExporterOutput(
//                new SimpleOutputStreamExporterOutput("employeeReport.pdf"));
//
//        SimplePdfReportConfiguration reportConfig
//                = new SimplePdfReportConfiguration();
//        reportConfig.setSizePageToContent(true);
//        reportConfig.setForceLineBreakPolicy(false);
//
//        SimplePdfExporterConfiguration exportConfig
//                = new SimplePdfExporterConfiguration();
//        exportConfig.setEncrypted(true);
//        exportConfig.setAllowedPermissionsHint("PRINTING");
//
//        exporter.setConfiguration(reportConfig);
//        exporter.setConfiguration(exportConfig);
//
//        try {
//            exporter.exportReport();
//        } catch (JRException e) {
//            e.printStackTrace();
//        }
//    }
}
