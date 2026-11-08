package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

// Estado de uma partida e regras do turno. E o objeto observado pela view:
// toda mudanca de estado termina em notificar().
class Manager implements ObservadoIF {

	static final int NENHUMA_COLUNA = -1;
	static final int MAX_JOGADORES = 5;
	static final int PRECO_DA_MELHORIA = 2;

	// pecas de bonus, uma em cada parada de conselheiro
	static final int BONUS_VOTO = 0, BONUS_ESPADAS = 1, BONUS_MOEDAS = 2, BONUS_YURTS = 3;
	static final int BONUS_CAVALOS = 4, BONUS_MAOS = 5, BONUS_TESOUROS = 6;
	static final int NUM_BONUS = 7;

	static final int NUM_CONSELHEIROS_SECRETOS = 10;

	private static final int CIDADES_REVELADAS = 3;
	private static final int TESOUROS_POR_CIDADE = 4;
	private static final int TESOUROS_DE_CADA_TIPO = 8;
	private static final int CARTAS_ABERTAS = 4;
	private static final int ESPACO_DA_MOEDA = 2, ESPACO_DA_ESPADA = 4;   // icones na area de melhorias
	private static final int[] VOTOS_PARA_O_FIM = { 0, 0, 10, 14, 16, 17 };
	// votos de influencia pelo numero de melhorias (0 a 4) na coluna da regiao
	private static final int[] VOTOS_DO_PRIMEIRO = { 1, 1, 2, 2, 3 };
	private static final int[] VOTOS_DO_SEGUNDO = { 0, 1, 1, 2, 2 };
	// coluna da cartela de cada regiao, na ordem Mapa.CHINA, RUSSIA, PERSIA
	private static final int[] COLUNA_DA_REGIAO = { Jogador.VERMELHA, Jogador.AMARELA, Jogador.VERDE };

	private final ArrayList<ObservadorIF> observadores = new ArrayList<ObservadorIF>();
	private int evento = NOVO_TURNO;

	private final Mapa mapa;
	private final ArrayList<Jogador> jogadores = new ArrayList<Jogador>();
	private int vez = 0;

	// estado do turno
	private int movimentos = 0;
	private int colunaAtivada = NENHUMA_COLUNA;
	private Recurso turno = new Recurso();       // acoes e recursos virtuais da coluna ativada
	private boolean khanPendente = false;
	private final ArrayList<Integer> paradasDoTurno = new ArrayList<Integer>();
	private final ArrayList<Integer> cidadesAtacadas = new ArrayList<Integer>();
	private final ArrayList<Melhorias> compradasNoTurno = new ArrayList<Melhorias>();
	private final ArrayList<Integer> tesourosParaEscolher = new ArrayList<Integer>();

	// pecas da mesa
	private final ArrayList<Integer> sacoDeTesouros = new ArrayList<Integer>();
	private final ArrayList<Integer> pilhaDeCidades = new ArrayList<Integer>();
	private final ArrayList<Integer> cidadesReveladas = new ArrayList<Integer>();
	private final ArrayList<Melhorias> melhorias = Melhorias.criarPilha();
	private final HashMap<Integer, Quest> cartasAbertas = new HashMap<Integer, Quest>();
	private final HashMap<Integer, Integer> bonusNoMapa = new HashMap<Integer, Integer>();

	private int turnosRestantes = -1;            // -1 enquanto o fim nao foi disparado
	private boolean terminada = false;

	Manager (int quantidadeDeJogadores) {
		this(herdeirosEmOrdem(quantidadeDeJogadores));
	}

	Manager (int[] herdeiros) {
		if (herdeiros.length < 2 || herdeiros.length > MAX_JOGADORES)
			throw new IllegalArgumentException("a partida tem de 2 a " + MAX_JOGADORES + " jogadores");
		this.mapa = new Mapa();
		ArrayList<Integer> secretos = embaralhados(NUM_CONSELHEIROS_SECRETOS);
		for (int i = 0; i < herdeiros.length; i++) {
			Jogador jogador = new Jogador(i, herdeiros[i]);
			jogador.r.setItem(Mapa.MOEDA, i < 2 ? 1 : 2);
			jogador.setConselheiroSecreto(secretos.get(i));
			jogadores.add(jogador);
		}
		prepararMesa();
		comecarTurno();
	}

