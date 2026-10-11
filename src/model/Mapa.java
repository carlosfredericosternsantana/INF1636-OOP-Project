package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
* O Mapa valida as regras que dependem de ONDE as coisas estão: movimento, construção
* de yurts, adjacência para tributo, ataque e entrega, e a ação do Khan. O que depende
* só do jogador (quantos movimentos e punhos ele tem neste turno) fica com o
* Jogador/Manager: o Mapa diz quanto um caminho custa e o Manager confere se cabe.
*
* O layout é montado pelos métodos "adicionar..." e "conectar", para que possa ser
* carregado de um arquivo texto (diagrama: IO TXT) e para que os testes usem mapas pequenos.
*
* Recursos seguem o padrão do {@link Recurso} (vetor de 12 posições, nomeadas em {@link Peca}).
* No Jogador, pega-se a cópia com getRecurso(), altera-se e devolve-se com setRecurso().
*/

class Mapa {

	/** A ordem segue o índice de {@code Jogador.yurtsPorRegiao}: 0 China, 1 Rússia, 2 Pérsia. */
	enum Regiao { CHINA, RUSSIA, PERSIA, NENHUMA }

	/**
	 * Nomes para as 12 posições do vetor do {@link Recurso}, na mesma ordem dele:
	 * 0 voto, 1 espada, 2 maozinha, 3 cavalo, 4 cabeção do Khan, 5 ficha de yurt, 6 moeda,
	 * 7 grãos, 8 tecido, 9 ferro, 10 pele, 11 vaca.
	 *
	 * Convenção do Recurso de um Coletavel: valor negativo é o que o jogador PAGA (a cidade
	 * tem espada = -1, o pote tem maozinha = -1, o conselheiro tem os tesouros pedidos
	 * negativos) e valor positivo é o que ele RECEBE (tesouros da cidade, tributo do pote,
	 * votos do conselheiro).
	 */
	enum Peca {
		VOTO, ESPADA, MAOZINHA, CAVALO, CABECAO_DO_KHAN, FICHA_DE_YURT, MOEDA,
		GRAOS, TECIDO, FERRO, PELE, VACA;

		/** A posição desta peça no vetor do Recurso. */
		int indice() { return ordinal(); }

		boolean ehTesouro() { return ordinal() >= GRAOS.ordinal(); }

		boolean ehTributo() { return this == ESPADA || this == FICHA_DE_YURT || this == MOEDA; }

		int em(Recurso r) { return r.getItem(indice()); }

		void somar(Recurso r, int quantidade) { r.setItem(indice(), em(r) + quantidade); }

		/** Soma dos tesouros positivos (o que está na mão do jogador ou no estoque da cidade). */
		static int tesouros(Recurso r) {
			int total = 0;
			for (Peca peca : values()) if (peca.ehTesouro() && peca.em(r) > 0) total += peca.em(r);
			return total;
		}
	}

	static final int YURTS_POR_JOGADOR = 12;
	static final int CIDADES_REVELADAS = 3;
	static final int TESOUROS_POR_CIDADE = 4;
	static final int TESOUROS_DE_CADA_TIPO = 8;
	static final int MAX_TRIBUTOS_POR_POTE = 3;

	private final Random sorteio;

	// layout do tabuleiro
	private final Map<String, Parada> paradas = new HashMap<>();
	private Parada karakorum;
	private final Map<String, PoteDeRecursos> potes = new HashMap<>();
	private final Map<PoteDeRecursos, Peca> tributoDoPote = new HashMap<>();     // ícone impresso na província
	private final Map<PoteDeRecursos, Set<Parada>> paradasDoPote = new HashMap<>();
	private final Set<PoteDeRecursos> potesDoKhan = new HashSet<>();
	private final Map<PoteDeRecursos, List<PoteDeRecursos>> vizinhosDoKhan = new HashMap<>(); // setas que saem do Khan
	private final Map<String, Cidade> cidades = new HashMap<>();
	private final Map<Cidade, Set<Parada>> paradasDaCidade = new HashMap<>();
	private final Map<Cidade, Regiao> regiaoDaCidade = new HashMap<>();
	private final Map<Conselheiro, Integer> tesourosQuaisquer = new HashMap<>(); // só o conselheiro de tipo livre

