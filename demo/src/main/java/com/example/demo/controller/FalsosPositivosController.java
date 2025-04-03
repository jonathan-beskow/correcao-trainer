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
        Map<String, String> data = processarCSV(file);

        // Caminho do template e onde o relatório será salvo
        String templatePath = "Template.docx"; // Certifique-se de que o template está no diretório correto (src/main/resources)

        // Gerar o relatório em memória e retornar como resposta
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        fillTemplate.fillTemplate(templatePath, byteArrayOutputStream, data);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=relatorio_falsos_positivos.docx");

        return new ResponseEntity<>(byteArrayOutputStream.toByteArray(), headers, HttpStatus.OK);
    }

    public Map<String, String> processarCSV(MultipartFile file) throws IOException {
        Map<String, String> data = new HashMap<>();

        // Lê o arquivo CSV
        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(reader);

            // Processa cada linha do CSV
            for (CSVRecord record : records) {
                // Mapeia os dados CSV para placeholders
                data.put("tipo_apontamento", record.get("category"));
                data.put("path", record.get("path"));
                data.put("friority", record.get("friority"));
            }
        }

        return data;
    }
}
