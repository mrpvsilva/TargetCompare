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
			decaimentoExponencial();
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
		double[] input = vetorEntrada[inputIndex];
		int bmuIndex = 0;
		double minDistSq = Double.MAX_VALUE;
		for (int i = 0; i < neuronios.size(); i++) {
			double[] pesos = neuronios.get(i).getPesos();
			double sum = 0;
			for (int j = 0; j < pesos.length; j++) {
				double d = pesos[j] - input[j];
				sum += d * d;
			}
			neuronios.get(i).setDistanciaEuclidiana(Math.sqrt(sum));
			if (sum < minDistSq) {
				minDistSq = sum;
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

	/**
	 * Encontra o neurônio vencedor (BMU) para a amostra dada em um único
	 * passo. Compara distâncias ao quadrado para evitar Math.sqrt na busca;
	 * armazena a distância euclidiana real no neurônio para uso externo.
	 */
	private void competir(int index) {
		double[] input = vetorEntrada[index];
		double minDistSq = Double.MAX_VALUE;
		vencedor = neuronios.get(0);
		for (Neuronio n : neuronios) {
			double[] pesos = n.getPesos();
			double sum = 0;
			for (int j = 0; j < pesos.length; j++) {
				double d = pesos[j] - input[j];
				sum += d * d;
			}
			n.setDistanciaEuclidiana(Math.sqrt(sum));
			if (sum < minDistSq) {
				minDistSq = sum;
				vencedor = n;
			}
		}
	}

	/**
	 * Calcula a gaussiana de vizinhança. Pré-calcula 2σ² fora do loop e opera
	 * com distâncias ao quadrado diretamente, sem Math.sqrt nem Math.pow.
	 */
	private void cooperar() {
		double[] pesosVencedor = vencedor.getPesos();
		double sigma2 = 2.0 * raioGaussiana * raioGaussiana;
		for (Neuronio n : neuronios) {
			double[] pesosN = n.getPesos();
			double distSq = 0;
			for (int j = 0; j < pesosN.length; j++) {
				double d = pesosN[j] - pesosVencedor[j];
				distSq += d * d;
			}
			n.setGaussiana(Math.exp(-distSq / sigma2));
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

	/**
	 * Decaimento por passo: σ(t+1) = σ(t) × exp(−1/T).
	 * Equivale a σ0 × exp(−t/T) sem precisar armazenar σ0,
	 * e garante decaimento uniforme ao longo de todas as épocas.
	 */
	private void decaimentoExponencial() {
		if (raioGaussiana > DECAY_THRESHOLD) {
			raioGaussiana *= Math.exp(-1.0 / constanteTempo);
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

	// --- mantido para compatibilidade com view.java ---
	public void Treinar() { treinar(); }
	public String Classificar(String[] mirnas) { return classificar(mirnas); }

	public Neuronio getVencedor() { return vencedor; }
	public double[][] getVetorEntrada() { return vetorEntrada; }
	public ArrayList<Neuronio> getNeuronios() { return neuronios; }
	public double getConstanteTempo() { return constanteTempo; }
	public double getRaioGaussiana() { return raioGaussiana; }
	public double getTaxaAprendizado() { return taxaAprendizado; }
}