	private static int[] herdeirosEmOrdem (int quantidade) {
		int[] herdeiros = new int[quantidade];
		for (int i = 0; i < quantidade; i++) herdeiros[i] = i;
		return herdeiros;
	}

	private static ArrayList<Integer> embaralhados (int quantidade) {
		ArrayList<Integer> lista = new ArrayList<Integer>();
		for (int i = 0; i < quantidade; i++) lista.add(i);
		Collections.shuffle(lista);
		return lista;
	}

	private void prepararMesa () {
		for (int pote = 0; pote < Mapa.NUM_POTES; pote++)
			((PoteDeRecursos) mapa.coletaveis.get(pote)).adicionar();
		for (int tipo = Mapa.GRAOS; tipo <= Mapa.VACA; tipo++)
			for (int i = 0; i < TESOUROS_DE_CADA_TIPO; i++) sacoDeTesouros.add(tipo);
		Collections.shuffle(sacoDeTesouros);
		for (int cidade : embaralhados(Mapa.NUM_CIDADES)) pilhaDeCidades.add(Mapa.PRIMEIRA_CIDADE + cidade);
		for (int i = 0; i < CIDADES_REVELADAS; i++) revelarCidade();
		Collections.shuffle(melhorias);
		reporMelhorias();
		HashMap<Integer, Quest> todas = Quest.criarCartas();
		ArrayList<Integer> ids = embaralhados(todas.size());
		for (int i = 0; i < CARTAS_ABERTAS; i++) cartasAbertas.put(ids.get(i), todas.get(ids.get(i)));
		ArrayList<Integer> bonus = embaralhados(NUM_BONUS);
		ArrayList<Integer> paradas = mapa.paradasDeConselheiro();
		for (int i = 0; i < paradas.size(); i++) bonusNoMapa.put(paradas.get(i), bonus.get(i));
	}

	// ---------- Observer ----------

	public void add (ObservadorIF o) { observadores.add(o); }

	public void remove (ObservadorIF o) { observadores.remove(o); }

	public int get (int i) {
		if (i == EVENTO) return evento;
		if (i == VEZ) return vez;
		return 0;
	}

	private void notificar (int evento) {
		this.evento = evento;
		// copia: um observador pode se registrar durante a notificacao (ex.: janela de resultado)
		for (ObservadorIF o : new ArrayList<ObservadorIF>(observadores)) o.notify(this);
	}

	// ---------- consultas ----------

	Mapa getMapa () { return mapa; }

	int getQuantidadeDeJogadores () { return jogadores.size(); }

	Jogador getJogador (int indice) { return jogadores.get(indice); }

	Jogador getJogadorDaVez () { return jogadores.get(vez); }

	int getVez () { return vez; }

	int getMovimentos () { return movimentos; }

	int getColunaAtivada () { return colunaAtivada; }

	int getRecursoDoTurno (int tipo) { return turno.getItem(tipo); }

	boolean isKhanPendente () { return khanPendente; }

	boolean isTerminada () { return terminada; }

	ArrayList<Integer> getTesourosParaEscolher () { return new ArrayList<Integer>(tesourosParaEscolher); }

	boolean cidadeRevelada (int cidade) { return cidadesReveladas.contains(Mapa.PRIMEIRA_CIDADE + cidade); }

	Cidade getCidade (int cidade) { return (Cidade) mapa.coletaveis.get(Mapa.PRIMEIRA_CIDADE + cidade); }

	PoteDeRecursos getPote (int pote) { return (PoteDeRecursos) mapa.coletaveis.get(pote); }

	// Peca de bonus ainda na parada de conselheiro, ou -1.
	int getBonusNaParada (int parada) {
		Integer bonus = bonusNoMapa.get(parada);
		return bonus == null ? -1 : bonus;
	}

