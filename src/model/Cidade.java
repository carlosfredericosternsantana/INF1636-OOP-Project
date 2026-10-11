package model;

import java.util.Map;
import model.Recurso;

class Cidade extends Coletavel {
	
	public Cidade () {
		super();
		this.r.setItem(1, -1);
	}
	public Cidade (Recurso r) {
		super(r);
		this.r.setItem(1, -1);
	}
}
