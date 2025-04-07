package com.example.demo.service;

import com.example.demo.model.CasoCorrigido;
import com.example.demo.repository.CasoCorrigidoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimilaridadeService {

    @Autowired
    private CasoCorrigidoRepository repository;

    @Autowired
    private CasoCorrigidoRepository casoCorrigidoRepository;

    public List<CasoCorrigidoComSimilaridade> buscarSimilares(String codigoNovo, String tipo) {
        try {
            // 1. Monta JSON para a API Python
            Map<String, Object> json = new HashMap<>();
            json.put("codigo", codigoNovo);
            json.put("tipo", tipo);
            json.put("k", 3);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/buscar_similar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(json)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode node = new ObjectMapper().readTree(response.body());

            List<CasoCorrigidoComSimilaridade> similares = new ArrayList<>();

            for (JsonNode item : node.get("similares")) {
                String codigoSimilar = item.get("codigo").asText();
                double distancia = item.get("distancia").asDouble();
                double similaridade = 1.0 / (1.0 + distancia); // Conversão simples

                repository.findByCodigoOriginal(codigoSimilar)
                        .ifPresent(caso -> similares.add(new CasoCorrigidoComSimilaridade(caso, similaridade)));
            }

            return similares;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static class CasoCorrigidoComSimilaridade {
        private CasoCorrigido caso;
        private double similaridade;

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


    public boolean inserirCasoCorrigido(CasoCorrigido caso) {
        try {
            Map<String, String> json = new HashMap<>();
            json.put("codigo", caso.getCodigoOriginal());
            json.put("tipo", caso.getTipo());

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8000/adicionar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(new ObjectMapper().writeValueAsString(json)))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());

            // Apenas armazena no Mongo, sem mais cálculo manual
            casoCorrigidoRepository.save(caso);
            return true;

        } catch (Exception e) {
            System.out.println("Erro ao inserir caso: " + e.getMessage());
            return false;
        }
    }


}

