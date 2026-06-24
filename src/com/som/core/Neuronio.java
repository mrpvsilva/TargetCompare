package com.som.core;

import java.io.Serializable;

public class Neuronio implements Serializable {

	//atributes
	private double[] pesos;
	private double distanciaEuclidiana;
	private double gaussiana;
	
	//Construtor
	public Neuronio(double[] pesos) {
		this.pesos = pesos;
	}

	//Get's and Set's
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
