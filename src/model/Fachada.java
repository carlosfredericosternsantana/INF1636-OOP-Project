package model;

import java.util.ArrayList;

// Unica porta de entrada do model: a view e o controller so enxergam esta classe.
public class Fachada {

	public static final int NUM_PARADAS = Mapa.NUM_PARADAS;
	public static final int KARAKORUM = Mapa.KARAKORUM;
	public static final int NUM_COLUNAS = Melhorias.NUM_COLUNAS;
	public static final int VERMELHA = Jogador.VERMELHA;
	public static final int VERDE = Jogador.VERDE;
	public static final int AMARELA = Jogador.AMARELA;
	public static final int BRANCA = Jogador.BRANCA;
	public static final int NENHUMA_COLUNA = Manager.NENHUMA_COLUNA;

	private static Fachada instancia;

	private Manager manager;

	private Fachada () {}

	public static Fachada getInstancia () {
		if (instancia == null) instancia = new Fachada();
		return instancia;
	}

	public void novaPartida (int quantidadeDeJogadores) {
		manager = new Manager(quantidadeDeJogadores);
	}

	public int getQuantidadeDeJogadores () { return manager.getQuantidadeDeJogadores(); }

	public int getVez () { return manager.getVez(); }

	public String getHerdeiro (int jogador) { return manager.getJogador(jogador).getHerdeiro(); }

	public int getParada (int jogador) { return manager.getJogador(jogador).getParada(); }

	public int getMovimentos () { return manager.getMovimentos(); }

	public int getColunaAtivada () { return manager.getColunaAtivada(); }

	public boolean colunaDisponivel (int coluna) {
		return manager.getJogadorDaVez().colunaDisponivel(coluna);
	}

	public int getCavalosDaColuna (int coluna) {
		return manager.getJogadorDaVez().getCavalosDaColuna(coluna);
	}

	public boolean ativarColuna (int coluna) { return manager.ativarColuna(coluna); }

	public void definirMovimentos (int quantidade) { manager.definirMovimentos(quantidade); }

	public ArrayList<Integer> getParadasAlcancaveis () { return manager.paradasAlcancaveis(); }

	public boolean mover (int destino) { return manager.mover(destino); }

	public void passarVez () { manager.passarVez(); }
}
