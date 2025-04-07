package com.example.demo.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class FillTemplate {

    public static void fillTemplate(String templatePath, ByteArrayOutputStream byteArrayOutputStream, Map<String, List<String[]>> groupedData) throws IOException {
        InputStream fis = FillTemplate.class.getClassLoader().getResourceAsStream(templatePath);
        if (fis == null) {
            throw new FileNotFoundException("Template não encontrado no caminho: " + templatePath);
        }

        XWPFDocument document = new XWPFDocument(fis);

        for (XWPFParagraph paragraph : document.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if (text != null) {
                    // Substitui o {{path}} pelas ocorrências agrupadas para cada tipo
                    for (Map.Entry<String, List<String[]>> entry : groupedData.entrySet()) {
                        String tipo = entry.getKey();
                        List<String[]> apontamentos = entry.getValue();

                        // Para cada tipo, substituímos o placeholder {{path}} pelas ocorrências
                        StringBuilder occurrences = new StringBuilder();
                        for (String[] apontamento : apontamentos) {
                            for (String s : apontamento) {
                                occurrences.append("Ocorrência: ").append(s).append("\n");
                            }
                        }

                        // Substituir {{path}} pelo conteúdo das ocorrências
                        text = text.replace("{{path}}", occurrences.toString());
                    }
                    run.setText(text, 0);
                }
            }
        }

        int totalApontamentos = 0;
        for (Map.Entry<String, List<String[]>> entry : groupedData.entrySet()) {
            String tipo = entry.getKey();
            List<String[]> apontamentos = entry.getValue();

            XWPFParagraph tipoParagrafo = document.createParagraph();
            tipoParagrafo.createRun().setText("Tipo de apontamento: " + tipo);

            String quantidade = String.valueOf(apontamentos.size());
            XWPFParagraph quantidadeParagrafo = document.createParagraph();
            quantidadeParagrafo.createRun().setText("Quantidade de vezes que aponta: " + quantidade);

            for (String[] apontamento : apontamentos) {
                for (String s : apontamento) {
                    XWPFParagraph apontamentoParagrafo = document.createParagraph();
                    apontamentoParagrafo.createRun().setText(s);
                }
            }

            // Somar o número total de apontamentos
            totalApontamentos += apontamentos.size();
        }

        // Substituir o total de apontamentos no documento
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if (text != null) {
                    text = text.replace("{{quantidade_de_vezes_que_repete}}", String.valueOf(totalApontamentos));
                    run.setText(text, 0);
                }
            }
        }

        document.write(byteArrayOutputStream);
        fis.close();
    }
}