	ArrayList<Integer> getCartasAbertas () {
		ArrayList<Integer> ids = new ArrayList<Integer>(cartasAbertas.keySet());
		Collections.sort(ids);
		return ids;
	}

	Melhorias melhoriaNaPosicao (int posicao) {
		for (Melhorias m : melhorias)
			if (m.getPosicao() == posicao) return m;
		return null;
	}

	int totalDeVotos () {
		int total = 0;
		for (Jogador jogador : jogadores) total += jogador.r.getItem(Mapa.VOTO);
		return total;
	}

	// ---------- turno ----------

	private void comecarTurno () {
		movimentos = 0;
		colunaAtivada = NENHUMA_COLUNA;
		turno = new Recurso();
		khanPendente = false;
		paradasDoTurno.clear();
		paradasDoTurno.add(getJogadorDaVez().getParada());
		cidadesAtacadas.clear();
		compradasNoTurno.clear();
	}

	// O jogador coloca uma peca de ativacao numa coluna e ganha as acoes dela.
	// Melhoria comprada neste turno so vale a partir do proximo.
	boolean ativarColuna (int coluna) {
		if (terminada || colunaAtivada != NENHUMA_COLUNA) return false;
		Jogador jogador = getJogadorDaVez();
		if (!jogador.ativarColuna(coluna)) return false;
		colunaAtivada = coluna;
		Recurso acoes = jogador.getColunas(coluna);
		for (Melhorias m : compradasNoTurno)
			if (m.getColuna() == coluna) somar(acoes, m.getBonus(), -1);
		somar(turno, acoes, 1);
		movimentos += acoes.getItem(Mapa.CAVALO);
		khanPendente = acoes.getItem(Mapa.CABECA_DO_KHAN) > 0;
		notificar(COLUNA_ATIVADA);
		return true;
	}

	private static void somar (Recurso destino, Recurso parcela, int sinal) {
		for (int i = 0; i < 12; i++)
			destino.setItem(i, destino.getItem(i) + sinal * parcela.getItem(i));
	}

	// Usado apenas para testar situacoes especificas, no lugar da coluna da cartela.
	void definirMovimentos (int quantidade) {
		if (quantidade < 0) return;
		movimentos = quantidade;
		notificar(PIAO_MOVIDO);
	}

	// O Khan e obrigatorio e a escolha do bonus tem de ser feita antes de passar a vez.
	boolean podePassarVez () {
		return !terminada && colunaAtivada != NENHUMA_COLUNA && !khanPendente && tesourosParaEscolher.isEmpty();
	}

	void passarVez () {
		if (terminada) return;
		if (turnosRestantes == 0) {
			terminada = true;
			notificar(FIM);
			return;
		}
		if (turnosRestantes > 0) turnosRestantes--;
		vez = (vez + 1) % jogadores.size();
		comecarTurno();
		notificar(NOVO_TURNO);
	}

	// Toda acao termina aqui: confere as cartas abertas e o gatilho do fim.
	private void depoisDaAcao (int evento) {
		Jogador jogador = getJogadorDaVez();
		for (int id : getCartasAbertas())
			if (cartasAbertas.get(id).checarQuests(jogador, vez)) {
				cartasAbertas.remove(id);
				ganharVotos(1);
			}
		boolean todasConquistadas = pilhaDeCidades.isEmpty() && cidadesReveladas.isEmpty();
		if (turnosRestantes < 0 && (totalDeVotos() >= VOTOS_PARA_O_FIM[jogadores.size()] || todasConquistadas))
			turnosRestantes = 2 * jogadores.size() - 1 - vez;   // termina a rodada e joga mais uma
		notificar(evento);
	}

	// ---------- recursos ----------

	// O jogador tem o recurso virtual do turno mais o que guarda na mao.
	private int disponivel (int tipo) {
		return turno.getItem(tipo) + getJogadorDaVez().r.getItem(tipo);
	}

