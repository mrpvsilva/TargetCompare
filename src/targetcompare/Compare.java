package targetcompare;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.som.view.view;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

/**
 * Janela principal do TargetCompare.
 * Etapa 1: busca genes-alvo em comum entre miRNAs em banco MySQL.
 */
public class Compare extends JFrame implements ActionListener {

	// --- Configuração de banco de dados ---
	private static final String DB_USER     = "root";
	private static final String DB_PASSWORD = "123456";
	private static final String DB_TARGETS_URL    = "jdbc:mysql://localhost:3306/targets";
	private static final String DB_TARGETSCAN_URL = "jdbc:mysql://localhost:3306/targetscan";

	// --- Componentes Swing ---
	private JLabel jLabel1;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JPanel jPanel1;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	private JButton jbInicio;
	private JComboBox<String> jcbBanco;
	private JComboBox<String> jcbMin;
	private JTextArea jtEntrada;
	private JButton jb_som;
	private JTextArea jtSaida;

	// --- Dados da análise ---
	private List<List<String>> target;
	private List<Gene> matches;
	private String[] mirnas;
	private int min, max;

	public Compare() {
		initComponents();
	}

	// -------------------------------------------------------------------------
	// Fluxo principal de análise
	// -------------------------------------------------------------------------

	public void iniciarBusca(String texto) {
		target = new ArrayList<>();
		matches = new ArrayList<>();

		String[] nomes = texto.split(",");
		setMax(nomes.length);
		mirnas = new String[nomes.length];
		for (int i = 0; i < nomes.length; i++) {
			mirnas[i] = nomes[i].trim();
		}

		for (String mirna : mirnas) {
			criaVetor(mirna);
		}

		matching();
		analizar();
	}

	/**
	 * Constroi um mapa gene->{@link Gene} iterando cada lista de alvos uma unica vez.
	 * Complexidade O(total de genes) em vez do O(n^4) original.
	 */
	private void matching() {
		Map<String, Gene> geneMap = new LinkedHashMap<>();

		for (List<String> mirTargets : target) {
			String mirnaName = mirTargets.get(0);
			for (int i = 1; i < mirTargets.size(); i++) {
				String geneName = mirTargets.get(i);
				Gene gene = geneMap.computeIfAbsent(geneName, g -> new Gene(g, mirnas));
				gene.markMirnaAsTarget(mirnaName);
			}
		}

		// Mantém apenas genes alvejados por pelo menos 2 miRNAs
		matches = geneMap.values().stream()
				.filter(g -> g.getQtdade() >= 2)
				.collect(Collectors.toList());
	}

	public void analizar() {
		int minCount = min + 1;

		List<Gene> filtered = matches.stream()
				.filter(g -> g.getQtdade() >= minCount)
				.sorted(Comparator.comparingInt(Gene::getQtdade).reversed())
				.collect(Collectors.toList());

		StringBuilder sb = new StringBuilder("nome\t");
		for (String mirna : mirnas) {
			sb.append(mirna).append('\t');
		}
		sb.append("\n\n");

		for (Gene gene : filtered) {
			sb.append(gene.toTableRow()).append('\n');
		}

		sb.append("\n genes alvos em comum: ").append(matches.size());
		sb.append("\n com matches >= ").append(minCount).append(": ").append(filtered.size());

		jtSaida.setText(sb.toString());
	}

	// -------------------------------------------------------------------------
	// Acesso ao banco de dados
	// -------------------------------------------------------------------------

	private void criaVetor(String mirna) {
		try {
			List<String> al = new ArrayList<>();
			al.add(mirna);

			String url;
			String sql;
			if (jcbBanco.getSelectedIndex() == 0) {
				url = DB_TARGETS_URL;
				sql = "SELECT DISTINCT gene FROM micrornaorg WHERE mirna = ?";
			} else {
				url = DB_TARGETSCAN_URL;
				sql = "SELECT DISTINCT targets.Gene "
						+ "FROM mirna INNER JOIN targets ON mirna.miRFamily = targets.miRFamily "
						+ "WHERE mirna.mirna = ?";
			}

			try (Connection con = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
				 PreparedStatement stmt = con.prepareStatement(sql)) {
				stmt.setString(1, mirna);
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						al.add(rs.getString(1));
					}
				}
			}

