package com.som.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

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
import javax.swing.SwingWorker;

import targetcompare.Compare;
import targetcompare.Gene;

import com.som.core.MetodosAcessorios;
import com.som.core.Parametros;
import com.som.core.Rede;

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

	private List<Gene> list;
	private int constanteTempo;
	private double taxaAprendizado;
	private int nrNeuronios;
	private double raioGaussiana;
	private Rede r;
	private double[][] entrada;
	private String[] mirnas;
	private Compare context;

	public view(List<Gene> list, String[] mirnas, Compare context, int banco) {
		this.list = list;
		this.context = context;

		// Parâmetros ajustados por banco:
		// micrornaorg (0): ~674 genes, 6 miRNAs → mais neurônios e épocas
		// targetScan  (1): ~108 genes, 5 miRNAs → rede menor
		if (banco == 0) {
			constanteTempo  = 8000;
			nrNeuronios     = 20;
			taxaAprendizado = 0.7;
			raioGaussiana   = 0.30;
		} else {
			constanteTempo  = 5000;
			nrNeuronios     = 12;
			taxaAprendizado = 0.7;
			raioGaussiana   = 0.25;
		}

		jl_epoca         = new JLabel("Épocas:");
		jl_nrNeuronio    = new JLabel("Nº Neurônios:");
		jl_raioGaussiana = new JLabel("Raio Gaussiano:");
		jl_txAprendizado = new JLabel("Taxa de Aprendizado:");

		tf_epoca         = new JTextField(6);
		tf_nrNeuronio    = new JTextField(6);
		tf_raioGaussiana = new JTextField(6);
		tf_txAprendizado = new JTextField(6);

		reiniciar = new JButton("Iniciar Rede");
		reiniciar.addActionListener(this);

		treinar = new JButton("Treinar Rede");
		treinar.setEnabled(false);
		treinar.addActionListener(this);

		jb_plot = new JButton("Plotar Gráfico");
		jb_plot.setEnabled(false);
		jb_plot.addActionListener(this);

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

		JPanel paramPanel = new JPanel(new GridBagLayout());
		paramPanel.setBorder(BorderFactory.createTitledBorder("Parâmetros da Rede"));
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

		JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 8));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
		buttonPanel.add(reiniciar);
		buttonPanel.add(treinar);
		buttonPanel.add(jb_plot);

		getContentPane().setLayout(new BorderLayout(0, 8));
		getContentPane().add(paramPanel, BorderLayout.NORTH);
		getContentPane().add(buttonPanel, BorderLayout.CENTER);

		setTitle("miRNACompare");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);

		entrada = list.isEmpty()
				? new double[0][0]
				: new double[list.size()][list.get(0).getMirnas().size()];
		carregarEntrada();
		this.mirnas = mirnas;
		carregarParametros();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == reiniciar) {
			jb_plot.setEnabled(false);
			iniciarRede();
			treinar.setEnabled(true);
			JOptionPane.showMessageDialog(null, "Rede iniciada");

		} else if (e.getSource() == treinar) {
			treinar.setEnabled(false);
			reiniciar.setEnabled(false);
			jb_plot.setEnabled(false);
			new SwingWorker<String, Void>() {
				@Override
				protected String doInBackground() {
					r.Treinar();
					return r.Classificar(mirnas);
				}
				@Override
				protected void done() {
					reiniciar.setEnabled(true);
					treinar.setEnabled(true);
					try {
						context.setResultado(get());
						jb_plot.setEnabled(true);
						JOptionPane.showMessageDialog(view.this, "Rede treinada com sucesso!");
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(view.this,
								"Erro no treinamento: " + ex.getMessage(),
								"Erro", JOptionPane.ERROR_MESSAGE);
					}
				}
			}.execute();

		} else if (e.getSource() == jb_plot) {
			MetodosAcessorios.Plot(r, entrada);

		} else if (e.getSource() == jm_salvar) {
			try {
				Parametros param = new Parametros(
						Integer.parseInt(tf_epoca.getText()),
						Double.parseDouble(tf_txAprendizado.getText()),
						Integer.parseInt(tf_nrNeuronio.getText()),
						Double.parseDouble(tf_raioGaussiana.getText()));
				MetodosAcessorios.SalvarRede("rede.ob", r);
				MetodosAcessorios.SalvarParametros("param.ob", param);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this,
						"Valores inválidos nos campos de parâmetros.",
						"Erro ao Salvar", JOptionPane.ERROR_MESSAGE);
			} catch (IOException ex) {
				JOptionPane.showMessageDialog(this,
						"Erro ao salvar arquivo: " + ex.getMessage(),
						"Erro de I/O", JOptionPane.ERROR_MESSAGE);
			}

		} else if (e.getSource() == jm_fechar) {
			dispose();

		} else if (e.getSource() == jm_abrir) {
			try {
				r = MetodosAcessorios.AbrirRede("rede.ob");
				Parametros param = MetodosAcessorios.AbrirParametros("param.ob");
				if (param != null) {
					tf_epoca.setText(String.valueOf(param.getConstanteTempo()));
					tf_nrNeuronio.setText(String.valueOf(param.getNrNeuronios()));
					tf_raioGaussiana.setText(String.valueOf(param.getRaioGaussiana()));
					tf_txAprendizado.setText(String.valueOf(param.getTaxaAprendizado()));
				}
			} catch (ClassNotFoundException | IOException ex) {
				JOptionPane.showMessageDialog(this,
						"Erro ao abrir arquivo: " + ex.getMessage(),
						"Erro de I/O", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void carregarParametros() {
		tf_epoca.setText(String.valueOf(constanteTempo));
		tf_nrNeuronio.setText(String.valueOf(nrNeuronios));
		tf_raioGaussiana.setText(String.valueOf(raioGaussiana));
		tf_txAprendizado.setText(String.valueOf(taxaAprendizado));
	}

	private void iniciarRede() {
		try {
			r = new Rede(entrada,
					Integer.parseInt(tf_nrNeuronio.getText()),
					Double.parseDouble(tf_raioGaussiana.getText()),
					Double.parseDouble(tf_txAprendizado.getText()),
					Integer.parseInt(tf_epoca.getText()));
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this,
					"Erro: valores inválidos. Verifique se os campos contêm números válidos.",
					"Parâmetros Inválidos", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void carregarEntrada() {
		for (int i = 0; i < list.size(); i++) {
			List<targetcompare.Mirna> mirnaList = list.get(i).getMirnas();
			for (int j = 0; j < mirnaList.size(); j++) {
				entrada[i][j] = mirnaList.get(j).isAlvo() ? 1 : 0;
			}
		}
	}
}
