package model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Carta de conselheiro aberto (manual, pagina 18).
 *
 * São 14 cartas, das quais 4 entram em jogo. Cada uma dá 1 voto no Kurultai ao
 * PRIMEIRO jogador que cumprir a sua demanda; feito isso a carta sai de jogo e não é
 * reposta. As cartas de conselheiro SECRETO não estão aqui. elas pertencem a um
 * jogador só e são contadas no fim da partida, então ficam no Jogador.
 *

 */
class Quest {

	/** O que cada carta mede. Cada valor tem um getter correspondente no Jogador. */
	enum TipoCondicao {
		ESPADAS,                     // primeiro a ter 4 espadas
		YURTS_NO_MAPA,               // 6 yurts no mapa; 10 yurts no mapa
		TESOUROS,                    // 4 tesouros
		TESOUROS_DIFERENTES,         // 5 tesouros diferentes
		YURTS_NA_CHINA,              // as duas cartas de região combinam os três
		YURTS_NA_RUSSIA,             // valores de região numa demanda só:
		YURTS_NA_PERSIA,             // "1 yurt em cada região", "2 yurts em cada região"
		PECAS_DE_BONUS,              // 2 peças de bônus; 3 peças de bônus
		CIDADES_CONQUISTADAS,        // conquistar 2 cidades; conquistar 3 cidades
		LINHAS_DE_MELHORIA,          // completar uma linha de melhorias
		COLUNAS_COM_DUAS_MELHORIAS,  // 2 colunas com 2 melhorias cada
		MELHORIAS_NA_MESMA_COLUNA    // 4 melhorias em uma só coluna
	}

	/** Toda carta de conselheiro aberto vale exatamente 1 voto. */
	static final int VOTOS = 1;

	private final Map<TipoCondicao, Integer> demanda;
	private Jogador cumpridaPor;

	/** Carta de exigência única, que é o caso de 12 das 14 cartas. */
	Quest(TipoCondicao condicao, int quantidadeExigida) {
		this(Map.of(Objects.requireNonNull(condicao, "condição obrigatória"), quantidadeExigida));
	}

	/** Carta de exigência composta: tem de cumprir todas as entradas do mapa. */
	Quest(Map<TipoCondicao, Integer> demanda) {
		if (demanda == null || demanda.isEmpty())
			throw new IllegalArgumentException("carta sem demanda");
		for (Integer quantidade : demanda.values())
			if (quantidade == null || quantidade <= 0)
				throw new IllegalArgumentException("a demanda tem de ser positiva");
		this.demanda = new HashMap<>(demanda); // cópia: a carta não muda depois de criada
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
		for (Map.Entry<TipoCondicao, Integer> exigencia : demanda.entrySet())
			if (progressoDe(jogador, exigencia.getKey()) < exigencia.getValue()) return false;
		cumpridaPor = jogador;
		return true;
	}

	/**
	 * O jogador mantém esses contadores em dia conforme joga,
	 * de modo que checar uma carta é só uma comparação  nada é recalculado aqui
	 */
	private int progressoDe(Jogador jogador, TipoCondicao condicao) {
		return switch (condicao) {
			case ESPADAS                    -> jogador.getEspadas();
			case YURTS_NO_MAPA              -> jogador.getYurtsNoMapa();
			case TESOUROS                   -> jogador.getTesouros();
			case TESOUROS_DIFERENTES        -> jogador.getTiposDeTesouroDiferentes();
			case YURTS_NA_CHINA             -> jogador.getYurtsNaChina();
			case YURTS_NA_RUSSIA            -> jogador.getYurtsNaRussia();
			case YURTS_NA_PERSIA            -> jogador.getYurtsNaPersia();
			case PECAS_DE_BONUS             -> jogador.getPecasDeBonus();
			case CIDADES_CONQUISTADAS       -> jogador.getCidadesConquistadas();
			case LINHAS_DE_MELHORIA         -> jogador.getLinhasDeMelhoriaCompletas();
			case COLUNAS_COM_DUAS_MELHORIAS -> jogador.getColunasComDuasMelhorias();
			case MELHORIAS_NA_MESMA_COLUNA  -> jogador.getMaiorNumeroDeMelhoriasNumaColuna();
		};
	}

	boolean foiCumprida() { return cumpridaPor != null; }


	Jogador getCumpridaPor() { return cumpridaPor; }

	Map<TipoCondicao, Integer> getDemanda() { return Map.copyOf(demanda); }

	/** Quanto esta carta exige da condição dada; 0 se ela não exige nada disso. */
	int getQuantidadeExigida(TipoCondicao condicao) { return demanda.getOrDefault(condicao, 0); }
}
