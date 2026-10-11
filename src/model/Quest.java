package model;

import java.util.Objects;

/**
 * Carta de conselheiro aberto (manual, p. 18).
 *
 * São 14 cartas, das quais 4 entram em jogo. Cada uma dá 1 voto no Kurultai ao
 * PRIMEIRO jogador que cumprir a sua demanda; feito isso a carta sai de jogo e não é
 * reposta. As cartas de conselheiro SECRETO não estão aqui: elas pertencem a um
 * jogador só e são contadas no fim da partida, então ficam no Jogador.
 *
 * A demanda das 14 cartas sempre se reduz a "ter pelo menos N de alguma coisa", então
 * a carta guarda apenas o par (condição, N) e compara com o progresso do jogador.
 * Carta nova = valor novo em {@link TipoCondicao} e um caso no switch, sem mexer em
 * mais nada (princípio aberto/fechado).
 */
class Quest {

	/** O que cada carta mede. Cada valor tem um getter correspondente no Jogador. */
	enum TipoCondicao {
		ESPADAS,                     // primeiro a ter 4 espadas
		YURTS_NO_MAPA,               // 6 yurts no mapa; 10 yurts no mapa
		TESOUROS,                    // 4 tesouros
		TESOUROS_DIFERENTES,         // 5 tesouros diferentes
		YURTS_EM_CADA_REGIAO,        // 1 yurt em cada região; 2 yurts em cada região
		PECAS_DE_BONUS,              // 2 peças de bônus; 3 peças de bônus
		CIDADES_CONQUISTADAS,        // conquistar 2 cidades; conquistar 3 cidades
		LINHAS_DE_MELHORIA,          // completar uma linha de melhorias
		COLUNAS_COM_DUAS_MELHORIAS,  // 2 colunas com 2 melhorias cada
		MELHORIAS_NA_MESMA_COLUNA    // 4 melhorias em uma só coluna
	}

	/** Toda carta de conselheiro aberto vale exatamente 1 voto. */
	static final int VOTOS = 1;

	private final TipoCondicao condicao;
	private final int quantidadeExigida;
	private Jogador cumpridaPor;

	Quest(TipoCondicao condicao, int quantidadeExigida) {
		this.condicao = Objects.requireNonNull(condicao, "condição obrigatória");
		if (quantidadeExigida <= 0) throw new IllegalArgumentException("a demanda tem de ser positiva");
		this.quantidadeExigida = quantidadeExigida;
	}

	/**
	 * Verifica se este jogador acabou de cumprir a carta. Deve ser chamada pelo Manager
	 * depois das ações do jogador.
	 *
	 * @return true se foi ele quem cumpriu agora, e portanto ganha {@link #VOTOS};
	 *         false se ainda não cumpriu ou se outro jogador já tinha levado a carta.
	 */
	boolean checarQuests(Jogador jogador) {
		Objects.requireNonNull(jogador, "jogador obrigatório");
		if (cumpridaPor != null) return false; // apenas o primeiro a cumprir recebe o voto (p. 13)
		if (progressoDe(jogador) < quantidadeExigida) return false;
		cumpridaPor = jogador;
		return true;
	}

	/**
	 * Quem conta é o próprio Jogador: ele mantém esses contadores em dia conforme joga,
	 * de modo que checar uma carta é só uma comparação - nada é recalculado aqui,
	 * varrendo mapa e tabuleiro, a cada turno.
	 */
	private int progressoDe(Jogador jogador) {
		return switch (condicao) {
			case ESPADAS                    -> jogador.getEspadas();
			case YURTS_NO_MAPA              -> jogador.getYurtsNoMapa();
			case TESOUROS                   -> jogador.getTesouros();
			case TESOUROS_DIFERENTES        -> jogador.getTiposDeTesouroDiferentes();
			case YURTS_EM_CADA_REGIAO       -> jogador.getMenorNumeroDeYurtsPorRegiao();
			case PECAS_DE_BONUS             -> jogador.getPecasDeBonus();
			case CIDADES_CONQUISTADAS       -> jogador.getCidadesConquistadas();
			case LINHAS_DE_MELHORIA         -> jogador.getLinhasDeMelhoriaCompletas();
			case COLUNAS_COM_DUAS_MELHORIAS -> jogador.getColunasComDuasMelhorias();
			case MELHORIAS_NA_MESMA_COLUNA  -> jogador.getMaiorNumeroDeMelhoriasNumaColuna();
		};
	}

	boolean foiCumprida() { return cumpridaPor != null; }

	/** Quem levou a carta, ou null se ela ainda está em jogo. */
	Jogador getCumpridaPor() { return cumpridaPor; }

	TipoCondicao getCondicao() { return condicao; }

	int getQuantidadeExigida() { return quantidadeExigida; }
}
