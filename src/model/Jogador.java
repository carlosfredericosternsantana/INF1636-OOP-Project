package model;
import model.Recurso;

import java.util.ArrayList;

class Jogador {

	static final int VERMELHA = 0, VERDE = 1, AMARELA = 2, BRANCA = 3;
	static final int SEM_HERDEIRO = -1;

	static final String[] HERDEIROS = { "Altani", "Jochi", "Chagatai", "Tolui", "Ogedei" };

	// acoes impressas na base de cada coluna da cartela, uma linha por herdeiro.
	// a ordem das colunas e a do tabuleiro de jogador: vermelha, verde, amarela, branca.
	private static final int[][] CAVALOS   = { {1,2,1,2}, {1,1,2,2}, {1,1,2,2}, {1,2,1,2}, {2,1,1,2} };
	private static final int[][] MAOZINHAS = { {2,1,1,0}, {1,2,1,0}, {2,1,1,0}, {1,1,2,0}, {1,1,2,0} };
	private static final int[][] KHANS     = { {1,0,0,1}, {0,1,0,1}, {1,0,0,1}, {0,0,1,1}, {0,0,1,1} };
	private static final int[][] MOEDAS    = { {0,0,1,0}, {1,0,0,0}, {0,1,0,0}, {1,0,0,0}, {0,1,0,0} };

	protected ArrayList<Integer> pos ;
	protected ArrayList<Integer> yurtsPorRegiao = new ArrayList<Integer>();                    
	protected int pecasDeBonus;                     
	protected int cidadesConquistadas;    
	protected Recurso r ; // inventario do player
	protected ArrayList<Recurso> colunas = new ArrayList<Recurso>(); // possiveis escolhas de acoes da tabela 
	protected ArrayList<Integer> melhoriasPorColuna = new ArrayList<Integer>();
	protected ArrayList<Boolean> colunasUsadas = new ArrayList<Boolean>();
	protected int indice;
	protected int herdeiro;
	protected int parada = Mapa.KARAKORUM;
	
	Jogador () {
		this(0);
	}
	
	Jogador (int indice) {
		this(indice, SEM_HERDEIRO);
	}
	
	Jogador (int indice, int herdeiro) {
		this.indice = indice;
		this.herdeiro = herdeiro;
		pos = new ArrayList<Integer>();
		for (int i  = 0 ; i < 3 ;i++)
			this.yurtsPorRegiao.add(0);
		this.pecasDeBonus = 0 ;
		this.cidadesConquistadas = 0;
		this.r = new Recurso();
		for (int i = 0 ; i < 4 ; i++) {
			colunas.add(new Recurso());
			melhoriasPorColuna.add(0);
			colunasUsadas.add(false);
		}
		if (herdeiro >= 0 && herdeiro < HERDEIROS.length)
			for (int i = 0 ; i < 4 ; i++) {
				Recurso coluna = colunas.get(i);
				coluna.setItem(Mapa.CAVALO, CAVALOS[herdeiro][i]);
				coluna.setItem(Mapa.MAOZINHA, MAOZINHAS[herdeiro][i]);
				coluna.setItem(Mapa.CABECA_DO_KHAN, KHANS[herdeiro][i]);
				coluna.setItem(Mapa.MOEDA, MOEDAS[herdeiro][i]);
			}
	}
	
	String getHerdeiro () {
		if (herdeiro < 0 || herdeiro >= HERDEIROS.length) return "Jogador " + (indice + 1);
		return HERDEIROS[herdeiro];
	}
	
	int getCavalosDaColuna (int coluna) {
		if (coluna < 0 || coluna >= Melhorias.NUM_COLUNAS) return 0;
		return colunas.get(coluna).getItem(Mapa.CAVALO);
	}
	
	boolean colunaDisponivel (int coluna) {
		if (coluna < 0 || coluna >= Melhorias.NUM_COLUNAS) return false;
		return !colunasUsadas.get(coluna);
	}
	
	// a peca de ativacao fica na coluna ate ser recuperada. a coluna branca
	// devolve todas as pecas, inclusive a que acabou de ser usada.
	boolean ativarColuna (int coluna) {
		if (!colunaDisponivel(coluna)) return false;
		colunasUsadas.set(coluna, true);
		if (coluna == BRANCA)
			for (int i = 0 ; i < Melhorias.NUM_COLUNAS ; i++) colunasUsadas.set(i, false);
		return true;
	}
	
	public int getYurtsPorRegiao (int i) {
		if  (i>=0 && i<=2) 
			return this.yurtsPorRegiao.get(i);
		System.out.println("Número incompatível com as regiões");
		return -1;
	}
	