			target.add(al);

		} catch (SQLException e) {
			JOptionPane.showMessageDialog(this,
					"Consulta erro: " + e.getMessage(),
					"Erro de Banco de Dados", JOptionPane.ERROR_MESSAGE);
		}
	}

	// -------------------------------------------------------------------------
	// Inicialização da interface (não modificar estrutura gerada)
	// -------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
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
		jcbMin = new JComboBox<>();
		jbInicio = new JButton();
		jcbBanco = new JComboBox<>();
		jLabel3 = new JLabel();

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("TargetCompare (v.0.1 Beta)");

		jtEntrada.setColumns(20);
		jtEntrada.setFont(new java.awt.Font("Monospaced", 0, 12));
		jtEntrada.setLineWrap(true);
		jtEntrada.setRows(3);
		jtEntrada.setText(
				"hsa-miR-16-5p, hsa-miR-30a-5p, hsa-miR-181a-5p, hsa-miR-17-5p, hsa-miR-27a-3p, hsa-miR-9-5p, hsa-miR-23a-3p, hsa-miR-19a-3p");
		jScrollPane2.setViewportView(jtEntrada);
		jtEntrada.setPreferredSize(new java.awt.Dimension(463, 125));

		jLabel1.setText("Digite os microRNAs (separados por vírgula):");
		jLabel2.setText("Número mínimo de matches:");

		jcbMin.setModel(new DefaultComboBoxModel<>(new String[]{
				"2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"}));

		jbInicio.setText("Iniciar Análise");

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
		jcbBanco.setModel(new DefaultComboBoxModel<>(new String[]{"microrna.org", "targetScan"}));
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
				.addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup()
						.addComponent(jPanel1, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 143, GroupLayout.PREFERRED_SIZE)
						.addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 137, GroupLayout.PREFERRED_SIZE)))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jScrollPane1, 0, 282, Short.MAX_VALUE)
				.addContainerGap());
		layout.setHorizontalGroup(layout
				.createSequentialGroup()
				.addContainerGap()
				.addGroup(layout.createParallelGroup()
						.addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup()
										.addComponent(jScrollPane2, GroupLayout.Alignment.LEADING, 0, 552, Short.MAX_VALUE)
										.addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
												.addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addGap(316)))
								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, 338, GroupLayout.PREFERRED_SIZE))
						.addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
								.addComponent(jScrollPane1, 0, 883, Short.MAX_VALUE)
								.addGap(13)))
				.addContainerGap());
		jPanel1Layout.setVerticalGroup(jPanel1Layout
				.createSequentialGroup()
				.addContainerGap(21, 21)
				.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jcbBanco, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabel3, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jcbMin, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabel2, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(0, 16, Short.MAX_VALUE)
				.addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jb_som, GroupLayout.Alignment.BASELINE, 0, 37, Short.MAX_VALUE)
						.addComponent(jbInicio, GroupLayout.Alignment.BASELINE, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE))
				.addContainerGap());
		jPanel1Layout.setHorizontalGroup(jPanel1Layout
				.createSequentialGroup()
				.addContainerGap()
				.addGroup(jPanel1Layout.createParallelGroup()
						.addComponent(jLabel2, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabel3, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 161, GroupLayout.PREFERRED_SIZE)
						.addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
								.addComponent(jbInicio, GroupLayout.PREFERRED_SIZE, 119, GroupLayout.PREFERRED_SIZE)
								.addGap(42)))
				.addGroup(jPanel1Layout.createParallelGroup()
						.addComponent(jb_som, GroupLayout.Alignment.LEADING, 0, 105, Short.MAX_VALUE)
						.addGroup(jPanel1Layout.createSequentialGroup()
								.addPreferredGap(jb_som, jcbMin, LayoutStyle.ComponentPlacement.INDENT)
								.addGroup(jPanel1Layout.createParallelGroup()
										.addComponent(jcbMin, GroupLayout.Alignment.LEADING, 0, 93, Short.MAX_VALUE)
										.addComponent(jcbBanco, GroupLayout.Alignment.LEADING, 0, 93, Short.MAX_VALUE))))
				.addContainerGap());

		this.setSize(1024, 514);
		setLocationRelativeTo(null);
	}

	// -------------------------------------------------------------------------
	// Eventos
	// -------------------------------------------------------------------------

	private void jbInicioActionPerformed(java.awt.event.ActionEvent evt) {
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

		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() {
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
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == jb_som) {
			if (matches == null || matches.isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"Nenhum resultado encontrado.\nPor favor, realize uma busca antes de abrir o SOM.",
						"Aviso", JOptionPane.WARNING_MESSAGE);
				return;
			}
			new view(new ArrayList<>(matches), mirnas, this, jcbBanco.getSelectedIndex())
					.setVisible(true);
		}
	}

	private void atualizarPresets(int bancoIndex) {
		if (bancoIndex == 0) {
			jtEntrada.setText(
					"hsa-miR-3689a-3p, hsa-miR-3689c, hsa-miR-4728-5p, hsa-miR-1827, hsa-miR-940, hsa-miR-485-5p");
		} else {
			jtEntrada.setText(
					"hsa-miR-124-3p.1, hsa-miR-16-5p, hsa-miR-27a-3p, hsa-miR-30a-5p, hsa-miR-15a-5p");
		}
		jcbMin.setSelectedIndex(0);
	}

	// -------------------------------------------------------------------------
	// API pública usada por view.java
	// -------------------------------------------------------------------------

	public void setResultado(String out) {
		jtSaida.setText(jtSaida.getText() + "\n" + out);
	}

	// -------------------------------------------------------------------------
	// Getters / setters
	// -------------------------------------------------------------------------

	public String[] getMirnas() { return mirnas; }
	public void setMirnas(String[] mirnas) { this.mirnas = mirnas; }
	public int getMax() { return max; }
	public void setMax(int max) { this.max = max; }
	public int getMin() { return min; }
	public void setMin(int min) { this.min = min; }

	// -------------------------------------------------------------------------
	// Entry point
	// -------------------------------------------------------------------------

	public static void main(String[] args) {
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
		java.awt.EventQueue.invokeLater(() -> new Compare().setVisible(true));
	}
}
