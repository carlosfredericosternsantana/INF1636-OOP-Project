package model;
import model.Recurso;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class Jogador {

	protected ArrayList<Integer> pos ;
	protected int [] yurtsPorRegiao = new int[3];                    
	protected int pecasDeBonus;                     
	protected int cidadesConquistadas;    
	protected Recurso r ; // inventario do player
	protected Recurso [] colunas = new Recurso[4] ; // possiveis escolhas de acoes da tabela 
	protected int [] melhoriasPorColuna = new int[4];
	
	Jogador () {
		pos = new ArrayList<Integer>();
		for (int i  = 0 ; i < 3 ;i++)
			this.yurtsPorRegiao[i]=0;
		this.pecasDeBonus = 0 ;
		this.cidadesConquistadas = 0;
		this.r = new Recurso();
		for (int i = 0 ; i < 4 ; i++)
			colunas[i] = new Recurso();
	}
	
	public int getYurtsPorRegiao (int i) {
		if  (i>=0 && i<=2) 
			return this.yurtsPorRegiao[i];
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
			temp.setItem(j, this.colunas[i].getItem(j));
		return temp;
	}
	
	public int getMelhoriasNaColuna (int i) {
		if (i>=0 && i<4)
			return this.melhoriasPorColuna[i];
		return -1;
	}
	
}