package com.example.demo.controller;

import com.example.demo.service.FillTemplate;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/falsos-positivos")
public class FalsosPositivosController {

    @Autowired
    private FillTemplate fillTemplate;

    // Tipos de vulnerabilidade que consideramos "falsos positivos"
    private static final List<String> TIPOS_FALSOS_POSITIVOS = List.of(
            "Error Handling: Overly Broad Catch",
            "System Information Leak: Internal",
            "Hidden Field",
            "Poor Error Handling: Overly Broad Catc",
            "Poor Style: Value Never Read",
            "System Information Leak: Internal: ",
            "J2EE Bad Practices: Threads:",
            "Build Misconfiguration: External Maven Dependency Repository"
    );

    @PostMapping("/gerar-relatorio")
    public ResponseEntity<byte[]> gerarRelatorioFalsosPositivos(@RequestParam("file") MultipartFile file) throws IOException {
        // Processa o CSV para extrair os dados
        Map<String, List<String[]>> groupedData = processarCSV(file);

        // Caminho do template e onde o relatório será salvo
        String templatePath = "Template.docx"; // Certifique-se de que o template está no diretório correto (src/main/resources)

        // Gerar o relatório em memória e retornar como resposta
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        fillTemplate.fillTemplate(templatePath, byteArrayOutputStream, groupedData);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=relatorio_falsos_positivos.docx");

        return new ResponseEntity<>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
    }

    public Map<String, List<String[]>> processarCSV(MultipartFile file) throws IOException {
        Map<String, List<String[]>> groupedData = new HashMap<>();

        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(reader);

            for (CSVRecord record : records) {
                String tipoApontamento = record.get("category");
                if (TIPOS_FALSOS_POSITIVOS.contains(tipoApontamento)) {
                    String path = record.get("path");
                    String[] apontamento = new String[]{path};

                    groupedData.computeIfAbsent(tipoApontamento, k -> new ArrayList<>()).add(apontamento);
                }
            }
        }

        return groupedData;
    }
}
