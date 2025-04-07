package com.example.demo.repository;


import com.example.demo.model.CasoCorrigido;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CasoCorrigidoRepository extends MongoRepository<CasoCorrigido, String> {


    Optional<CasoCorrigido> findByCodigoOriginal(String codigoOriginal);

    Optional<CasoCorrigido> findByCodigoCorrigido(String codigoCorrigido);


}