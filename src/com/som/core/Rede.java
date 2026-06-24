package com.som.core;

import java.io.Serializable;
import java.util.ArrayList;

import Jama.Matrix;

public class Rede implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final double DECAY_THRESHOLD = 0.01;

	private ArrayList<Neuronio> neuronios;
	private Neuronio vencedor;
	private double[][] vetorEntrada;
	private double raioGaussiana;
	private double taxaAprendizado;
	private double constanteTempo;
	private int[][] resultado;

	public Rede(double[][] entrada, int nrNeuronios, double raioGaussiana,
			double taxaAprendizado, int constanteTempo) {
		transpor(entrada);
		criarRede(nrNeuronios);
		resultado = new int[entrada[0].length][nrNeuronios];
		this.taxaAprendizado = taxaAprendizado;
		this.constanteTempo = constanteTempo;
		this.raioGaussiana = raioGaussiana;
	}

	public void treinar() {
		for (int i = 1; i < constanteTempo + 1; i++) {
			int index = sortearIndex();
			competir(index);
			decaimentoExponencial(i);
			cooperar();
			adaptar(index);
			atualizarTaxaAprendizado();
		}
	}

	public String classificar(String[] mirnas) {
		for (int i = 0; i < resultado.length; i++) {
			resultado[i][findBmuIndex(i)] = 1;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < resultado[0].length; i++) {
			sb.append("N").append(i + 1).append("\t\t");
		}
		sb.append('\n');

		for (int i = 0; i < resultado.length; i++) {
			for (int j = 0; j < resultado[i].length; j++) {
				sb.append(resultado[i][j] == 1 ? mirnas[i] : "").append("\t\t");
			}
			sb.append('\n');
		}

		return sb.toString();
	}

	private int findBmuIndex(int inputIndex) {
		for (int i = 0; i < neuronios.size(); i++) {
			double sum = 0;
			double[] pesos = neuronios.get(i).getPesos();
			for (int j = 0; j < pesos.length; j++) {
				sum += Math.pow(pesos[j] - vetorEntrada[inputIndex][j], 2);
			}
			neuronios.get(i).setDistanciaEuclidiana(Math.sqrt(sum));
		}

		int bmuIndex = 0;
		double menorDistancia = neuronios.get(0).getDistanciaEuclidiana();
		for (int i = 1; i < neuronios.size(); i++) {
			double d = neuronios.get(i).getDistanciaEuclidiana();
			if (d < menorDistancia) {
				menorDistancia = d;
				bmuIndex = i;
			}
		}
		return bmuIndex;
	}

	private void transpor(double[][] vetorEntrada) {
		Matrix m = new Matrix(vetorEntrada);
		this.vetorEntrada = m.transpose().getArray();
	}

	private void criarRede(int nrNeuronios) {
		neuronios = new ArrayList<>();

		double maior = vetorEntrada[0][0];
		double menor = vetorEntrada[0][0];

		for (double[] linha : vetorEntrada) {
			for (double valor : linha) {
				if (valor < menor) menor = valor;
				if (valor > maior) maior = valor;
			}
		}

		for (int i = 0; i < nrNeuronios; i++) {
			double[] pesos = new double[vetorEntrada[0].length];
			for (int j = 0; j < pesos.length; j++) {
				pesos[j] = menor + Math.random() * (maior - menor);
			}
			neuronios.add(new Neuronio(pesos));
		}
	}

	private void competir(int index) {
		calcularDistanciaEuclidiana(index);
		encontrarNeuronioVencedor();
	}

	private void cooperar() {
		for (Neuronio n : neuronios) {
			double dist = distanciaNeuronioParaVencedor(n);
			double gau = MetodosAcessorios.arredondar(
					Math.exp(-(Math.pow(dist, 2) / (2 * Math.pow(raioGaussiana, 2)))), 8);
			n.setGaussiana(gau);
		}
	}

	private void adaptar(int index) {
		for (Neuronio n : neuronios) {
			double[] pesos = n.getPesos();
			for (int j = 0; j < pesos.length; j++) {
				pesos[j] += taxaAprendizado * n.getGaussiana() * (vetorEntrada[index][j] - pesos[j]);
			}
			n.setPesos(pesos);
		}
	}

	private void decaimentoExponencial(int epoca) {
		if (raioGaussiana > DECAY_THRESHOLD) {
			raioGaussiana *= Math.exp(-((double) epoca / constanteTempo));
		}
	}

	private void atualizarTaxaAprendizado() {
		if (taxaAprendizado > DECAY_THRESHOLD) {
			taxaAprendizado *= Math.exp(-1.0 / constanteTempo);
		}
	}

	private int sortearIndex() {
		return (int) (Math.random() * vetorEntrada.length);
	}

	private void calcularDistanciaEuclidiana(int index) {
		for (Neuronio n : neuronios) {
			double sum = 0;
			double[] pesos = n.getPesos();
			for (int j = 0; j < pesos.length; j++) {
				sum += Math.pow(pesos[j] - vetorEntrada[index][j], 2);
			}
			n.setDistanciaEuclidiana(Math.sqrt(sum));
		}
	}

	private void encontrarNeuronioVencedor() {
		vencedor = neuronios.get(0);
		double menor = vencedor.getDistanciaEuclidiana();
		for (int i = 1; i < neuronios.size(); i++) {
			Neuronio n = neuronios.get(i);
			if (n.getDistanciaEuclidiana() < menor) {
				menor = n.getDistanciaEuclidiana();
				vencedor = n;
			}
		}
	}

	private double distanciaNeuronioParaVencedor(Neuronio n) {
		double[] pesosVencedor = vencedor.getPesos();
		double[] pesosN = n.getPesos();
		double sum = 0;
		for (int j = 0; j < pesosN.length; j++) {
			sum += Math.pow(pesosN[j] - pesosVencedor[j], 2);
		}
		return Math.sqrt(sum);
	}

	// --- kept for backward compatibility with view.java ---
	public void Treinar() { treinar(); }
	public String Classificar(String[] mirnas) { return classificar(mirnas); }

	public Neuronio getVencedor() { return vencedor; }
	public double[][] getVetorEntrada() { return vetorEntrada; }
	public ArrayList<Neuronio> getNeuronios() { return neuronios; }
	public double getConstanteTempo() { return constanteTempo; }
	public double getRaioGaussiana() { return raioGaussiana; }
	public double getTaxaAprendizado() { return taxaAprendizado; }
}