	public int getPecasDeBonus () {
		return this.pecasDeBonus;
	}
	
	public int getCidadesConquistadas () {
		return this.cidadesConquistadas;
	}
	
	public Recurso getRecurso () {
		Recurso temp = new Recurso();
		for (int i = 0 ;i < 12 ;i++)
			temp.setItem(i, this.r.getItem(i));;
		return temp;
	}

	public void setPecasDeBonus(int pecasDeBonus) {
		this.pecasDeBonus = pecasDeBonus;
	}

	public void setCidadesConquistadas(int cidadesConquistadas) {
		this.cidadesConquistadas = cidadesConquistadas;
	}

	public void setInventário (Recurso r) {
		this.r = r;
	}
	
	public Recurso getColunas (int i) {
		Recurso temp = new Recurso();
		for (int j = 0 ;j < 12 ;j++)
			temp.setItem(j, this.colunas.get(i).getItem(j));
		return temp;
	}
	
	public int getMelhoriasNaColuna (int i) {
		if (i>=0 && i<4)
			return this.melhoriasPorColuna.get(i);
		return -1;
	}

	int getIndice () {
		return this.indice;
	}

	int getParada () {
		return this.parada;
	}

	void setParada (int parada) {
		this.parada = parada;
	}

	protected boolean alcancaParaYurt (Mapa mapa, int destino) {
		return destino == this.parada;
	}

	boolean construirYurt (Mapa mapa) {
		return construirYurt(mapa, this.parada);
	}

	boolean construirYurt (Mapa mapa, int destino) {
		if (mapa == null || destino < 0 || destino >= Mapa.NUM_PARADAS) return false;
		if (!alcancaParaYurt(mapa, destino)) return false;
		if (r.getItem(Mapa.YURT) < 1) return false;
		ArrayList<Integer> yurts = mapa.yurtsEm(destino);
		if (yurts.size() >= mapa.capacidadeDeYurts(destino)) return false;
		r.setItem(Mapa.YURT, r.getItem(Mapa.YURT) - 1);
		yurts.add(this.indice);
		int regiao = mapa.getRegiao(destino);
		yurtsPorRegiao.set(regiao, yurtsPorRegiao.get(regiao) + 1);
		return true;
	}

	boolean pegarTributo (Mapa mapa, int idPote) {
		if (mapa == null || idPote < 0 || idPote >= mapa.coletaveis.size()) return false;
		if (!(mapa.coletaveis.get(idPote) instanceof PoteDeRecursos)) return false;
		PoteDeRecursos pote = (PoteDeRecursos) mapa.coletaveis.get(idPote);
		if (r.getItem(Mapa.MAOZINHA) < 1) return false;
		if (!mapa.alcanca(this.parada, idPote)) return false;
		if (!pote.retirar()) return false;
		r.setItem(Mapa.MAOZINHA, r.getItem(Mapa.MAOZINHA) - 1);
		r.setItem(pote.getTipo(), r.getItem(pote.getTipo()) + 1);
		return true;
	}

	boolean entregarAoConselheiro (Mapa mapa, int idConselheiro) {
		if (mapa == null || idConselheiro < 0 || idConselheiro >= mapa.coletaveis.size()) return false;
		if (!(mapa.coletaveis.get(idConselheiro) instanceof Conselheiro)) return false;
		Conselheiro conselheiro = (Conselheiro) mapa.coletaveis.get(idConselheiro);
		if (!mapa.alcanca(this.parada, idConselheiro)) return false;
		int sobra = 0;
		for (int i = 0; i < 12; i++) {
			if (r.getItem(i) < conselheiro.getPedido(i)) return false;
			if (i >= Mapa.GRAOS && i <= Mapa.VACA) sobra += r.getItem(i) - conselheiro.getPedido(i);
		}
		if (sobra < conselheiro.getQuaisquer()) return false;
		for (int i = 0; i < 12; i++)
			r.setItem(i, r.getItem(i) - conselheiro.getPedido(i));
		int faltam = conselheiro.getQuaisquer();
		for (int i = Mapa.GRAOS; i <= Mapa.VACA && faltam > 0; i++) {
			int gasto = Math.min(faltam, r.getItem(i));
			r.setItem(i, r.getItem(i) - gasto);
			faltam -= gasto;
		}
		r.setItem(Mapa.VOTO, r.getItem(Mapa.VOTO) + conselheiro.getPontuacao());
		conselheiro.registrarEntrega();
		return true;
	}
	
}