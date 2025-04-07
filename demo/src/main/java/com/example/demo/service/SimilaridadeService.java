package com.example.demo.service;

import com.example.demo.dto.CodigoCorrigidoComSimilaridadeDTO;
import com.example.demo.model.CasoCorrigido;
import com.example.demo.repository.CasoCorrigidoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class SimilaridadeService {

    private static final Logger logger = LoggerFactory.getLogger(SimilaridadeService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String URL_PYTHON = "http://localhost:8000";

    @Autowired
    private CasoCorrigidoRepository casoCorrigidoRepository;

    //public Optional<String> obterCodigoCorrigidoMaisSimilar(String codigoNovo, String tipo) {
    public Optional<CodigoCorrigidoComSimilaridadeDTO> obterCodigoCorrigidoMaisSimilar(String codigoNovo, String tipo){
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("codigo", codigoNovo);
            payload.put("tipo", tipo);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(URL_PYTHON + "/buscar_similar", entity, String.class);
            logger.info("Resposta do Python: {}", response.getBody());

            if (response.getStatusCodeValue() != 200) {
                logger.error("Erro na resposta do Python: HTTP {}", response.getStatusCodeValue());
                return Optional.empty();
            }

            JsonNode node = objectMapper.readTree(response.getBody());
            JsonNode corrigidoNode = node.get("codigoCorrigido");

            if (corrigidoNode == null || corrigidoNode.isNull()) {
                logger.warn("Resposta do Python sem campo 'codigoCorrigido': {}", response.getBody());
                return Optional.empty();
            }

            String codigoCorrigido = corrigidoNode.asText();
            if (codigoCorrigido.isBlank() || codigoCorrigido.toLowerCase().contains("nenhuma sugest")) {
                return Optional.empty();
            }
            double similaridade = node.has("similaridade") ? node.get("similaridade").asDouble() : 0.0;

            return Optional.of(new CodigoCorrigidoComSimilaridadeDTO(codigoCorrigido, similaridade));

        } catch (Exception e) {
            logger.error("Erro ao buscar código corrigido similar", e);
            return Optional.empty();
        }
    }

    public boolean inserirCasoCorrigido(CasoCorrigido caso) {
        try {
            Map<String, String> json = new HashMap<>();
            json.put("codigo", caso.getCodigoOriginal());
            json.put("tipo", caso.getTipo());

            String requestBody = objectMapper.writeValueAsString(json);
            logger.info("Enviando novo caso para Python: {}", requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_PYTHON + "/adicionar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Resposta do Python ao adicionar caso: {}", response.body());

            casoCorrigidoRepository.save(caso);
            return true;

        } catch (Exception e) {
            logger.error("Erro ao inserir caso corrigido", e);
            return false;
        }
    }

    public List<CasoCorrigidoComSimilaridade> buscarSimilares(String codigoNovo, String tipo) {
        List<CasoCorrigidoComSimilaridade> similares = new ArrayList<>();
        try {
            Map<String, Object> json = new HashMap<>();
            json.put("codigo", codigoNovo);
            json.put("tipo", tipo);
            json.put("k", 3);

            String requestBody = objectMapper.writeValueAsString(json);
            logger.info("Requisição para buscar similares: {}", requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_PYTHON + "/buscar_similar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Resposta do Python ao buscar similares: {}", response.body());

            if (response.statusCode() != 200) {
                logger.error("Erro ao buscar similares. Código HTTP: {}", response.statusCode());
                return similares;
            }

            JsonNode node = objectMapper.readTree(response.body());

            for (JsonNode item : node.get("similares")) {
                String codigoSimilarCorrigido = item.get("codigo").asText();
                double distancia = item.get("distancia").asDouble();
                double similaridade = 1.0 / (1.0 + distancia);

                Optional<CasoCorrigido> casoOptional = casoCorrigidoRepository.findByCodigoCorrigido(codigoSimilarCorrigido);
                casoOptional.ifPresent(caso -> similares.add(new CasoCorrigidoComSimilaridade(caso, similaridade)));
            }

        } catch (Exception e) {
            logger.error("Erro ao buscar similares", e);
        }

        return similares;
    }

    public static class CasoCorrigidoComSimilaridade {
        private final CasoCorrigido caso;
        private final double similaridade;

        public CasoCorrigidoComSimilaridade(CasoCorrigido caso, double similaridade) {
            this.caso = caso;
            this.similaridade = similaridade;
        }

        public CasoCorrigido getCaso() {
            return caso;
        }

        public double getSimilaridade() {
            return similaridade;
        }
    }
}
