package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ColetavelTest {

	private static final int TEMPO = 1000;

	@Test(timeout = TEMPO)
	public void poteNaoPassaDeTresEEsvaziaTudo() {
		PoteDeRecursos pote = new PoteDeRecursos(Mapa.MOEDA);
		assertTrue("1a unidade deveria entrar", pote.adicionar());
		assertTrue("2a unidade deveria entrar", pote.adicionar());
		assertTrue("3a unidade deveria entrar", pote.adicionar());
		assertFalse("4a unidade deveria ser recusada", pote.adicionar());
		assertEquals("quantidade no pote cheio", 3, pote.getQuantidade());
		assertEquals("custo de 1 maozinha", -1, pote.getRecurso().getItem(2));
		assertEquals("unidades devolvidas ao esvaziar", 3, pote.esvaziar());
		assertEquals("quantidade apos esvaziar", 0, pote.getQuantidade());
	}

	@Test(timeout = TEMPO)
	public void cidadeGuardaTesourosESoEDominadaUmaVez() {
		Cidade cidade = new Cidade(new Recurso());
		assertTrue("graos e tesouro", cidade.colocarTesouro(Mapa.GRAOS));
		assertTrue("graos e tesouro", cidade.colocarTesouro(Mapa.GRAOS));
		assertTrue("vaca e tesouro", cidade.colocarTesouro(Mapa.VACA));
		assertFalse("espada nao e tesouro", cidade.colocarTesouro(Mapa.ESPADA));
		assertEquals("total de tesouros", 3, cidade.totalDeTesouros());
		assertTrue("deveria retirar 1 grao", cidade.retirarTesouro(Mapa.GRAOS));
		assertFalse("nao ha pele para retirar", cidade.retirarTesouro(Mapa.PELE));
		assertEquals("graos restantes", 1, cidade.getTesouro(Mapa.GRAOS));
		assertEquals("custo de ataque de 1 espada", -1, cidade.getRecurso().getItem(Mapa.ESPADA));
		assertEquals("cidade comeca sem dono", -1, cidade.getDono());
		assertTrue("jogador 1 deveria dominar", cidade.dominar(1));
		assertFalse("cidade ja dominada", cidade.dominar(0));
		assertEquals("dono da cidade", 1, cidade.getDono());
	}

	@Test(timeout = TEMPO)
	public void conselheiroGuardaTrocaEMarcaPrimeiraEntrega() {
		Recurso troca = new Recurso();
		troca.setItem(Mapa.PELE, 2);
		Conselheiro conselheiro = new Conselheiro(troca, 0, 1);
		assertEquals("peles pedidas", 2, conselheiro.getPedido(Mapa.PELE));
		assertEquals("peles guardadas como custo", -2, conselheiro.getRecurso().getItem(Mapa.PELE));
		assertEquals("pontuacao", 1, conselheiro.getPontuacao());
		assertFalse("ninguem entregou ainda", conselheiro.isPrimeiro());
		assertTrue("primeira entrega da bonus", conselheiro.registrarEntrega());
		assertTrue("conselheiro ja recebeu a primeira entrega", conselheiro.isPrimeiro());
		assertFalse("segunda entrega nao da bonus", conselheiro.registrarEntrega());
	}
}
