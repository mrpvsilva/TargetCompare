/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Compare.java
 *
 * Created on 20/01/2010, 19:28:42
 */

package targetcompare;

import java.util.ArrayList;
import com.som.view.view;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

/**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
/**
 * 
 * @author fabiano
 */
public class Compare extends JFrame implements ActionListener {

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JLabel jLabel1;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JPanel jPanel1;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	private JButton jbInicio;
	private JComboBox jcbBanco;
	private JComboBox jcbMin;
	private JTextArea jtEntrada;
	private JButton jb_som;
	private JTextArea jtSaida;
	private ArrayList<ArrayList<String>> target;
	private ArrayList<Gene> matches;
	private String[] mirnas;
	private int min, max;

	public static final int REPETIDO = 0;
	public static final int MATCH = 1;
	public static final int NOVO = 2;

	/** Creates new form Compare */
	public Compare() {
		initComponents();
	}

	public void iniciarBusca(String texto) {
		target = new ArrayList<ArrayList<String>>();
		matches = new ArrayList<Gene>();

		String nome[];
		nome = texto.split(",");

		setMax(nome.length); // o maior n\u00FAmero de matches \u00E9 sempre igual ao
								// n\u00FAmero de microRNAs
		mirnas = new String[nome.length];

		for (int i = 0; i < nome.length; i++) {

			getMirnas()[i] = nome[i].replace(" ", "");
		}

		// l\u00EA o arquivo e divide os targets de cada arquivo
		for (int i = 0; i < getMirnas().length; i++)
			criaVetor(getMirnas()[i]);

		matching();
		analizar();

	}

	public void analizar() {

		String saida = "nome\t";
		Gene gene;
		ArrayList<Gene> al = new ArrayList<Gene>();

		for (int i = max; i > min; i--) {// come\u00E7ando pela qtdade maxima
			for (int j = 0; j < matches.size(); j++) {// v\u00E1 em todos os genes
														// que tiveram match e
														// compare com a qtdade
														// exigida
				gene = matches.get(j);
				if (gene.getQtdade() == i)
					al.add(gene);// se der, adicione em al
			}
		}

		for (int i = 0; i < getMirnas().length; i++)
			saida += getMirnas()[i] + "\t";
		saida += "\n\n";

		for (int i = 0; i < al.size(); i++) {// imprimir os genes q obtiveram a
												// quantidade de matches
			gene = (Gene) al.get(i);
			saida += gene.printMirna() + "\n";
		}

		saida += "\n genes alvos em comum: " + matches.size();
		saida += "\n com matches >= " + (min + 1) + ": " + al.size();

		jtSaida.setText(saida);

	}

	public void matching() {

		for (int mirI = 0; mirI < target.size(); mirI++) { // para todos os
															// micorRNAs
			ArrayList<String> mir1 = target.get(mirI); // primeiro microRNA

			for (int geneI = 1; geneI < mir1.size(); geneI++) {// para todos os
																// genes alvo-->
																// o primeiro \u00E9
																// o nome do
																// microRNA

				for (int mirJ = mirI + 1; mirJ < target.size(); mirJ++) { // do
																			// segundo
																			// microRNA
																			// em
																			// diante
					ArrayList<String> mir2 = target.get(mirJ);// microRNA
																// que ser\u00E1
																// comparado
																// com o
																// primeiro

					for (int geneJ = 1; geneJ < mir2.size(); geneJ++) { // para
																		// todos
																		// os
																		// genes
																		// alvo
																		// do
																		// segundo
																		// microRNA
						// compara\u00E7\u00E3o para ver se os 2 genes alvo s\u00E3o iguais
						String gene1 = (String) mir1.get(geneI);
						String gene2 = (String) mir2.get(geneJ);

						if (gene1.equals(gene2)) {// nome dos miRNA pra ver se
													// eles j\u00E1 existem
							String mirna1 = (String) mir1.get(0);
							String mirna2 = (String) mir2.get(0);

							verMatches(gene1, mirna1, mirna2);

						}
					}
				}
			}
		}
	}

