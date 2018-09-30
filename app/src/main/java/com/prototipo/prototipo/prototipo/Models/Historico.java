package com.prototipo.prototipo.prototipo.Models;

public class Historico {
    private String fecha;
    private Double consumo;

    public Historico() {
        this.fecha = "No disponible";
        this.consumo = 0.0;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Double getConsumo() {
        return consumo;
    }

    public void setConsumo(Double consumo) {
        this.consumo = consumo;
    }
}
