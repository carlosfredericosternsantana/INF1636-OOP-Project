package model;

class Cidade extends Coletavel {

	static final int PRIMEIRO_TESOURO = 7;  // graos
	static final int ULTIMO_TESOURO = 11;   // vaca

	private int dono = -1;                  // ninguém

	Cidade(Recurso tesouros) {
		super();
		for (int i = PRIMEIRO_TESOURO; i <= ULTIMO_TESOURO; i++)
			this.r.setItem(i, tesouros.getItem(i));
		this.r.setItem(1, -1);
	}

	int getTesouro(int tipo) {
		if (tipo < PRIMEIRO_TESOURO || tipo > ULTIMO_TESOURO) return 0;
		return r.getItem(tipo);
	}

	int totalDeTesouros() {
		int total = 0;
		for (int i = PRIMEIRO_TESOURO; i <= ULTIMO_TESOURO; i++) total += r.getItem(i);
		return total;
	}

	boolean retirarTesouro(int tipo) {
		if (getTesouro(tipo) <= 0) return false;
		r.setItem(tipo, r.getItem(tipo) - 1);
		return true;
	}

	int getDono() { return dono; }
	boolean foiDominada() { return dono != -1; }

	boolean dominar(int jogador) {
		if (foiDominada() || jogador < 0) return false;
		dono = jogador;
		return true;
	}
	
	boolean colocarTesouro(int tipo) {
		if (tipo < PRIMEIRO_TESOURO || tipo > ULTIMO_TESOURO) return false;
		r.setItem(tipo, r.getItem(tipo) + 1);
		return true;
	}
}