package com.example.demo.service;

import com.example.demo.util.Promptutils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CodeCorrectionService {

    private static final Logger logger = LoggerFactory.getLogger(CodeCorrectionService.class);

    @Value("${openai.api.key}")
    private String OPEN_API_KEY;


    @Value("${openai.model:gpt-4-turbo}")
    private String openaiModel;


    public String gerarCorrecao(String tipo, String codigoOriginal, String codigoCorrigido, String codigoAlvo) {
        logger.info("Iniciando correção de código com tipo: {}", tipo);

        String prompt = "";

        if (codigoCorrigido == null || codigoCorrigido.isBlank()) {
            prompt = Promptutils.generatePromptWithOutBase(tipo, codigoAlvo);
        } else {
            //String prompt = StringUtils.generatePrompt(tipo, codigoOriginal, codigoCorrigido, codigoAlvo);
            prompt = Promptutils.generatePrompt(tipo, codigoCorrigido, codigoAlvo);
        }


        ObjectMapper objectMapper = new ObjectMapper();
        String payload = null;

        try {
            JsonNode messagesNode = objectMapper.createArrayNode()
                    .add(objectMapper.createObjectNode().put("role", "system").put("content", "Você é um especialista em correção de código seguro."))
                    .add(objectMapper.createObjectNode().put("role", "user").put("content", prompt));

            JsonNode payloadNode = objectMapper.createObjectNode()
                    .put("model", openaiModel)
                    .set("messages", messagesNode);

            payload = objectMapper.writeValueAsString(payloadNode);
        } catch (Exception e) {
            logger.error("Erro ao gerar o payload JSON: {}", e.getMessage(), e);
            return "Erro ao gerar o payload JSON";
        }

        logger.info("Payload gerado: {}", payload);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + OPEN_API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(payload, headers);


        String apiUrl = "https://api.openai.com/v1/chat/completions";

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            String responseBodyString = response.getBody();
            logger.info("Resposta completa da API: {}", responseBodyString);

            JsonNode responseBody = objectMapper.readTree(responseBodyString);
            JsonNode choicesNode = responseBody.path("choices");

            if (choicesNode.isArray() && choicesNode.size() > 0) {
                String conteudoCompleto = choicesNode.get(0)
                        .path("message")
                        .path("content")
                        .asText()
                        .trim();

                if (conteudoCompleto == null || conteudoCompleto.isEmpty()) {
                    logger.error("Resposta sem conteúdo útil.");
                    return "Erro: O conteúdo retornado está vazio.";
                }

                // Aplica o regex para extrair apenas os blocos de código
                String somenteCodigo = extrairCodigosMarkdown(conteudoCompleto);

                if (somenteCodigo.isEmpty()) {
                    logger.warn("Nenhum bloco de código Markdown encontrado. Retornando conteúdo bruto.");
                    return conteudoCompleto;
                }


                logger.info("Código corrigido extraído com sucesso.");
                return somenteCodigo;
            } else {
                logger.error("Resposta da API não contém dados válidos no campo 'choices'.");
                return "Erro: A resposta da API não contém os dados esperados.";
            }

        } catch (Exception e) {
            logger.error("Erro ao chamar a API OpenAI: {}", e.getMessage(), e);
            return "Erro ao chamar a API: " + e.getMessage();
        }
    }


    public static String extrairCodigosMarkdown(String texto) {
        StringBuilder codigosExtraidos = new StringBuilder();
        Pattern pattern = Pattern.compile("(?s)```(?:\\w+)?\\s*(.*?)```"); // suporta ```java também
        Matcher matcher = pattern.matcher(texto);

        while (matcher.find()) {
            String codigo = matcher.group(1).trim();
            codigosExtraidos.append(codigo).append("\n\n");
        }

        return codigosExtraidos.toString().trim(); // remove último \n
    }
}


