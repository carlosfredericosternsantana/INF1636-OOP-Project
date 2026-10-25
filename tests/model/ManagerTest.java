package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ManagerTest {

	private static final int TEMPO = 1000;

	// Vizinhas de Karakorum: 6 (Sarai), 14 (Pequim), 25 (Samarcanda) e 28 (dupla do ferro).
	@Test(timeout = TEMPO)
	public void colunaDaCartelaDefineQuantosMovimentosOJogadorTem() {
		Manager manager = new Manager(2);
		assertTrue("Altani deveria ativar a coluna verde", manager.ativarColuna(Jogador.VERDE));
		assertEquals("a coluna verde de Altani tem 2 cavalos", 2, manager.getMovimentos());
		assertFalse("so uma coluna por turno", manager.ativarColuna(Jogador.VERMELHA));
	}

	@Test(timeout = TEMPO)
	public void aPecaFicaNaColunaAteAColunaBrancaRecuperarTodas() {
		Manager manager = new Manager(2);
		assertTrue(manager.ativarColuna(Jogador.VERDE));
		manager.passarVez();
		manager.passarVez();
		assertFalse("a peca de ativacao continua na coluna verde", manager.ativarColuna(Jogador.VERDE));
		assertTrue(manager.ativarColuna(Jogador.BRANCA));
		manager.passarVez();
		manager.passarVez();
		assertTrue("a coluna branca devolve todas as pecas", manager.ativarColuna(Jogador.VERDE));
	}

	@Test(timeout = TEMPO)
	public void soDaParaAndarAteOndeOsMovimentosAlcancam() {
		Manager manager = new Manager(2);
		manager.definirMovimentos(1);
		assertFalse("a parada 7 esta a 2 movimentos de Karakorum", manager.mover(7));
		assertTrue("a parada 6 e vizinha de Karakorum", manager.paradasAlcancaveis().contains(6));
		assertFalse("a parada 7 nao esta ao alcance", manager.paradasAlcancaveis().contains(7));
		assertTrue(manager.mover(6));
		assertEquals("o movimento foi gasto", 0, manager.getMovimentos());
	}

	@Test(timeout = TEMPO)
	public void piaoPassaPorParadaSimplesOcupadaMasNaoParaNela() {
		Manager manager = new Manager(2);
		manager.definirMovimentos(1);
		assertTrue(manager.mover(6));
		manager.passarVez();
		manager.definirMovimentos(2);
		assertFalse("a parada 6 e simples e ja esta ocupada", manager.mover(6));
		assertTrue("mas da para passar por ela e parar na 7", manager.mover(7));
	}

	@Test(timeout = TEMPO)
	public void paradaDuplaRecebeDoisPioesENaoTres() {
		Manager manager = new Manager(3);
		for (int jogador = 0; jogador < 3; jogador++) {
			manager.definirMovimentos(1);
			boolean coube = manager.mover(28);
			assertEquals("a parada dupla cabe 2 pioes", jogador < 2, coube);
			manager.passarVez();
		}
	}

	@Test(timeout = TEMPO)
	public void yurtDoProprioJogadorPermitePularUmaParada() {
		Manager manager = new Manager(2);
		manager.definirMovimentos(1);
		assertFalse("sem yurt, a parada 7 custa 2 movimentos", manager.mover(7));
		manager.getMapa().yurtsEm(6).add(manager.getJogadorDaVez().getIndice());
		assertTrue("o yurt na parada 6 deixa pular ate a parada 7", manager.mover(7));
	}
}
