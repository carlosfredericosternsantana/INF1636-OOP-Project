package model;

class Conselheiro extends Coletavel {

	private final int pontuacao;
	private final int quaisquer;
	private boolean primeiro = false;   // vira true na 1 entrega

	Conselheiro(Recurso troca, int quaisquer, int pontuacao) {
		super();
		for (int i = 0; i < 12; i++)
			this.r.setItem(i, -Math.abs(troca.getItem(i)));
		this.quaisquer = quaisquer;
		this.pontuacao = pontuacao;
	}
	int getQuaisquer() { return quaisquer; }
	int getPedido(int i) { return -r.getItem(i); }
	int getPontuacao() { return pontuacao; }
	boolean isPrimeiro() { return primeiro; }

	boolean registrarEntrega() {
		if (primeiro) return false;
		primeiro = true;
		return true;
	}
}