	// Gasta primeiro o virtual, que nao passa para o proximo turno.
	private void gastar (int tipo, int quantidade) {
		int doTurno = Math.min(quantidade, turno.getItem(tipo));
		turno.setItem(tipo, turno.getItem(tipo) - doTurno);
		ganhar(tipo, doTurno - quantidade);
	}

	private void ganhar (int tipo, int quantidade) {
		Recurso mao = getJogadorDaVez().r;
		mao.setItem(tipo, mao.getItem(tipo) + quantidade);
	}

	private void ganharVotos (int votos) {
		ganhar(Mapa.VOTO, votos);
	}

	private int sacarTesouro () {
		return sacoDeTesouros.remove(sacoDeTesouros.size() - 1);
	}

	// Vale para pegar tributo, atacar cidade e entregar a conselheiro.
	private boolean alcancaNoTurno (int coletavel) {
		for (int parada : paradasDoTurno)
			if (mapa.alcanca(parada, coletavel)) return true;
		return false;
	}

	// ---------- movimento ----------

	ArrayList<Integer> paradasAlcancaveis () {
		ArrayList<Integer> alcancaveis = new ArrayList<Integer>();
		if (movimentos <= 0 || terminada) return alcancaveis;
		Jogador jogador = getJogadorDaVez();
		int[] distancia = distancias(jogador);
		for (int parada = 0; parada < Mapa.NUM_PARADAS; parada++)
			if (distancia[parada] <= movimentos && podeParar(jogador, parada))
				alcancaveis.add(parada);
		return alcancaveis;
	}

	boolean mover (int destino) {
		if (movimentos <= 0 || terminada) return false;
		if (destino < 0 || destino >= Mapa.NUM_PARADAS) return false;
		Jogador jogador = getJogadorDaVez();
		if (!podeParar(jogador, destino)) return false;
		int gasto = distancias(jogador)[destino];
		if (gasto > movimentos) return false;
		movimentos -= gasto;
		jogador.setParada(destino);
		paradasDoTurno.add(destino);
		notificar(PIAO_MOVIDO);
		return true;
	}

	// Quantos movimentos custa chegar a cada parada. Sair de uma parada onde o
	// proprio jogador tem um yurt nao custa movimento: e o pulo que o yurt permite.
	private int[] distancias (Jogador jogador) {
		int[] distancia = new int[Mapa.NUM_PARADAS];
		for (int parada = 0; parada < distancia.length; parada++)
			distancia[parada] = Integer.MAX_VALUE;
		int origem = jogador.getParada();
		distancia[origem] = 0;
		ArrayDeque<Integer> fila = new ArrayDeque<Integer>();
		fila.addLast(origem);
		while (!fila.isEmpty()) {
			int atual = fila.pollFirst();
			int custo = (atual != origem && mapa.yurtsEm(atual).contains(jogador.getIndice())) ? 0 : 1;
			for (int vizinho : mapa.adj(atual)) {
				if (distancia[atual] + custo >= distancia[vizinho]) continue;
				distancia[vizinho] = distancia[atual] + custo;
				if (custo == 0) fila.addFirst(vizinho);
				else fila.addLast(vizinho);
			}
		}
		return distancia;
	}

	// Da para passar por uma parada ocupada, mas so da para parar nela se couber
	// mais um piao: 1 nas paradas simples, 2 nas duplas e todos em Karakorum.
	private boolean podeParar (Jogador jogador, int parada) {
		if (parada == jogador.getParada()) return false;
		int ocupantes = 0;
		for (Jogador outro : jogadores)
			if (outro != jogador && outro.getParada() == parada) ocupantes++;
		return ocupantes < mapa.capacidadeDePioes(parada);
	}

	// ---------- acoes da coluna ----------

	boolean pegarTributo (int pote) {
		if (terminada || pote < 0 || pote >= Mapa.NUM_POTES) return false;
		if (turno.getItem(Mapa.MAOZINHA) < 1 || !alcancaNoTurno(pote)) return false;
		PoteDeRecursos p = getPote(pote);
		if (!p.retirar()) return false;
		gastar(Mapa.MAOZINHA, 1);
		ganhar(p.getTipo(), 1);
		depoisDaAcao(TABULEIRO);
		return true;
	}

