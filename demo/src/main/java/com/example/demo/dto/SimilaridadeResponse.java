package com.example.demo.dto;

public class SimilaridadeResponse {

    private String tipo;
    private String codigoOriginal;
    private String codigoCorrigido;
    private double similaridade;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCodigoOriginal() {
        return codigoOriginal;
    }

    public void setCodigoOriginal(String codigoOriginal) {
        this.codigoOriginal = codigoOriginal;
    }

    public String getCodigoCorrigido() {
        return codigoCorrigido;
    }

    public void setCodigoCorrigido(String codigoCorrigido) {
        this.codigoCorrigido = codigoCorrigido;
    }

    public double getSimilaridade() {
        return similaridade;
    }

    public void setSimilaridade(double similaridade) {
        this.similaridade = similaridade;
    }
}
