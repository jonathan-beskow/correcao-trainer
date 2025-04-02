package com.example.demo.service;

import com.example.demo.model.CasoCorrigido;
import com.example.demo.repository.CasoCorrigidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CasoCorrigidoService {

    @Autowired
    private CasoCorrigidoRepository repository;

    public CasoCorrigido salvar(CasoCorrigido caso) {
        return repository.save(caso);
    }

    public List<String> listarTiposDeApontamentos() {
        List<CasoCorrigido> todos = repository.findAll();
        return todos.stream()
                .map(CasoCorrigido::getTipo)
                .distinct()
                .collect(Collectors.toList());
    }

}