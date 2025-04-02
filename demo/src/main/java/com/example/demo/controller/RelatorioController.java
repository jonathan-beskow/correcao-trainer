package com.example.demo.controller;

import com.example.demo.dto.TipoContagemDTO;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class RelatorioController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @GetMapping("/estatisticas/tipos")
    public List<TipoContagemDTO> contarTiposDeVulnerabilidade() {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.group("tipo").count().as("total"),
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "total")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "casosCorrigidos", Document.class);

        return results.getMappedResults().stream()
                .map(doc -> new TipoContagemDTO(
                        doc.getString("_id"),
                        ((Number) doc.get("total")).longValue()  // <-- corrigido aqui
                ))
                .collect(Collectors.toList());

    }
}