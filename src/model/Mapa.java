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

import model.Coletavel.TipoPeca;

/**
* O Mapa valida as regras que dependem de ONDE as coisas estão: movimento, construção
* de yurts, adjacência para tributo, ataque e entrega, e a ação do Khan. O que depende
* só do jogador (quantos movimentos e punhos ele tem neste turno) fica com o
* Jogador/Manager: o Mapa diz quanto um caminho custa e o Manager confere se cabe.
*
* O layout é montado pelos métodos "adicionar..." e "conectar", para que possa ser
* carregado de um arquivo texto (diagrama: IO TXT) e para que os testes usem mapas pequenos.
*/

class Mapa {

	enum Regiao { CHINA, RUSSIA, PERSIA, NENHUMA }

	static final int YURTS_POR_JOGADOR = 12;
	static final int CIDADES_REVELADAS = 3;
	static final int TESOUROS_DE_CADA_TIPO = 8;

	private final Random sorteio;

	// layout do tabuleiro
	private final Map<String, Parada> paradas = new HashMap<>();
	private Parada karakorum;
	private final Map<String, Provincia> provincias = new HashMap<>();
	private final Map<Provincia, Set<Parada>> paradasDaProvincia = new HashMap<>();
	private final Map<Provincia, List<Provincia>> vizinhasDoKhan = new HashMap<>(); // setas que saem do Khan
	private final Map<String, Cidade> cidades = new HashMap<>();
	private final Map<Cidade, Set<Parada>> paradasDaCidade = new HashMap<>();
	private final Map<Cidade, Regiao> regiaoDaCidade = new HashMap<>();

	// estado da partida
	private final Deque<String> pilhaDeCidades = new ArrayDeque<>();
	private final Set<Cidade> cidadesReveladas = new HashSet<>();
	private final Map<Cidade, Jogador> yurtNoCentroDaCidade = new HashMap<>();
	private final Deque<TipoPeca> pilhaDeTesouros = new ArrayDeque<>();
	private final Map<Jogador, Parada> posicoes = new HashMap<>();
	private final Map<Jogador, Set<Parada>> paradasNoTurno = new HashMap<>();
	private Provincia khanEmProvincia;   // null se o Khan está fora do mapa ou na área de melhorias
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

	void colocarConselheiro(String parada, Conselheiro conselheiro) {
		getParada(parada).setConselheiro(conselheiro);
	}

	/** Província e as paradas de onde se pode pegar o tributo dela. */
	void adicionarProvincia(Provincia provincia, String... paradasAdjacentes) {
		provincias.put(provincia.getNome(), provincia);
		paradasDaProvincia.put(provincia, paradasPorNome(paradasAdjacentes));
	}

	/** As 2 províncias que recebem tributo junto com a província do Khan. */
	void definirVizinhasDoKhan(String provinciaDoKhan, String vizinha1, String vizinha2) {
		Provincia khan = getProvincia(provinciaDoKhan);
		if (!khan.ehDoKhan()) throw new IllegalArgumentException(provinciaDoKhan + " não é província do Khan");
		vizinhasDoKhan.put(khan, List.of(getProvincia(vizinha1), getProvincia(vizinha2)));
	}

	/** Cidade impressa no mapa: ainda não revelada; entra na partida quando sair da pilha. */
	void adicionarCidade(Cidade cidade, Regiao regiao, String... paradasAdjacentes) {
		cidades.put(cidade.getNome(), cidade);
		regiaoDaCidade.put(cidade, regiao);
		paradasDaCidade.put(cidade, paradasPorNome(paradasAdjacentes));
	}

	// ======================================================================= preparação

	/** Embaralha as peças de cidade e de tesouro e revela as 3 primeiras cidades (página 4). */
	void preparar() {
		List<String> nomes = new ArrayList<>(cidades.keySet());
		Collections.shuffle(nomes, sorteio);
		List<TipoPeca> tesouros = new ArrayList<>();
		for (TipoPeca peca : TipoPeca.values())
			if (peca.ehTesouro())
				for (int i = 0; i < TESOUROS_DE_CADA_TIPO; i++) tesouros.add(peca);
		Collections.shuffle(tesouros, sorteio);
		prepararCom(nomes, tesouros);
	}