	// Numa provincia, o Khan faz 3 potes receberem tributo. Na area de melhorias,
	// o jogador leva a moeda e a espada dos icones descobertos e a area e reposta.
	boolean colocarKhan (int posicao) {
		if (terminada || !khanPendente || posicao == Mapa.FORA_DO_MAPA) return false;
		if (!mapa.moverKhan(posicao)) return false;
		if (posicao == Mapa.TRILHA_DE_MELHORIAS) {
			if (melhoriaNaPosicao(ESPACO_DA_MOEDA) == null) ganhar(Mapa.MOEDA, 1);
			if (melhoriaNaPosicao(ESPACO_DA_ESPADA) == null) ganhar(Mapa.ESPADA, 1);
			reporMelhorias();
		}
		khanPendente = false;
		depoisDaAcao(TABULEIRO);
		return true;
	}

	// ---------- acoes extras ----------

	int custoDoAtaque (int cidade) {
		return cidadesAtacadas.contains(cidade) ? 2 : 1;
	}

	boolean podeAtacar (int cidade) {
		if (terminada || cidade < 0 || cidade >= Mapa.NUM_CIDADES || !cidadeRevelada(cidade)) return false;
		return alcancaNoTurno(Mapa.PRIMEIRA_CIDADE + cidade) && disponivel(Mapa.ESPADA) >= custoDoAtaque(cidade);
	}

	// O 1o tesouro de uma cidade no turno custa 1 espada; os seguintes, 2.
	boolean atacarCidade (int cidade, int tesouro) {
		if (!podeAtacar(cidade)) return false;
		Cidade c = getCidade(cidade);
		int custo = custoDoAtaque(cidade);
		if (!c.retirarTesouro(tesouro)) return false;
		gastar(Mapa.ESPADA, custo);
		ganhar(tesouro, 1);
		if (!cidadesAtacadas.contains(cidade)) cidadesAtacadas.add(cidade);
		if (c.totalDeTesouros() == 0) {
			c.dominar(vez);
			getJogadorDaVez().conquistarCidade(Mapa.regiaoDaCidade(cidade));
			cidadesReveladas.remove(Integer.valueOf(Mapa.PRIMEIRA_CIDADE + cidade));
			revelarCidade();
		}
		depoisDaAcao(TABULEIRO);
		return true;
	}

	private void revelarCidade () {
		if (pilhaDeCidades.isEmpty()) return;
		int coletavel = pilhaDeCidades.remove(pilhaDeCidades.size() - 1);
		Cidade cidade = (Cidade) mapa.coletaveis.get(coletavel);
		for (int i = 0; i < TESOUROS_POR_CIDADE && !sacoDeTesouros.isEmpty(); i++)
			cidade.colocarTesouro(sacarTesouro());
		cidadesReveladas.add(coletavel);
	}

	// Yurt so na parada atual, pagando uma ficha de yurt (real ou virtual).
	boolean podeConstruirYurt () {
		Jogador jogador = getJogadorDaVez();
		return !terminada && disponivel(Mapa.YURT) >= 1 && jogador.podeColocarYurt(mapa, jogador.getParada());
	}

	boolean construirYurt () {
		if (!podeConstruirYurt()) return false;
		Jogador jogador = getJogadorDaVez();
		jogador.colocarYurt(mapa, jogador.getParada());
		gastar(Mapa.YURT, 1);
		depoisDaAcao(TABULEIRO);
		return true;
	}

