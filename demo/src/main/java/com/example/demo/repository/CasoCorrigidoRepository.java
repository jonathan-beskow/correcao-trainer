package com.example.demo.repository;


import com.example.demo.model.CasoCorrigido;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CasoCorrigidoRepository extends MongoRepository<CasoCorrigido, String> {
}