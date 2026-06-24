package com.som.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import Jama.Matrix;

public class Rede implements Serializable {

	private static final long serialVersionUID = 2L;

	private static final double DECAY_THRESHOLD = 0.01;
	/** -ln(1e-6) ≈ 13.82 — gaussianas abaixo de 1e-6 são zeradas sem chamar Math.exp */
	private static final double EXP_CUTOFF = 13.815510557964274;

	private int nNeuronios;
	private int nDim;
	private int nAmostras;

	/**
	 * Pesos de todos os neurônios em array contíguo (row-major).
	 * Acesso: pesosFlat[i * nDim + j], onde i = neurônio, j = dimensão.
	 */
	private double[] pesosFlat;

	/**
	 * Vetor de entrada transposto em array contíguo (row-major).
	 * Acesso: vetorFlat[k * nDim + j], onde k = amostra, j = dimensão.
	 */
	private double[] vetorFlat;

	/** Cópia 2D de vetorFlat mantida apenas para getVetorEntrada(). */
	private double[][] vetorEntrada2D;

	/** Valores gaussianos calculados em cooperar() para cada neurônio. */
	private double[] gaussianas;

	/** Índice do neurônio vencedor no último passo de competição. */
	private int vencedorIdx;

	private double raioGaussiana;
	private double taxaAprendizado;
	private double constanteTempo;
	private int[][] resultado;

	public Rede(double[][] entrada, int nrNeuronios, double raioGaussiana,
			double taxaAprendizado, int constanteTempo) {
		transpor(entrada);
		criarRede(nrNeuronios);
		resultado = new int[nAmostras][nrNeuronios];
		this.taxaAprendizado = taxaAprendizado;
		this.constanteTempo = constanteTempo;
		this.raioGaussiana = raioGaussiana;
	}

	public void treinar() {
		// Fator constante por passo: exp(-1/T) calculado uma única vez,
		// eliminando 2 × constanteTempo chamadas a Math.exp durante o loop.
		final double decayFactor = Math.exp(-1.0 / constanteTempo);
		final ThreadLocalRandom rng = ThreadLocalRandom.current();

		for (int epoch = 1; epoch <= (int) constanteTempo; epoch++) {
			int idx = rng.nextInt(nAmostras);
			competir(idx);
			if (raioGaussiana > DECAY_THRESHOLD) raioGaussiana *= decayFactor;
			cooperar();
			adaptar(idx);
			if (taxaAprendizado > DECAY_THRESHOLD) taxaAprendizado *= decayFactor;
		}
	}

	public String classificar(String[] mirnas) {
		for (int i = 0; i < nAmostras; i++) {
			resultado[i][findBmuIndex(i)] = 1;
		}

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < nNeuronios; i++) {
			sb.append("N").append(i + 1).append("\t\t");
		}
		sb.append('\n');

		for (int i = 0; i < nAmostras; i++) {
			for (int j = 0; j < nNeuronios; j++) {
				sb.append(resultado[i][j] == 1 ? mirnas[i] : "").append("\t\t");
			}
			sb.append('\n');
		}