	/** Preparação com ordem conhecida das pilhas, para testes e para carregar um jogo salvo. */
	void prepararCom(List<String> ordemDasCidades, List<TipoPeca> ordemDosTesouros) {
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
		for (Cidade cidade : cidadesReveladas) cidade.novoTurno();
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
	 * seja Karakorum. Gasta 1 peça de tributo de yurt e 1 dos 12 yurts do jogador.
	 */
	boolean construirYurt(Jogador jogador, String nomeDaParada) {
		Parada parada = getParada(nomeDaParada);
		if (!fezParadaEm(jogador, parada) || !parada.cabeYurt()) return false;
		if (yurtsColocados(jogador) >= YURTS_POR_JOGADOR) return false;
		if (!jogador.tem(Map.of(TipoPeca.YURT, 1))) return false;

		jogador.gastar(Map.of(TipoPeca.YURT, 1));
		parada.receberYurt(jogador);
		registrarYurt(jogador, parada.getRegiao());
		return true;
	}

	/** Pega 1 tributo de uma província adjacente a alguma parada feita neste turno (página 8). */
	boolean pegarTributo(Jogador jogador, String nomeDaProvincia) {
		Provincia provincia = getProvincia(nomeDaProvincia);
		if (!adjacenteAoTurno(jogador, paradasDaProvincia.get(provincia))) return false;
		return provincia.coletar(jogador);
	}

	/**
	 * Toma um tesouro de uma cidade revelada adjacente a uma parada feita neste turno
	 * (página 10). Se foi o último, o jogador põe um yurt no centro da cidade e a próxima
	 * cidade da pilha é revelada.
	 */
	boolean atacarCidade(Jogador jogador, String nomeDaCidade, TipoPeca tesouro) {
		Cidade cidade = getCidade(nomeDaCidade);
		if (!cidadesReveladas.contains(cidade)) return false;
		if (!adjacenteAoTurno(jogador, paradasDaCidade.get(cidade))) return false;
		if (!cidade.coletar(jogador, tesouro)) return false;

		if (cidade.foiConquistada()) {
			cidadesReveladas.remove(cidade);
			if (yurtsColocados(jogador) < YURTS_POR_JOGADOR) { // sem yurts sobrando, a conquista vale sem o marcador
				yurtNoCentroDaCidade.put(cidade, jogador);
				registrarYurt(jogador, regiaoDaCidade.get(cidade));
			}
			revelarProximaCidade();
		}
		return true;
	}

	/** Entrega tesouros ao conselheiro da parada, se o jogador parou nela neste turno (página 12). */
	int entregar(Jogador jogador, String nomeDaParada, Map<TipoPeca, Integer> pagamento) {
		Parada parada = getParada(nomeDaParada);
		Conselheiro conselheiro = parada.getConselheiro();
		if (conselheiro == null || !fezParadaEm(jogador, parada)) return 0;
		return conselheiro.entregar(jogador, pagamento);
	}

	// ======================================================================= Khan (página 9)

	/**
	 * Leva o Khan a uma das províncias do Khan: ela e as 2 vizinhas indicadas recebem 1
	 * tributo cada (sem passar de 3). Devolve false se o destino não é válido ou se o
	 * Khan já está lá: ele não pode ficar onde está.
	 */
	boolean moverKhanParaProvincia(String nomeDaProvincia) {
		Provincia destino = getProvincia(nomeDaProvincia);
		if (!destino.ehDoKhan() || destino == khanEmProvincia) return false;
		khanEmProvincia = destino;
		khanNasMelhorias = false;
		destino.receberTributo();
		for (Provincia vizinha : vizinhasDoKhan.getOrDefault(destino, List.of())) vizinha.receberTributo();
		return true;
	}

	/**
	 * Leva o Khan à área de melhorias. A reposição das melhorias e a moeda/espada dos
	 * espaços vazios ficam com a área de melhorias; aqui só se valida e registra a posição.
	 */
	boolean moverKhanParaMelhorias() {
		if (khanNasMelhorias) return false;
		khanNasMelhorias = true;
		khanEmProvincia = null;
		return true;
	}

	// ======================================================================= consultas

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

	Provincia getProvincia(String nome) {
		Provincia provincia = provincias.get(nome);
		if (provincia == null) throw new IllegalArgumentException("província inexistente: " + nome);
		return provincia;
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

	Provincia getKhanEmProvincia() { return khanEmProvincia; }

	boolean khanEstaNasMelhorias() { return khanNasMelhorias; }

	// ======================================================================= auxiliares

	/** Tira a próxima cidade da pilha e põe nela 4 tesouros da pilha de tesouros. */
	private void revelarProximaCidade() {
		if (pilhaDeCidades.isEmpty()) return; // sem peças viradas para baixo, pula a etapa (página 10)
		Cidade cidade = getCidade(pilhaDeCidades.pollFirst());
		for (int i = 0; i < Cidade.TESOUROS_INICIAIS && !pilhaDeTesouros.isEmpty(); i++)
			cidade.adicionar(pilhaDeTesouros.pollFirst(), 1);
		cidadesReveladas.add(cidade);
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

	/**
	 * Atualiza os contadores que as cartas de conselheiro aberto leem.
	 * TODO: quando o Jogador tiver um método próprio (ex.: registrarYurt(Regiao)), chamar ele aqui.
	 */
	private static void registrarYurt(Jogador jogador, Regiao regiao) {
		jogador.yurtsNoMapa++;
		switch (regiao) {
			case CHINA -> jogador.yurtsNaChina++;
			case RUSSIA -> jogador.yurtsNaRussia++;
			case PERSIA -> jogador.yurtsNaPersia++;
			case NENHUMA -> { }
		}
	}

	private Set<Parada> paradasPorNome(String... nomes) {
		Set<Parada> resultado = new HashSet<>();
		for (String nome : nomes) resultado.add(getParada(nome));
		return resultado;
	}
}