	// estado da partida
	private final Deque<String> pilhaDeCidades = new ArrayDeque<>();
	private final Set<Cidade> cidadesReveladas = new HashSet<>();
	private final Map<Cidade, Integer> tesourosTomadosNoTurno = new HashMap<>();
	private final Map<Cidade, Jogador> yurtNoCentroDaCidade = new HashMap<>();
	private final Deque<Peca> pilhaDeTesouros = new ArrayDeque<>();
	private final Set<Conselheiro> conselheirosComBonus = new HashSet<>();
	private final Map<Jogador, Parada> posicoes = new HashMap<>();
	private final Map<Jogador, Set<Parada>> paradasNoTurno = new HashMap<>();
	private PoteDeRecursos khanEmPote;   // null se o Khan está fora do mapa ou na área de melhorias
	private boolean khanNasMelhorias;

	Mapa(Random sorteio) {
		this.sorteio = Objects.requireNonNull(sorteio, "gerador obrigatório");
	}

	Mapa() {
		this(new Random());
	}

	// ======================================================================= montagem

	Parada adicionarParada(String nome, Parada.Tipo tipo, Regiao regiao) {
		if (paradas.containsKey(nome)) throw new IllegalArgumentException("parada repetida: " + nome);
		if (tipo == Parada.Tipo.KARAKORUM && karakorum != null)
			throw new IllegalStateException("só existe uma Karakorum");
		Parada parada = new Parada(nome, tipo, regiao);
		paradas.put(nome, parada);
		if (tipo == Parada.Tipo.KARAKORUM) karakorum = parada;
		return parada;
	}

	void conectar(String a, String b) {
		getParada(a).conectar(getParada(b));
	}

	/**
	 * Conselheiro que pede tesouros específicos (página 13). No Recurso dele, os tesouros
	 * pedidos ficam negativos (o que se paga) e o voto, positivo (o que se recebe).
	 */
	Conselheiro colocarConselheiro(String parada, Map<Peca, Integer> demanda, int votos) {
		Recurso troca = new Recurso();
		for (Map.Entry<Peca, Integer> item : demanda.entrySet()) {
			if (!item.getKey().ehTesouro() || item.getValue() <= 0)
				throw new IllegalArgumentException("conselheiro só pede quantidades positivas de tesouro");
			item.getKey().somar(troca, -item.getValue());
		}
		Peca.VOTO.somar(troca, votos);
		return colocar(parada, troca);
	}

	/** O conselheiro que aceita N tesouros de qualquer tipo (página 13). */
	Conselheiro colocarConselheiroDeTipoLivre(String parada, int quantidade, int votos) {
		Recurso troca = new Recurso();
		Peca.VOTO.somar(troca, votos);
		Conselheiro conselheiro = colocar(parada, troca);
		tesourosQuaisquer.put(conselheiro, quantidade);
		return conselheiro;
	}

	/** Província (pote de recursos) com o tributo impresso nela e as paradas de onde se pega. */
	PoteDeRecursos adicionarPote(String nome, Peca tributo, boolean doKhan, String... paradasAdjacentes) {
		if (!tributo.ehTributo()) throw new IllegalArgumentException("pote só rende espada, ficha de yurt ou moeda");
		if (potes.containsKey(nome)) throw new IllegalArgumentException("pote repetido: " + nome);
		PoteDeRecursos pote = new PoteDeRecursos(); // já nasce com maozinha = -1: o custo de cada tributo
		potes.put(nome, pote);
		tributoDoPote.put(pote, tributo);
		paradasDoPote.put(pote, paradasPorNome(paradasAdjacentes));
		if (doKhan) potesDoKhan.add(pote);
		return pote;
	}

