package model;
import model.Recurso;

import java.util.ArrayList;

class Jogador {

	static final int VERMELHA = 0, VERDE = 1, AMARELA = 2, BRANCA = 3;
	static final int SEM_HERDEIRO = -1;
	static final int YURTS_POR_JOGADOR = 12;

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
	protected int yurtsRestantes = YURTS_POR_JOGADOR;
	protected int melhoriasGratis = 0;      // ganhas ao completar linhas, vao para a coluna branca
	protected int conselheiroSecreto = -1;
	protected ArrayList<Integer> bonusRecebidos = new ArrayList<Integer>();
	protected ArrayList<ArrayList<Melhorias>> melhoriasNaCartela = new ArrayList<ArrayList<Melhorias>>();
	
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
			melhoriasNaCartela.add(new ArrayList<Melhorias>());
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

	// Quantas linhas completas ha nas 3 colunas coloridas (a branca nao conta).
	int linhasCompletas () {
		int menor = Melhorias.LINHAS;
		for (int c = VERMELHA ; c <= AMARELA ; c++)
			menor = Math.min(menor, melhoriasPorColuna.get(c));
		return menor;
	}

	boolean podeColocarYurt (Mapa mapa, int parada) {
		if (yurtsRestantes < 1 || parada < 0 || parada >= Mapa.NUM_PARADAS) return false;
		return mapa.yurtsEm(parada).size() < mapa.capacidadeDeYurts(parada);
	}

	// So coloca a peca: quem cobra a ficha de yurt (real ou virtual) e o Manager.
	boolean colocarYurt (Mapa mapa, int parada) {
		if (!podeColocarYurt(mapa, parada)) return false;
		mapa.yurtsEm(parada).add(this.indice);
		yurtsRestantes--;
		int regiao = mapa.getRegiao(parada);
		yurtsPorRegiao.set(regiao, yurtsPorRegiao.get(regiao) + 1);
		return true;
	}

	// O yurt no centro da cidade conta para a regiao, mas nao ajuda no movimento.
	void conquistarCidade (int regiao) {
		cidadesConquistadas++;
		if (yurtsRestantes < 1) return;
		yurtsRestantes--;
		yurtsPorRegiao.set(regiao, yurtsPorRegiao.get(regiao) + 1);
	}

	void receberBonus (int bonus) {
		pecasDeBonus++;
		bonusRecebidos.add(bonus);
	}

	ArrayList<Integer> getBonusRecebidos () {
		return new ArrayList<Integer>(bonusRecebidos);
	}

	int getYurtsRestantes () { return yurtsRestantes; }

	int getConselheiroSecreto () { return conselheiroSecreto; }

	void setConselheiroSecreto (int carta) { conselheiroSecreto = carta; }

	int getIndiceDoHerdeiro () { return herdeiro; }

	int getTesouros () {
		int total = 0;
		for (int i = Mapa.GRAOS ; i <= Mapa.VACA ; i++) total += r.getItem(i);
		return total;
	}
}
