package model;

import model.Recurso;

abstract class Coletavel {
	Recurso r ; 
	
	public Coletavel () {
		this.r = new Recurso ();

	}
	public Coletavel (Recurso r) {
		this.r = r;

	}
	
	public Recurso getRecurso () {
		Recurso temp = new Recurso();
		for (int i  = 0 ; i <12 ;i++)
			temp.setItem(i,this.r.getItem(i));
		return temp;
	}




}
