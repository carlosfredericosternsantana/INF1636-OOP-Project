package model;

import java.util.ArrayList;

class Melhorias {

	static final int NUM_COLUNAS = 4;
	static final int COMPRADA = -1;
	static final int EMBARALHADA = 0;
	static final int TAMANHO_TRILHA = 5;
	static final int LINHAS = 4;            // espacos de melhoria em cada coluna da cartela
	static final int SEM_TIPO = -1;

	private final int tipo;                 // recurso que a melhoria da (Mapa.MOEDA, Mapa.CAVALO...)
	private final int cor;
	private final Recurso bonus;
	private int posicao = EMBARALHADA;
	private int coluna = -1;                // coluna da cartela onde foi colocada

	Melhorias(int cor, Recurso bonus) {
		this(SEM_TIPO, cor, bonus);
	}

	Melhorias(int tipo, int cor) {
		this(tipo, cor, bonusDe(tipo));
	}

	private Melhorias(int tipo, int cor, Recurso bonus) {
		if (cor < 0 || cor >= NUM_COLUNAS)
			throw new IllegalArgumentException("cor deve estar entre 0 e " + (NUM_COLUNAS - 1));
		this.tipo = tipo;
		this.cor = cor;
		this.bonus = new Recurso();
		for (int i = 0; i < 12; i++)
			this.bonus.setItem(i, bonus.getItem(i));
	}

	private static Recurso bonusDe(int tipo) {
		Recurso r = new Recurso();
		r.setItem(tipo, tipo == Mapa.CAVALO ? 2 : 1);
		return r;
	}

	// As 44 pecas do manual. A cor de cada tipo reveza vermelha, verde e amarela;
	// os tesouros virtuais tem a cor da imagem da peca.
	static ArrayList<Melhorias> criarPilha() {
		ArrayList<Melhorias> pilha = new ArrayList<Melhorias>();
		adicionar(pilha, Mapa.MOEDA, 8);
		adicionar(pilha, Mapa.ESPADA, 8);
		adicionar(pilha, Mapa.YURT, 8);
		adicionar(pilha, Mapa.MAOZINHA, 6);
		adicionar(pilha, Mapa.CAVALO, 9);
		pilha.add(new Melhorias(Mapa.GRAOS, Jogador.AMARELA));
		pilha.add(new Melhorias(Mapa.TECIDO, Jogador.VERDE));
		pilha.add(new Melhorias(Mapa.FERRO, Jogador.VERMELHA));
		pilha.add(new Melhorias(Mapa.PELE, Jogador.VERDE));
		pilha.add(new Melhorias(Mapa.VACA, Jogador.AMARELA));
		return pilha;
	}

	private static void adicionar(ArrayList<Melhorias> pilha, int tipo, int quantidade) {
		for (int i = 0; i < quantidade; i++)
			pilha.add(new Melhorias(tipo, i % 3));
	}

	int getTipo() { return tipo; }

	int getCor() { return cor; }

	int getPosicao() { return posicao; }

	int getColuna() { return coluna; }

	boolean estaNaTrilha() { return posicao >= 1; }

	boolean foiComprada() { return posicao == COMPRADA; }

	Recurso getBonus() {
		Recurso temp = new Recurso();
		for (int i = 0; i < 12; i++)
			temp.setItem(i, bonus.getItem(i));
		return temp;
	}

	boolean setPosicao(int posicao) {
		if (posicao < COMPRADA || posicao > TAMANHO_TRILHA) return false;
		this.posicao = posicao;
		return true;
	}

	boolean aplicar(Jogador jogador) {
		return aplicar(jogador, cor);
	}

	// A coluna branca aceita melhoria de qualquer cor.
	boolean aplicar(Jogador jogador, int coluna) {
		if (jogador == null || !estaNaTrilha()) return false;
		Recurso destino = jogador.colunas.get(coluna);
		for (int i = 0; i < 12; i++)
			destino.setItem(i, destino.getItem(i) + bonus.getItem(i));
		jogador.melhoriasPorColuna.set(coluna, jogador.melhoriasPorColuna.get(coluna) + 1);
		jogador.melhoriasNaCartela.get(coluna).add(this);
		posicao = COMPRADA;
		this.coluna = coluna;
		return true;
	}
}
