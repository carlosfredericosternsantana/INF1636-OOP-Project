package model;

import java.util.Map;

/**
 * Província do mapa, o "pote de recursos" do diagrama (páginas 5, 8 e 9).
 *
 * Cada província tem um tipo de tributo fixo, impresso no tabuleiro (espada, yurt ou
 * moeda), e guarda no máximo 3 peças. O jogador paga 1 punho por peça e pode pegar mais
 * de uma da mesma província, se tiver punhos. O Khan acrescenta 1 peça quando para nela
 * ou numa província vizinha; se ela já estiver cheia, nada acontece.
 *
 * As 4 províncias marcadas com a cabeça do Khan são lugares onde ele pode parar; a
 * carta secreta Bajulador também conta yurts adjacentes a elas.
 */
class Provincia extends Coletavel {

	static final int MAX_TRIBUTOS = 3;

	private final TipoPeca tributo;
	private final boolean doKhan;

	Provincia(String nome, TipoPeca tributo, boolean doKhan) {
		super(nome);
		if (tributo == null || !tributo.ehTributo())
			throw new IllegalArgumentException("província só rende espada, yurt ou moeda");
		this.tributo = tributo;
		this.doKhan = doKhan;
	}

	Provincia(String nome, TipoPeca tributo) {
		this(nome, tributo, false);
	}

	/** Cada peça custa 1 punho. */
	@Override
	Map<TipoPeca, Integer> getCusto() {
		return Map.of(TipoPeca.PUNHO, 1);
	}

	/** Só o tributo impresso nesta província. */
	@Override
	protected boolean aceita(TipoPeca peca) {
		return peca == tributo;
	}

	/**
	 * Acrescenta 1 peça do tributo desta província (preparação do jogo e ação do Khan).
	 *
	 * @return false se ela já estava com {@link #MAX_TRIBUTOS} peças e nada foi acrescentado.
	 */
	boolean receberTributo() {
		if (total() >= MAX_TRIBUTOS) return false;
		guardar(tributo, 1);
		return true;
	}

	/** O jogador paga 1 punho e leva 1 peça. Devolve false, sem cobrar, se não houver peça ou punho. */
	boolean coletar(Jogador jogador) {
		return retirarPagando(jogador, tributo);
	}

	TipoPeca getTributo() { return tributo; }

	boolean ehDoKhan() { return doKhan; }
}
