package com.som.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import Jama.Matrix;

import com.som.view.ScatterAdd;

public class MetodosAcessorios {

	public static String getDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("ddMMyyyyHHmmss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	private static String replace(double value) {
		String out = value + "";
		out = out.replace('.', ',');

		return out;
	}

	public static void Plot(Rede r, double[][] entrada) {
		ArrayList<Neuronio> listaTreinada = r.getNeuronios();

		// Inicializa o vetor para a separa os valores de x e y para enviar pro
		// JFrame de plot
		double[] xe = entrada[0];
		double[] ye = entrada[1];
		double[] xs = new double[listaTreinada.size()];
		double[] ys = new double[listaTreinada.size()];

		// Recebe os resultado
		for (int i = 0; i < listaTreinada.size(); i++) {

			// Pega os pesos dos neur\u00F4nios
			double[] f = listaTreinada.get(i).getPesos();

			// Atribui os valores de x e y da rede treinada
			for (int j = 0; j < f.length; j++) {

				// gambiarra fdp
				if (j == 0) {
					xs[i] = f[j];
				} else {
					ys[i] = f[j];
				}

			}// Final do para atribuir os valores de x e y da rede treinada

		}// Final do la\u00E7o

		ScatterAdd plotagem = new ScatterAdd(null, xe, ye, xs, ys);
		plotagem.setVisible(true);
	}

	public static void generateCsv(String arquivo, Rede r) {
		try {
			FileWriter writer = new FileWriter(arquivo);
			ArrayList<Neuronio> list = r.getNeuronios();

			for (int i = 0; i < list.size(); i++) {

				double[] pesos = list.get(i).getPesos();

				for (int j = 0; j < pesos.length; j++) {
					writer.append(replace(pesos[j]));
					if (j == 0) {
						writer.append('\t');
					}

				}
				writer.append('\n');
			}

			// generate whatever data you want

			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void PrintNeuronioVencedor(Neuronio vencedor, int ind) {

		double[] pesos = vencedor.getPesos();

		for (int j = 0; j < pesos.length; j++) {
			System.out.print("P" + (j + 1) + "VN" + (ind + 1) + ": "
					+ arredondar(pesos[j], 4) + " ");

		}
		System.out.println();

	}

	public static void PrintNeuronios(ArrayList<Neuronio> n) {

		for (int i = 0; i < n.size(); i++) {

			double[] pesos = n.get(i).getPesos();

			for (int j = 0; j < pesos.length; j++) {
				System.out.print("P" + (j + 1) + "N" + (i + 1) + ": "
						+ arredondar(pesos[j], 4) + " ");

			}
			System.out.print("GauN" + (i + 1) + ": "
					+ arredondar(n.get(i).getGaussiana(), 20) + " ");
			System.out.print("DEN" + (i + 1) + ": "
					+ arredondar(n.get(i).getDistanciaEuclidiana(), 20) + "\n");

		}

	}

	public static void PrintEntrada(double[][] entrada) {

		Matrix m = new Matrix(entrada);
		m = m.transpose();
		entrada = m.getArray();
		
		for (int i = 0; i < entrada.length; i++) {
			
			for (int j = 0; j < entrada[i].length; j++) {
				
				System.out.print(entrada[i][j]+" ");
				
			}
			System.out.println();
		}
		
		
	}

	public static double arredondar(double valor, int casas) {
		double arredondado = valor;

		arredondado *= (Math.pow(10, casas));
		arredondado = Math.floor(arredondado);
		arredondado /= (Math.pow(10, casas));
		return arredondado;
	}

	public static void SalvarRede(String nomeArquivo, Rede rede)
			throws IOException {
		if ((rede != null) && (nomeArquivo != null)) {
			try (FileOutputStream fos = new FileOutputStream(nomeArquivo);
				 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
				oos.writeObject(rede);
			}
		}

	}

	public static void SalvarParametros(String nomeArquivo, Parametros param)
			throws IOException {
		if ((param != null) && (nomeArquivo != null)) {
			try (FileOutputStream fos = new FileOutputStream(nomeArquivo);
				 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
				oos.writeObject(param);
			}
		}
	}

	public static Rede AbrirRede(String nomeArquivo) throws IOException,
			ClassNotFoundException {
		Rede rede = null;

		if (nomeArquivo != null) {
			try (FileInputStream fis = new FileInputStream(nomeArquivo);
				 ObjectInputStream ois = new ObjectInputStream(fis)) {
				rede = (Rede) ois.readObject();
			}
		}
		return rede;

	}

	public static Parametros AbrirParametros(String nomeArquivo)
			throws IOException, ClassNotFoundException {
		Parametros param = null;

		if (nomeArquivo != null) {
			try (FileInputStream fis = new FileInputStream(nomeArquivo);
				 ObjectInputStream ois = new ObjectInputStream(fis)) {
				param = (Parametros) ois.readObject();
			}
		}
		return param;

	}

}
