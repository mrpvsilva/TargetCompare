package com.som.core;

import java.io.Serializable;

public class Parametros implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int constanteTempo;
    private final double taxaAprendizado;
    private final int nrNeuronios;
    private final double raioGaussiana;

    public Parametros(int constanteTempo, double taxaAprendizado,
            int nrNeuronios, double raioGaussiana) {
        this.constanteTempo = constanteTempo;
        this.taxaAprendizado = taxaAprendizado;
        this.nrNeuronios = nrNeuronios;
        this.raioGaussiana = raioGaussiana;
    }

    public int getConstanteTempo() {
        return constanteTempo;
    }

    public double getTaxaAprendizado() {
        return taxaAprendizado;
    }

    public int getNrNeuronios() {
        return nrNeuronios;
    }

    public double getRaioGaussiana() {
        return raioGaussiana;
    }
}
