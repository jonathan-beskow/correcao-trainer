package com.example.demo.dto;

public class SugestaoCorrecaoDTO {
    private String tipo;
    private String linguagem;
    private String contexto;
    private String codigoOriginal;
    private String codigoCorrigido;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getLinguagem() {
        return linguagem;
    }

    public void setLinguagem(String linguagem) {
        this.linguagem = linguagem;
    }

    public String getContexto() {
        return contexto;
    }

    public void setContexto(String contexto) {
        this.contexto = contexto;
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

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public double getSimilaridade() {
        return similaridade;
    }

    public void setSimilaridade(double similaridade) {
        this.similaridade = similaridade;
    }
}