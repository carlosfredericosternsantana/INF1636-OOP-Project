package model;

class Melhorias {

	static final int NUM_COLUNAS = 4;
	static final int COMPRADA = -1;
	static final int EMBARALHADA = 0;
	static final int TAMANHO_TRILHA = 5;

	private final int cor;
	private final Recurso bonus;
	private int posicao = EMBARALHADA;

	Melhorias(int cor, Recurso bonus) {
		if (cor < 0 || cor >= NUM_COLUNAS)
			throw new IllegalArgumentException("cor deve estar entre 0 e " + (NUM_COLUNAS - 1));
		this.cor = cor;
		this.bonus = new Recurso();
		for (int i = 0; i < 12; i++)
			this.bonus.setItem(i, bonus.getItem(i));
	}

	int getCor() { return cor; }

	int getPosicao() { return posicao; }

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
		if (jogador == null || !estaNaTrilha()) return false;
		Recurso coluna = jogador.colunas[cor];
		for (int i = 0; i < 12; i++)
			coluna.setItem(i, coluna.getItem(i) + bonus.getItem(i));
		jogador.melhoriasPorColuna[cor]++;
		posicao = COMPRADA;
		return true;
	}
}
