package com.som.core;

import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class RedeTest {

    /**
     * Entrada no formato esperado por Rede: entrada[gene][mirna].
     * 3 genes, 2 miRNAs.
     * Após transpor internamente: vetorEntrada[mirna][gene].
     *   miRNA-0 = [1, 0, 1]
     *   miRNA-1 = [0, 1, 1]
     * Média das amostras = [0.5, 0.5, 1.0]
     */
    private static final double[][] ENTRADA = {
        {1.0, 0.0},  // gene 0: miRNA0=alvo, miRNA1=não-alvo
        {0.0, 1.0},  // gene 1: miRNA0=não-alvo, miRNA1=alvo
        {1.0, 1.0},  // gene 2: ambos alvos
    };

    @Test
    public void construtorCriaNeuroniosCorretamente() {
        Rede rede = new Rede(ENTRADA, 5, 0.5, 0.7, 100);
        assertNotNull(rede.getNeuronios());
        assertEquals(5, rede.getNeuronios().size());
    }

    @Test
    public void neuronioPesosInicializadosNoDominioDaEntrada() {
        Rede rede = new Rede(ENTRADA, 4, 0.5, 0.7, 100);
        for (Neuronio n : rede.getNeuronios()) {
            for (double p : n.getPesos()) {
                assertTrue("Peso fora do intervalo [0,1]: " + p, p >= 0.0 && p <= 1.0);
            }
        }
    }

    @Test
    public void treinarNaoLancaExcecao() {
        Rede rede = new Rede(ENTRADA, 3, 0.3, 0.5, 200);
        rede.treinar();
    }

    @Test
    public void taxaAprendizadoDecaiAposTreinamento() {
        Rede rede = new Rede(ENTRADA, 3, 0.5, 0.7, 300);
        double taxaInicial = rede.getTaxaAprendizado();
        rede.treinar();
        assertTrue("Taxa de aprendizado deveria decair", rede.getTaxaAprendizado() < taxaInicial);
    }

    @Test
    public void raioGaussianaDecaiAposTreinamento() {
        Rede rede = new Rede(ENTRADA, 3, 0.5, 0.7, 300);
        double raioInicial = rede.getRaioGaussiana();
        rede.treinar();
        assertTrue("Raio gaussiano deveria decair", rede.getRaioGaussiana() < raioInicial);
    }

    @Test
    public void raioGaussianaNaoDecaiAbaixoDoLimite() {
        // Com muitas épocas o raio deve parar no DECAY_THRESHOLD (0.01), não em zero
        Rede rede = new Rede(ENTRADA, 2, 0.5, 0.7, 50000);
        rede.treinar();
        assertTrue("Raio gaussiano não deve ficar negativo ou zero",
                rede.getRaioGaussiana() > 0.0);
    }

    @Test
    public void vencedorDefinidoAposTreinamento() {
        Rede rede = new Rede(ENTRADA, 3, 0.3, 0.5, 200);
        rede.treinar();
        assertNotNull(rede.getVencedor());
    }

    @Test
    public void classificarRetornaStringComTodosOsNeuronios() {
        Rede rede = new Rede(ENTRADA, 3, 0.3, 0.5, 100);
        rede.treinar();
        String[] mirnas = {"mir-1", "mir-2"};
        String resultado = rede.classificar(mirnas);
        assertNotNull(resultado);
        assertTrue("Deve conter N1", resultado.contains("N1"));
        assertTrue("Deve conter N2", resultado.contains("N2"));
        assertTrue("Deve conter N3", resultado.contains("N3"));
    }

    @Test
    public void classificarContémNomesDosMirnas() {
        Rede rede = new Rede(ENTRADA, 2, 0.3, 0.5, 100);
        rede.treinar();
        String[] mirnas = {"mir-alpha", "mir-beta"};
        String resultado = rede.classificar(mirnas);
        // Cada miRNA deve aparecer exatamente uma vez no resultado
        int countAlpha = contarOcorrencias(resultado, "mir-alpha");
        int countBeta  = contarOcorrencias(resultado, "mir-beta");
        assertEquals("mir-alpha deve aparecer exatamente 1 vez", 1, countAlpha);
        assertEquals("mir-beta deve aparecer exatamente 1 vez",  1, countBeta);
    }

    @Test
    public void umNeuronioConvergeParaMediaDasAmostras() {
        // Com 1 neurônio e muitas épocas, os pesos convergem para a média:
        // miRNA-0=[1,0,1], miRNA-1=[0,1,1] → média=[0.5, 0.5, 1.0]
        //
        // 50000 épocas garante que lr decaia abaixo do DECAY_THRESHOLD (~22.5k épocas),
        // estabilizando os pesos próximo à média com variância residual desprezível.
        Rede rede = new Rede(ENTRADA, 1, 0.5, 0.9, 50000);
        rede.treinar();
        double[] pesos = rede.getNeuronios().get(0).getPesos();
        assertEquals("Gene 0 deve convergir para 0.5", 0.5, pesos[0], 0.15);
        assertEquals("Gene 1 deve convergir para 0.5", 0.5, pesos[1], 0.15);
        assertEquals("Gene 2 deve convergir para 1.0", 1.0, pesos[2], 0.10);
    }

    @Test
    public void vetorEntradaTranspostoCorretamente() {
        Rede rede = new Rede(ENTRADA, 2, 0.3, 0.5, 10);
        double[][] ve = rede.getVetorEntrada();
        // ENTRADA[gene][mirna] → vetorEntrada[mirna][gene]
        assertEquals("vetorEntrada deve ter 2 linhas (miRNAs)", 2, ve.length);
        assertEquals("vetorEntrada deve ter 3 colunas (genes)", 3, ve[0].length);
        // Verifica valores: ENTRADA[0][0]=1 → ve[0][0]=1
        assertEquals(1.0, ve[0][0], 1e-9);
        // ENTRADA[1][0]=0 → ve[0][1]=0
        assertEquals(0.0, ve[0][1], 1e-9);
        // ENTRADA[0][1]=0 → ve[1][0]=0
        assertEquals(0.0, ve[1][0], 1e-9);
    }

    private static int contarOcorrencias(String texto, String padrao) {
        int count = 0;
        int idx = 0;
        while ((idx = texto.indexOf(padrao, idx)) != -1) {
            count++;
            idx += padrao.length();
        }
        return count;
    }
}
