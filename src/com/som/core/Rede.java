package com.som.core;

import java.io.Serializable;
import java.util.ArrayList;

import Jama.Matrix;

public class Rede implements Serializable {

	private ArrayList<Neuronio> neuronios;
	private Neuronio vencedor;
	private double[][] vetorEntrada;
	private double raioGaussiana;
	private double taxaAprendizado;
	private double constanteTempo;
	private int[][] resultado;


	// construtor
	public Rede(double[][] entrada, int nrNeuronios, double raioGaussiana,
			double taxaAprendizado, int constanteTempo) {

		Transpor(entrada);
		CriarRede(nrNeuronios);
		resultado = new int[entrada[0].length][nrNeuronios];
		this.taxaAprendizado = taxaAprendizado;
		this.constanteTempo = constanteTempo;
		this.raioGaussiana = raioGaussiana;

	}

	public void Treinar() {

		for (int i = 1; i < constanteTempo + 1; i++) {

			int index = SortearIndex();
			Competir(index);
			DecaimentoExponencial(i);
			Cooperar();
			Adaptar(index);			
			TaxaAprendizado();
		}

	}

	public String Classificar(String[] mirnas) {

		for (int i = 0; i < resultado.length; i++) {

			int index = kkk(i);

			resultado[i][index] = 1;
		}

		String out = "";

		for (int i = 0; i < resultado[0].length; i++) {
			out += "N" + (i + 1) + "\t\t";
		}

		out += "\n";

		
		for (int i = 0; i < resultado.length; i++) {
			
			for (int j = 0; j < resultado[i].length; j++) {

				if (resultado[i][j] == 1) {
					
					out += mirnas[i];
					
					
				} else {
					
					out += "";
				}
				
				
				out += "\t\t";
			}
			
			out += "\n";
		}

		System.out.println(out);
		
		return out;

	}

	private int kkk(int vEntrada) {

		ArrayList<Neuronio> list = neuronios;

		int ret = 0;

		for (int i = 0; i < list.size(); i++) {

			double pow = 0;
			double[] pesos = list.get(i).getPesos();

			for (int j = 0; j < pesos.length; j++) {

				pow += Math.pow(pesos[j] - vetorEntrada[vEntrada][j], 2);
			}

			list.get(i).setDistanciaEuclidiana(Math.sqrt(pow));

		}

		double de = list.get(0).getDistanciaEuclidiana();

		for (int i = 1; i < list.size(); i++) {

			if (list.get(i).getDistanciaEuclidiana() < de) {
				ret = i;
				de = list.get(i).getDistanciaEuclidiana();
			}

		}

		return ret;
	}

	private void Transpor(double[][] vetorEntrada) {
		Matrix m = new Matrix(vetorEntrada);
		m = m.transpose();
		this.vetorEntrada = m.getArray();
	}

	private void CriarRede(int nrNeuronios) {

		neuronios = new ArrayList<Neuronio>();

		double maior = vetorEntrada[0][0];
		double menor = vetorEntrada[0][0];

		for (int i = 0; i < vetorEntrada.length; i++) {

			for (int j = 0; j < vetorEntrada[i].length; j++) {

				if (vetorEntrada[i][j] < menor) {
					menor = vetorEntrada[i][j];
				}

				if (vetorEntrada[i][j] > maior) {
					maior = vetorEntrada[i][j];
				}

			}

		}

		for (int i = 0; i < nrNeuronios; i++) {
			double[] pesosNeuronios = new double[vetorEntrada[0].length];
			for (int j = 0; j < pesosNeuronios.length; j++) {
				pesosNeuronios[j] = menor + Math.random() * (maior - menor);
			}
			neuronios.add(new Neuronio(pesosNeuronios));

		}

		// System.out.println("pesos dos neuronios inicializados");

	}

	private void Competir(int index) {

		CalcularDistanciaEuclidiana(index);
		EncontrarNeuronioVencedor();

	}

	private void Cooperar() {

		for (int i = 0; i < neuronios.size(); i++) {

			double gau = MetodosAcessorios.arredondar(Math.pow(Math.E,-((Math.pow(DistanciaNeuronioToVencedor(i), 2) / (2 * Math.pow(raioGaussiana, 2))))), 8);

			neuronios.get(i).setGaussiana(gau);

		}

	}

	private void Adaptar(int index) {

		for (int i = 0; i < neuronios.size(); i++) {

			double[] pesosN = neuronios.get(i).getPesos();

			for (int j = 0; j < pesosN.length; j++) {

				pesosN[j] = pesosN[j] + (taxaAprendizado * neuronios.get(i).getGaussiana() * (vetorEntrada[index][j] - pesosN[j]) ) ;

			}

			this.neuronios.get(i).setPesos(pesosN);
		}

	}

	private void DecaimentoExponencial(int epoca) {

		if (raioGaussiana > 0.01) {
			raioGaussiana = raioGaussiana * Math.pow(Math.E, -((double)epoca / constanteTempo));
		}

	}

	private void TaxaAprendizado() {

		if (taxaAprendizado > 0.01) {
			taxaAprendizado = taxaAprendizado * Math.pow(Math.E, -1.0 / constanteTempo);
		}

	}

	public Neuronio getVencedor() {
		return vencedor;
	}

	public double[][] getVetorEntrada() {
		return vetorEntrada;
	}

	public ArrayList<Neuronio> getNeuronios() {
		return this.neuronios;
	}

	private double DistanciaNeuronioToVencedor(int index) {

		double[] pesosVencedor = vencedor.getPesos();

		double pow = 0;
		double pesosNeuronio[] = neuronios.get(index).getPesos();

		for (int j = 0; j < pesosNeuronio.length; j++) {

			pow += Math.pow(pesosNeuronio[j] - pesosVencedor[j], 2);
		}

		return Math.sqrt(pow);

	}

	private int SortearIndex() {

		return (int) (Math.random() * (vetorEntrada.length));

	}

	private void CalcularDistanciaEuclidiana(int index) {

		for (int i = 0; i < neuronios.size(); i++) {

			double pow = 0;
			double[] pesos = neuronios.get(i).getPesos();

			for (int j = 0; j < pesos.length; j++) {

				pow += Math.pow(pesos[j] - vetorEntrada[index][j], 2);

			}

			neuronios.get(i).setDistanciaEuclidiana(Math.sqrt(pow));

		}
	}

	private void EncontrarNeuronioVencedor() {
		double menor = neuronios.get(0).getDistanciaEuclidiana();
		vencedor = neuronios.get(0);

		for (int i = 1; i < neuronios.size(); i++) {

			if (neuronios.get(i).getDistanciaEuclidiana() < menor) {

				menor = neuronios.get(i).getDistanciaEuclidiana();
				vencedor = neuronios.get(i);

			}

		}

	}

	public double getConstanteTempo() {
		return constanteTempo;
	}

	public double getRaioGaussiana() {
		return raioGaussiana;
	}

	public double getTaxaAprendizado() {
		return taxaAprendizado;
	}

}
