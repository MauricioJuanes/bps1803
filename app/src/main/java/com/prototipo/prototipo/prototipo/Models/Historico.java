package com.prototipo.prototipo.prototipo.Models;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        return new BigDecimal(consumo.toString()).setScale(2,RoundingMode.HALF_UP).doubleValue();
    }

    public void setConsumo(Double consumo) {
        this.consumo = consumo;
    }
}