	/** Os 2 potes que recebem tributo junto com o pote do Khan. */
	void definirVizinhosDoKhan(String poteDoKhan, String vizinho1, String vizinho2) {
		PoteDeRecursos khan = getPote(poteDoKhan);
		if (!potesDoKhan.contains(khan)) throw new IllegalArgumentException(poteDoKhan + " não é província do Khan");
		vizinhosDoKhan.put(khan, List.of(getPote(vizinho1), getPote(vizinho2)));
	}

	/** Cidade impressa no mapa: ainda não revelada; entra na partida quando sair da pilha. */
	Cidade adicionarCidade(String nome, Regiao regiao, String... paradasAdjacentes) {
		if (cidades.containsKey(nome)) throw new IllegalArgumentException("cidade repetida: " + nome);
		Cidade cidade = new Cidade(); // já nasce com espada = -1: o custo do 1º tesouro do turno
		cidades.put(nome, cidade);
		regiaoDaCidade.put(cidade, regiao);
		paradasDaCidade.put(cidade, paradasPorNome(paradasAdjacentes));
		return cidade;
	}

	// ======================================================================= preparação

	/** Embaralha as peças de cidade e de tesouro e revela as 3 primeiras cidades (página 4). */
	void preparar() {
		List<String> nomes = new ArrayList<>(cidades.keySet());
		Collections.shuffle(nomes, sorteio);
		List<Peca> tesouros = new ArrayList<>();
		for (Peca peca : Peca.values())
			if (peca.ehTesouro())
				for (int i = 0; i < TESOUROS_DE_CADA_TIPO; i++) tesouros.add(peca);
		Collections.shuffle(tesouros, sorteio);
		prepararCom(nomes, tesouros);
	}

	/** Preparação com ordem conhecida das pilhas, para testes e para carregar um jogo salvo. */
	void prepararCom(List<String> ordemDasCidades, List<Peca> ordemDosTesouros) {
		pilhaDeCidades.clear();
		pilhaDeTesouros.clear();
		for (String nome : ordemDasCidades) {
			getCidade(nome); // valida o nome
			pilhaDeCidades.addLast(nome);
		}
		pilhaDeTesouros.addAll(ordemDosTesouros);
		for (int i = 0; i < CIDADES_REVELADAS; i++) revelarProximaCidade();
	}

	/** Coloca o peão em Karakorum (página 4). */
	void colocarPeao(Jogador jogador) {
		if (karakorum == null) throw new IllegalStateException("o mapa não tem Karakorum");
		posicoes.put(jogador, karakorum);
		paradasNoTurno.put(jogador, new HashSet<>());
	}

	// ======================================================================= turno

	/** A parada onde o jogador começa o turno já conta como parada feita (página 8). */
	void iniciarTurno(Jogador jogador) {
		Set<Parada> feitas = new HashSet<>();
		feitas.add(getPosicao(jogador));
		paradasNoTurno.put(jogador, feitas);
	}

	/** Fim da jogada: tesouros das cidades voltam a custar 1 espada. */
	void encerrarTurno(Jogador jogador) {
		tesourosTomadosNoTurno.clear();
		paradasNoTurno.put(jogador, new HashSet<>());
	}

	// ======================================================================= movimento

	/**
	 * Quantos movimentos o caminho custa, ou -1 se ele não é válido.
	 *
	 * O caminho lista as paradas depois da posição atual, em ordem. Cada passo custa 1
	 * movimento, exceto entrar numa parada com yurt do próprio jogador para seguir
	 * adiante: isso é pular a parada e não custa nada (página 11). O caminho pode passar
	 * por paradas ocupadas, mas não pode terminar numa parada cheia (página 7).
	 */
	int custoDoCaminho(Jogador jogador, List<String> caminho) {
		if (caminho == null || caminho.isEmpty()) return -1;
		Parada atual = getPosicao(jogador);
		int custo = 0;
		for (int i = 0; i < caminho.size(); i++) {
			Parada proxima = paradas.get(caminho.get(i));
			if (proxima == null || !atual.ehAdjacenteA(proxima)) return -1;
			boolean ultima = i == caminho.size() - 1;
			if (ultima || !proxima.temYurtDe(jogador)) custo++;
			atual = proxima;
		}
		return peoesEm(atual, jogador) < atual.capacidadeDePeoes() ? custo : -1;
	}

