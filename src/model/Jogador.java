package model;
import model.Recurso;
import java.util.Map;
import java.util.Objects;

public class Jogador {

	                           
	int [] yurtsPorRegiao = new int[3];                    
	int pecasDeBonus;                     
	int cidadesConquistadas;    
	Recurso r ;
	
//	int linhasDeMelhoriaCompletas;       
//	int colunasComDuasMelhorias;          
//	int maiorNumeroDeMelhoriasNumaColuna; 
	
	
	
	Jogador () {
		for (int i  = 0 ; i < 3 ;i++)
			this.yurtsPorRegiao[i]=0;
		this.pecasDeBonus = 0 ;
		this.cidadesConquistadas = 0;
		this.r = new Recurso();
		
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
			temp.v[i] = this.r.getItem(i);
		return temp;
	}

	
	public void setPecasDeBonus(int pecasDeBonus) {
		this.pecasDeBonus = pecasDeBonus;
	}

	
	public void setCidadesConquistadas(int cidadesConquistadas) {
		this.cidadesConquistadas = cidadesConquistadas;
	}

	
	public void setRecurso (Recurso r) {
		this.r = r;
	}
	
}