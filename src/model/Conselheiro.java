package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Parada de conselheiro (páginas 5, 12, 13 e 17).
 *
 * São 7 no mapa:
 * <ul>
 *   <li>5 pedem 2 tesouros iguais de um tipo específico e dão 1 voto ({@link #deParDeTesouros});</li>
 *   <li>1 pede 3 tesouros de qualquer tipo e dá 1 voto ({@link #deTesourosQuaisquer});</li>
 *   <li>Karakorum pede 3 tipos específicos, definidos na preparação, e dá 2 votos ({@link #karakorum}).</li>
 * </ul>
 * A demanda não se esgota: o mesmo conselheiro pode receber várias entregas. A peça de
 * bônus colocada nele na preparação vai só para quem fizer a PRIMEIRA entrega.
 *
 * Fica fora daqui, porque depende do mapa: conferir que o jogador parou nesta parada.
 */
class Conselheiro extends Coletavel {

	/** As 7 peças de bônus (página 17); cada uma é usada uma única vez. */
	enum PecaBonus {
		VOTO,                         // 1 voto extra imediatamente
		DUAS_ESPADAS,                 // 2 peças de tributo de espada
		DUAS_MOEDAS,                  // 2 peças de tributo de moeda
		DOIS_YURTS,                   // 2 peças de tributo de yurt
		TRES_MOVIMENTOS_E_UM_TRIBUTO, // 3 movimentos extras e 1 ação extra de pegar tributo
		DUAS_ACOES_DE_TRIBUTO,        // 2 ações extras de pegar tributo
		TESOURO_A_ESCOLHA             // pega 3 tesouros da pilha, fica com 1
	}

	static final int VOTOS_POR_ENTREGA = 1;
	static final int VOTOS_EM_KARAKORUM = 2;
	static final int TESOUROS_IGUAIS = 2;
	static final int TESOUROS_QUAISQUER = 3;

	private final int votos;
	private final Map<TipoPeca, Integer> demanda = new HashMap<>(); // vazia no conselheiro de tipo livre
	private final int tesourosQuaisquer;                            // 0, exceto no conselheiro de tipo livre
	private PecaBonus bonus;                                        // null depois da primeira entrega

	private Conselheiro(String nome, int votos, Map<TipoPeca, Integer> demanda, int tesourosQuaisquer,
			PecaBonus bonus) {
		super(nome);
		Objects.requireNonNull(bonus, "todo conselheiro começa com uma peça de bônus");
		for (Map.Entry<TipoPeca, Integer> item : demanda.entrySet())
			if (!item.getKey().ehTesouro() || item.getValue() <= 0)
				throw new IllegalArgumentException("conselheiro só pede quantidades positivas de tesouro");
		this.votos = votos;
		this.demanda.putAll(demanda);
		this.tesourosQuaisquer = tesourosQuaisquer;
		this.bonus = bonus;
	}

	/** Um dos 5 conselheiros que pedem 2 tesouros iguais de um tipo e dão 1 voto. */
	static Conselheiro deParDeTesouros(String nome, TipoPeca tesouro, PecaBonus bonus) {
		return new Conselheiro(nome, VOTOS_POR_ENTREGA, Map.of(tesouro, TESOUROS_IGUAIS), 0, bonus);
	}

	/** O conselheiro que pede 3 tesouros de qualquer tipo, misturados ou não, e dá 1 voto. */
	static Conselheiro deTesourosQuaisquer(String nome, PecaBonus bonus) {
		return new Conselheiro(nome, VOTOS_POR_ENTREGA, Map.of(), TESOUROS_QUAISQUER, bonus);
	}

	/** Karakorum: 1 tesouro de cada um dos 3 tipos sorteados na preparação, 2 votos. */
	static Conselheiro karakorum(Set<TipoPeca> tesouros, PecaBonus bonus) {
		if (tesouros == null || tesouros.size() != 3)
			throw new IllegalArgumentException("Karakorum pede exatamente 3 tipos de tesouro");
		Map<TipoPeca, Integer> demanda = new HashMap<>();
		for (TipoPeca tesouro : tesouros) demanda.put(tesouro, 1);
		return new Conselheiro("Karakorum", VOTOS_EM_KARAKORUM, demanda, 0, bonus);
	}

	/**
	 * A demanda por tipo. No conselheiro de tipo livre é vazia; nesse caso, veja
	 * {@link #getTesourosQuaisquer()}.
	 */
	@Override
	Map<TipoPeca, Integer> getCusto() {
		return Map.copyOf(demanda);
	}

	/** Conselheiro não tem estoque: ele recebe tesouros, não os guarda para ninguém pegar. */
	@Override
	protected boolean aceita(TipoPeca peca) {
		return false;
	}

	/**
	 * O jogador entrega tesouros e recebe os votos; quem faz a primeira entrega leva
	 * também a peça de bônus. O pagamento tem de ser EXATAMENTE a demanda (no conselheiro
	 * de tipo livre: exatamente 3 tesouros, de qualquer tipo) e estar na mão do jogador.
	 *
	 * @return os votos creditados, ou 0 se a entrega foi recusada (nada é cobrado).
	 */
	int entregar(Jogador jogador, Map<TipoPeca, Integer> pagamento) {
		Objects.requireNonNull(jogador, "jogador obrigatório");
		if (!pagamentoValido(pagamento)) return 0;
		if (!cobrar(jogador, pagamento)) return 0;

		jogador.receberVotos(votos);
		if (bonus != null) {
			bonus = null;
			jogador.receberBonus();
		}
		return votos;
	}

	/** A peça de bônus ainda disponível; null se alguém já fez a primeira entrega. */
	PecaBonus getBonus() { return bonus; }

	boolean temBonus() { return bonus != null; }

	int getVotos() { return votos; }

	int getTesourosQuaisquer() { return tesourosQuaisquer; }

	private boolean pagamentoValido(Map<TipoPeca, Integer> pagamento) {
		if (pagamento == null || pagamento.isEmpty()) return false;
		int tesouros = 0;
		for (Map.Entry<TipoPeca, Integer> item : pagamento.entrySet()) {
			if (!item.getKey().ehTesouro() || item.getValue() == null || item.getValue() <= 0) return false;
			tesouros += item.getValue();
		}
		if (tesourosQuaisquer > 0) return tesouros == tesourosQuaisquer;
		return demanda.equals(pagamento);
	}
}
