package model;

class PoteDeRecursos extends Coletavel {

	static final int MAX = 3;

	private final int tipo;   
	private int quantidade;   

	PoteDeRecursos(int tipo) {
		super();
		this.tipo = tipo;
		this.quantidade = 0;
		this.r.setItem(2, -1);
	}

	int getTipo() { return tipo; }
	int getQuantidade() { return quantidade; }

	boolean adicionar() {
		if (quantidade >= MAX) return false;
		quantidade++;
		return true;
	}

	int esvaziar() {
		int q = quantidade;
		quantidade = 0;
		return q;
	}
}