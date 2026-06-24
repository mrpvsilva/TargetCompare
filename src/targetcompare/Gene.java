package targetcompare;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Gene {

    private final String nomeGene;
    private final List<Mirna> mirnas;
    private final Map<String, Mirna> mirnaMap;
    private int qtdade;

    public Gene(String nomeGene, String[] nomesMirnas) {
        this.nomeGene = nomeGene;
        this.mirnas = new ArrayList<>(nomesMirnas.length);
        this.mirnaMap = new LinkedHashMap<>(nomesMirnas.length * 2);
        for (String nome : nomesMirnas) {
            Mirna m = new Mirna(nome);
            mirnas.add(m);
            mirnaMap.put(nome, m);
        }
    }

    /** Marca o miRNA como alvo deste gene. Chamadas duplicadas são ignoradas. */
    public void markMirnaAsTarget(String nome) {
        Mirna mirna = mirnaMap.get(nome);
        if (mirna != null && !mirna.isAlvo()) {
            mirna.setAlvo(true);
            qtdade++;
        }
    }

    public String toTableRow() {
        StringBuilder sb = new StringBuilder(nomeGene).append('\t');
        for (Mirna mirna : mirnas) {
            sb.append(mirna.isAlvo()).append('\t');
        }
        sb.append(qtdade);
        return sb.toString();
    }

    public String getNomeGene() { return nomeGene; }
    public int getQtdade() { return qtdade; }
    public List<Mirna> getMirnas() { return mirnas; }
}
