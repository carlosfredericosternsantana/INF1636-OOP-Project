package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ColetavelTest {

	@Test
	public void poteNaoPassaDeTresEEsvaziaTudo() {
		PoteDeRecursos pote = new PoteDeRecursos(Mapa.MOEDA);
		assertTrue(pote.adicionar());
		assertTrue(pote.adicionar());
		assertTrue(pote.adicionar());
		assertFalse(pote.adicionar());
		assertEquals(3, pote.getQuantidade());
		assertEquals(-1, pote.getRecurso().getItem(2));
		assertEquals(3, pote.esvaziar());
		assertEquals(0, pote.getQuantidade());
	}

	@Test
	public void cidadeGuardaTesourosESoEDominadaUmaVez() {
		Cidade cidade = new Cidade(new Recurso());
		assertTrue(cidade.colocarTesouro(Mapa.GRAOS));
		assertTrue(cidade.colocarTesouro(Mapa.GRAOS));
		assertTrue(cidade.colocarTesouro(Mapa.VACA));
		assertFalse(cidade.colocarTesouro(Mapa.ESPADA));
		assertEquals(3, cidade.totalDeTesouros());
		assertTrue(cidade.retirarTesouro(Mapa.GRAOS));
		assertFalse(cidade.retirarTesouro(Mapa.PELE));
		assertEquals(1, cidade.getTesouro(Mapa.GRAOS));
		assertEquals(-1, cidade.getRecurso().getItem(Mapa.ESPADA));
		assertEquals(-1, cidade.getDono());
		assertTrue(cidade.dominar(1));
		assertFalse(cidade.dominar(0));
		assertEquals(1, cidade.getDono());
	}

	@Test
	public void conselheiroGuardaTrocaEMarcaPrimeiraEntrega() {
		Recurso troca = new Recurso();
		troca.setItem(Mapa.PELE, 2);
		Conselheiro conselheiro = new Conselheiro(troca, 0, 1);
		assertEquals(2, conselheiro.getPedido(Mapa.PELE));
		assertEquals(-2, conselheiro.getRecurso().getItem(Mapa.PELE));
		assertEquals(1, conselheiro.getPontuacao());
		assertFalse(conselheiro.isPrimeiro());
		assertTrue(conselheiro.registrarEntrega());
		assertTrue(conselheiro.isPrimeiro());
		assertFalse(conselheiro.registrarEntrega());
	}
}
