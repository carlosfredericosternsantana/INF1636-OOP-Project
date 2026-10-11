package model;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import model.Coletavel;

class Mapa{
	protected ArrayList<HashMap<String,Object>> paradas;
	Mapa(){
		for(int i = 0; i<25; i++) {
			paradas.add(new HashMap<String,Object>());
			ArrayList<Integer> adj;
			ArrayList<Coletavel> coletaveis;
			paradas.get(i).put("yurt", -1);
		}
	}
}
