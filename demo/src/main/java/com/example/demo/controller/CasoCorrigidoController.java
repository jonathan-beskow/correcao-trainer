package com.example.demo.controller;

import com.example.demo.model.CasoCorrigido;
import com.example.demo.service.CasoCorrigidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/casos-corrigidos")
public class CasoCorrigidoController {

    @Autowired
    private CasoCorrigidoService service;

    @PostMapping
    public CasoCorrigido cadastrar(@RequestBody CasoCorrigido caso) {
        return service.salvar(caso);
    }

    @GetMapping("/tipos")
    public List<String> listarTipos() {
        return service.listarTiposDeApontamentos();
    }

}