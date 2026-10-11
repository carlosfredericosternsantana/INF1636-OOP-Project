package model;

import java.util.Map;

/**
 * Cidade revelada no mapa (página 10).
 *
 * Começa com 4 tesouros. O jogador que fizer uma parada adjacente toma um tesouro pagando
 * espadas: o 1º do turno custa 1 espada e cada seguinte, da mesma cidade, custa 2. Quem
 * tomar o último tesouro conquista a cidade.
 *
 * Fica fora daqui, porque depende do mapa: conferir a adjacência da parada, revelar a
 * próxima cidade da pilha e colocar o yurt do conquistador no centro da cidade.
 */
class Cidade extends Coletavel {

	static final int TESOUROS_INICIAIS = 4;

	private int tesourosTomadosNesteTurno;
	private Jogador conquistador;

	Cidade(String nome) {
		super(nome);
	}

	/** 1 espada pelo 1º tesouro do turno, 2 por cada um dos seguintes. */
	@Override
	Map<TipoPeca, Integer> getCusto() {
		return Map.of(TipoPeca.ESPADA, tesourosTomadosNesteTurno == 0 ? 1 : 2);
	}

	/** Cidade só guarda tesouros. */
	@Override
	protected boolean aceita(TipoPeca peca) {
		return peca.ehTesouro();
	}

	/** Os tesouros sorteados quando a cidade é revelada. */
	void adicionar(TipoPeca tesouro, int quantidade) {
		if (foiConquistada()) throw new IllegalStateException(getNome() + " já foi conquistada");
		guardar(tesouro, quantidade);
	}

	/**
	 * O jogador paga as espadas e leva o tesouro escolhido. Se era o último, conquista a
	 * cidade. Devolve false, sem cobrar nada, se o tesouro não está aqui ou se faltam espadas.
	 */
	boolean coletar(Jogador jogador, TipoPeca tesouro) {
		if (!retirarPagando(jogador, tesouro)) return false;
		tesourosTomadosNesteTurno++;
		if (estaVazio()) {
			conquistador = jogador;
			jogador.conquistarCidade();
		}
		return true;
	}

	/** Chamado ao fim de cada jogada: o próximo tesouro volta a custar 1 espada. */
	void novoTurno() {
		tesourosTomadosNesteTurno = 0;
	}

	boolean foiConquistada() { return conquistador != null; }

	/** Quem tomou o último tesouro; null se a cidade ainda não foi conquistada. */
	Jogador getConquistador() { return conquistador; }
}
