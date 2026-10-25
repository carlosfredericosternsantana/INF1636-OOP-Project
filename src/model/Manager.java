package model;

import java.util.ArrayDeque;
import java.util.ArrayList;

// Estado de uma partida: o mapa, a fila de turnos e o deslocamento do piao da vez.
class Manager {

	static final int NENHUMA_COLUNA = -1;
	static final int MAX_JOGADORES = 5;

	private final Mapa mapa;
	private final ArrayList<Jogador> jogadores = new ArrayList<Jogador>();
	private int vez = 0;
	private int movimentos = 0;
	private int colunaAtivada = NENHUMA_COLUNA;

	Manager (int quantidadeDeJogadores) {
		if (quantidadeDeJogadores < 2 || quantidadeDeJogadores > MAX_JOGADORES)
			throw new IllegalArgumentException("a partida tem de 2 a " + MAX_JOGADORES + " jogadores");
		this.mapa = new Mapa();
		for (int i = 0; i < quantidadeDeJogadores; i++)
			jogadores.add(new Jogador(i, i));
	}

	Mapa getMapa () { return mapa; }

	int getQuantidadeDeJogadores () { return jogadores.size(); }

	Jogador getJogador (int indice) { return jogadores.get(indice); }

	Jogador getJogadorDaVez () { return jogadores.get(vez); }

	int getVez () { return vez; }

	int getMovimentos () { return movimentos; }

	int getColunaAtivada () { return colunaAtivada; }

	// O jogador coloca uma peca de ativacao numa coluna: os cavalos dela viram movimentos.
	boolean ativarColuna (int coluna) {
		if (colunaAtivada != NENHUMA_COLUNA) return false;
		Jogador jogador = getJogadorDaVez();
		if (!jogador.ativarColuna(coluna)) return false;
		colunaAtivada = coluna;
		movimentos = jogador.getCavalosDaColuna(coluna);
		return true;
	}

	// Usado apenas para testar situacoes especificas, no lugar da coluna da cartela.
	void definirMovimentos (int quantidade) {
		if (quantidade < 0) return;
		movimentos = quantidade;
	}

	void passarVez () {
		vez = (vez + 1) % jogadores.size();
		movimentos = 0;
		colunaAtivada = NENHUMA_COLUNA;
	}

	ArrayList<Integer> paradasAlcancaveis () {
		ArrayList<Integer> alcancaveis = new ArrayList<Integer>();
		if (movimentos <= 0) return alcancaveis;
		Jogador jogador = getJogadorDaVez();
		int[] distancia = distancias(jogador);
		for (int parada = 0; parada < Mapa.NUM_PARADAS; parada++)
			if (distancia[parada] <= movimentos && podeParar(jogador, parada))
				alcancaveis.add(parada);
		return alcancaveis;
	}

	boolean mover (int destino) {
		if (movimentos <= 0) return false;
		if (destino < 0 || destino >= Mapa.NUM_PARADAS) return false;
		Jogador jogador = getJogadorDaVez();
		if (!podeParar(jogador, destino)) return false;
		int gasto = distancias(jogador)[destino];
		if (gasto > movimentos) return false;
		movimentos -= gasto;
		jogador.setParada(destino);
		return true;
	}

	// Quantos movimentos custa chegar a cada parada. Sair de uma parada onde o
	// proprio jogador tem um yurt nao custa movimento: e o pulo que o yurt permite.
	private int[] distancias (Jogador jogador) {
		int[] distancia = new int[Mapa.NUM_PARADAS];
		for (int parada = 0; parada < distancia.length; parada++)
			distancia[parada] = Integer.MAX_VALUE;
		int origem = jogador.getParada();
		distancia[origem] = 0;
		ArrayDeque<Integer> fila = new ArrayDeque<Integer>();
		fila.addLast(origem);
		while (!fila.isEmpty()) {
			int atual = fila.pollFirst();
			int custo = (atual != origem && mapa.yurtsEm(atual).contains(jogador.getIndice())) ? 0 : 1;
			for (int vizinho : mapa.adj(atual)) {
				if (distancia[atual] + custo >= distancia[vizinho]) continue;
				distancia[vizinho] = distancia[atual] + custo;
				if (custo == 0) fila.addFirst(vizinho);
				else fila.addLast(vizinho);
			}
		}
		return distancia;
	}

	// Da para passar por uma parada ocupada, mas so da para parar nela se couber
	// mais um piao: 1 nas paradas simples, 2 nas duplas e todos em Karakorum.
	private boolean podeParar (Jogador jogador, int parada) {
		if (parada == jogador.getParada()) return false;
		int ocupantes = 0;
		for (Jogador outro : jogadores)
			if (outro != jogador && outro.getParada() == parada) ocupantes++;
		return ocupantes < mapa.capacidadeDePioes(parada);
	}
}
