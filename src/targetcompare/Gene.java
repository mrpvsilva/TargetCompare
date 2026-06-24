package targetcompare;

import java.util.ArrayList;
import java.util.List;

public class Gene {

    private final String nomeGene;
    private final List<Mirna> mirnas;
    private int qtdade;

    public Gene(String nomeGene, String[] nomesMirnas) {
        this.nomeGene = nomeGene;
        this.mirnas = new ArrayList<>(nomesMirnas.length);
        for (String nome : nomesMirnas) {
            mirnas.add(new Mirna(nome));
        }
    }

    public void markMirnaAsTarget(String nome) {
        for (Mirna mirna : mirnas) {
            if (mirna.getNome().equals(nome)) {
                mirna.setAlvo(true);
            }
        }
        qtdade = (int) mirnas.stream().filter(Mirna::isAlvo).count();
    }

    public String toTableRow() {
        StringBuilder sb = new StringBuilder(nomeGene).append('\t');
        for (Mirna mirna : mirnas) {
            sb.append(mirna.isAlvo()).append('\t');
        }
        sb.append(qtdade);
        return sb.toString();
    }

    public String getNomeGene() {
        return nomeGene;
    }

    public int getQtdade() {
        return qtdade;
    }

    public List<Mirna> getMirnas() {
        return mirnas;
    }
}
