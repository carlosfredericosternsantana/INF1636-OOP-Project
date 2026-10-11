package model;

/**
 * Quem for implemntar essa classe por favor implementar esses getters sem mexer e quebrar, porque a classe quest depende deles
 */
public class Jogador {

	int espadas;                          // pegar/gastar peça de tributo de espada
	int yurtsNoMapa;                      // construir yurt, inclusive o posto no centro da cidade conquistada
	int tesouros;                         // saquear cidade e entregar tesouro a conselheiro
	int tiposDeTesouroDiferentes;         // idem: quantos dos 5 tipos ele tem na mão
	int menorNumeroDeYurtsPorRegiao;      // construir yurt: menor contagem entre China, Rússia e Pérsia
	int pecasDeBonus;                     // primeira entrega a cada conselheiro
	int cidadesConquistadas;              // tomar o último tesouro de uma cidade
	int linhasDeMelhoriaCompletas;        // comprar/ganhar melhoria
	int colunasComDuasMelhorias;          // idem
	int maiorNumeroDeMelhoriasNumaColuna; // idem

	int getEspadas() { return espadas; }

	int getYurtsNoMapa() { return yurtsNoMapa; }

	int getTesouros() { return tesouros; }

	int getTiposDeTesouroDiferentes() { return tiposDeTesouroDiferentes; }

	/** "1 yurt em cada região" é o mesmo que "a região com menos yurts já tem 1". */
	int getMenorNumeroDeYurtsPorRegiao() { return menorNumeroDeYurtsPorRegiao; }

	int getPecasDeBonus() { return pecasDeBonus; }

	int getCidadesConquistadas() { return cidadesConquistadas; }

	int getLinhasDeMelhoriaCompletas() { return linhasDeMelhoriaCompletas; }

	int getColunasComDuasMelhorias() { return colunasComDuasMelhorias; }

	int getMaiorNumeroDeMelhoriasNumaColuna() { return maiorNumeroDeMelhoriasNumaColuna; }
}
