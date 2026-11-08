package model;

// Observado do padrao Observer, como no Capitulo 17. Depois de ser notificado,
// o observador pergunta o que mudou com get(EVENTO).
public interface ObservadoIF {

	int EVENTO = 0;
	int VEZ = 1;

	// valores de get(EVENTO)
	int NOVO_TURNO = 1;
	int PIAO_MOVIDO = 2;
	int COLUNA_ATIVADA = 3;
	int TABULEIRO = 4;
	int CARTAS = 5;
	int ESCOLHA = 6;
	int FIM = 7;

	void add (ObservadorIF o);

	void remove (ObservadorIF o);

	int get (int i);
}
