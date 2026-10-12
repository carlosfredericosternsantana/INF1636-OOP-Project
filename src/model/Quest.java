package model;

import java.util.HashMap;

class Quest {

	enum TipoCondicao {
		ESPADAS,
		YURTS_NO_MAPA,
		TESOUROS,
		TESOUROS_DIFERENTES,
		YURTS_NA_CHINA,
		YURTS_NA_RUSSIA,
		YURTS_NA_PERSIA,
		PECAS_DE_BONUS,
		CIDADES_CONQUISTADAS,
		LINHAS_DE_MELHORIA,
		COLUNAS_COM_DUAS_MELHORIAS,
		MELHORIAS_NA_MESMA_COLUNA
	}

	static final int VOTOS = 1;

	private final HashMap<TipoCondicao, Integer> demanda;
	private int cumpridaPor = -1;

	Quest(TipoCondicao condicao, int quantidadeExigida) {
		this(umaCondicao(condicao, quantidadeExigida));
	}

	Quest(HashMap<TipoCondicao, Integer> demanda) {
		if (demanda == null || demanda.isEmpty())
			throw new IllegalArgumentException("carta sem demanda");
		for (Integer quantidade : demanda.values())
			if (quantidade == null || quantidade <= 0)
				throw new IllegalArgumentException("a demanda tem de ser positiva");
		this.demanda = new HashMap<TipoCondicao, Integer>(demanda);
	}

	private static HashMap<TipoCondicao, Integer> umaCondicao(TipoCondicao condicao, int quantidade) {
		if (condicao == null) throw new IllegalArgumentException("condicao obrigatoria");
		HashMap<TipoCondicao, Integer> d = new HashMap<TipoCondicao, Integer>();
		d.put(condicao, quantidade);
		return d;
	}

	boolean checarQuests(Jogador jogador, int indiceJogador) {
		if (jogador == null || indiceJogador < 0) return false;
		if (foiCumprida()) return false;
		for (TipoCondicao condicao : demanda.keySet())
			if (progressoDe(jogador, condicao) < demanda.get(condicao)) return false;
		cumpridaPor = indiceJogador;
		return true;
	}

	private int progressoDe(Jogador jogador, TipoCondicao condicao) {
		Recurso mao = jogador.getRecurso();
		switch (condicao) {
			case ESPADAS:                    return mao.getItem(Mapa.ESPADA);
			case YURTS_NO_MAPA:              return yurtsNoMapa(jogador);
			case TESOUROS:                   return totalDeTesouros(mao);
			case TESOUROS_DIFERENTES:        return tiposDeTesouro(mao);
			case YURTS_NA_CHINA:             return jogador.getYurtsPorRegiao(Mapa.CHINA);
			case YURTS_NA_RUSSIA:            return jogador.getYurtsPorRegiao(Mapa.RUSSIA);
			case YURTS_NA_PERSIA:            return jogador.getYurtsPorRegiao(Mapa.PERSIA);
			case PECAS_DE_BONUS:             return jogador.getPecasDeBonus();
			case CIDADES_CONQUISTADAS:       return jogador.getCidadesConquistadas();
			case LINHAS_DE_MELHORIA:         return linhasCompletas(jogador);
			case COLUNAS_COM_DUAS_MELHORIAS: return colunasCom(jogador, 2);
			case MELHORIAS_NA_MESMA_COLUNA:  return maiorColuna(jogador);
			default:                         return 0;
		}
	}

	private static int yurtsNoMapa(Jogador jogador) {
		int total = 0;
		for (int regiao = Mapa.CHINA; regiao <= Mapa.PERSIA; regiao++)
			total += jogador.getYurtsPorRegiao(regiao);
		return total;
	}

	private static int totalDeTesouros(Recurso mao) {
		int total = 0;
		for (int i = Mapa.GRAOS; i <= Mapa.VACA; i++) total += mao.getItem(i);
		return total;
	}

	private static int tiposDeTesouro(Recurso mao) {
		int tipos = 0;
		for (int i = Mapa.GRAOS; i <= Mapa.VACA; i++)
			if (mao.getItem(i) > 0) tipos++;
		return tipos;
	}

	private static int linhasCompletas(Jogador jogador) {
		int menor = jogador.getMelhoriasNaColuna(0);
		for (int c = 1; c < Melhorias.NUM_COLUNAS; c++)
			menor = Math.min(menor, jogador.getMelhoriasNaColuna(c));
		return menor;
	}

	private static int colunasCom(Jogador jogador, int minimo) {
		int colunas = 0;
		for (int c = 0; c < Melhorias.NUM_COLUNAS; c++)
			if (jogador.getMelhoriasNaColuna(c) >= minimo) colunas++;
		return colunas;
	}

	private static int maiorColuna(Jogador jogador) {
		int maior = 0;
		for (int c = 0; c < Melhorias.NUM_COLUNAS; c++)
			maior = Math.max(maior, jogador.getMelhoriasNaColuna(c));
		return maior;
	}

	boolean foiCumprida() { return cumpridaPor != -1; }

	int getCumpridaPor() { return cumpridaPor; }

	HashMap<TipoCondicao, Integer> getDemanda() { return new HashMap<TipoCondicao, Integer>(demanda); }

	int getQuantidadeExigida(TipoCondicao condicao) {
		Integer q = demanda.get(condicao);
		return q == null ? 0 : q;
	}
}
