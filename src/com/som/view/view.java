package com.som.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import targetcompare.Compare;
import targetcompare.Gene;

import com.som.core.MetodosAcessorios;
import com.som.core.Neuronio;
import com.som.core.Parametros;
import com.som.core.Rede;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class view extends JFrame implements ActionListener {

	private JButton reiniciar;
	private JMenuItem jm_abrir;
	private JMenuItem jm_salvar;
	private JMenuItem jm_fechar;
	private JMenu jMenu1;
	private JMenuBar jMenuBar;
	private JButton jb_plot;
	private JButton treinar;

	private JTextField tf_epoca;
	private JTextField tf_nrNeuronio;
	private JTextField tf_txAprendizado;
	private JTextField tf_raioGaussiana;

	private JLabel jl_epoca;
	private JLabel jl_nrNeuronio;
	private JLabel jl_raioGaussiana;
	private JLabel jl_txAprendizado;
	private boolean iniciada;

	private ArrayList<Gene> list;
	private int constanteTempo;
	private double taxaAprendizado;
	private int nrNeuronios;
	private double raioGaussiana;
	private Rede r;
	private double[][] entrada; /*
								 * = { { 0.1, 0.1, 0.1, 0.1, 0.2, 0.2, 0.2, 0.2,
								 * 0.8, 0.8, 0.8, 0.8, 0.9,0.9, 0.9, 0.9 }, {
								 * 0.1, 0.2, 0.8, 0.9, 0.1, 0.2, 0.8, 0.9, 0.1,
								 * 0.2, 0.8, 0.9, 0.1,0.2, 0.8, 0.9 } };
								 */
	private String[] mirnas;
	private Compare context;

	public view(ArrayList<Gene> list, String[] mirnas, Compare context, int banco) {

		this.list = list;
		this.context = context;

		// Parâmetros SOM ajustados por banco:
		// micrornaorg (0): ~674 genes, 6 miRNAs → mais neurônios e épocas
		// targetScan  (1): ~108 genes, 5 miRNAs → rede menor
		if (banco == 0) {
			constanteTempo    = 8000;
			nrNeuronios       = 20;
			taxaAprendizado   = 0.7;
			raioGaussiana     = 0.30;
		} else {
			constanteTempo    = 5000;
			nrNeuronios       = 12;
			taxaAprendizado   = 0.7;
			raioGaussiana     = 0.25;
		}

		// Labels
		jl_epoca = new JLabel("Ep\u00F3cas:");
		jl_nrNeuronio = new JLabel("N\u00BA Neur\u00F4nios:");
		jl_raioGaussiana = new JLabel("Raio Gaussiano:");
		jl_txAprendizado = new JLabel("Taxa de Aprendizado:");

		// Text fields
		tf_epoca = new JTextField(6);
		tf_nrNeuronio = new JTextField(6);
		tf_raioGaussiana = new JTextField(6);
		tf_txAprendizado = new JTextField(6);

		// Buttons
		reiniciar = new JButton("Iniciar Rede");
		reiniciar.addActionListener(this);

		treinar = new JButton("Treinar Rede");
		treinar.setEnabled(false);
		treinar.addActionListener(this);

		jb_plot = new JButton("Plotar Gr\u00E1fico");
		jb_plot.setEnabled(false);
		jb_plot.addActionListener(this);

		// Menu
		jMenuBar = new JMenuBar();
		setJMenuBar(jMenuBar);
		jMenu1 = new JMenu("Arquivo");
		jMenuBar.add(jMenu1);
		jm_abrir = new JMenuItem("Abrir");
		jm_abrir.addActionListener(this);
		jMenu1.add(jm_abrir);
		jm_salvar = new JMenuItem("Salvar");
		jm_salvar.addActionListener(this);
		jMenu1.add(jm_salvar);
		jm_fechar = new JMenuItem("Fechar");
		jm_fechar.addActionListener(this);
		jMenu1.add(jm_fechar);

		// Parameter panel (GridBagLayout: 2 rows \u00D7 4 cols)
		JPanel paramPanel = new JPanel(new GridBagLayout());
		paramPanel.setBorder(BorderFactory.createTitledBorder("Par\u00E2metros da Rede"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 8, 4, 8);
		gbc.anchor = GridBagConstraints.WEST;

		gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE;
		paramPanel.add(jl_epoca, gbc);
		gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
		paramPanel.add(tf_epoca, gbc);
		gbc.gridx = 2; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		paramPanel.add(jl_nrNeuronio, gbc);
		gbc.gridx = 3; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
		paramPanel.add(tf_nrNeuronio, gbc);

		gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		paramPanel.add(jl_raioGaussiana, gbc);
		gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
		paramPanel.add(tf_raioGaussiana, gbc);
		gbc.gridx = 2; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
		paramPanel.add(jl_txAprendizado, gbc);
		gbc.gridx = 3; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
		paramPanel.add(tf_txAprendizado, gbc);

		// Button panel (GridLayout: 3 bot\u00F5es empilhados)
		JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 8));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
		buttonPanel.add(reiniciar);
		buttonPanel.add(treinar);
		buttonPanel.add(jb_plot);

		getContentPane().setLayout(new BorderLayout(0, 8));
		getContentPane().add(paramPanel, BorderLayout.NORTH);
		getContentPane().add(buttonPanel, BorderLayout.CENTER);

		this.setTitle("miRNACompare");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);

		if (!list.isEmpty()) {
			entrada = new double[list.size()][list.get(0).getMirna().length];
		} else {
			entrada = new double[0][0];
		}
		CarregarEntrada();
		this.mirnas = mirnas;
		CarregarParametros();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == reiniciar) {
			jb_plot.setEnabled(false);
			//CarregarParametros();
			IniciarRede();
			treinar.setEnabled(true);
			JOptionPane.showMessageDialog(null, "Rede iniciada");

		} else if (e.getSource() == treinar) {

			//IniciarRede();
			r.Treinar();
			JOptionPane.showMessageDialog(null, "Rede treinada com sucesso!");
			jb_plot.setEnabled(true);
			//r.Classificar(mirnas);
			
			context.setResultado(r.Classificar(mirnas));

		} else if (e.getSource() == jb_plot) {

			MetodosAcessorios.Plot(r, entrada);

		} else if (e.getSource() == jm_salvar) {

			try {

				MetodosAcessorios.SalvarRede("rede.ob", r);
				MetodosAcessorios.SalvarParametros("param.ob", new Parametros(
						tf_epoca.getText(), tf_txAprendizado.getText(),
						tf_nrNeuronio.getText(), tf_raioGaussiana.getText()));

				System.out.println("salvou");

			} catch (IOException e1) {

				e1.printStackTrace();
			}
		} else if (e.getSource() == jm_fechar) {

			dispose();

		} else if (e.getSource() == jm_abrir) {

			try {

				r = MetodosAcessorios.AbrirRede("rede.ob");
				Parametros param = MetodosAcessorios
						.AbrirParametros("param.ob");

				tf_epoca.setText(param.getConstanteTempo());
				tf_nrNeuronio.setText(param.getNrNeuronios());
				tf_raioGaussiana.setText(param.getRaioGaussiana());
				tf_txAprendizado.setText(param.getTaxaAprendizado());

				System.out.println("carregou");

			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

	}

	private void CarregarParametros() {

		tf_epoca.setText(constanteTempo + "");
		tf_nrNeuronio.setText(nrNeuronios + "");
		tf_raioGaussiana.setText(raioGaussiana + "");
		tf_txAprendizado.setText(taxaAprendizado + "");

	}

	private void IniciarRede() {

		try {
			r = new Rede(entrada, Integer.parseInt(tf_nrNeuronio.getText()),
					Double.parseDouble(tf_raioGaussiana.getText()),
					Double.parseDouble(tf_txAprendizado.getText()),
					Integer.parseInt(tf_epoca.getText()));
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this,
				"Erro: valores inv\u00E1lidos. Verifique se os campos cont\u00EAm n\u00FAmeros v\u00E1lidos.",
				"Par\u00E2metros Inv\u00E1lidos", JOptionPane.ERROR_MESSAGE);
		}

	}

	private void CarregarEntrada() {

		System.out.println("Padr\u00F5es de entrada");

		for (int i = 0; i < entrada.length; i++) {

			for (int j = 0; j < entrada[i].length; j++) {

				if (list.get(i).getMirna()[j].isAlvo()) {

					entrada[i][j] = 1;

				} else {

					entrada[i][j] = 0;

				}

			}		
		
		}
		
		MetodosAcessorios.PrintEntrada(entrada);

	}

	private void print() {

		System.out.println("Neur\u00F4nios");

		ArrayList<Neuronio> ls = r.getNeuronios();

		for (Neuronio neuronio : ls) {

			double[] p = neuronio.getPesos();

			for (double d : p) {
				System.out.print(MetodosAcessorios.arredondar(d, 6) + " ");
			}

			System.out.println();

		}

	}

	public static void main(String[] args) {

		/*JFrame jf = new view(null,null);
		jf.setVisible(true);
		jf.setDefaultCloseOperation(jf.EXIT_ON_CLOSE);*/
	}

}
