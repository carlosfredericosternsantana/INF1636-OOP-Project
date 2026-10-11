package model;

import model.Recurso;

abstract class Coletavel {
	Recurso r ; 
	
	public Coletavel () {
		this.r = new Recurso ();

	}
	
	
	public Recurso getRecurso () {
		Recurso temp = new Recurso();
		temp.cavalo = r.getCavalo();
		temp.maozinha = r.getMaozinha();
		temp.voto = r.getVoto();
		temp.espada = r.getEspada();
		temp.cabecaoDoKhan = r.getCabecaoDoKhan();
		temp.fichaDeYurt = r.getFichaDeYurt();
		temp.moeda = r.getMoeda();
		
		
		
		
		temp.graos = r.getGraos();
		temp.tecido = r.getTecido();
		temp.ferro = r.getFerro();
		temp.pele = r.getPele();
		temp.vaca = r.getVaca();
		
		
		return temp;
	}

	public void setRecurso (Recurso r) {
		this.r = r;
	}



}
