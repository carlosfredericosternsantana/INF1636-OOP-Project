package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MelhoriasTest {

	@Test
	public void posicaoVaiDeMenosUmACinco() {
		Melhorias melhoria = new Melhorias(0, new Recurso());
		assertEquals(Melhorias.EMBARALHADA, melhoria.getPosicao());
		assertFalse(melhoria.estaNaTrilha());
		assertTrue(melhoria.setPosicao(5));
		assertTrue(melhoria.estaNaTrilha());
		assertFalse(melhoria.setPosicao(6));
		assertFalse(melhoria.setPosicao(-2));
		assertEquals(5, melhoria.getPosicao());
		assertTrue(melhoria.setPosicao(Melhorias.COMPRADA));
		assertTrue(melhoria.foiComprada());
		assertFalse(melhoria.estaNaTrilha());
	}

	@Test
	public void aplicarSomaNaColunaDaCorEMarcaComprada() {
		Jogador jogador = new Jogador();
		Recurso bonus = new Recurso();
		bonus.setItem(Mapa.ESPADA, 1);
		bonus.setItem(Mapa.MOEDA, 2);
		Melhorias melhoria = new Melhorias(2, bonus);
		assertFalse(melhoria.aplicar(jogador));
		melhoria.setPosicao(3);
		assertTrue(melhoria.aplicar(jogador));
		assertEquals(2, jogador.getColunas(2).getItem(Mapa.MOEDA));
		assertEquals(1, jogador.getColunas(2).getItem(Mapa.ESPADA));
		assertEquals(0, jogador.getColunas(0).getItem(Mapa.MOEDA));
		assertEquals(1, jogador.getMelhoriasNaColuna(2));
		assertTrue(melhoria.foiComprada());
		assertFalse(melhoria.aplicar(jogador));
		assertEquals(1, jogador.getMelhoriasNaColuna(2));
	}
}
