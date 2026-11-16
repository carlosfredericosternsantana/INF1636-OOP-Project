package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

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
		assertFalse("Altani comeca com 1 moeda", manager.comprarMelhoria(5, -1));
		jogador.r.setItem(Mapa.MOEDA, 2);
		assertTrue(manager.comprarMelhoria(5, -1));
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
		assertFalse("Karakorum nao aceita yurt", manager.construirYurt(Mapa.KARAKORUM));
		manager.definirMovimentos(1);
		assertTrue(manager.mover(6));
		assertFalse("Altani nao e Tolui: so na parada atual", manager.construirYurt(7));
		assertTrue(manager.construirYurt(6));
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

	// ---------- 4a iteracao ----------

	private static String salvar (Manager manager) {
		StringWriter texto = new StringWriter();
		PrintWriter out = new PrintWriter(texto);
		manager.salvar(out);
		out.flush();
		return texto.toString();
	}

	// 5 jogadores: Altani, Jochi, Chagatai, Tolui e Ogedei.
	@Test(timeout = TEMPO)
	public void partidaSalvaVoltaIgualAoCarregar() throws IOException {
		Manager manager = new Manager(5);
		while (manager.getJogadorEscolhendo() >= 0) assertTrue(manager.escolherNoInicio(0));
		manager.getJogador(4).setNome("Jo\u00e3o Paulo");
		assertTrue(manager.ativarColuna(Jogador.VERMELHA));
		assertFalse("depois da 1a acao nao da para salvar", manager.podeSalvar());
		assertTrue(manager.colocarKhan(2));
		assertTrue(manager.mover(6));
		assertTrue(manager.pegarTributo(3));
		manager.passarVez();
		assertTrue("no inicio do turno da para salvar", manager.podeSalvar());
		String salvo = salvar(manager);
		Manager carregado = Manager.carregar(new BufferedReader(new StringReader(salvo)));
		assertEquals("gravar a partida carregada da o mesmo arquivo", salvo, salvar(carregado));
		assertEquals(1, carregado.getVez());
		assertEquals("o nome fica sem acento (ASCII)", "Joao Paulo", carregado.getJogador(4).getNome());
		assertEquals(6, carregado.getJogador(0).getParada());
		assertEquals("2o peao de Altani", Mapa.KARAKORUM, carregado.getJogador(0).getOutraParada());
	}

	@Test(timeout = TEMPO, expected = IOException.class)
	public void arquivoQueNaoEPartidaNaoCarrega() throws IOException {
		Manager.carregar(new BufferedReader(new StringReader("vez 2\n")));
	}

	@Test(timeout = TEMPO)
	public void altaniEscolheQualDosDoisPeoesJoga() {
		Manager manager = new Manager(2);
		Jogador altani = manager.getJogadorDaVez();
		manager.definirMovimentos(1);
		assertTrue(manager.mover(6));
		manager.passarVez();
		manager.passarVez();
		assertTrue("antes de jogar Altani troca de peao", manager.usarHabilidade());
		assertEquals("o peao de Karakorum e o ativo", Mapa.KARAKORUM, altani.getParada());
		assertTrue("trocar de peao nao impede salvar", manager.podeSalvar());
		manager.definirMovimentos(1);
		assertFalse("o outro peao de Altani ocupa a parada 6", manager.mover(6));
		assertFalse("depois de comecar a jogar nao troca mais", manager.podeUsarHabilidade());
	}

	@Test(timeout = TEMPO)
	public void jochiComecaComMelhoriaNaBrancaEEscolheAColuna() {
		Manager manager = new Manager(2);
		assertTrue(manager.escolherNoInicio(0));
		assertTrue(manager.escolherNoInicio(1));
		assertEquals("falta a melhoria inicial de Jochi", 1, manager.getJogadorEscolhendo());
		Melhorias escolhida = manager.getOpcaoDeMelhoria(1);
		assertTrue(manager.escolherNoInicio(1));
		assertEquals("todos escolheram", -1, manager.getJogadorEscolhendo());
		Jogador jochi = manager.getJogador(1);
		assertEquals(escolhida, jochi.melhoriasNaCartela.get(Jogador.BRANCA).get(0));
		manager.passarVez();
		jochi.r.setItem(Mapa.MOEDA, 4);
		int outraCor = (manager.melhoriaNaPosicao(5).getCor() + 1) % 3;
		assertFalse("so nas colunas coloridas", manager.comprarMelhoria(5, Jogador.BRANCA));
		assertTrue("Jochi nao segue a cor", manager.comprarMelhoria(5, outraCor));
		assertEquals(1, jochi.getMelhoriasNaColuna(outraCor));
		assertTrue("compra do topo da pilha", manager.comprarMelhoria(Melhorias.EMBARALHADA, outraCor));
		assertEquals(2, jochi.getMelhoriasNaColuna(outraCor));
		assertEquals("4 moedas gastas", 0, jochi.r.getItem(Mapa.MOEDA));
	}

	@Test(timeout = TEMPO)
	public void habilidadesDeChagataiToluiEOgedei() {
		Manager manager = new Manager(5);
		manager.passarVez();
		manager.passarVez();
		manager.definirMovimentos(2);
		assertTrue("Chagatai troca cavalo por espada", manager.usarHabilidade());
		assertEquals(1, manager.getMovimentos());
		assertEquals(1, manager.getJogadorDaVez().r.getItem(Mapa.ESPADA));
		assertFalse("so 1x por turno", manager.usarHabilidade());
		manager.passarVez();

		Jogador tolui = manager.getJogadorDaVez();
		assertTrue("Tolui comeca com yurt em Karakorum", manager.getMapa().yurtsEm(Mapa.KARAKORUM).contains(tolui.getIndice()));
		assertEquals(Jogador.YURTS_POR_JOGADOR - 1, tolui.getYurtsRestantes());
		tolui.r.setItem(Mapa.YURT, 1);
		assertTrue("Tolui constroi na vizinha sem sair de Karakorum", manager.construirYurt(6));
		manager.passarVez();

		Jogador ogedei = manager.getJogadorDaVez();
		int cidade = 0;
		while (!manager.cidadeRevelada(cidade)) cidade++;
		int tesouro = Mapa.GRAOS;
		while (manager.getCidade(cidade).getTesouro(tesouro) == 0) tesouro++;
		assertTrue("Ogedei toma tesouro de cidade longe", manager.tomarComMoeda(cidade, tesouro));
		assertEquals("pagou 1 das 2 moedas", 1, ogedei.r.getItem(Mapa.MOEDA));
		assertEquals(3, manager.getCidade(cidade).totalDeTesouros());
		assertFalse("so 1x por turno", manager.podeTomarComMoeda(cidade));
	}

	// Duplas 27 e 28 nao sao vizinhas; 27-1-3 formam uma rede. Provincias do Khan:
	// pote 0 (paradas 1, 2, 3, 32) e pote 3 (6, 8, 28, 33).
	@Test(timeout = TEMPO)
	public void conselheiroSecretoDaVotosNoFim() {
		Manager manager = new Manager(2);
		Jogador jogador = manager.getJogador(0);
		for (int parada : new int[] { 27, 28, 1, 3 }) manager.getMapa().yurtsEm(parada).add(0);
		jogador.setConselheiroSecreto(6);
		assertEquals("Mensageiro: 2 duplas", 1, manager.votosDoSecreto(0));
		jogador.setConselheiroSecreto(2);
		assertEquals("Viajante: rede de 3", 1, manager.votosDoSecreto(0));
		jogador.setConselheiroSecreto(9);
		assertEquals("Bajulador: so a provincia 0 tem 2 yurts", 0, manager.votosDoSecreto(0));
		manager.getMapa().yurtsEm(Mapa.KARAKORUM).add(0);
		assertEquals("yurt em Karakorum nao conta para o secreto", 0, manager.votosDoSecreto(0));
		manager.getMapa().yurtsEm(6).add(0);
		assertEquals("Bajulador: provincias 0 e 3", 1, manager.votosDoSecreto(0));
		assertEquals("o secreto entra no total", manager.votosDeInfluencia(0) + 1, manager.votosFinais(0));
	}
}
