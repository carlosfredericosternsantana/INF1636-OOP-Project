package model;
import model.Recurso;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

class Jogador {

	protected ArrayList<Integer> pos ;
	protected ArrayList<Integer> yurtsPorRegiao = new ArrayList<Integer>();                    
	protected int pecasDeBonus;                     
	protected int cidadesConquistadas;    
	protected Recurso r ; // inventario do player
	protected ArrayList<Recurso> colunas = new ArrayList<Recurso>(); // possiveis escolhas de acoes da tabela 
	protected ArrayList<Integer> melhoriasPorColuna = new ArrayList<Integer>();
	
	Jogador () {
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
	
}