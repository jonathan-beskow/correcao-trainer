package com.example.demo.model;

import java.util.List;

public class Exemplo {
    public String codigoOriginal;
    public String codigoCorrigido;
    public List<Float> embedding;

    public Exemplo(String codigoOriginal, String codigoCorrigido, List<Float> embedding) {
        this.codigoOriginal = codigoOriginal;
        this.codigoCorrigido = codigoCorrigido;
        this.embedding = embedding;
    }
}