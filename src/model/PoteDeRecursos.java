package model;

import java.util.Map;

class PoteDeRecursos extends Coletavel {

	public PoteDeRecursos () {
		super();
		this.r.setItem(2, -1);
	}
	public PoteDeRecursos (Recurso r) {
		super(r);
		this.r.setItem(2, -1);
	}
	
}
