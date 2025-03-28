package com.example.demo.service;

import com.example.demo.model.CasoCorrigido;
import com.example.demo.repository.CasoCorrigidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CasoCorrigidoService {

    @Autowired
    private CasoCorrigidoRepository repository;

    public CasoCorrigido salvar(CasoCorrigido caso) {
        return repository.save(caso);
    }
}