	/**
	 * Move o peão pelo caminho e registra as paradas feitas (as puladas por yurt não
	 * contam). Devolve os movimentos gastos, ou -1 sem mover nada se o caminho é inválido.
	 */
	int mover(Jogador jogador, List<String> caminho) {
		int custo = custoDoCaminho(jogador, caminho);
		if (custo < 0) return -1;
		Set<Parada> feitas = paradasNoTurno.computeIfAbsent(jogador, j -> new HashSet<>());
		for (int i = 0; i < caminho.size(); i++) {
			Parada parada = paradas.get(caminho.get(i));
			boolean pulada = i < caminho.size() - 1 && parada.temYurtDe(jogador);
			if (!pulada) feitas.add(parada);
		}
		posicoes.put(jogador, paradas.get(caminho.get(caminho.size() - 1)));
		return custo;
	}

	// ======================================================================= ações

	/**
	 * Constrói um yurt (página 11): numa parada feita neste turno, que tenha espaço e não
	 * seja Karakorum. Gasta 1 ficha de yurt e 1 dos 12 yurts do jogador.
	 */
	boolean construirYurt(Jogador jogador, String nomeDaParada) {
		Parada parada = getParada(nomeDaParada);
		if (!fezParadaEm(jogador, parada) || !parada.cabeYurt()) return false;
		if (yurtsColocados(jogador) >= YURTS_POR_JOGADOR) return false;
		if (!pagar(jogador, Peca.FICHA_DE_YURT, 1)) return false;

		parada.receberYurt(jogador);
		registrarYurt(jogador, parada.getRegiao());
		return true;
	}

	/**
	 * Pega 1 tributo de um pote adjacente a alguma parada feita neste turno, pagando
	 * 1 maozinha (página 8).
	 */
	boolean pegarTributo(Jogador jogador, String nomeDoPote) {
		PoteDeRecursos pote = getPote(nomeDoPote);
		Peca tributo = tributoDoPote.get(pote);
		if (!adjacenteAoTurno(jogador, paradasDoPote.get(pote))) return false;
		if (quantidadeNoPote(nomeDoPote) == 0) return false;
		if (!pagar(jogador, Peca.MAOZINHA, -Peca.MAOZINHA.em(pote.getRecurso()))) return false;

		alterar(pote, tributo, -1);
		dar(jogador, tributo, 1);
		return true;
	}

	/** Quanto um tesouro desta cidade custa agora: 1 espada no 1º do turno, 2 nos seguintes (página 10). */
	int custoDeAtaque(String nomeDaCidade) {
		Cidade cidade = getCidade(nomeDaCidade);
		int custoInicial = -Peca.ESPADA.em(cidade.getRecurso()); // a cidade guarda espada = -1
		return tesourosTomadosNoTurno.getOrDefault(cidade, 0) == 0 ? custoInicial : 2 * custoInicial;
	}

