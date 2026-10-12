package model;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.HashMap;
import model.Coletavel;
// Single 0 - 26, double 27 - 32, karakorum - 33
// Prov 12 9 cidades 6 conselheiros karakorum 1
class Mapa {
    static final int NUM_PARADAS = 34;
    static final int NUM_COL = 12;
    private static final int[][] ROTAS = {
        //Kiev
        {0,1}, {0,2}, {1,2},
        {1,27}, {1,3}, {2,32},
        //Moscou
        {3,4}, {3,5}, {4,5},
        {3,32}, {4,27}, {4,6}, {5,26},
        //Sarai
        {6,7}, {6,8}, {7,8},
        {6,33}, {8,28},
        {28,9}, {28,33},
        //Kaesong
        {9,10}, {9,11}, {10,11},
        {11,12}, {11,29},
        //Pequim
        {12,13}, {12,14}, {13,14},
        {12,33}, {13,15}, {14,20}, {14,30},
        {29,15},
        //Cantão
        {15,16}, {15,17}, {16,17},
        {17,30}, {30,18},
        //Cabul
        {18,19}, {18,20}, {19,20},
        {20,25}, {20,31},
        {31,21},
        //Bagdá
        {21,22}, {21,23}, {22,23},
        {23,24}, {23,32},
        //Samarcanda
        {24,25}, {24,26}, {25,26},
        {25,33}, {26,32}
    };
    protected ArrayList<HashMap<String,Object>> paradas;
    protected ArrayList<Coletavel> coletaveis;
    Mapa() {
        paradas = new ArrayList<HashMap<String,Object>>();
        for (int i = 0; i < NUM_PARADAS; i++) {
            HashMap<String,Object> pa = new HashMap<String,Object>();
            pa.put("yurt", -1);
            pa.put("adj", new ArrayList<Integer>());
            pa.put("coletaveis", new ArrayList<Coletavel>());
            paradas.add(pa);
        }
        for (int[] r : ROTAS) conectar(r[0], r[1]);
        
        coletaveis = new ArrayList<Coletavel>();
        for (int i = 0; i < NUM_COL; i++) {
        }
    }

    @SuppressWarnings("unchecked")
    ArrayList<Integer> adj(int parada) {
        return (ArrayList<Integer>) paradas.get(parada).get("adj");
    }

    private void conectar(int a, int b) {
        adj(a).add(b);
        adj(b).add(a);
    }
}
