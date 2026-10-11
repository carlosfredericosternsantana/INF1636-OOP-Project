package model;

/**
 * Quem for implemntar essa classe por favor implementar esses getters sem mexer e quebrar, porque a classe quest depende deles
 */
public class Jogador {

	int espadas;                          // pegar/gastar peça de tributo de espada
	int yurtsNoMapa;                      // construir yurt, inclusive o posto no centro da cidade conquistada
	int tesouros;                         // saquear cidade e entregar tesouro a conselheiro
	int tiposDeTesouroDiferentes;         // idem: quantos dos 5 tipos ele tem na mão
	int yurtsNaChina;                     // construir yurt: contagem por região,
	int yurtsNaRussia;                    // usada pelas duas cartas que pedem
	int yurtsNaPersia;                    // yurts em CADA uma das 3 regiões
	int pecasDeBonus;                     // primeira entrega a cada conselheiro
	int cidadesConquistadas;              // tomar o último tesouro de uma cidade
	int linhasDeMelhoriaCompletas;        // comprar/ganhar melhoria
	int colunasComDuasMelhorias;          // idem
	int maiorNumeroDeMelhoriasNumaColuna; // idem

	int getEspadas() { return espadas; }

	int getYurtsNoMapa() { return yurtsNoMapa; }

	int getTesouros() { return tesouros; }

	int getTiposDeTesouroDiferentes() { return tiposDeTesouroDiferentes; }

	int getYurtsNaChina() { return yurtsNaChina; }

	int getYurtsNaRussia() { return yurtsNaRussia; }

	int getYurtsNaPersia() { return yurtsNaPersia; }

	int getPecasDeBonus() { return pecasDeBonus; }

	int getCidadesConquistadas() { return cidadesConquistadas; }

	int getLinhasDeMelhoriaCompletas() { return linhasDeMelhoriaCompletas; }

	int getColunasComDuasMelhorias() { return colunasComDuasMelhorias; }

	int getMaiorNumeroDeMelhoriasNumaColuna() { return maiorNumeroDeMelhoriasNumaColuna; }
}
