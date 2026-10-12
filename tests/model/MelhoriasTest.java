package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MelhoriasTest {

	private static final int TEMPO = 1000;

	@Test(timeout = TEMPO)
	public void posicaoVaiDeMenosUmACinco() {
		Melhorias melhoria = new Melhorias(0, new Recurso());
		assertEquals("melhoria comeca embaralhada", Melhorias.EMBARALHADA, melhoria.getPosicao());
		assertFalse("embaralhada nao esta na trilha", melhoria.estaNaTrilha());
		assertTrue("posicao 5 e valida", melhoria.setPosicao(5));
		assertTrue("posicao 5 esta na trilha", melhoria.estaNaTrilha());
		assertFalse("posicao 6 e invalida", melhoria.setPosicao(6));
		assertFalse("posicao -2 e invalida", melhoria.setPosicao(-2));
		assertEquals("posicao invalida nao altera a atual", 5, melhoria.getPosicao());
		assertTrue("posicao -1 (comprada) e valida", melhoria.setPosicao(Melhorias.COMPRADA));
		assertTrue("melhoria deveria estar comprada", melhoria.foiComprada());
	}

	@Test(timeout = TEMPO)
	public void aplicarSomaNaColunaDaCorEMarcaComprada() {
		Jogador jogador = new Jogador();
		Recurso bonus = new Recurso();
		bonus.setItem(Mapa.ESPADA, 1);
		bonus.setItem(Mapa.MOEDA, 2);
		Melhorias melhoria = new Melhorias(2, bonus);
		assertFalse("melhoria embaralhada nao pode ser aplicada", melhoria.aplicar(jogador));
		melhoria.setPosicao(3);
		assertTrue("melhoria da trilha deveria ser aplicada", melhoria.aplicar(jogador));
		assertEquals("moedas na coluna 2", 2, jogador.getColunas(2).getItem(Mapa.MOEDA));
		assertEquals("espadas na coluna 2", 1, jogador.getColunas(2).getItem(Mapa.ESPADA));
		assertEquals("coluna 0 nao recebe o bonus", 0, jogador.getColunas(0).getItem(Mapa.MOEDA));
		assertEquals("melhorias na coluna 2", 1, jogador.getMelhoriasNaColuna(2));
		assertTrue("melhoria deveria ficar comprada", melhoria.foiComprada());
		assertFalse("melhoria comprada nao pode ser aplicada de novo", melhoria.aplicar(jogador));
		assertEquals("melhorias na coluna 2 apos 2a tentativa", 1, jogador.getMelhoriasNaColuna(2));
	}

	@Test(timeout = TEMPO, expected = IllegalArgumentException.class)
	public void corForaDeZeroATresLancaExcecao() {
		new Melhorias(Melhorias.NUM_COLUNAS, new Recurso());
	}
}