	// O jogador compra a melhoria do espaco; se tiver uma gratis, ela vai para a
	// coluna branca. As restantes andam para a direita.
	boolean comprarMelhoria (int posicao) {
		if (terminada) return false;
		Melhorias m = melhoriaNaPosicao(posicao);
		if (m == null) return false;
		Jogador jogador = getJogadorDaVez();
		boolean gratis = jogador.melhoriasGratis > 0;
		int coluna = gratis ? Jogador.BRANCA : m.getCor();
		if (jogador.getMelhoriasNaColuna(coluna) >= Melhorias.LINHAS) return false;
		if (!gratis && disponivel(Mapa.MOEDA) < PRECO_DA_MELHORIA) return false;
		if (gratis) jogador.melhoriasGratis--;
		else gastar(Mapa.MOEDA, PRECO_DA_MELHORIA);
		m.aplicar(jogador, coluna);
		compradasNoTurno.add(m);
		encostarMelhorias();
		if (jogador.linhasCompletas() > jogador.getMelhoriasNaColuna(Jogador.BRANCA) + jogador.melhoriasGratis)
			jogador.melhoriasGratis++;
		depoisDaAcao(TABULEIRO);
		return true;
	}

	// As melhorias da area ficam encostadas a direita (posicao 5).
	private void encostarMelhorias () {
		int livre = Melhorias.TAMANHO_TRILHA;
		for (int posicao = Melhorias.TAMANHO_TRILHA; posicao >= 1; posicao--) {
			Melhorias m = melhoriaNaPosicao(posicao);
			if (m == null) continue;
			m.setPosicao(livre);
			livre--;
		}
	}

	private void reporMelhorias () {
		encostarMelhorias();
		for (int posicao = 1; posicao <= Melhorias.TAMANHO_TRILHA; posicao++) {
			if (melhoriaNaPosicao(posicao) != null) continue;
			for (Melhorias m : melhorias)
				if (m.getPosicao() == Melhorias.EMBARALHADA) {
					m.setPosicao(posicao);
					break;
				}
		}
	}

	// Entrega no conselheiro de alguma parada do turno, comecando pela atual.
	boolean podeEntregar () {
		return paradaParaEntrega() >= 0;
	}

	private int paradaParaEntrega () {
		if (terminada) return -1;
		for (int i = paradasDoTurno.size() - 1; i >= 0; i--) {
			int parada = paradasDoTurno.get(i);
			int idx = mapa.conselheiroEm(parada);
			if (idx >= 0 && podePagar((Conselheiro) mapa.coletaveis.get(idx))) return parada;
		}
		return -1;
	}

	private boolean podePagar (Conselheiro conselheiro) {
		int sobra = 0;
		for (int t = Mapa.GRAOS; t <= Mapa.VACA; t++) {
			if (disponivel(t) < conselheiro.getPedido(t)) return false;
			sobra += disponivel(t) - conselheiro.getPedido(t);
		}
		return sobra >= conselheiro.getQuaisquer();
	}

	// Os tesouros "quaisquer" saem automaticamente do tipo que o jogador tem mais.
	boolean entregarAoConselheiro () {
		int parada = paradaParaEntrega();
		if (parada < 0) return false;
		Conselheiro conselheiro = (Conselheiro) mapa.coletaveis.get(mapa.conselheiroEm(parada));
		for (int t = Mapa.GRAOS; t <= Mapa.VACA; t++) gastar(t, conselheiro.getPedido(t));
		for (int i = 0; i < conselheiro.getQuaisquer(); i++) {
			int maior = Mapa.GRAOS;
			for (int t = Mapa.GRAOS; t <= Mapa.VACA; t++)
				if (disponivel(t) > disponivel(maior)) maior = t;
			gastar(maior, 1);
		}
		ganharVotos(conselheiro.getPontuacao());
		if (conselheiro.registrarEntrega()) receberBonus(parada);
		depoisDaAcao(tesourosParaEscolher.isEmpty() ? CARTAS : ESCOLHA);
		return true;
	}

	// O efeito da peca de bonus e aplicado na hora.
	private void receberBonus (int parada) {
		Integer bonus = bonusNoMapa.remove(parada);
		if (bonus == null) return;
		getJogadorDaVez().receberBonus(bonus);
		switch (bonus) {
			case BONUS_VOTO:     ganharVotos(1); break;
			case BONUS_ESPADAS:  ganhar(Mapa.ESPADA, 2); break;
			case BONUS_MOEDAS:   ganhar(Mapa.MOEDA, 2); break;
			case BONUS_YURTS:    ganhar(Mapa.YURT, 2); break;
			case BONUS_CAVALOS:  movimentos += 3; turno.setItem(Mapa.MAOZINHA, turno.getItem(Mapa.MAOZINHA) + 1); break;
			case BONUS_MAOS:     turno.setItem(Mapa.MAOZINHA, turno.getItem(Mapa.MAOZINHA) + 2); break;
			case BONUS_TESOUROS:
				for (int i = 0; i < 3 && !sacoDeTesouros.isEmpty(); i++) tesourosParaEscolher.add(sacarTesouro());
				break;
			default: break;
		}
	}

