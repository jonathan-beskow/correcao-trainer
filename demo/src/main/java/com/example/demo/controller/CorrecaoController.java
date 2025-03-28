package com.example.demo.controller;


import com.example.demo.dto.ApontamentoDTO;
import com.example.demo.service.VetorizacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/sugerir-correcao")

public class CorrecaoController {

    @Autowired
    private VetorizacaoService vetorizacaoService;

    @PostMapping
    public List<Float> sugerir(@RequestBody ApontamentoDTO dto) {
        return vetorizacaoService.gerarEmbedding(dto.getCodigo(), dto.getTipo());
    }

}
