package com.som.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import Jama.Matrix;

import com.som.view.ScatterAdd;

public class MetodosAcessorios {

    public static void Plot(Rede r, double[][] entrada) {
        ArrayList<Neuronio> listaTreinada = r.getNeuronios();

        double[] xe = entrada[0];
        double[] ye = entrada[1];
        double[] xs = new double[listaTreinada.size()];
        double[] ys = new double[listaTreinada.size()];

        for (int i = 0; i < listaTreinada.size(); i++) {
            double[] pesos = listaTreinada.get(i).getPesos();
            xs[i] = pesos[0];
            ys[i] = pesos.length > 1 ? pesos[1] : 0;
        }

        ScatterAdd plotagem = new ScatterAdd(null, xe, ye, xs, ys);
        plotagem.setVisible(true);
    }

    public static void generateCsv(String arquivo, Rede r) {
        try (FileWriter writer = new FileWriter(arquivo)) {
            for (Neuronio n : r.getNeuronios()) {
                double[] pesos = n.getPesos();
                for (int j = 0; j < pesos.length; j++) {
                    writer.append(formatDecimal(pesos[j]));
                    if (j == 0) {
                        writer.append('\t');
                    }
                }
                writer.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double arredondar(double valor, int casas) {
        double fator = Math.pow(10, casas);
        return Math.floor(valor * fator) / fator;
    }

    public static void SalvarRede(String nomeArquivo, Rede rede) throws IOException {
        if (rede != null && nomeArquivo != null) {
            try (FileOutputStream fos = new FileOutputStream(nomeArquivo);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(rede);
            }
        }
    }

    public static void SalvarParametros(String nomeArquivo, Parametros param) throws IOException {
        if (param != null && nomeArquivo != null) {
            try (FileOutputStream fos = new FileOutputStream(nomeArquivo);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                oos.writeObject(param);
            }
        }
    }

    public static Rede AbrirRede(String nomeArquivo) throws IOException, ClassNotFoundException {
        if (nomeArquivo == null) return null;
        try (FileInputStream fis = new FileInputStream(nomeArquivo);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (Rede) ois.readObject();
        }
    }

    public static Parametros AbrirParametros(String nomeArquivo) throws IOException, ClassNotFoundException {
        if (nomeArquivo == null) return null;
        try (FileInputStream fis = new FileInputStream(nomeArquivo);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (Parametros) ois.readObject();
        }
    }

    private static String formatDecimal(double value) {
        return String.valueOf(value).replace('.', ',');
    }
}
