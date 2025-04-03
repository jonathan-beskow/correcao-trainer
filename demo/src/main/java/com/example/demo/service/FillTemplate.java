package com.example.demo.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class FillTemplate {

    public static void fillTemplate(String templatePath, ByteArrayOutputStream byteArrayOutputStream, Map<String, String> data) throws IOException {
        InputStream fis = FillTemplate.class.getClassLoader().getResourceAsStream(templatePath);
        if (fis == null) {
            throw new FileNotFoundException("Template não encontrado no caminho: " + templatePath);
        }

        XWPFDocument document = new XWPFDocument(fis);

        // Substituir placeholders nos parágrafos
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if (text != null) {
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        // Substitui os placeholders pelos valores do CSV
                        text = text.replace("{{" + entry.getKey() + "}}", entry.getValue());
                    }
                    run.setText(text, 0);
                }
            }
        }

        // Salvar o arquivo preenchido no ByteArrayOutputStream
        document.write(byteArrayOutputStream);
        fis.close();
    }
}

