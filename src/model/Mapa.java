package model;

import java.util.ArrayList;
import java.util.HashMap;
// Single 0 - 26, double 27 - 32, karakorum - 33
// Prov 12 9 cidades 6 conselheiros karakorum 1

class Mapa {
	static final int KARAKORUM = 33;
    static final int FORA_DO_MAPA = 0;
    static final int TRILHA_DE_MELHORIAS = 1;
    static final int NUM_PARADAS = 34;
    static final int NUM_COL = 28;
    static final int ESPADA = 1, YURT = 5, MOEDA = 6;
    static final int GRAOS = 7, TECIDO = 8, FERRO = 9, PELE = 10, VACA = 11;
    static final int CHINA = 0, RUSSIA = 1, PERSIA = 2;
    
    protected ArrayList<Coletavel> coletaveis;   // lista global
    protected ArrayList<ArrayList<Integer>> posicoesDoKhan;
    protected int posKhan = FORA_DO_MAPA;
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
    private static final int[][] POTES = {
        {YURT,   1, 2, 3, 32},
        {ESPADA, 1, 3, 4, 27},
        {MOEDA,  3, 5, 26, 32},
        {MOEDA,  6, 8, 28, 33},
        {ESPADA, 9, 11, 12, 28, 33},
        {YURT,   4, 5, 6, 25, 26, 33},
        {YURT,   13, 14, 15, 17, 30},
        {ESPADA, 14, 18, 20, 30},
        {MOEDA,  11, 12, 13, 15, 29},
        {MOEDA,  20, 21, 23, 24, 25, 31},
        {ESPADA, 23, 24, 26, 32},
        {YURT,   12, 14, 20, 25, 33}
    };
    private static final int[][] KHAN = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {9, 10, 11}
    };
    private static final int[][] CIDADES = {
        {0, 1, 2},
        {3, 4, 5},
        {6, 7, 8},
        {9, 10, 11},
        {12, 13, 14},
        {15, 16, 17},
        {18, 19, 20},
        {21, 22, 23},
        {24, 25, 26}
    };
    private static final int[][] CONSELHEIROS = {
        {32, 1,  0, 0, 0, 2, 0,  0},
        {27, 1,  0, 2, 0, 0, 0,  0},
        {28, 1,  0, 0, 2, 0, 0,  0},
        {29, 1,  0, 0, 0, 0, 2,  0},
        {30, 1,  0, 0, 0, 0, 0,  3},
        {31, 1,  2, 0, 0, 0, 0,  0},
        {KARAKORUM, 2,  0, 0, 1, 1, 1,  0}
    };
    protected ArrayList<HashMap<String,Object>> paradas;
    Mapa() {
        paradas = new ArrayList<HashMap<String,Object>>();
        for (int i = 0; i < NUM_PARADAS; i++) {
            HashMap<String,Object> pa = new HashMap<String,Object>();
            pa.put("yurt", -1);
            pa.put("adj", new ArrayList<Integer>());
            pa.put("coletaveis", new ArrayList<Integer>());
            if (i<27) {pa.put("capacidade", 1);}
            else if (i<33) {pa.put("capacidade", 2);}
            else{pa.put("capacidade", 6);}
            paradas.add(pa);
        }
        for (int[] r : ROTAS) conectar(r[0], r[1]);
        coletaveis = new ArrayList<Coletavel>();
        for (int[] p : POTES)
            adicionarColetavel(new PoteDeRecursos(p[0]), p, 1);
        for (int[] c : CIDADES)
            adicionarColetavel(new Cidade(new Recurso()), c, 0);
        for (int[] c : CONSELHEIROS) {
            Recurso troca = new Recurso();
            for (int t = GRAOS; t <= VACA; t++) troca.setItem(t, c[2 + t - GRAOS]);
            adicionarColetavel(new Conselheiro(troca, c[7], c[1]), new int[] { c[0] }, 0);
        }
        posicoesDoKhan = new ArrayList<ArrayList<Integer>>();
        posicoesDoKhan.add(new ArrayList<Integer>());   // 0 fora do mapa
        posicoesDoKhan.add(new ArrayList<Integer>());   // 1 melhorias
        for (int[] k : KHAN) {
            ArrayList<Integer> potes = new ArrayList<Integer>();
            for (int pote : k) potes.add(pote);
            posicoesDoKhan.add(potes);
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

    @SuppressWarnings("unchecked")
    ArrayList<Integer> coletaveisDe(int parada) {
        return (ArrayList<Integer>) paradas.get(parada).get("coletaveis");
    }

    private int adicionarColetavel(Coletavel c, int[] linha, int inicio) {
        coletaveis.add(c);
        int idx = coletaveis.size() - 1;
        for (int i = inicio; i < linha.length; i++) coletaveisDe(linha[i]).add(idx);
        return idx;
    }

    boolean alcanca(int parada, int idx) {
        return coletaveisDe(parada).contains(idx);
    }

    boolean moverKhan(int pos) {
        if (pos == posKhan || pos < 0 || pos >= posicoesDoKhan.size()) return false;
        posKhan = pos;
        for (int idx : posicoesDoKhan.get(pos))
            ((PoteDeRecursos) coletaveis.get(idx)).adicionar();
        return true;
    }
}
