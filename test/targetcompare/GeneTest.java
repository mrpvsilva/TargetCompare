package targetcompare;

import org.junit.Test;
import java.util.List;
import static org.junit.Assert.*;

public class GeneTest {

    private static final String[] MIRNAS_3 = {"mir-1", "mir-2", "mir-3"};

    @Test
    public void novoGeneTemQtdadeZero() {
        Gene g = new Gene("GENE_A", MIRNAS_3);
        assertEquals(0, g.getQtdade());
    }

    @Test
    public void markMirnaAsTargetIncrementaQtdade() {
        Gene g = new Gene("GENE_A", MIRNAS_3);
        g.markMirnaAsTarget("mir-1");
        assertEquals(1, g.getQtdade());
        g.markMirnaAsTarget("mir-3");
        assertEquals(2, g.getQtdade());
    }

    @Test
    public void marcacaoDuplicadaNaoIncrementaQtdade() {
        Gene g = new Gene("GENE_A", MIRNAS_3);
        g.markMirnaAsTarget("mir-2");
        g.markMirnaAsTarget("mir-2");
        assertEquals(1, g.getQtdade());
    }

    @Test
    public void mirnaDesconhecidoNaoAlteraNada() {
        Gene g = new Gene("GENE_A", MIRNAS_3);
        g.markMirnaAsTarget("mir-inexistente");
        assertEquals(0, g.getQtdade());
    }

    @Test
    public void mirnasMarcadosRefleteNaLista() {
        Gene g = new Gene("GENE_A", MIRNAS_3);
        g.markMirnaAsTarget("mir-2");
        List<Mirna> mirnas = g.getMirnas();
        assertFalse("mir-1 não deve ser alvo", mirnas.get(0).isAlvo());
        assertTrue("mir-2 deve ser alvo",      mirnas.get(1).isAlvo());
        assertFalse("mir-3 não deve ser alvo", mirnas.get(2).isAlvo());
    }

    @Test
    public void toTableRowFormatoCorreto() {
        Gene g = new Gene("GENE_X", new String[]{"mir-a", "mir-b"});
        g.markMirnaAsTarget("mir-a");
        assertEquals("GENE_X\ttrue\tfalse\t1", g.toTableRow());
    }

    @Test
    public void todosOsMirnasMarcados() {
        Gene g = new Gene("GENE_A", MIRNAS_3);
        for (String m : MIRNAS_3) {
            g.markMirnaAsTarget(m);
        }
        assertEquals(MIRNAS_3.length, g.getQtdade());
    }

    @Test
    public void getMirnasMantémOrdemDeInsercao() {
        Gene g = new Gene("GENE_A", MIRNAS_3);
        List<Mirna> mirnas = g.getMirnas();
        assertEquals("mir-1", mirnas.get(0).getNome());
        assertEquals("mir-2", mirnas.get(1).getNome());
        assertEquals("mir-3", mirnas.get(2).getNome());
    }

    @Test
    public void geneComUmMirnaMarcadoNaoApareceMaisDe1() {
        Gene g = new Gene("GENE_A", new String[]{"mir-unico"});
        g.markMirnaAsTarget("mir-unico");
        g.markMirnaAsTarget("mir-unico");
        g.markMirnaAsTarget("mir-unico");
        assertEquals(1, g.getQtdade());
    }
}