	/**
	 * Toma um tesouro de uma cidade revelada adjacente a uma parada feita neste turno
	 * (página 10). Se foi o último, a cidade é conquistada: o jogador põe um yurt no
	 * centro dela e a próxima cidade da pilha é revelada.
	 */
	boolean atacarCidade(Jogador jogador, String nomeDaCidade, Peca tesouro) {
		Cidade cidade = getCidade(nomeDaCidade);
		if (!tesouro.ehTesouro() || !cidadesReveladas.contains(cidade)) return false;
		if (!adjacenteAoTurno(jogador, paradasDaCidade.get(cidade))) return false;
		if (tesouro.em(cidade.getRecurso()) == 0) return false;
		if (!pagar(jogador, Peca.ESPADA, custoDeAtaque(nomeDaCidade))) return false;

		alterar(cidade, tesouro, -1);
		dar(jogador, tesouro, 1);
		tesourosTomadosNoTurno.merge(cidade, 1, Integer::sum);

		if (Peca.tesouros(cidade.getRecurso()) == 0) {
			cidadesReveladas.remove(cidade);
			jogador.setCidadesConquistadas(jogador.getCidadesConquistadas() + 1);
			if (yurtsColocados(jogador) < YURTS_POR_JOGADOR) { // sem yurts sobrando, a conquista vale sem o marcador
				yurtNoCentroDaCidade.put(cidade, jogador);
				registrarYurt(jogador, regiaoDaCidade.get(cidade));
			}
			revelarProximaCidade();
		}
		return true;
	}

	/**
	 * Entrega tesouros ao conselheiro da parada, se o jogador parou nela neste turno
	 * (página 12). O pagamento tem de ser exatamente a demanda (no conselheiro de tipo
	 * livre, exatamente N tesouros de qualquer tipo). A primeira entrega leva a peça de
	 * bônus. Devolve os votos ganhos, ou 0 se a entrega foi recusada (nada é cobrado).
	 */
	int entregar(Jogador jogador, String nomeDaParada, Recurso pagamento) {
		Parada parada = getParada(nomeDaParada);
		Conselheiro conselheiro = parada.getConselheiro();
		if (conselheiro == null || !fezParadaEm(jogador, parada)) return 0;
		if (!pagamentoValido(conselheiro, pagamento)) return 0;

		Recurso mao = jogador.getRecurso();
		for (Peca peca : Peca.values())
			if (peca.ehTesouro() && peca.em(mao) < peca.em(pagamento)) return 0;
		for (Peca peca : Peca.values())
			if (peca.ehTesouro()) peca.somar(mao, -peca.em(pagamento));

		int votos = Peca.VOTO.em(conselheiro.getRecurso());
		Peca.VOTO.somar(mao, votos);
		jogador.setRecurso(mao);
		if (conselheirosComBonus.remove(conselheiro))
			jogador.setPecasDeBonus(jogador.getPecasDeBonus() + 1);
		return votos;
	}

	// ======================================================================= Khan (página 9)

	/**
	 * Leva o Khan a um dos potes do Khan: ele e os 2 vizinhos indicados recebem 1 tributo
	 * cada (sem passar de 3). Devolve false se o destino não é válido ou se o Khan já está
	 * lá: ele não pode ficar onde está.
	 */
	boolean moverKhanParaPote(String nomeDoPote) {
		PoteDeRecursos destino = getPote(nomeDoPote);
		if (!potesDoKhan.contains(destino) || destino == khanEmPote) return false;
		khanEmPote = destino;
		khanNasMelhorias = false;
		receberTributo(destino);
		for (PoteDeRecursos vizinho : vizinhosDoKhan.getOrDefault(destino, List.of())) receberTributo(vizinho);
		return true;
	}

	/**
	 * Leva o Khan à área de melhorias. A reposição das melhorias e a moeda/espada dos
	 * espaços vazios ficam com a área de melhorias; aqui só se valida e registra a posição.
	 */
	boolean moverKhanParaMelhorias() {
		if (khanNasMelhorias) return false;
		khanNasMelhorias = true;
		khanEmPote = null;
		return true;
	}

	/** 1 peça do tributo impresso no pote, sem passar de 3 (preparação e Khan). */
	boolean receberTributo(String nomeDoPote) {
		return receberTributo(getPote(nomeDoPote));
	}

	// ======================================================================= consultas

	int quantidadeNoPote(String nomeDoPote) {
		PoteDeRecursos pote = getPote(nomeDoPote);
		return tributoDoPote.get(pote).em(pote.getRecurso());
	}

