package com.example.demo.service;

import com.example.demo.model.Exemplo;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class VetorizacaoService {

    private final RestTemplate restTemplate = new RestTemplate();

    //private final String EMBEDDING_URL = "http://microservico-embed:8000/embed";
    private final String EMBEDDING_URL = "http://localhost:8000/embed";

    public List<Float> gerarEmbedding(String codigo, String tipo) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("codigo", codigo);
        requestBody.put("tipo", tipo);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(EMBEDDING_URL, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                List<Double> raw = (List<Double>) response.getBody().get("embedding");
                List<Float> embedding = new ArrayList<>();
                for (Double d : raw) {
                    embedding.add(d.floatValue());
                }
                return embedding;
            } else {
                System.out.println("⚠️ Microserviço respondeu com status: " + response.getStatusCode());
            }

        } catch (ResourceAccessException e) {
            System.out.println("⚠️ Não foi possível conectar ao microserviço de vetorização em: " + EMBEDDING_URL);
            System.out.println("🔧 Verifique se o container do Python está ativo e escutando na porta correta.");
        } catch (Exception e) {
            System.out.println("❌ Erro inesperado ao gerar embedding: " + e.getMessage());
        }

        return Collections.emptyList();
    }

    // Comparação por similaridade de cosseno
    private double cosineSimilarity(List<Float> a, List<Float> b) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += Math.pow(a.get(i), 2);
            normB += Math.pow(b.get(i), 2);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    // Selecionar o exemplo mais próximo com base no embedding
    private Exemplo buscarExemploMaisSimilar(List<Float> alvo, List<Exemplo> exemplos) {
        double melhorSimilaridade = -1.0;
        Exemplo maisSimilar = null;

        for (Exemplo exemplo : exemplos) {
            double sim = cosineSimilarity(alvo, exemplo.embedding);
            if (sim > melhorSimilaridade) {
                melhorSimilaridade = sim;
                maisSimilar = exemplo;
            }
        }
        return maisSimilar;
    }

    // Chamada para o endpoint /gerar-correcao
    public String gerarCorrecao(String tipo, String codigoAlvo, Exemplo exemplo) {
        Map<String, Object> body = new HashMap<>();
        body.put("tipo", tipo);
        body.put("codigo_alvo", codigoAlvo);

        Map<String, String> exemploMap = new HashMap<>();
        exemploMap.put("codigo_original", exemplo.codigoOriginal);
        exemploMap.put("codigo_corrigido", exemplo.codigoCorrigido);
        body.put("exemplos", List.of(exemploMap));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://microservico-embed:8000/gerar-correcao", request, Map.class);
            return (String) response.getBody().get("codigoCorrigido");

        } catch (Exception e) {
            System.out.println("❌ Erro ao gerar correção: " + e.getMessage());
            return null;
        }
    }

    public String executarPipeline(String codigoAlvo, String tipo, List<Exemplo> exemplos) {
        // 1. Gerar embedding do código enviado
        List<Float> embeddingAlvo = gerarEmbedding(codigoAlvo, tipo);

        // 2. Buscar exemplo mais próximo
        Exemplo maisSimilar = buscarExemploMaisSimilar(embeddingAlvo, exemplos);

        if (maisSimilar == null) {
            System.out.println("⚠️ Nenhum exemplo semelhante encontrado.");
            return null;
        }

        // 3. Enviar para o endpoint /gerar-correcao
        return gerarCorrecao(tipo, codigoAlvo, maisSimilar);
    }

    public String gerarCorrecao(String tipo, String codigoAlvo, String codigoOriginalExemplo, String codigoCorrigidoExemplo) {
        Map<String, Object> body = new HashMap<>();
        body.put("tipo", tipo);
        body.put("codigo_alvo", codigoAlvo);

        Map<String, String> exemploMap = new HashMap<>();
        exemploMap.put("codigo_original", codigoOriginalExemplo);
        exemploMap.put("codigo_corrigido", codigoCorrigidoExemplo);
        body.put("exemplos", List.of(exemploMap));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "http://localhost:8000/gerar-correcao", request, Map.class);

            if ((String) response.getBody().get("codigoCorrigido") == null){

            }

            return (String) response.getBody().get("codigoCorrigido");

        } catch (Exception e) {
            System.out.println("Erro ao gerar correção: " + e.getMessage());
            return "Erro ao gerar sugestão de correção.";
        }
    }
}
