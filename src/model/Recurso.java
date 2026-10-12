package model;

import java.util.ArrayList;

final class Recurso {
	
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
	private ArrayList<Integer> v = new ArrayList<Integer>();
	
	public Recurso () {
		for (int i = 0 ; i < 12 ; i++) 
			v.add(0);
		
	}
	int getItem (int i ) {
		if (i>=0 && i< 12)
			return this.v.get(i);
		System.out.println("Não faz sentido esse numero ");
		return -1;
	}
	void setItem (int i , int n) {
		this.v.set(i, n);
	}
	
	
}
