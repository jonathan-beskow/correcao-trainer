package com.example.demo.controller;

import com.example.demo.dto.ApontamentoDTO;
import com.example.demo.dto.SugestaoCorrecaoDTO;
import com.example.demo.model.CasoCorrigido;
import com.example.demo.service.SimilaridadeService;
import com.example.demo.service.VetorizacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sugerir-correcao")
public class CorrecaoController {

    @Autowired
    private VetorizacaoService vetorizacaoService;

    @Autowired
    private SimilaridadeService similaridadeService;

    @PostMapping("/corrigir")
    public SugestaoCorrecaoDTO corrigir(@RequestBody ApontamentoDTO dto) {
        List<Float> embedding = vetorizacaoService.gerarEmbedding(dto.getCodigo(), dto.getTipo());
        List<SimilaridadeService.CasoCorrigidoComSimilaridade> similares =
                similaridadeService.buscarSimilares(embedding, dto.getTipo());

        if (similares.isEmpty()) {
            return new SugestaoCorrecaoDTO(
                    dto.getTipo(),
                    dto.getLinguagem(),
                    dto.getContexto(),
                    dto.getCodigo(),
                    "Nenhuma sugestão encontrada",
                    "Não foram encontrados casos semelhantes no banco de dados.",
                    0.0
            );
        }

        SimilaridadeService.CasoCorrigidoComSimilaridade melhor = similares.get(0);
        CasoCorrigido c = melhor.getCaso();

        String correcaoGerada = vetorizacaoService.gerarCorrecao(
                dto.getTipo(),
                dto.getCodigo(),
                c.getCodigoOriginal(),
                c.getCodigoCorrigido()
        );

        return new SugestaoCorrecaoDTO(
                dto.getTipo(),
                dto.getLinguagem(),
                dto.getContexto(),
                dto.getCodigo(),
                correcaoGerada,
                "Correção gerada com base em exemplo semelhante encontrado no histórico.",
                melhor.getSimilaridade()
        );
    }

    @PostMapping("/cadastrar-caso")
    public ResponseEntity<Map<String, String>> cadastrarCaso(@RequestBody CasoCorrigido novoCaso) {
        boolean sucesso = similaridadeService.inserirCasoCorrigido(novoCaso);

        Map<String, String> resposta = new HashMap<>();

        if (sucesso) {
            resposta.put("mensagem", "Caso inserido com sucesso.");
            return ResponseEntity.ok(resposta);
        } else {
            resposta.put("mensagem", "Erro ao inserir caso.");
            return ResponseEntity.status(500).body(resposta);
        }
    }
}