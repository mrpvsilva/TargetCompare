/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package targetcompare;

/**
 * 
 * @author fabiano
 */
public class Gene {

	Mirna[] mirna;
	int qtdade;
	String nomeGene;

	Gene(String[] nome) {

		// inicializa\u00E7\u00E3o dos mirnas
		mirna = new Mirna[nome.length];
		for (int i = 0; i < nome.length; i++) {
			mirna[i] = new Mirna();
			mirna[i].setNome(nome[i]);
			mirna[i].setAlvo(false);
		}
	}

	public void setMirnaNome(String nome) {
		int cont = 0;
		for (int i = 0; i < mirna.length; i++) {
			if (mirna[i].getNome().equals(nome))
				mirna[i].setAlvo(true);
			if (mirna[i].isAlvo())
				cont++;
		}

		// determina a quantidade de mirna apontados para esse gene alvo
		setQtdade(cont);
	}

	public String printMirna() {

		String linha = getNomeGene() + "\t";

		for (int i = 0; i < mirna.length; i++) {
			linha += mirna[i].isAlvo() + "\t";
		}

		linha += getQtdade();

		// String linha = nome +"\t"+ mir135b +"\t"+ mir29c +"\t"+ mir143 +"\t"+
		// mir215 +"\t"+ mir141 +"\t"+ mir126 +"\t"+
		// mir93 +"\t"+ mir99a +"\t"+ mir10a +"\t"+ mir17 +"\t"+ mir3607
		// +"\t"+qtdade;

		// String linha = nome +"\t"+ mir135b +"\t"+ mir29c +"\t"+ mir664 +"\t"+
		// mir150 +"\t"+qtdade;

		return linha;
	}

	public String getNomeGene() {
		return nomeGene;
	}

	public void setNomeGene(String nome) {
		this.nomeGene = nome;
	}

	public int getQtdade() {
		return qtdade;
	}

	private void setQtdade(int qtdade) {
		this.qtdade = qtdade;
	}

	public Mirna[] getMirna() {
		return mirna;
	}

	

}
