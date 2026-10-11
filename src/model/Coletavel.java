package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Elemento do mapa com que o jogador interage pagando um preço (diagrama: Coletável).
 *
 * Há três tipos, cada um numa subclasse:
 * <ul>
 *   <li>{@link Cidade}: custa espada e dá tesouro (página 10);</li>
 *   <li>{@link Provincia}: o "pote de recursos", custa punho e dá tributo (página 8);</li>
 *   <li>{@link Conselheiro}: recebe tesouros e dá votos (páginas 12 e 13).</li>
 * </ul>
 *
 * Esta classe guarda o que é comum: o nome, o estoque de peças e o protocolo de cobrança
 * (confere se o jogador tem, debita e só então entrega). Cada subclasse decide quanto
 * custa ({@link #getCusto()}) e quais peças aceita no estoque ({@link #aceita}).
 */
abstract class Coletavel {

	/** Peças que circulam entre o mapa e a mão do jogador. */
	enum TipoPeca {
		ESPADA, YURT, MOEDA,                                     // peças de tributo
		PUNHO,                                                   // ação de pegar tributo, dada pela coluna ativada
		TESOURO_A, TESOURO_B, TESOURO_C, TESOURO_D, TESOURO_E;   // 5 tipos de tesouro, 8 de cada

		boolean ehTesouro() { return name().startsWith("TESOURO"); }

		boolean ehTributo() { return this == ESPADA || this == YURT || this == MOEDA; }
	}

	private final String nome;
	private final Map<TipoPeca, Integer> pecas = new HashMap<>(); // estoque: tesouros da cidade, tributos da província

	protected Coletavel(String nome) {
		if (nome == null || nome.isBlank()) throw new IllegalArgumentException("nome obrigatório");
		this.nome = nome;
	}

	/** O que o jogador tem de pagar agora para interagir com este elemento. */
	abstract Map<TipoPeca, Integer> getCusto();

	/** Se esta peça pode ficar no estoque deste elemento. */
	protected abstract boolean aceita(TipoPeca peca);

	String getNome() { return nome; }

	/** Sem peças no estoque: cidade conquistada, província que não rende tributo. */
	boolean estaVazio() {
		return pecas.isEmpty();
	}

	int getQuantidade(TipoPeca peca) {
		return pecas.getOrDefault(peca, 0);
	}

	int total() {
		int soma = 0;
		for (int quantidade : pecas.values()) soma += quantidade;
		return soma;
	}

	/** Coloca peças no estoque (tesouros da cidade revelada, tributo trazido pelo Khan). */
	protected void guardar(TipoPeca peca, int quantidade) {
		Objects.requireNonNull(peca, "peça obrigatória");
		if (quantidade <= 0) throw new IllegalArgumentException("quantidade tem de ser positiva");
		if (!aceita(peca)) throw new IllegalArgumentException(nome + " não aceita " + peca);
		pecas.merge(peca, quantidade, Integer::sum);
	}

	/**
	 * Protocolo de cobrança das peças do estoque: o jogador paga o custo atual e leva uma
	 * peça. Devolve false, sem cobrar nada, se a peça não está aqui ou se ele não tem com
	 * que pagar.
	 */
	protected boolean retirarPagando(Jogador jogador, TipoPeca escolhida) {
		Objects.requireNonNull(jogador, "jogador obrigatório");
		int disponivel = getQuantidade(escolhida);
		if (disponivel == 0) return false;
		if (!cobrar(jogador, getCusto())) return false;

		if (disponivel == 1) pecas.remove(escolhida);
		else pecas.put(escolhida, disponivel - 1);
		jogador.receber(escolhida, 1);
		return true;
	}

	/** Debita o custo se o jogador tiver como pagar; caso contrário, não mexe em nada. */
	protected static boolean cobrar(Jogador jogador, Map<TipoPeca, Integer> custo) {
		if (!jogador.tem(custo)) return false;
		jogador.gastar(custo);
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + nome;
	}
}