	// Dos 3 tesouros sacados pelo bonus, o jogador fica com 1 e descarta os outros.
	boolean escolherTesouro (int tipo) {
		if (!tesourosParaEscolher.contains(tipo)) return false;
		tesourosParaEscolher.clear();
		ganhar(tipo, 1);
		depoisDaAcao(CARTAS);
		return true;
	}

	// ---------- fim de jogo ----------

	// Em cada regiao, o 1o e o 2o em yurts ganham votos pelas melhorias da coluna
	// da regiao. Empate no 1o: todos sao 1o e ninguem e 2o. Sem yurt, nada.
	int votosDeInfluencia (int indice) {
		int total = 0;
		for (int regiao = Mapa.CHINA; regiao <= Mapa.PERSIA; regiao++) {
			int meus = jogadores.get(indice).getYurtsPorRegiao(regiao);
			if (meus == 0) continue;
			int maior = 0, quantosNoMaior = 0, segundo = 0;
			for (Jogador outro : jogadores) maior = Math.max(maior, outro.getYurtsPorRegiao(regiao));
			for (Jogador outro : jogadores) {
				int yurts = outro.getYurtsPorRegiao(regiao);
				if (yurts == maior) quantosNoMaior++;
				else segundo = Math.max(segundo, yurts);
			}
			int melhorias = Math.min(Melhorias.LINHAS, jogadores.get(indice).getMelhoriasNaColuna(COLUNA_DA_REGIAO[regiao]));
			if (meus == maior) total += VOTOS_DO_PRIMEIRO[melhorias];
			else if (quantosNoMaior == 1 && meus == segundo) total += VOTOS_DO_SEGUNDO[melhorias];
		}
		return total;
	}

	int votosFinais (int indice) {
		return jogadores.get(indice).r.getItem(Mapa.VOTO) + votosDeInfluencia(indice);
	}

	// Mais votos; depois mais tesouros nao entregues; depois mais perto de Karakorum.
	ArrayList<Integer> vencedores () {
		ArrayList<Integer> vencedores = new ArrayList<Integer>();
		for (int i = 0; i < jogadores.size(); i++) {
			int comparacao = vencedores.isEmpty() ? 1 : comparar(i, vencedores.get(0));
			if (comparacao > 0) vencedores.clear();
			if (comparacao >= 0) vencedores.add(i);
		}
		return vencedores;
	}

	private int comparar (int a, int b) {
		if (votosFinais(a) != votosFinais(b)) return votosFinais(a) - votosFinais(b);
		Jogador ja = jogadores.get(a), jb = jogadores.get(b);
		if (ja.getTesouros() != jb.getTesouros()) return ja.getTesouros() - jb.getTesouros();
		int[] distancia = distanciasDeKarakorum();
		return distancia[jb.getParada()] - distancia[ja.getParada()];
	}

	private int[] distanciasDeKarakorum () {
		int[] distancia = new int[Mapa.NUM_PARADAS];
		Arrays.fill(distancia, -1);
		distancia[Mapa.KARAKORUM] = 0;
		ArrayDeque<Integer> fila = new ArrayDeque<Integer>();
		fila.addLast(Mapa.KARAKORUM);
		while (!fila.isEmpty()) {
			int atual = fila.pollFirst();
			for (int vizinho : mapa.adj(atual))
				if (distancia[vizinho] < 0) {
					distancia[vizinho] = distancia[atual] + 1;
					fila.addLast(vizinho);
				}
		}
		return distancia;
	}
}
