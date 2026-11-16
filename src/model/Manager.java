package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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

	private final Mapa mapa = new Mapa();
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
	private boolean jogadaComecou = false;       // so da para salvar antes da 1a acao do turno
	private boolean habilidadeUsada = false;     // Chagatai e Ogedei: 1x por turno

	// escolhas antes da 1a rodada
	private final ArrayList<Integer> secretos = new ArrayList<Integer>();   // 2 cartas por jogador
	private boolean jochiEscolhendo = false;     // Jochi ainda vai escolher a melhoria inicial

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
		criarJogadores(herdeiros);
		secretos.addAll(embaralhados(NUM_CONSELHEIROS_SECRETOS));
		for (Jogador jogador : jogadores) {
			jogador.r.setItem(Mapa.MOEDA, jogador.getIndice() < 2 ? 1 : 2);
			if (jogador.getIndiceDoHerdeiro() == Jogador.JOCHI) jochiEscolhendo = true;
			// Tolui comeca com 1 yurt em Karakorum, que nao pertence a nenhuma regiao
			if (jogador.getIndiceDoHerdeiro() == Jogador.TOLUI) {
				mapa.yurtsEm(Mapa.KARAKORUM).add(jogador.getIndice());
				jogador.yurtsRestantes--;
			}
		}
		prepararMesa();
		comecarTurno();
	}

	// Usado por carregar(), que preenche o estado a partir do arquivo.
	private Manager () {}

	private void criarJogadores (int[] herdeiros) {
		if (herdeiros.length < 2 || herdeiros.length > MAX_JOGADORES)
			throw new IllegalArgumentException("a partida tem de 2 a " + MAX_JOGADORES + " jogadores");
		for (int i = 0; i < herdeiros.length; i++) jogadores.add(new Jogador(i, herdeiros[i]));
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

	// Qualquer acao do jogador da vez notifica, e depois dela nao da mais para salvar.
	private void notificar (int evento) {
		this.evento = evento;
		if (evento != NOVO_TURNO && evento != PEAO_TROCADO) jogadaComecou = true;
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

	boolean podeSalvar () { return !terminada && !jogadaComecou; }

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
		jogadaComecou = false;
		habilidadeUsada = false;
	}

	// ---------- escolhas antes da 1a rodada ----------

	// Quem ainda tem de escolher: o conselheiro secreto de cada um e depois a
	// melhoria inicial de Jochi. -1 quando todos ja escolheram.
	int getJogadorEscolhendo () {
		for (Jogador jogador : jogadores)
			if (jogador.getConselheiroSecreto() < 0) return jogador.getIndice();
		if (jochiEscolhendo)
			for (Jogador jogador : jogadores)
				if (jogador.getIndiceDoHerdeiro() == Jogador.JOCHI) return jogador.getIndice();
		return -1;
	}

	// Cada jogador recebe 2 cartas de conselheiro secreto (opcao 0 ou 1).
	int getOpcaoDeSecreto (int jogador, int opcao) { return secretos.get(2 * jogador + opcao); }

	// Jochi escolhe entre as 2 melhorias do topo da pilha (opcao 0 ou 1).
	Melhorias getOpcaoDeMelhoria (int opcao) {
		for (Melhorias m : melhorias) {
			if (m.getPosicao() != Melhorias.EMBARALHADA) continue;
			if (opcao == 0) return m;
			opcao--;
		}
		return null;
	}

	boolean escolherNoInicio (int opcao) {
		int indice = getJogadorEscolhendo();
		if (indice < 0 || opcao < 0 || opcao > 1) return false;
		Jogador jogador = jogadores.get(indice);
		if (jogador.getConselheiroSecreto() < 0) {
			jogador.setConselheiroSecreto(getOpcaoDeSecreto(indice, opcao));
			return true;
		}
		Melhorias escolhida = getOpcaoDeMelhoria(opcao), outra = getOpcaoDeMelhoria(1 - opcao);
		escolhida.aplicar(jogador, Jogador.BRANCA);
		melhorias.remove(outra);
		melhorias.add(outra);   // volta para o fundo da pilha
		jochiEscolhendo = false;
		return true;
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
	// O peao parado de Altani tambem ocupa lugar.
	private boolean podeParar (Jogador jogador, int parada) {
		if (parada == jogador.getParada()) return false;
		int ocupantes = 0;
		for (Jogador outro : jogadores) {
			if (outro.getParada() == parada) ocupantes++;
			if (outro.getOutraParada() == parada) ocupantes++;
		}
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

	// Yurt na parada atual (Tolui tambem nas vizinhas), pagando uma ficha de
	// yurt (real ou virtual).
	ArrayList<Integer> paradasParaYurt () {
		ArrayList<Integer> paradas = new ArrayList<Integer>();
		if (terminada || disponivel(Mapa.YURT) < 1) return paradas;
		Jogador jogador = getJogadorDaVez();
		ArrayList<Integer> candidatas = new ArrayList<Integer>();
		candidatas.add(jogador.getParada());
		if (eh(Jogador.TOLUI)) candidatas.addAll(mapa.adj(jogador.getParada()));
		for (int parada : candidatas)
			if (jogador.podeColocarYurt(mapa, parada)) paradas.add(parada);
		return paradas;
	}

	boolean construirYurt (int parada) {
		if (!paradasParaYurt().contains(parada)) return false;
		getJogadorDaVez().colocarYurt(mapa, parada);
		gastar(Mapa.YURT, 1);
		depoisDaAcao(TABULEIRO);
		return true;
	}

	// Jochi escolhe a coluna colorida ao comprar; a gratis vai sempre para a branca.
	boolean escolheColunaDaMelhoria () {
		return eh(Jogador.JOCHI) && getJogadorDaVez().melhoriasGratis == 0;
	}

	boolean podeComprarDoTopo () {
		return !terminada && melhoriaNaPosicao(Melhorias.EMBARALHADA) != null
				&& (getJogadorDaVez().melhoriasGratis > 0 || disponivel(Mapa.MOEDA) >= PRECO_DA_MELHORIA);
	}

	// O jogador compra a melhoria do espaco (1 a 5) ou a do topo da pilha
	// (Melhorias.EMBARALHADA). Se tiver uma gratis, ela vai para a coluna branca;
	// senao vai para a coluna da sua cor, ou para a que Jochi escolheu
	// (colunaDeJochi, ignorada para os outros). As restantes andam para a direita.
	boolean comprarMelhoria (int posicao, int colunaDeJochi) {
		if (terminada) return false;
		Melhorias m = melhoriaNaPosicao(posicao);
		if (m == null) return false;
		Jogador jogador = getJogadorDaVez();
		boolean gratis = jogador.melhoriasGratis > 0;
		int coluna = m.getCor();
		if (gratis) coluna = Jogador.BRANCA;
		else if (escolheColunaDaMelhoria()) coluna = colunaDeJochi;
		boolean colunaValida = gratis || (coluna >= Jogador.VERMELHA && coluna <= Jogador.AMARELA);
		if (!colunaValida || jogador.getMelhoriasNaColuna(coluna) >= Melhorias.LINHAS) return false;
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

	// ---------- habilidades dos herdeiros ----------

	private boolean eh (int herdeiro) {
		return getJogadorDaVez().getIndiceDoHerdeiro() == herdeiro;
	}

	// Altani troca de peao antes de comecar a jogar. Chagatai deixa de usar
	// 1 cavalo para ganhar 1 espada, 1x por turno.
	boolean podeUsarHabilidade () {
		if (terminada) return false;
		if (eh(Jogador.ALTANI)) return !jogadaComecou;
		if (eh(Jogador.CHAGATAI)) return !habilidadeUsada && movimentos >= 1;
		return false;
	}

	boolean usarHabilidade () {
		if (!podeUsarHabilidade()) return false;
		if (eh(Jogador.ALTANI)) {
			getJogadorDaVez().trocarPeao();
			paradasDoTurno.clear();
			paradasDoTurno.add(getJogadorDaVez().getParada());
			notificar(PEAO_TROCADO);
			return true;
		}
		movimentos--;
		ganhar(Mapa.ESPADA, 1);
		habilidadeUsada = true;
		depoisDaAcao(TABULEIRO);
		return true;
	}

	// Ogedei: 1x por turno, 1 moeda (real ou virtual) por 1 tesouro de qualquer
	// cidade revelada, desde que nao seja o ultimo dela. Nao muda o preco em
	// espadas dos outros tesouros da cidade.
	boolean podeTomarComMoeda (int cidade) {
		if (terminada || !eh(Jogador.OGEDEI) || habilidadeUsada) return false;
		if (cidade < 0 || cidade >= Mapa.NUM_CIDADES || !cidadeRevelada(cidade)) return false;
		return getCidade(cidade).totalDeTesouros() >= 2 && disponivel(Mapa.MOEDA) >= 1;
	}

	boolean tomarComMoeda (int cidade, int tesouro) {
		if (!podeTomarComMoeda(cidade) || !getCidade(cidade).retirarTesouro(tesouro)) return false;
		gastar(Mapa.MOEDA, 1);
		ganhar(tesouro, 1);
		habilidadeUsada = true;
		depoisDaAcao(TABULEIRO);
		return true;
	}

	// Entrega no conselheiro de alguma parada do turno, comecando pela atual.
	boolean podeEntregar () {
		return paradaParaEntrega() >= 0;
	}

	private int paradaParaEntrega () {
		if (terminada) return -1;
		for (int i = paradasDoTurno.size() - 1; i >= 0; i--) {
			int parada = paradasDoTurno.get(i);
			if (mapa.conselheiroEm(parada) >= 0 && podePagar(conselheiroEm(parada))) return parada;
		}
		return -1;
	}

	private Conselheiro conselheiroEm (int parada) {
		return (Conselheiro) mapa.coletaveis.get(mapa.conselheiroEm(parada));
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
		Conselheiro conselheiro = conselheiroEm(parada);
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
		return jogadores.get(indice).r.getItem(Mapa.VOTO) + votosDeInfluencia(indice) + votosDoSecreto(indice);
	}

	// Quanto falta para 1, 2 e 3 votos em cada carta, na ordem das imagens secreto0..9.
	private static final int[][] METAS_DO_SECRETO = {
		{ 1, 2, 4 },    // 0 Sacerdote: pares de melhorias com o mesmo simbolo
		{ 4, 6, 7 },    // 1 Colecionador: tipos diferentes de melhoria
		{ 3, 6, 8 },    // 2 Viajante: yurts conectados numa rede
		{ 4, 7, 10 },   // 3 Soldado: pecas de melhoria
		{ 3, 6, 8 },    // 4 Cidadao: cidades com 1+ yurt adjacente
		{ 2, 4, 5 },    // 5 Cavaleiro: melhorias com cavalo ou punho
		{ 2, 4, 6 },    // 6 Mensageiro: paradas duplas com 1+ yurt
		{ 1, 2, 3 },    // 7 Governador: regioes com 2+ melhorias e 2+ yurts
		{ 2, 3, 4 },    // 8 Prefeito: cidades com 2+ yurts adjacentes
		{ 2, 3, 4 }     // 9 Bajulador: provincias do Khan com 2+ yurts adjacentes
	};

	int votosDoSecreto (int indice) {
		int carta = jogadores.get(indice).getConselheiroSecreto();
		if (carta < 0) return 0;
		int progresso = progressoDoSecreto(jogadores.get(indice), carta), votos = 0;
		for (int meta : METAS_DO_SECRETO[carta])
			if (progresso >= meta) votos++;
		return votos;
	}

	// Melhorias contam nas 4 colunas; yurts de cidade so contam para o Governador
	// (pelos yurts da regiao, como na influencia).
	private int progressoDoSecreto (Jogador jogador, int carta) {
		int[] tipos = new int[12];   // melhorias do jogador por simbolo
		for (ArrayList<Melhorias> coluna : jogador.melhoriasNaCartela)
			for (Melhorias m : coluna)
				if (m.getTipo() >= 0) tipos[m.getTipo()]++;
		int indice = jogador.getIndice(), total = 0;
		switch (carta) {
			case 0: for (int n : tipos) total += n / 2; return total;
			case 1: for (int n : tipos) if (n > 0) total++; return total;
			case 2: return maiorRedeDeYurts(indice);
			case 3: for (int n : tipos) total += n; return total;
			case 4: return comYurtsAdjacentes(indice, cidades(), 1);
			case 5: return tipos[Mapa.CAVALO] + tipos[Mapa.MAOZINHA];
			case 6:
				for (int parada : mapa.paradasDeConselheiro())
					if (yurtsParaOSecreto(parada, indice) > 0) total++;
				return total;
			case 7:
				for (int regiao = Mapa.CHINA; regiao <= Mapa.PERSIA; regiao++)
					if (jogador.getYurtsPorRegiao(regiao) >= 2 && jogador.getMelhoriasNaColuna(COLUNA_DA_REGIAO[regiao]) >= 2) total++;
				return total;
			case 8: return comYurtsAdjacentes(indice, cidades(), 2);
			case 9: return comYurtsAdjacentes(indice, Mapa.provinciasDoKhan(), 2);
			default: return 0;
		}
	}

	// Yurts do jogador numa parada. O de Tolui em Karakorum so ajuda no movimento
	// e nas cartas abertas (FAQ do manual), entao nao conta para o secreto.
	private int yurtsParaOSecreto (int parada, int jogador) {
		return parada == Mapa.KARAKORUM ? 0 : Collections.frequency(mapa.yurtsEm(parada), jogador);
	}

	private static ArrayList<Integer> cidades () {
		ArrayList<Integer> cidades = new ArrayList<Integer>();
		for (int c = 0; c < Mapa.NUM_CIDADES; c++) cidades.add(Mapa.PRIMEIRA_CIDADE + c);
		return cidades;
	}

	// Quantos coletaveis (cidades ou provincias) tem pelo menos `minimo` yurts
	// do jogador nas paradas em volta.
	private int comYurtsAdjacentes (int jogador, ArrayList<Integer> coletaveis, int minimo) {
		int quantos = 0;
		for (int coletavel : coletaveis) {
			int yurts = 0;
			for (int parada = 0; parada < Mapa.NUM_PARADAS; parada++)
				if (mapa.alcanca(parada, coletavel)) yurts += yurtsParaOSecreto(parada, jogador);
			if (yurts >= minimo) quantos++;
		}
		return quantos;
	}

	// Maior grupo de yurts do jogador em paradas ligadas por rotas.
	private int maiorRedeDeYurts (int jogador) {
		boolean[] visitada = new boolean[Mapa.NUM_PARADAS];
		int maior = 0;
		for (int inicio = 0; inicio < Mapa.NUM_PARADAS; inicio++) {
			if (visitada[inicio] || yurtsParaOSecreto(inicio, jogador) == 0) continue;
			int rede = 0;
			ArrayDeque<Integer> fila = new ArrayDeque<Integer>();
			fila.add(inicio);
			visitada[inicio] = true;
			while (!fila.isEmpty()) {
				int atual = fila.poll();
				rede += yurtsParaOSecreto(atual, jogador);
				for (int vizinha : mapa.adj(atual))
					if (!visitada[vizinha] && yurtsParaOSecreto(vizinha, jogador) > 0) {
						visitada[vizinha] = true;
						fila.add(vizinha);
					}
			}
			maior = Math.max(maior, rede);
		}
		return maior;
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
		return maisPerto(distancia, jb) - maisPerto(distancia, ja);
	}

	// Altani vale pelo peao mais perto de Karakorum.
	private static int maisPerto (int[] distancia, Jogador jogador) {
		int outra = jogador.getOutraParada();
		int ativa = distancia[jogador.getParada()];
		return outra == Jogador.SEM_PEAO ? ativa : Math.min(ativa, distancia[outra]);
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

	// ---------- salvamento e recuperacao ----------

	// Grava o estado do inicio do turno, uma linha rotulada por item. O estado do
	// turno nao entra: ao carregar, o jogador da vez comeca o turno do zero.
	void salvar (PrintWriter out) {
		ArrayList<Integer> herdeiros = new ArrayList<Integer>();
		for (Jogador jogador : jogadores) herdeiros.add(jogador.getIndiceDoHerdeiro());
		escrever(out, "herdeiros", herdeiros);
		escrever(out, "vez", vez);
		escrever(out, "turnosRestantes", turnosRestantes);
		escrever(out, "khan", mapa.getPosKhan());
		ArrayList<Integer> potes = new ArrayList<Integer>();
		for (int pote = 0; pote < Mapa.NUM_POTES; pote++) potes.add(getPote(pote).getQuantidade());
		escrever(out, "potes", potes);
		for (int c = 0; c < Mapa.NUM_CIDADES; c++) {
			Cidade cidade = getCidade(c);
			escrever(out, "cidade", cidade.getDono(), cidade.getTesouro(Mapa.GRAOS), cidade.getTesouro(Mapa.TECIDO),
					cidade.getTesouro(Mapa.FERRO), cidade.getTesouro(Mapa.PELE), cidade.getTesouro(Mapa.VACA));
		}
		escrever(out, "pilhaDeCidades", pilhaDeCidades);
		escrever(out, "cidadesReveladas", cidadesReveladas);
		escrever(out, "saco", sacoDeTesouros);
		escrever(out, "cartasAbertas", getCartasAbertas());
		ArrayList<Integer> bonus = new ArrayList<Integer>();
		for (int parada : mapa.paradasDeConselheiro()) bonus.add(getBonusNaParada(parada));
		escrever(out, "bonus", bonus);
		ArrayList<Integer> yurts = new ArrayList<Integer>();   // pares parada, jogador
		for (int parada = 0; parada < Mapa.NUM_PARADAS; parada++)
			for (int dono : mapa.yurtsEm(parada)) {
				yurts.add(parada);
				yurts.add(dono);
			}
		escrever(out, "yurts", yurts);
		ArrayList<Melhorias> area = new ArrayList<Melhorias>();
		for (int espaco = 1; espaco <= Melhorias.TAMANHO_TRILHA; espaco++) area.add(melhoriaNaPosicao(espaco));
		escrever(out, "area", tiposECores(area));
		ArrayList<Melhorias> pilha = new ArrayList<Melhorias>();
		for (Melhorias m : melhorias)
			if (m.getPosicao() == Melhorias.EMBARALHADA) pilha.add(m);
		escrever(out, "pilha", tiposECores(pilha));
		for (Jogador j : jogadores) {
			out.println("nome " + j.nome);
			escrever(out, "jogador", j.parada, j.outraParada, j.yurtsRestantes, j.cidadesConquistadas,
					j.melhoriasGratis, j.conselheiroSecreto);
			ArrayList<Integer> recursos = new ArrayList<Integer>();
			for (int tipo = 0; tipo < 12; tipo++) recursos.add(j.r.getItem(tipo));
			escrever(out, "recursos", recursos);
			ArrayList<Integer> usadas = new ArrayList<Integer>();
			for (boolean usada : j.colunasUsadas) usadas.add(usada ? 1 : 0);
			escrever(out, "colunasUsadas", usadas);
			escrever(out, "yurtsPorRegiao", j.yurtsPorRegiao);
			escrever(out, "bonusRecebidos", j.bonusRecebidos);
			for (ArrayList<Melhorias> coluna : j.melhoriasNaCartela) escrever(out, "coluna", tiposECores(coluna));
		}
	}

	// Le o que salvar() gravou, na mesma ordem.
	static Manager carregar (BufferedReader in) throws IOException {
		Manager m = new Manager();
		m.criarJogadores(ler(in, "herdeiros"));
		m.vez = ler(in, "vez")[0];
		m.turnosRestantes = ler(in, "turnosRestantes")[0];
		m.mapa.posKhan = ler(in, "khan")[0];
		int[] potes = ler(in, "potes");
		for (int pote = 0; pote < Mapa.NUM_POTES; pote++)
			for (int i = 0; i < potes[pote]; i++) m.getPote(pote).adicionar();
		for (int c = 0; c < Mapa.NUM_CIDADES; c++) {
			int[] cidade = ler(in, "cidade");
			m.getCidade(c).dominar(cidade[0]);   // dono -1: ninguem
			for (int tipo = Mapa.GRAOS; tipo <= Mapa.VACA; tipo++)
				for (int i = 0; i < cidade[1 + tipo - Mapa.GRAOS]; i++) m.getCidade(c).colocarTesouro(tipo);
		}
		adicionar(m.pilhaDeCidades, ler(in, "pilhaDeCidades"));
		adicionar(m.cidadesReveladas, ler(in, "cidadesReveladas"));
		adicionar(m.sacoDeTesouros, ler(in, "saco"));
		HashMap<Integer, Quest> cartas = Quest.criarCartas();
		for (int id : ler(in, "cartasAbertas")) m.cartasAbertas.put(id, cartas.get(id));
		int[] bonus = ler(in, "bonus");
		ArrayList<Integer> conselheiros = m.mapa.paradasDeConselheiro();
		for (int i = 0; i < conselheiros.size(); i++) {
			if (bonus[i] >= 0) m.bonusNoMapa.put(conselheiros.get(i), bonus[i]);
			else m.conselheiroEm(conselheiros.get(i)).registrarEntrega();   // sem a peca: ja houve entrega
		}
		int[] yurts = ler(in, "yurts");
		for (int i = 0; i < yurts.length; i += 2) m.mapa.yurtsEm(yurts[i]).add(yurts[i + 1]);
		m.melhorias.clear();
		int[] area = ler(in, "area");
		for (int espaco = 1; espaco <= Melhorias.TAMANHO_TRILHA; espaco++) {
			int tipo = area[2 * espaco - 2], cor = area[2 * espaco - 1];
			if (tipo < 0) continue;
			Melhorias melhoria = new Melhorias(tipo, cor);
			melhoria.setPosicao(espaco);
			m.melhorias.add(melhoria);
		}
		int[] pilha = ler(in, "pilha");
		for (int i = 0; i < pilha.length; i += 2) m.melhorias.add(new Melhorias(pilha[i], pilha[i + 1]));
		for (Jogador j : m.jogadores) {
			j.setNome(lerTexto(in, "nome"));
			int[] dados = ler(in, "jogador");
			j.parada = dados[0];
			j.outraParada = dados[1];
			j.yurtsRestantes = dados[2];
			j.cidadesConquistadas = dados[3];
			j.melhoriasGratis = dados[4];
			j.conselheiroSecreto = dados[5];
			int[] recursos = ler(in, "recursos");
			for (int tipo = 0; tipo < 12; tipo++) j.r.setItem(tipo, recursos[tipo]);
			int[] usadas = ler(in, "colunasUsadas");
			for (int c = 0; c < Melhorias.NUM_COLUNAS; c++) j.colunasUsadas.set(c, usadas[c] == 1);
			int[] regioes = ler(in, "yurtsPorRegiao");
			for (int regiao = Mapa.CHINA; regiao <= Mapa.PERSIA; regiao++) j.yurtsPorRegiao.set(regiao, regioes[regiao]);
			for (int peca : ler(in, "bonusRecebidos")) j.receberBonus(peca);
			for (int c = 0; c < Melhorias.NUM_COLUNAS; c++) {
				int[] pecas = ler(in, "coluna");
				for (int i = 0; i < pecas.length; i += 2) {
					Melhorias melhoria = new Melhorias(pecas[i], pecas[i + 1]);
					melhoria.aplicar(j, c);
					m.melhorias.add(melhoria);
				}
			}
		}
		m.comecarTurno();
		return m;
	}

	// Pares tipo, cor de cada melhoria; -1 -1 para espaco vazio.
	private static ArrayList<Integer> tiposECores (ArrayList<Melhorias> lista) {
		ArrayList<Integer> valores = new ArrayList<Integer>();
		for (Melhorias m : lista) {
			valores.add(m == null ? -1 : m.getTipo());
			valores.add(m == null ? -1 : m.getCor());
		}
		return valores;
	}

	private static void escrever (PrintWriter out, String rotulo, ArrayList<Integer> valores) {
		StringBuilder linha = new StringBuilder(rotulo);
		for (int valor : valores) linha.append(' ').append(valor);
		out.println(linha);
	}

	private static void escrever (PrintWriter out, String rotulo, int... valores) {
		ArrayList<Integer> lista = new ArrayList<Integer>();
		for (int valor : valores) lista.add(valor);
		escrever(out, rotulo, lista);
	}

	// Texto da proxima linha depois do rotulo; erro se o rotulo nao for o esperado.
	private static String lerTexto (BufferedReader in, String rotulo) throws IOException {
		String linha = in.readLine();
		if (linha == null || !(linha + " ").startsWith(rotulo + " "))
			throw new IOException("esperava a linha '" + rotulo + "'");
		return linha.substring(rotulo.length()).trim();
	}

	private static int[] ler (BufferedReader in, String rotulo) throws IOException {
		String texto = lerTexto(in, rotulo);
		if (texto.isEmpty()) return new int[0];
		String[] partes = texto.split(" ");
		int[] numeros = new int[partes.length];
		for (int i = 0; i < partes.length; i++) numeros[i] = Integer.parseInt(partes[i]);
		return numeros;
	}

	private static void adicionar (ArrayList<Integer> lista, int[] valores) {
		for (int valor : valores) lista.add(valor);
	}
}
