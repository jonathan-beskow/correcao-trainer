package com.example.demo.controller;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/falsos-positivos")
public class FalsosPositivosController {

    // Tipos de vulnerabilidade que consideramos "falsos positivos"
    private static final List<String> TIPOS_FALSOS_POSITIVOS = List.of(
            "Error Handling: Overly Broad Catch",
            "System Information Leak: Internal",
            "Hidden Field",
            "Poor Error Handling: Overly Broad Catc",
            "Poor Style: Value Never Read",
            "System Information Leak: Internal: ",
            "J2EE Bad Practices: Threads:"
    );

    @PostMapping("/gerar-relatorio")
    public ResponseEntity<byte[]> gerarRelatorioFalsosPositivos(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, List<String>> falsosPositivosAgrupados = processarCSV(file);

        byte[] wordFile = gerarRelatorioWord(falsosPositivosAgrupados);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=relatorio_falsos_positivos.docx");

        return new ResponseEntity<>(wordFile, headers, HttpStatus.OK);
    }

    public Map<String, List<String>> processarCSV(MultipartFile file) throws IOException {
        Map<String, List<String>> falsosPositivosAgrupados = new HashMap<>();

        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(reader);

            // Itera sobre o CSV, agrupando os apontamentos por tipo
            for (CSVRecord record : records) {
                String tipo = record.get("category");  // Assumindo que há uma coluna chamada "category" para o tipo
                if (TIPOS_FALSOS_POSITIVOS.contains(tipo)) {
                    String localizacao = record.get("path");  // Assumindo que há uma coluna chamada "path" para o caminho

                    // Agrupa as localizações por tipo de apontamento
                    falsosPositivosAgrupados.computeIfAbsent(tipo, k -> new ArrayList<>()).add(localizacao);
                }
            }
        }

        return falsosPositivosAgrupados;
    }

    public byte[] gerarRelatorioWord(Map<String, List<String>> falsosPositivosAgrupados) throws IOException {
        XWPFDocument document = new XWPFDocument();

        // Adiciona o título do relatório
        XWPFParagraph titulo = document.createParagraph();
        titulo.createRun().setText("RELATÓRIO DE FALSOS POSITIVOS");

        // Preencher as informações de projeto
        XWPFParagraph descricaoProjeto = document.createParagraph();
        descricaoProjeto.createRun().setText("DESCRIÇÃO DO PROJETO\n\nDADOS DO PROJETO");

        // Detalhes do projeto como nome, versão, etc.
        XWPFParagraph projetoInfo = document.createParagraph();
        projetoInfo.createRun().setText("Nome do projeto: ${nome_da_aplicacao}");
        projetoInfo.createRun().setText("Nome do projeto na Esteira: ${nome_da_aplicacao_esteira}");
        projetoInfo.createRun().setText("Versão do Projeto: ${versao_do_projeto}");


        // Itera sobre os tipos de vulnerabilidade, agrupando-os por tipo
        for (Map.Entry<String, List<String>> entry : falsosPositivosAgrupados.entrySet()) {
            String tipo = entry.getKey();
            List<String> localizacoes = entry.getValue();
            int quantidade = localizacoes.size();

            // Título para o tipo de apontamento
            XWPFParagraph tipoParagrafo = document.createParagraph();
            tipoParagrafo.createRun().setText("Nome do apontamento: " + tipo);

            // Quantidade de vezes que o apontamento ocorre
            XWPFParagraph quantidadeParagrafo = document.createParagraph();
            quantidadeParagrafo.createRun().setText("Quantidade: " + quantidade);

            // Adiciona as localizações de cada tipo
            XWPFParagraph ocorrenciasParagrafo = document.createParagraph();
            ocorrenciasParagrafo.createRun().setText("Ocorrências:");

            for (String localizacao : localizacoes) {
                XWPFParagraph localizacaoParagrafo = document.createParagraph();
                localizacaoParagrafo.createRun().setText(localizacao);
            }

            // Adiciona os campos solicitados (justificativa, severidade, etc.)
            XWPFParagraph analiseParagrafo = document.createParagraph();
            analiseParagrafo.createRun().setText("Tipo de análise: " + tipo);

            XWPFParagraph justificativaParagrafo = document.createParagraph();
            justificativaParagrafo.createRun().setText("Justificativa: [Preenchido pela equipe de DSS]");

            XWPFParagraph statusParagrafo = document.createParagraph();
            statusParagrafo.createRun().setText("Status: [Preenchido pela equipe de DSS]");

            // Adiciona um espaço entre os registros
            document.createParagraph();
        }

        // Salva o arquivo Word em memória
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.write(baos);
        baos.close();

        return baos.toByteArray();
    }
}
