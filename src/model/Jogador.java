package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import model.Coletavel.TipoPeca;

/**
 * Herdeiro do Khan controlado por uma pessoa.
 *
 * ATENÇÃO - esta classe ainda não é minha: o que está aqui é o mínimo de que a Quest e
 * a Coletavel precisam. Quem for implementar o resto do Jogador deve manter esta API.
 *
 * São duas coisas. Primeiro, o que o jogador TEM na mão: espadas, moedas, yurts,
 * tesouros e os punhos da coluna ativada, num mapa só, de onde os coletáveis debitam o
 * preço ({@link #tem}, {@link #gastar}, {@link #receber}).
 *
 * Segundo, os contadores que as cartas de conselheiro aberto consultam. Os que dependem
 * do que ele tem na mão saem do mapa na hora; os que dependem do mapa do jogo e do
 * tabuleiro dele são campos, atualizados pelas ações do turno, para que checar uma carta
 * não vire uma varredura a cada rodada. O comentário ao lado de cada campo diz quem o
 * atualiza.
 */
public class Jogador {

	private final Map<TipoPeca, Integer> recursos = new HashMap<>();

	int votos;                            // trilha do Kurultai: entregas e cartas cumpridas
	int yurtsNoMapa;                      // construir yurt, inclusive o posto no centro da cidade conquistada
	int yurtsNaChina;                     // construir yurt: contagem por região,
	int yurtsNaRussia;                    // usada pelas duas cartas que pedem
	int yurtsNaPersia;                    // yurts em CADA uma das 3 regiões
	int pecasDeBonus;                     // primeira entrega a cada conselheiro
	int cidadesConquistadas;              // tomar o último tesouro de uma cidade
	int linhasDeMelhoriaCompletas;        // comprar/ganhar melhoria
	int colunasComDuasMelhorias;          // idem
	int maiorNumeroDeMelhoriasNumaColuna; // idem

	/** Ele tem tudo o que este custo pede? */
	boolean tem(Map<TipoPeca, Integer> custo) {
		for (Map.Entry<TipoPeca, Integer> item : custo.entrySet())
			if (getQuantidade(item.getKey()) < item.getValue()) return false;
		return true;
	}

	/** Debita o custo. Chame {@link #tem} antes: aqui, faltar peça é erro de quem chamou. */
	void gastar(Map<TipoPeca, Integer> custo) {
		if (!tem(custo)) throw new IllegalStateException("jogador não tem como pagar " + custo);
		for (Map.Entry<TipoPeca, Integer> item : custo.entrySet()) {
			int restante = getQuantidade(item.getKey()) - item.getValue();
			if (restante == 0) recursos.remove(item.getKey());
			else recursos.put(item.getKey(), restante);
		}
	}

	void receber(TipoPeca peca, int quantidade) {
		Objects.requireNonNull(peca, "peça obrigatória");
		if (quantidade <= 0) throw new IllegalArgumentException("quantidade tem de ser positiva");
		recursos.merge(peca, quantidade, Integer::sum);
	}

	int getQuantidade(TipoPeca peca) {
		return recursos.getOrDefault(peca, 0);
	}

	Map<TipoPeca, Integer> getRecursos() {
		return Map.copyOf(recursos);
	}

	void receberVotos(int quantidade) {
		votos += quantidade;
	}

	void receberBonus() {
		pecasDeBonus++;
	}

	void conquistarCidade() {
		cidadesConquistadas++;
	}

	int getVotos() { return votos; }

	// Contadores lidos pelas cartas de conselheiro aberto (ver Quest).

	/** "Você não precisa gastá-las": conta as espadas que ele tem na mão. */
	int getEspadas() { return getQuantidade(TipoPeca.ESPADA); }

	int getTesouros() {
		int tesouros = 0;
		for (Map.Entry<TipoPeca, Integer> item : recursos.entrySet())
			if (item.getKey().ehTesouro()) tesouros += item.getValue();
		return tesouros;
	}

	int getTiposDeTesouroDiferentes() {
		int tipos = 0;
		for (Map.Entry<TipoPeca, Integer> item : recursos.entrySet())
			if (item.getKey().ehTesouro() && item.getValue() > 0) tipos++;
		return tipos;
	}

	int getYurtsNoMapa() { return yurtsNoMapa; }

	int getYurtsNaChina() { return yurtsNaChina; }

	int getYurtsNaRussia() { return yurtsNaRussia; }

	int getYurtsNaPersia() { return yurtsNaPersia; }

	int getPecasDeBonus() { return pecasDeBonus; }

	int getCidadesConquistadas() { return cidadesConquistadas; }

	int getLinhasDeMelhoriaCompletas() { return linhasDeMelhoriaCompletas; }

	int getColunasComDuasMelhorias() { return colunasComDuasMelhorias; }

	int getMaiorNumeroDeMelhoriasNumaColuna() { return maiorNumeroDeMelhoriasNumaColuna; }
}
