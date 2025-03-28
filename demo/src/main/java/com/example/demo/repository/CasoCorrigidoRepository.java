package com.example.demo.repository;


import com.example.demo.model.CasoCorrigido;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CasoCorrigidoRepository extends MongoRepository<CasoCorrigido, String> {

    List<CasoCorrigido> findByTipo(String tipo);
}