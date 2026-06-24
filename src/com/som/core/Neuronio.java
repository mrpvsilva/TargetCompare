package com.som.core;

import java.io.Serializable;

public class Neuronio implements Serializable {

	private static final long serialVersionUID = 1L;

	private double[] pesos;
	private double distanciaEuclidiana;
	private double gaussiana;

	public Neuronio(double[] pesos) {
		this.pesos = pesos;
	}

	public double getGaussiana() {
		return gaussiana;
	}

	public double[] getPesos() {
		return this.pesos;
	}

	public double getDistanciaEuclidiana() {
		return distanciaEuclidiana;
	}

	public void setDistanciaEuclidiana(double distanciaEuclidiana) {
		this.distanciaEuclidiana = distanciaEuclidiana;
	}

	public void setPesos(double[] pesos) {
		this.pesos = pesos;
	}

	public void setGaussiana(double gaussiana) {
		this.gaussiana = gaussiana;

	}

	

	

}
