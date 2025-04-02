package com.example.demo.dto;

public class TipoContagemDTO {
    private String tipo;
    private long total;

    public TipoContagemDTO(String tipo, long total) {
        this.tipo = tipo;
        this.total = total;
    }

    public String getTipo() {
        return tipo;
    }

    public long getTotal() {
        return total;
    }
}
