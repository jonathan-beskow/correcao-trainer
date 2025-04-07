package com.example.demo.service;


import com.example.demo.model.CasoCorrigido;
import com.example.demo.repository.CasoCorrigidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CasoCorrigidoService {

    @Autowired
    private CasoCorrigidoRepository repository;

    private final static String URL_PYTHON = "http://microservico-embed:8000/adicionar";


    public CasoCorrigido salvar(CasoCorrigido caso) {
        CasoCorrigido salvo = repository.save(caso);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String body = mapper.writeValueAsString(new EmbeddingDTO(caso.getCodigoOriginal(), caso.getTipo()));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_PYTHON))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        } catch (Exception e) {
            System.err.println("Erro ao enviar código para FAISS: " + e.getMessage());
        }

        return salvo;
    }

    public List<String> listarTiposDeApontamentos() {
        List<CasoCorrigido> todos = repository.findAll();
        return todos.stream()
                .map(CasoCorrigido::getTipo)
                .distinct()
                .collect(Collectors.toList());
    }


    static class EmbeddingDTO {
        public String codigo;
        public String tipo;

        public EmbeddingDTO(String codigo, String tipo) {
            this.codigo = codigo;
            this.tipo = tipo;
        }
    }

}