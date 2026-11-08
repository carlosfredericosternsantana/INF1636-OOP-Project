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

	// Vizinhas de Karakorum: potes 3 (moeda), 4 (espada), 5 (yurt) e 11 (yurt).
	@Test(timeout = TEMPO)
	public void tributoPrecisaDePunhoEDeProvinciaVizinha() {
		Manager manager = new Manager(2);
		assertFalse("sem coluna ativada nao ha punho", manager.pegarTributo(3));
		assertTrue(manager.ativarColuna(Jogador.VERMELHA));
		assertFalse("o pote 0 nao e vizinho de Karakorum", manager.pegarTributo(0));
		assertTrue(manager.pegarTributo(3));
		assertEquals("a moeda do pote vai para a mao", 2, manager.getJogadorDaVez().r.getItem(Mapa.MOEDA));
		assertFalse("o pote 3 ficou vazio", manager.pegarTributo(3));
		assertTrue(manager.pegarTributo(5));
		assertFalse("a coluna vermelha de Altani so tem 2 punhos", manager.pegarTributo(11));
	}

	@Test(timeout = TEMPO)
	public void khanEObrigatorioENaoFicaNoMesmoLugar() {
		Manager manager = new Manager(2);
		assertTrue(manager.ativarColuna(Jogador.VERMELHA));
		assertFalse("falta colocar o Khan", manager.podePassarVez());
		assertTrue(manager.colocarKhan(2));
		assertEquals("o Khan acrescenta tributo no pote 0", 2, manager.getPote(0).getQuantidade());
		assertTrue(manager.podePassarVez());
		manager.passarVez();
		assertTrue(manager.ativarColuna(Jogador.BRANCA));
		assertFalse("o Khan tem de mudar de lugar", manager.colocarKhan(2));
		assertTrue(manager.colocarKhan(3));
	}

	@Test(timeout = TEMPO)
	public void melhoriaCustaDuasMoedasESoValeNoProximoTurno() {
		Manager manager = new Manager(2);
		Jogador jogador = manager.getJogadorDaVez();
		Melhorias melhoria = manager.melhoriaNaPosicao(5);
		assertFalse("Altani comeca com 1 moeda", manager.comprarMelhoria(5));
		jogador.r.setItem(Mapa.MOEDA, 2);
		assertTrue(manager.comprarMelhoria(5));
		assertEquals("as 2 moedas foram gastas", 0, jogador.r.getItem(Mapa.MOEDA));
		assertEquals("a melhoria vai para a coluna da sua cor", 1, jogador.getMelhoriasNaColuna(melhoria.getCor()));
		assertEquals("as restantes andam para a direita", null, manager.melhoriaNaPosicao(1));
		int tipo = melhoria.getTipo();
		assertTrue(manager.ativarColuna(melhoria.getCor()));
		assertEquals("a melhoria comprada neste turno ainda nao vale",
				jogador.getColunas(melhoria.getCor()).getItem(tipo) - melhoria.getBonus().getItem(tipo),
				manager.getRecursoDoTurno(tipo));
	}

	@Test(timeout = TEMPO)
	public void yurtSoNaParadaAtualEPagandoUmaFicha() {
		Manager manager = new Manager(2);
		Jogador jogador = manager.getJogadorDaVez();
		jogador.r.setItem(Mapa.YURT, 1);
		assertFalse("Karakorum nao aceita yurt", manager.construirYurt());
		manager.definirMovimentos(1);
		assertTrue(manager.mover(6));
		assertTrue(manager.construirYurt());
		assertEquals("a ficha de yurt foi gasta", 0, jogador.r.getItem(Mapa.YURT));
		assertEquals("saiu um yurt do estoque", Jogador.YURTS_POR_JOGADOR - 1, jogador.getYurtsRestantes());
		assertTrue(manager.getMapa().yurtsEm(6).contains(jogador.getIndice()));
	}

	// O 1o tesouro custa 1 espada e os seguintes, 2: tomar os 4 custa 7 e conquista a cidade.
	@Test(timeout = TEMPO)
	public void tomarTodosOsTesourosConquistaACidade() {
		Manager manager = new Manager(2);
		int cidade = 0;
		while (!manager.cidadeRevelada(cidade)) cidade++;
		manager.getJogador(1).setParada(cidade * 3);   // primeira parada em volta da cidade
		manager.passarVez();
		Jogador jogador = manager.getJogadorDaVez();
		jogador.r.setItem(Mapa.ESPADA, 7);
		for (int i = 0; i < 4; i++) {
			int tesouro = Mapa.GRAOS;
			while (manager.getCidade(cidade).getTesouro(tesouro) == 0) tesouro++;
			assertTrue("ataque " + (i + 1), manager.atacarCidade(cidade, tesouro));
		}
		assertEquals("as 7 espadas foram gastas", 0, jogador.r.getItem(Mapa.ESPADA));
		assertEquals("a cidade e do jogador 1", 1, manager.getCidade(cidade).getDono());
		assertEquals("o yurt da cidade sai do estoque", Jogador.YURTS_POR_JOGADOR - 1, jogador.getYurtsRestantes());
		assertFalse("cidade conquistada sai de jogo", manager.cidadeRevelada(cidade));
	}

	// A parada dupla 27 pede 2 tecidos e da 1 voto; a 1a entrega leva a peca de bonus.
	@Test(timeout = TEMPO)
	public void entregaAoConselheiroDaVotoEBonus() {
		Manager manager = new Manager(2);
		manager.getJogador(1).setParada(27);
		manager.passarVez();
		Jogador jogador = manager.getJogadorDaVez();
		assertFalse("sem os tecidos nao ha entrega", manager.entregarAoConselheiro());
		jogador.r.setItem(Mapa.TECIDO, 2);
		assertTrue(manager.entregarAoConselheiro());
		assertEquals("os tecidos foram entregues", 0, jogador.r.getItem(Mapa.TECIDO));
		assertTrue("ganhou pelo menos 1 voto", jogador.r.getItem(Mapa.VOTO) >= 1);
		assertEquals("levou a peca de bonus", 1, jogador.getPecasDeBonus());
		assertEquals("a peca saiu do mapa", -1, manager.getBonusNaParada(27));
	}

	// Com 2 jogadores o fim dispara em 10 votos: termina a rodada e joga mais uma.
	@Test(timeout = TEMPO)
	public void fimTerminaARodadaEJogaMaisUma() {
		Manager manager = new Manager(2);
		manager.getJogadorDaVez().r.setItem(Mapa.VOTO, 10);
		assertTrue(manager.ativarColuna(Jogador.VERMELHA));
		assertTrue("uma acao confere o gatilho do fim", manager.pegarTributo(3));
		for (int turno = 0; turno < 3; turno++) {
			manager.passarVez();
			assertFalse("ainda faltam turnos", manager.isTerminada());
		}
		manager.passarVez();
		assertTrue(manager.isTerminada());
	}

	@Test(timeout = TEMPO)
	public void influenciaPremiaPrimeiroESegundoEmYurts() {
		Manager manager = new Manager(3);
		Jogador a = manager.getJogador(0), b = manager.getJogador(1), c = manager.getJogador(2);
		a.yurtsPorRegiao.set(Mapa.CHINA, 3);
		b.yurtsPorRegiao.set(Mapa.CHINA, 3);
		c.yurtsPorRegiao.set(Mapa.CHINA, 1);
		a.melhoriasPorColuna.set(Jogador.VERMELHA, 4);
		c.melhoriasPorColuna.set(Jogador.VERMELHA, 2);
		c.yurtsPorRegiao.set(Mapa.RUSSIA, 2);
		a.yurtsPorRegiao.set(Mapa.RUSSIA, 1);
		a.melhoriasPorColuna.set(Jogador.AMARELA, 1);
		assertEquals("A: 1o empatado na China (3) e 2o na Russia (1)", 4, manager.votosDeInfluencia(0));
		assertEquals("B: 1o empatado na China sem melhorias", 1, manager.votosDeInfluencia(1));
		assertEquals("C: ninguem e 2o na China; 1o na Russia", 1, manager.votosDeInfluencia(2));
	}
}