	// verifica se j\u00E1 existe o gene no ArrayList matches
	private void verMatches(String geneAlvo, String miRNA1, String miRNA2) {
		boolean flag = true;

		Gene gene;
		if (!matches.isEmpty()) {

			for (int i = 0; i < matches.size(); i++) {
				gene = matches.get(i);
				if (geneAlvo.equals(gene.getNomeGene())) {
					flag = false;
					gene.setMirnaNome(miRNA1);
					gene.setMirnaNome(miRNA2);
				}
			}
			if (flag) {
				gene = new Gene(getMirnas());
				gene.setNomeGene(geneAlvo);
				gene.setMirnaNome(miRNA1);
				gene.setMirnaNome(miRNA2);
				matches.add(gene);
			}
		}

		else {
			gene = new Gene(getMirnas());
			gene.setNomeGene(geneAlvo);
			gene.setMirnaNome(miRNA1);
			gene.setMirnaNome(miRNA2);

			matches.add(gene);
		}
	}

	private void criaVetor(String mirna) {

		try {
			Connection con;
			PreparedStatement stmt;
			ResultSet RS;
			ArrayList<String> al = new ArrayList<String>();
			al.add(mirna);

			switch (jcbBanco.getSelectedIndex()) {
				case 0:
					con = DriverManager
							.getConnection("jdbc:mysql://localhost:3306/targets?user=root&password=123456");
					stmt = con.prepareStatement(
							"SELECT DISTINCT gene FROM micrornaorg WHERE mirna = ?");
					stmt.setString(1, mirna);
					RS = stmt.executeQuery();
					break;
				default:
					con = DriverManager
							.getConnection("jdbc:mysql://localhost:3306/targetscan?user=root&password=123456");
					stmt = con.prepareStatement(
							"SELECT DISTINCT targets.Gene "
									+ "FROM mirna INNER JOIN targets ON mirna.miRFamily = targets.miRFamily "
									+ "WHERE mirna.mirna = ?");
					stmt.setString(1, mirna);
					RS = stmt.executeQuery();
					break;
			}

			while (RS.next()) {
				al.add(RS.getString(1));
			}

			RS.close();
			stmt.close();
			con.close();

			target.add(al);

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this,
					"Consulta erro:" + e.getMessage());
		}

	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jScrollPane1 = new JScrollPane();
		{
			jtSaida = new JTextArea();
			jScrollPane1.setViewportView(jtSaida);
			jtSaida.setColumns(20);
			jtSaida.setRows(5);
			jtSaida.setEditable(false);
			jtSaida.setFont(new java.awt.Font("Monospaced", 0, 12));
		}
		GroupLayout layout = new GroupLayout((JComponent) getContentPane());
		getContentPane().setLayout(layout);
		jScrollPane2 = new JScrollPane();
		jtEntrada = new JTextArea();
		jLabel1 = new JLabel();
		jPanel1 = new JPanel();
		jLabel2 = new JLabel();
		jcbMin = new JComboBox();
		jbInicio = new JButton();
		jcbBanco = new JComboBox();
		jLabel3 = new JLabel();

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("TargetCompare (v.0.1 Beta)");

		jtEntrada.setColumns(20);
		jtEntrada.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
		jtEntrada.setLineWrap(true);
		jtEntrada.setRows(3);
		jtEntrada
				.setText(
						"hsa-miR-16-5p, hsa-miR-30a-5p, hsa-miR-181a-5p, hsa-miR-17-5p, hsa-miR-27a-3p, hsa-miR-9-5p, hsa-miR-23a-3p, hsa-miR-19a-3p");
		jScrollPane2.setViewportView(jtEntrada);
		jtEntrada.setPreferredSize(new java.awt.Dimension(463, 125));

		jLabel1.setText("Digite os microRNAs (separados por v\u00EDrgula):");

		jLabel2.setText("N\u00FAmero m\u00EDnimo de matches:");

		jcbMin.setModel(new DefaultComboBoxModel(new String[] { "2", "3", "4",
				"5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));

		jbInicio.setText("Iniciar An\u00E1lise");

		jb_som = new JButton();
		jb_som.setText("Abrir SOM");
		jb_som.setEnabled(false);
		jb_som.setSize(132, 41);
		jb_som.addActionListener(this);

		jbInicio.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jbInicioActionPerformed(evt);
			}
		});

