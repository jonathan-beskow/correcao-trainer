package com.example.demo.controller;


import com.example.demo.dto.ApontamentoDTO;
import com.example.demo.dto.SugestaoCorrecaoDTO;
import com.example.demo.model.CasoCorrigido;
import com.example.demo.service.SimilaridadeService;
import com.example.demo.service.VetorizacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sugerir-correcao")
public class CorrecaoController {

    @Autowired
    private VetorizacaoService vetorizacaoService;

    @Autowired
    private SimilaridadeService similaridadeService;

    @PostMapping
    public List<SugestaoCorrecaoDTO> sugerir(@RequestBody ApontamentoDTO dto) {
        List<Float> embedding = vetorizacaoService.gerarEmbedding(dto.getCodigo(), dto.getTipo());

        List<SimilaridadeService.CasoCorrigidoComSimilaridade> similares =
                similaridadeService.buscarSimilares(embedding, dto.getTipo());

        return similares.stream().map(sim -> {
            CasoCorrigido c = sim.getCaso();
            return new SugestaoCorrecaoDTO(
                    c.getTipo(),
                    c.getLinguagem(),
                    c.getContexto(),
                    c.getCodigoOriginal(),
                    c.getCodigoCorrigido(),
                    c.getJustificativa(),
                    sim.getSimilaridade()
            );
        }).collect(Collectors.toList());
    }
}
