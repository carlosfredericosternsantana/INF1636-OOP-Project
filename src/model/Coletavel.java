package model;

import java.util.HashMap;
import java.util.Map;

/**
 * CLASSE AINDA EM DESENVOLVIMENTO
 * 
 * 
 * Elemento do mapa com que o jogador interage pagando um preço: o coletável diz quanto
 * custa, confere se ele tem, debita e só então entrega a recompensa.
 *
 * Cidade custa espada e dá tesouro. o 1º tesouro do turno custa 1 espada e os seguintes,
 * da mesma cidade, custam 2 (pagina 10). Província custa punho e dá tributo (pagina 8).
 * Conselheiro custa tesouro e dá votos, mais a peça de bônus para o primeiro que entregar
 * (paginas 12 e 17); a demanda dele não se esgota.
 */
class Coletavel {

	enum Tipo { CIDADE, PROVINCIA, CONSELHEIRO }

	enum TipoPeca {
		ESPADA, YURT, MOEDA,
		PUNHO, 
		TESOURO_A, TESOURO_B, TESOURO_C, TESOURO_D, TESOURO_E;

		boolean ehTesouro() { return name().startsWith("TESOURO"); }
	}

	private static final int MAX_TRIBUTOS_POR_PROVINCIA = 3;

	private final String nome;
	private final Tipo tipo;
	private final Map<TipoPeca, Integer> pecas = new HashMap<>();   // estoque da cidade ou da província
	private final Map<TipoPeca, Integer> demanda = new HashMap<>(); // o que o conselheiro exige
	private final int votos;
	private final int tesourosQuaisquer; // só a parada que pede "3 tesouros de qualquer tipo"
	private boolean bonusDisponivel;
	private int tesourosTomadosNesteTurno;

	/** Cidade ou província. */
	Coletavel(String nome, Tipo tipo) {
		if (tipo == Tipo.CONSELHEIRO) throw new IllegalArgumentException("conselheiro precisa de votos e demanda");
		this.nome = nome;
		this.tipo = tipo;
		this.votos = 0;
		this.tesourosQuaisquer = 0;
	}

	/** Conselheiro. Das 7 paradas, 6 exigem tesouro por tipo e uma exige 3 de tipo livre. */
	Coletavel(String nome, int votos, Map<TipoPeca, Integer> demanda, int tesourosQuaisquer) {
		this.nome = nome;
		this.tipo = Tipo.CONSELHEIRO;
		this.votos = votos;
		this.demanda.putAll(demanda);
		this.tesourosQuaisquer = tesourosQuaisquer;
		this.bonusDisponivel = true;
	}

	/** O que o jogador tem de pagar agora para tirar uma peça daqui. */
	Map<TipoPeca, Integer> getCusto() {
		return switch (tipo) {
			case CIDADE -> Map.of(TipoPeca.ESPADA, tesourosTomadosNesteTurno == 0 ? 1 : 2);
			case PROVINCIA -> Map.of(TipoPeca.PUNHO, 1);
			case CONSELHEIRO -> Map.copyOf(demanda);
		};
	}

	/**
	 * O jogador paga o custo e leva a peça que escolheu, na cidade ou na província.
	 * Devolve false, sem cobrar nada, se a peça não está aqui ou se ele não tem com que pagar.
	 */
	boolean coletar(Jogador jogador, TipoPeca escolhida) {
		if (tipo == Tipo.CONSELHEIRO) throw new IllegalStateException("conselheiro recebe entrega, não é coletado");
		int disponivel = getQuantidade(escolhida);
		if (disponivel == 0) return false;

		Map<TipoPeca, Integer> custo = getCusto();
		if (!jogador.tem(custo)) return false;
		jogador.gastar(custo);

		if (disponivel == 1) pecas.remove(escolhida);
		else pecas.put(escolhida, disponivel - 1);
		jogador.receber(escolhida, 1);

		if (tipo == Tipo.CIDADE) {
			tesourosTomadosNesteTurno++;
			if (estaVazio()) jogador.conquistarCidade(); // o último tesouro conquista a cidade
		}
		return true;
	}

	/**
	 * Entrega de tesouros a um conselheiro: o pagamento tem de atender à demanda e estar
	 * mesmo com o jogador. Devolve os votos creditados, ou 0 se a entrega não pôde ser feita.
	 */
	int entregar(Jogador jogador, Map<TipoPeca, Integer> pagamento) {
		if (tipo != Tipo.CONSELHEIRO) throw new IllegalStateException("só se entrega a um conselheiro");
		if (!atendeADemanda(pagamento) || !jogador.tem(pagamento)) return 0;

		jogador.gastar(pagamento);
		jogador.receberVotos(votos);
		if (bonusDisponivel) {
			bonusDisponivel = false;
			jogador.receberBonus();
		}
		return votos;
	}

	/** Chamado ao fim da jogada: o próximo tesouro volta a custar 1 espada. */
	void novoTurno() {
		tesourosTomadosNesteTurno = 0;
	}

	/** Os tesouros da cidade revelada, o tributo trazido pelo Khan. */
	void adicionar(TipoPeca peca, int quantidade) {
		if (quantidade <= 0) throw new IllegalArgumentException("quantidade tem de ser positiva");
		if (tipo == Tipo.PROVINCIA && total() + quantidade > MAX_TRIBUTOS_POR_PROVINCIA)
			throw new IllegalStateException("província comporta no máximo " + MAX_TRIBUTOS_POR_PROVINCIA + " peças");
		pecas.merge(peca, quantidade, Integer::sum);
	}

	/** Cidade vazia é cidade conquistada; província vazia não rende tributo. */
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

	boolean temBonus() { return bonusDisponivel; }


	Tipo getTipo() { return tipo; }

	private boolean atendeADemanda(Map<TipoPeca, Integer> pagamento) {
		if (tesourosQuaisquer > 0) {
			int tesouros = 0;
			for (Map.Entry<TipoPeca, Integer> pago : pagamento.entrySet())
				if (pago.getKey().ehTesouro()) tesouros += pago.getValue();
			return tesouros >= tesourosQuaisquer;
		}
		for (Map.Entry<TipoPeca, Integer> exigido : demanda.entrySet())
			if (pagamento.getOrDefault(exigido.getKey(), 0) < exigido.getValue()) return false;
		return true;
	}
}
