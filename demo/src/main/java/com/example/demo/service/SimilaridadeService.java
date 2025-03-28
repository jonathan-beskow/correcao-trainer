package com.example.demo.service;

import com.example.demo.model.CasoCorrigido;
import com.example.demo.repository.CasoCorrigidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SimilaridadeService {

    @Autowired
    private CasoCorrigidoRepository repository;

    public List<CasoCorrigidoComSimilaridade> buscarSimilares(List<Float> novoEmbedding, String tipo) {
        List<CasoCorrigido> casos = repository.findByTipo(tipo);
        List<CasoCorrigidoComSimilaridade> similares = new ArrayList<>();

        for (CasoCorrigido caso : casos) {
            double similaridade = calcularSimilaridadeCosseno(novoEmbedding, caso.getEmbedding());
            similares.add(new CasoCorrigidoComSimilaridade(caso, similaridade));
        }

        // Ordena do mais similar para o menos similar
        similares.sort((a, b) -> Double.compare(b.getSimilaridade(), a.getSimilaridade()));

        // Retorna os top 3 (ou todos, se tiver poucos)
        return similares.subList(0, Math.min(similares.size(), 3));
    }

    private double calcularSimilaridadeCosseno(List<Float> v1, List<Float> v2) {
        if (v1 == null || v2 == null || v1.size() != v2.size()) return 0.0;

        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            normA += Math.pow(v1.get(i), 2);
            normB += Math.pow(v2.get(i), 2);
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-10);
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
}