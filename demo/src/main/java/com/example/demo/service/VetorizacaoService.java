package com.example.demo.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class VetorizacaoService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String EMBEDDING_URL = "http://localhost:8000/embed";

    public List<Float> gerarEmbedding(String codigo, String tipo) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("codigo", codigo);
        requestBody.put("tipo", tipo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(EMBEDDING_URL, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            List<Double> raw = (List<Double>) response.getBody().get("embedding");
            List<Float> embedding = new ArrayList<>();
            for (Double d : raw) {
                embedding.add(d.floatValue());
            }
            return embedding;
        } else {
            throw new RuntimeException("Erro ao gerar embedding: " + response.getStatusCode());
        }
    }
}