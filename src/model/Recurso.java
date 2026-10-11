package model;

final public class Recurso {
	
//	int voto ;
//	int espada;
//	int maozinha;
//	int cavalo;
//	int cabecaoDoKhan;
//	int fichaDeYurt ; // nao eh o yurt   
//	int moeda ; 
//	int graos ; 
//	int tecido ; 
//	int ferro ;
//	int pele;
//	int vaca;
	private int[] v = new int [12];
	
	public Recurso () {
		for (int i = 0 ; i < 12 ; i++) 
			v[i]=0;
		
	}
	int getItem (int i ) {
		if (i>=0 && i< 12)
			return this.v[i];
		System.out.println("Não faz sentido esse numero ");
		return -1;
	}
	void setItem (int i , int n) {
		this.v[i]=n;
	}
	
	
}