	/** Tesouros que ainda restam na cidade. */
	int tesourosNaCidade(String nomeDaCidade) {
		return Peca.tesouros(getCidade(nomeDaCidade).getRecurso());
	}

	boolean temBonus(String nomeDaParada) {
		Conselheiro conselheiro = getParada(nomeDaParada).getConselheiro();
		return conselheiro != null && conselheirosComBonus.contains(conselheiro);
	}

	/** Yurts do jogador em cada região, para a influência no fim do jogo (página 14). */
	Map<Regiao, Integer> yurtsPorRegiao(Jogador jogador) {
		Map<Regiao, Integer> contagem = new HashMap<>();
		for (Parada parada : paradas.values())
			for (Jogador dono : parada.getYurts())
				if (dono == jogador) contagem.merge(parada.getRegiao(), 1, Integer::sum);
		for (Map.Entry<Cidade, Jogador> centro : yurtNoCentroDaCidade.entrySet())
			if (centro.getValue() == jogador) contagem.merge(regiaoDaCidade.get(centro.getKey()), 1, Integer::sum);
		contagem.remove(Regiao.NENHUMA);
		return contagem;
	}

	/** Maior grupo de yurts do jogador ligados por rotas (carta secreta Viajante, página 19). */
	int maiorRedeDeYurts(Jogador jogador) {
		Set<Parada> visitadas = new HashSet<>();
		int maior = 0;
		for (Parada inicio : paradas.values()) {
			if (!inicio.temYurtDe(jogador) || visitadas.contains(inicio)) continue;
			int tamanho = 0;
			Deque<Parada> fila = new ArrayDeque<>();
			fila.add(inicio);
			visitadas.add(inicio);
			while (!fila.isEmpty()) {
				tamanho++;
				for (Parada vizinha : fila.poll().getAdjacentes())
					if (vizinha.temYurtDe(jogador) && visitadas.add(vizinha)) fila.add(vizinha);
			}
			maior = Math.max(maior, tamanho);
		}
		return maior;
	}

	/** Todos os yurts do jogador no mapa, inclusive os do centro das cidades conquistadas. */
	int yurtsColocados(Jogador jogador) {
		int total = 0;
		for (Parada parada : paradas.values())
			for (Jogador dono : parada.getYurts())
				if (dono == jogador) total++;
		for (Jogador dono : yurtNoCentroDaCidade.values())
			if (dono == jogador) total++;
		return total;
	}

	boolean fezParadaEm(Jogador jogador, Parada parada) {
		return paradasNoTurno.getOrDefault(jogador, Set.of()).contains(parada);
	}

	Parada getPosicao(Jogador jogador) {
		Parada posicao = posicoes.get(jogador);
		if (posicao == null) throw new IllegalStateException("peão do jogador não está no mapa");
		return posicao;
	}

	Parada getParada(String nome) {
		Parada parada = paradas.get(nome);
		if (parada == null) throw new IllegalArgumentException("parada inexistente: " + nome);
		return parada;
	}

	PoteDeRecursos getPote(String nome) {
		PoteDeRecursos pote = potes.get(nome);
		if (pote == null) throw new IllegalArgumentException("pote inexistente: " + nome);
		return pote;
	}

	Cidade getCidade(String nome) {
		Cidade cidade = cidades.get(nome);
		if (cidade == null) throw new IllegalArgumentException("cidade inexistente: " + nome);
		return cidade;
	}

	boolean estaRevelada(String nomeDaCidade) { return cidadesReveladas.contains(getCidade(nomeDaCidade)); }

	/** Fim de jogo raro: todas as cidades conquistadas (página 14). */
	boolean todasAsCidadesConquistadas() {
		return cidadesReveladas.isEmpty() && pilhaDeCidades.isEmpty();
	}

	PoteDeRecursos getKhanEmPote() { return khanEmPote; }