		jcbBanco.setMaximumRowCount(3);
		jcbBanco.setModel(new DefaultComboBoxModel(new String[] {
				"microrna.org", "targetScan" }));
		jcbBanco.setSelectedIndex(1);
		jcbBanco.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(java.awt.event.ItemEvent evt) {
				if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
					atualizarPresets(jcbBanco.getSelectedIndex());
				}
			}
		});

		jLabel3.setText("Banco de Dados");

		GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
		jPanel1.setLayout(jPanel1Layout);
		layout.setVerticalGroup(layout
				.createSequentialGroup()
				.addContainerGap(17, 17)
				.addComponent(jLabel1, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGroup(
						layout.createParallelGroup()
								.addComponent(jPanel1,
										GroupLayout.Alignment.LEADING,
										GroupLayout.PREFERRED_SIZE, 143,
										GroupLayout.PREFERRED_SIZE)
								.addGroup(
										GroupLayout.Alignment.LEADING,
										layout.createSequentialGroup()
												.addPreferredGap(
														LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(
														jScrollPane2,
														GroupLayout.PREFERRED_SIZE,
														137,
														GroupLayout.PREFERRED_SIZE)))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jScrollPane1, 0, 282, Short.MAX_VALUE)
				.addContainerGap());
		layout.setHorizontalGroup(layout
				.createSequentialGroup()
				.addContainerGap()
				.addGroup(
						layout.createParallelGroup()
								.addGroup(
										GroupLayout.Alignment.LEADING,
										layout.createSequentialGroup()
												.addGroup(
														layout.createParallelGroup()
																.addComponent(
																		jScrollPane2,
																		GroupLayout.Alignment.LEADING,
																		0,
																		552,
																		Short.MAX_VALUE)
																.addGroup(
																		GroupLayout.Alignment.LEADING,
																		layout.createSequentialGroup()
																				.addComponent(
																						jLabel1,
																						GroupLayout.PREFERRED_SIZE,
																						GroupLayout.PREFERRED_SIZE,
																						GroupLayout.PREFERRED_SIZE)
																				.addGap(316)))
												.addPreferredGap(
														LayoutStyle.ComponentPlacement.RELATED)
												.addComponent(
														jPanel1,
														GroupLayout.PREFERRED_SIZE,
														338,
														GroupLayout.PREFERRED_SIZE))
								.addGroup(
										GroupLayout.Alignment.LEADING,
										layout.createSequentialGroup()
												.addComponent(jScrollPane1, 0,
														883, Short.MAX_VALUE)
												.addGap(13)))
				.addContainerGap());
		jPanel1Layout.setVerticalGroup(jPanel1Layout
				.createSequentialGroup()
				.addContainerGap(21, 21)
				.addGroup(
						jPanel1Layout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(jcbBanco,
										GroupLayout.Alignment.BASELINE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel3,
										GroupLayout.Alignment.BASELINE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(
						jPanel1Layout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(jcbMin,
										GroupLayout.Alignment.BASELINE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabel2,
										GroupLayout.Alignment.BASELINE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
				.addGap(0, 16, Short.MAX_VALUE)
				.addGroup(
						jPanel1Layout
								.createParallelGroup(
										GroupLayout.Alignment.BASELINE)
								.addComponent(jb_som,
										GroupLayout.Alignment.BASELINE, 0, 37,
										Short.MAX_VALUE)
								.addComponent(jbInicio,
										GroupLayout.Alignment.BASELINE,
										GroupLayout.PREFERRED_SIZE, 36,
										GroupLayout.PREFERRED_SIZE))
				.addContainerGap());
		jPanel1Layout
				.setHorizontalGroup(jPanel1Layout
						.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								jPanel1Layout
										.createParallelGroup()
										.addComponent(jLabel2,
												GroupLayout.Alignment.LEADING,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(jLabel3,
												GroupLayout.Alignment.LEADING,
												GroupLayout.PREFERRED_SIZE,
												161, GroupLayout.PREFERRED_SIZE)
										.addGroup(
												GroupLayout.Alignment.LEADING,
												jPanel1Layout
														.createSequentialGroup()
														.addComponent(
																jbInicio,
																GroupLayout.PREFERRED_SIZE,
																119,
																GroupLayout.PREFERRED_SIZE)
														.addGap(42)))
						.addGroup(
								jPanel1Layout
										.createParallelGroup()
										.addComponent(jb_som,
												GroupLayout.Alignment.LEADING,
												0, 105, Short.MAX_VALUE)
										.addGroup(
												jPanel1Layout
														.createSequentialGroup()
														.addPreferredGap(
																jb_som,
																jcbMin,
																LayoutStyle.ComponentPlacement.INDENT)
														.addGroup(
																jPanel1Layout
																		.createParallelGroup()
																		.addComponent(
																				jcbMin,
																				GroupLayout.Alignment.LEADING,
																				0,
																				93,
																				Short.MAX_VALUE)
																		.addComponent(
																				jcbBanco,
																				GroupLayout.Alignment.LEADING,
																				0,
																				93,
																				Short.MAX_VALUE))))
						.addContainerGap());

		this.setSize(1024, 514);
		setLocationRelativeTo(null);
	}// </editor-fold>//GEN-END:initComponents

	private void jbInicioActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jbInicioActionPerformed
		final String txt = jtEntrada.getText().trim();
		if (txt.isEmpty()) {
			JOptionPane.showMessageDialog(this,
				"Por favor, digite pelo menos um microRNA.",
				"Entrada vazia", JOptionPane.WARNING_MESSAGE);
			return;
		}
		setMin(Integer.parseInt(jcbMin.getSelectedItem().toString()) - 1);
		jtSaida.setText("Consultando banco de dados...");
		jbInicio.setEnabled(false);
		jb_som.setEnabled(false);
		jcbBanco.setEnabled(false);
		jcbMin.setEnabled(false);

		new javax.swing.SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				iniciarBusca(txt);
				return null;
			}
			@Override
			protected void done() {
				jbInicio.setEnabled(true);
				jcbBanco.setEnabled(true);
				jcbMin.setEnabled(true);
				try {
					get();
					jb_som.setEnabled(true);
				} catch (Exception ex) {
					jtSaida.setText("");
					Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
					JOptionPane.showMessageDialog(Compare.this,
						"Erro ao conectar ao banco de dados:\n" + cause.getMessage(),
						"Erro de Consulta", JOptionPane.ERROR_MESSAGE);
				}
			}
		}.execute();
	}// GEN-LAST:event_jbInicioActionPerformed

	/**
	 * @param args
	 *             the command line arguments
	 */
	public static void main(String args[]) {
		try {
			java.awt.Color sysBg = java.awt.SystemColor.window;
			boolean isDark = (sysBg.getRed() + sysBg.getGreen() + sysBg.getBlue()) / 3 < 128;
			if (isDark) {
				com.formdev.flatlaf.FlatDarculaLaf.setup();
			} else {
				com.formdev.flatlaf.FlatLightLaf.setup();
			}
		} catch (Throwable t) {
			try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
			catch (Exception ignored) {}
		}
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new Compare().setVisible(true);
			}
		});
	}

	public String[] getMirnas() {
		return mirnas;
	}

	public void setMirnas(String[] mirnas) {
		this.mirnas = mirnas;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public void setResultado(String out) {

		String p = jtSaida.getText() + "\n" + out;

		jtSaida.setText(p);

	}

	private void atualizarPresets(int bancoIndex) {
		if (bancoIndex == 0) {
			// microrna.org — 6 miRNAs com 674 genes em comum confirmados
			jtEntrada.setText(
				"hsa-miR-3689a-3p, hsa-miR-3689c, hsa-miR-4728-5p, hsa-miR-1827, hsa-miR-940, hsa-miR-485-5p");
			jcbMin.setSelectedIndex(0); // mínimo 2
		} else {
			// targetScan — 5 miRNAs com 108 genes em comum confirmados
			jtEntrada.setText(
				"hsa-miR-124-3p.1, hsa-miR-16-5p, hsa-miR-27a-3p, hsa-miR-30a-5p, hsa-miR-15a-5p");
			jcbMin.setSelectedIndex(0); // mínimo 2
		}
	}

	// End of variables declaration//GEN-END:variables

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == jb_som) {
			if (matches == null || matches.isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"Nenhum resultado encontrado.\nPor favor, realize uma busca antes de abrir o SOM.",
						"Aviso",
						JOptionPane.WARNING_MESSAGE);
				return;
			}
			view v = new view(matches, mirnas, this, jcbBanco.getSelectedIndex());

			v.setVisible(true);
		}
	}

}
