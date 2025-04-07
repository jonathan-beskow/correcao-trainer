package com.example.demo.dto;

public class SimilaridadeRequest {

    private String tipo;
    private String codigo;
    private int k = 3;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
