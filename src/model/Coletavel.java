package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Elemento do mapa de onde o jogador tira peças, ou para onde ele as leva.
 *
 * Os três casos do diagrama (cidade, pote de recursos e conselheiro) são uma classe
 * só, diferenciados pelo campo {@link Tipo}:
 *
 * <ul>
 * <li>CIDADE: guarda as peças de tesouro que podem ser saqueadas com espadas. Quando
 *     a última sai, a cidade está conquistada ({@link #estaVazio()}) - manual, p. 10.</li>
 * <li>PROVINCIA: o "pote de recursos". Guarda as peças de tributo (espada, yurt ou
 *     moeda) que o jogador coleta das províncias adjacentes às paradas onde esteve.
 *     No máximo 3 peças por província - manual, p. 9.</li>
 * <li>CONSELHEIRO: aqui o mapa de peças é a DEMANDA, isto é, os tesouros que têm de
 *     ser entregues para render {@link #getVotos()} votos no Kurultai - manual, p. 13.</li>
 * </ul>
 */
class Coletavel {

	enum Tipo { CIDADE, PROVINCIA, CONSELHEIRO }

	/** Peças que circulam pelo tabuleiro: as 3 de tributo e os 5 tipos de tesouro. */
	enum TipoPeca { ESPADA, YURT, MOEDA, TESOURO_A, TESOURO_B, TESOURO_C, TESOURO_D, TESOURO_E }

	private static final int MAX_TRIBUTOS_POR_PROVINCIA = 3;

	private final String nome;
	private final Tipo tipo;
	private final Map<TipoPeca, Integer> pecas = new HashMap<>();
	private final int votos;
	private final int tesourosQuaisquerExigidos;
	private boolean bonusDisponivel;

	/** Cidade ou província. */
	Coletavel(String nome, Tipo tipo) {
		this(nome, tipo, 0, 0);
	}

	/**
	 * Conselheiro: quantos votos cada entrega rende e quantos tesouros de tipo livre
	 * ele exige. Só a parada que pede "3 tesouros de qualquer tipo" usa esse segundo
	 * valor; nas outras a exigência é por tipo e entra por {@link #adicionar}.
	 */
	Coletavel(String nome, Tipo tipo, int votos, int tesourosQuaisquerExigidos) {
		if (nome == null || nome.isBlank()) throw new IllegalArgumentException("coletável precisa de nome");
		if (votos < 0 || tesourosQuaisquerExigidos < 0) throw new IllegalArgumentException("valor negativo");
		this.nome = nome;
		this.tipo = Objects.requireNonNull(tipo, "tipo obrigatório");
		this.votos = votos;
		this.tesourosQuaisquerExigidos = tesourosQuaisquerExigidos;
		// Na preparação, uma peça de bônus é posta sobre cada um dos 7 conselheiros (p. 17).
		this.bonusDisponivel = (tipo == Tipo.CONSELHEIRO);
	}

	/** Põe peças aqui: tesouros na cidade revelada, tributo trazido pelo Khan, ou a demanda do conselheiro. */
	void adicionar(TipoPeca peca, int quantidade) {
		Objects.requireNonNull(peca, "peça obrigatória");
		if (quantidade <= 0) throw new IllegalArgumentException("quantidade tem de ser positiva");
		if (tipo == Tipo.PROVINCIA && total() + quantidade > MAX_TRIBUTOS_POR_PROVINCIA)
			throw new IllegalStateException("província comporta no máximo " + MAX_TRIBUTOS_POR_PROVINCIA + " peças");
		pecas.merge(peca, quantidade, Integer::sum);
	}

	/**
	 * Tira peças daqui: saque de tesouro na cidade, coleta de tributo na província.
	 *
	 * @return quantas peças saíram de fato, que pode ser menos do que o pedido (ou 0).
	 */
	int coletar(TipoPeca peca, int quantidade) {
		Objects.requireNonNull(peca, "peça obrigatória");
		if (tipo == Tipo.CONSELHEIRO)
			throw new IllegalStateException("não se coleta de um conselheiro: entrega-se a ele");
		if (quantidade <= 0) throw new IllegalArgumentException("quantidade tem de ser positiva");
		int disponivel = getQuantidade(peca);
		int retirado = Math.min(quantidade, disponivel);
		if (retirado == disponivel) pecas.remove(peca);
		else pecas.put(peca, disponivel - retirado);
		return retirado;
	}

	/** Cidade sem nenhum tesouro é cidade conquistada; província vazia não rende tributo. */
	boolean estaVazio() {
		return pecas.isEmpty();
	}

	int getQuantidade(TipoPeca peca) {
		return pecas.getOrDefault(peca, 0);
	}

	/** Total de peças guardadas - ou exigidas, no caso do conselheiro. */
	int total() {
		int soma = 0;
		for (int quantidade : pecas.values()) soma += quantidade;
		return soma;
	}

	Map<TipoPeca, Integer> getPecas() {
		return Map.copyOf(pecas);
	}

	boolean temBonus() {
		return bonusDisponivel;
	}

	/** Só o primeiro a entregar tesouros a este conselheiro leva a peça de bônus (p. 17). */
	boolean pegarBonus() {
		boolean tinha = bonusDisponivel;
		bonusDisponivel = false;
		return tinha;
	}

	String getNome() { return nome; }

	Tipo getTipo() { return tipo; }

	int getVotos() { return votos; }

	int getTesourosQuaisquerExigidos() { return tesourosQuaisquerExigidos; }
}