		return sb.toString();
	}

	/** Retorna o índice do neurônio mais próximo da amostra inputIdx. */
	private int findBmuIndex(int inputIdx) {
		int inputBase = inputIdx * nDim;
		int bmuIdx = 0;
		double minDistSq = Double.MAX_VALUE;
		for (int i = 0; i < nNeuronios; i++) {
			int iBase = i * nDim;
			double sum = 0;
			for (int j = 0; j < nDim; j++) {
				double d = pesosFlat[iBase + j] - vetorFlat[inputBase + j];
				sum += d * d;
			}
			if (sum < minDistSq) {
				minDistSq = sum;
				bmuIdx = i;
			}
		}
		return bmuIdx;
	}

	private void transpor(double[][] entrada) {
		Matrix m = new Matrix(entrada);
		vetorEntrada2D = m.transpose().getArray();
		nAmostras = vetorEntrada2D.length;
		nDim = vetorEntrada2D[0].length;
		vetorFlat = new double[nAmostras * nDim];
		for (int i = 0; i < nAmostras; i++) {
			System.arraycopy(vetorEntrada2D[i], 0, vetorFlat, i * nDim, nDim);
		}
	}

	private void criarRede(int nrNeuronios) {
		this.nNeuronios = nrNeuronios;
		pesosFlat  = new double[nNeuronios * nDim];
		gaussianas = new double[nNeuronios];

		double min = vetorFlat[0], max = vetorFlat[0];
		for (double v : vetorFlat) {
			if (v < min) min = v;
			if (v > max) max = v;
		}

		double range = max - min;
		ThreadLocalRandom rng = ThreadLocalRandom.current();
		for (int j = 0; j < pesosFlat.length; j++) {
			pesosFlat[j] = min + rng.nextDouble() * range;
		}
	}

	/**
	 * Encontra o neurônio vencedor comparando distâncias ao quadrado — sem Math.sqrt.
	 * Array plano garante acesso sequencial à memória (cache-friendly).
	 */
	private void competir(int idx) {
		int inputBase = idx * nDim;
		double minDistSq = Double.MAX_VALUE;
		vencedorIdx = 0;
		for (int i = 0; i < nNeuronios; i++) {
			int iBase = i * nDim;
			double sum = 0;
			for (int j = 0; j < nDim; j++) {
				double d = pesosFlat[iBase + j] - vetorFlat[inputBase + j];
				sum += d * d;
			}
			if (sum < minDistSq) {
				minDistSq = sum;
				vencedorIdx = i;
			}
		}
	}

	/**
	 * Gaussiana de vizinhança:
	 * — vencedor recebe 1.0 diretamente (evita cálculo e Math.exp com distSq=0);
	 * — neurônios com distSq/σ² ≥ EXP_CUTOFF recebem 0.0 (evita Math.exp);
	 * — demais: Math.exp(-distSq/σ²).
	 */
	private void cooperar() {
		int vBase = vencedorIdx * nDim;
		double sigma2 = 2.0 * raioGaussiana * raioGaussiana;
		for (int i = 0; i < nNeuronios; i++) {
			if (i == vencedorIdx) {
				gaussianas[i] = 1.0;
				continue;
			}
			int iBase = i * nDim;
			double distSq = 0;
			for (int j = 0; j < nDim; j++) {
				double d = pesosFlat[iBase + j] - pesosFlat[vBase + j];
				distSq += d * d;
			}
			gaussianas[i] = (distSq / sigma2 >= EXP_CUTOFF) ? 0.0 : Math.exp(-distSq / sigma2);
		}
	}

	/**
	 * Adapta os pesos. Neurônios com gaussiana zero são ignorados por inteiro.
	 * O produto η = taxaAprendizado × gaussiana é pré-calculado fora do loop interno.
	 */
	private void adaptar(int idx) {
		int inputBase = idx * nDim;
		for (int i = 0; i < nNeuronios; i++) {
			double gau = gaussianas[i];
			if (gau == 0.0) continue;
			int iBase = i * nDim;
			double eta = taxaAprendizado * gau;
			for (int j = 0; j < nDim; j++) {
				pesosFlat[iBase + j] += eta * (vetorFlat[inputBase + j] - pesosFlat[iBase + j]);
			}
		}
	}

	// --- mantido para compatibilidade com view.java ---
	public void Treinar() { treinar(); }
	public String Classificar(String[] mirnas) { return classificar(mirnas); }

	// --- API pública ---

	/**
	 * Materializa ArrayList<Neuronio> a partir do array interno.
	 * Chamado apenas pós-treinamento (plot, CSV, debug) — não está no hot path.
	 */
	public ArrayList<Neuronio> getNeuronios() {
		ArrayList<Neuronio> list = new ArrayList<>(nNeuronios);
		for (int i = 0; i < nNeuronios; i++) {
			double[] p = new double[nDim];
			System.arraycopy(pesosFlat, i * nDim, p, 0, nDim);
			Neuronio n = new Neuronio(p);
			n.setGaussiana(gaussianas[i]);
			list.add(n);
		}
		return list;
	}

	/** Materializa e retorna o último neurônio vencedor. */
	public Neuronio getVencedor() {
		double[] p = new double[nDim];
		System.arraycopy(pesosFlat, vencedorIdx * nDim, p, 0, nDim);
		return new Neuronio(p);
	}

	public double[][] getVetorEntrada() { return vetorEntrada2D; }
	public double getConstanteTempo() { return constanteTempo; }
	public double getRaioGaussiana() { return raioGaussiana; }
	public double getTaxaAprendizado() { return taxaAprendizado; }
}