	boolean khanEstaNasMelhorias() { return khanNasMelhorias; }

	// ======================================================================= auxiliares

	private Conselheiro colocar(String nomeDaParada, Recurso troca) {
		Conselheiro conselheiro = new Conselheiro(troca);
		getParada(nomeDaParada).setConselheiro(conselheiro);
		conselheirosComBonus.add(conselheiro); // a peça de bônus é colocada na preparação (página 4)
		return conselheiro;
	}

	private boolean pagamentoValido(Conselheiro conselheiro, Recurso pagamento) {
		if (pagamento == null) return false;
		for (Peca peca : Peca.values())
			if (peca.em(pagamento) < 0 || (!peca.ehTesouro() && peca.em(pagamento) != 0)) return false; // só tesouro se entrega
		Integer livre = tesourosQuaisquer.get(conselheiro);
		if (livre != null) return Peca.tesouros(pagamento) == livre;
		Recurso troca = conselheiro.getRecurso();
		for (Peca peca : Peca.values())
			if (peca.ehTesouro() && peca.em(pagamento) != -peca.em(troca)) return false; // demanda guardada negativa
		return true;
	}

	private boolean receberTributo(PoteDeRecursos pote) {
		Peca tributo = tributoDoPote.get(pote);
		if (tributo.em(pote.getRecurso()) >= MAX_TRIBUTOS_POR_POTE) return false;
		alterar(pote, tributo, 1);
		return true;
	}

	/** Tira a próxima cidade da pilha e põe nela 4 tesouros da pilha de tesouros. */
	private void revelarProximaCidade() {
		if (pilhaDeCidades.isEmpty()) return; // sem peças viradas para baixo, pula a etapa (página 10)
		Cidade cidade = getCidade(pilhaDeCidades.pollFirst());
		for (int i = 0; i < TESOUROS_POR_CIDADE && !pilhaDeTesouros.isEmpty(); i++)
			alterar(cidade, pilhaDeTesouros.pollFirst(), 1);
		cidadesReveladas.add(cidade);
	}

	/** Debita do jogador, se ele tiver; devolve false sem mexer em nada se faltar. */
	private static boolean pagar(Jogador jogador, Peca peca, int quantidade) {
		Recurso mao = jogador.getRecurso();
		if (peca.em(mao) < quantidade) return false;
		peca.somar(mao, -quantidade);
		jogador.setRecurso(mao);
		return true;
	}

	private static void dar(Jogador jogador, Peca peca, int quantidade) {
		Recurso mao = jogador.getRecurso();
		peca.somar(mao, quantidade);
		jogador.setRecurso(mao);
	}

	/** Coletavel não tem setRecurso: altera o Recurso dele diretamente, como fazem os construtores. */
	private static void alterar(Coletavel coletavel, Peca peca, int quantidade) {
		peca.somar(coletavel.r, quantidade);
	}

	private int peoesEm(Parada parada, Jogador quemChega) {
		int peoes = 0;
		for (Map.Entry<Jogador, Parada> posicao : posicoes.entrySet())
			if (posicao.getValue() == parada && posicao.getKey() != quemChega) peoes++;
		return peoes;
	}

	private boolean adjacenteAoTurno(Jogador jogador, Set<Parada> adjacentes) {
		for (Parada feita : paradasNoTurno.getOrDefault(jogador, Set.of()))
			if (adjacentes.contains(feita)) return true;
		return false;
	}

	/** Soma 1 yurt na região, no vetor yurtsPorRegiao do Jogador (índice = ordem do enum Regiao). */
	private static void registrarYurt(Jogador jogador, Regiao regiao) {
		if (regiao != Regiao.NENHUMA) jogador.yurtsPorRegiao[regiao.ordinal()]++;
	}

	private Set<Parada> paradasPorNome(String... nomes) {
		Set<Parada> resultado = new HashSet<>();
		for (String nome : nomes) resultado.add(getParada(nome));
		return resultado;
	}
}
