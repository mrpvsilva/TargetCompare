package com.som.core;

import java.io.Serializable;

public class Parametros implements Serializable {

	private String constanteTempo;
	private String taxaAprendizado;
	private String nrNeuronios;
	private String raioGaussiana;

	public Parametros(String constanteTempo, String taxaAprendizado,
			String nrNeuronios, String raioGaussiana) {

		this.constanteTempo = constanteTempo+"";
		this.taxaAprendizado = taxaAprendizado+"";
		this.nrNeuronios = nrNeuronios+"";
		this.raioGaussiana = raioGaussiana+"";

	}

	public String getConstanteTempo() {
		return constanteTempo;
	}

	public String getTaxaAprendizado() {
		return taxaAprendizado;
	}

	public String getNrNeuronios() {
		return nrNeuronios;
	}

	public String getRaioGaussiana() {
		return raioGaussiana;
	}

}
