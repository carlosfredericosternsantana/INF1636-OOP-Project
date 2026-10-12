package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

public class MapaTest {

	private Mapa mapa;

	@Before
	public void criarMapa() {
		mapa = new Mapa();
	}

	@Test
	public void rotasSaoSimetricasEKarakorumTemQuatroVizinhos() {
		for (int p = 0; p < Mapa.NUM_PARADAS; p++)
			for (int v : mapa.adj(p))
				assertTrue(mapa.adj(v).contains(p));
		assertEquals(new HashSet<Integer>(Arrays.asList(6, 12, 25, 28)),
				new HashSet<Integer>(mapa.adj(Mapa.KARAKORUM)));
	}

	@Test
	public void coletaveisSoSaoAlcancadosDeParadasVizinhas() {
		assertEquals(28, mapa.coletaveis.size());
		assertTrue(mapa.alcanca(1, 0));
		assertFalse(mapa.alcanca(0, 0));
		assertTrue(mapa.alcanca(Mapa.KARAKORUM, 27));
		for (int p = 0; p < Mapa.KARAKORUM; p++)
			assertFalse(mapa.alcanca(p, 27));
	}

	@Test
	public void khanEncheOsTresPotesENaoRepetePosicao() {
		assertTrue(mapa.moverKhan(2));
		for (int pote = 0; pote <= 2; pote++)
			assertEquals(1, ((PoteDeRecursos) mapa.coletaveis.get(pote)).getQuantidade());
		assertEquals(0, ((PoteDeRecursos) mapa.coletaveis.get(3)).getQuantidade());
		assertFalse(mapa.moverKhan(2));
		assertTrue(mapa.moverKhan(Mapa.TRILHA_DE_MELHORIAS));
		assertTrue(mapa.moverKhan(2));
		assertEquals(2, ((PoteDeRecursos) mapa.coletaveis.get(0)).getQuantidade());
	}
}
