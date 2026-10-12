package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

public class MapaTest {

	private static final int TEMPO = 1000;

	private Mapa mapa;

	@Before
	public void criarMapa() {
		mapa = new Mapa();
	}

	private int quantidadeNoPote(int pote) {
		return ((PoteDeRecursos) mapa.coletaveis.get(pote)).getQuantidade();
	}

	@Test(timeout = TEMPO)
	public void rotasSaoSimetricasEKarakorumTemQuatroVizinhos() {
		for (int p = 0; p < Mapa.NUM_PARADAS; p++)
			for (int v : mapa.adj(p))
				assertTrue("parada " + v + " deveria ter a parada " + p + " como vizinha", mapa.adj(v).contains(p));
		assertEquals("vizinhos de Karakorum", new HashSet<Integer>(Arrays.asList(6, 12, 25, 28)),
				new HashSet<Integer>(mapa.adj(Mapa.KARAKORUM)));
	}

	@Test(timeout = TEMPO)
	public void coletaveisSoSaoAlcancadosDeParadasVizinhas() {
		assertEquals("total de coletaveis no mapa", 28, mapa.coletaveis.size());
		assertTrue("pote 0 deveria ser alcancado pela parada 1", mapa.alcanca(1, 0));
		assertFalse("pote 0 nao deveria ser alcancado pela parada 0", mapa.alcanca(0, 0));
		assertTrue("conselheiro de Karakorum deveria ser alcancado pela parada 33", mapa.alcanca(Mapa.KARAKORUM, 27));
		for (int p = 0; p < Mapa.KARAKORUM; p++)
			assertFalse("conselheiro de Karakorum nao deveria ser alcancado pela parada " + p, mapa.alcanca(p, 27));
	}

	@Test(timeout = TEMPO)
	public void khanEncheOsTresPotesENaoRepetePosicao() {
		assertTrue("Khan deveria ir para a posicao 2", mapa.moverKhan(2));
		assertEquals("pote 0 apos o Khan", 1, quantidadeNoPote(0));
		assertEquals("pote 1 apos o Khan", 1, quantidadeNoPote(1));
		assertEquals("pote 2 apos o Khan", 1, quantidadeNoPote(2));
		assertEquals("pote 3 nao e apontado pela posicao 2", 0, quantidadeNoPote(3));
		assertFalse("Khan nao pode ficar na mesma posicao", mapa.moverKhan(2));
		assertTrue("Khan deveria ir para a trilha de melhorias", mapa.moverKhan(Mapa.TRILHA_DE_MELHORIAS));
		assertTrue("Khan deveria voltar para a posicao 2", mapa.moverKhan(2));
		assertEquals("pote 0 apos o Khan voltar", 2, quantidadeNoPote(0));
	}
}
