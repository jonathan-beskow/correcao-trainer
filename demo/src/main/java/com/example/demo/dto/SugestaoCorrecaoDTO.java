package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SugestaoCorrecaoDTO {
    private String tipo;

    @JsonIgnore
    private String linguagem;

    @JsonIgnore
    private String contexto;

    private String codigoOriginal;
    private String codigoCorrigido;

    @JsonIgnore
    private String justificativa;

    private double similaridade;



    public SugestaoCorrecaoDTO(String tipo, String linguagem, String contexto,
                               String codigoOriginal, String codigoCorrigido,
                               String justificativa, double similaridade) {
        this.tipo = tipo;
        this.linguagem = linguagem;
        this.contexto = contexto;
        this.codigoOriginal = codigoOriginal;
        this.codigoCorrigido = codigoCorrigido;
        this.justificativa = justificativa;
        this.similaridade = similaridade;
    }

    public SugestaoCorrecaoDTO(String tipo, String codigoOriginal, String codigoCorrigido, double similaridade){
        this.tipo = tipo;
        this.codigoOriginal = codigoOriginal;
        this.codigoCorrigido = codigoCorrigido;
        this.similaridade = similaridade;

    }
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCodigoOriginal() {
        return codigoOriginal;
    }

    public String getCodigoCorrigido() {
        return codigoCorrigido;
    }

    public double getSimilaridade() {
        return similaridade;
    }
}