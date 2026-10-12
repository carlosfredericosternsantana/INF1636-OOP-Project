package model;
import model.Recurso;

import java.util.ArrayList;

class Jogador {

	protected ArrayList<Integer> pos ;
	protected ArrayList<Integer> yurtsPorRegiao = new ArrayList<Integer>();                    
	protected int pecasDeBonus;                     
	protected int cidadesConquistadas;    
	protected Recurso r ; // inventario do player
	protected ArrayList<Recurso> colunas = new ArrayList<Recurso>(); // possiveis escolhas de acoes da tabela 
	protected ArrayList<Integer> melhoriasPorColuna = new ArrayList<Integer>();
	protected int indice;
	protected int parada = Mapa.KARAKORUM;
	
	Jogador () {
		this(0);
	}
	
	Jogador (int indice) {
		this.indice = indice;
		pos = new ArrayList<Integer>();
		for (int i  = 0 ; i < 3 ;i++)
			this.yurtsPorRegiao.add(0);
		this.pecasDeBonus = 0 ;
		this.cidadesConquistadas = 0;
		this.r = new Recurso();
		for (int i = 0 ; i < 4 ; i++) {
			colunas.add(new Recurso());
			melhoriasPorColuna.add(0);
		}
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