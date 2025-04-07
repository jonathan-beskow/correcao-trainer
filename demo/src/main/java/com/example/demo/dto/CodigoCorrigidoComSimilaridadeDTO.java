package com.example.demo.dto;

public class CodigoCorrigidoComSimilaridadeDTO {

    private String codigoCorrigido;
    private double similaridade;

    public CodigoCorrigidoComSimilaridadeDTO(String codigoCorrigido, double similaridade) {
        this.codigoCorrigido = codigoCorrigido;
        this.similaridade = similaridade;
    }

    public String getCodigoCorrigido() {
        return codigoCorrigido;
    }

    public double getSimilaridade() {
        return similaridade;
    }
}
