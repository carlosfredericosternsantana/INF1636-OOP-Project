package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * <ul>
 *   <li>SIMPLES: 1 yurt; só 1 peão pode terminar o movimento nela.</li>
 *   <li>DUPLA: até 2 yurts e 2 peões.
 *       Toda parada dupla tem um conselheiro.</li>
 *   <li>KARAKORUM: a capital. Não aceita yurts, cabe todo mundo, tem o conselheiro de 2 votos.</li>
 * </ul>
 */
class Parada {

	enum Tipo { SIMPLES, DUPLA, KARAKORUM }

	private final String nome;
	private final Tipo tipo;
	private final Mapa.Regiao regiao;
	private final Set<Parada> adjacentes = new HashSet<>();
	private final List<Jogador> yurts = new ArrayList<>();
	private Conselheiro conselheiro;

	Parada(String nome, Tipo tipo, Mapa.Regiao regiao) {
		this.nome = Objects.requireNonNull(nome, "nome obrigatório");
		this.tipo = Objects.requireNonNull(tipo, "tipo obrigatório");
		this.regiao = Objects.requireNonNull(regiao, "região obrigatória");
	}

	/** Ligação nos dois sentidos*/
	void conectar(Parada outra) {
		if (outra == this) throw new IllegalArgumentException("parada não se liga a si mesma");
		adjacentes.add(outra);
		outra.adjacentes.add(this);
	}

	boolean ehAdjacenteA(Parada outra) { return adjacentes.contains(outra); }

	int capacidadeDeYurts() {
		return switch (tipo) {
			case SIMPLES -> 1;
			case DUPLA -> 2;
			case KARAKORUM -> 0;
		};
	}

	int capacidadeDePeoes() {
		return switch (tipo) {
			case SIMPLES -> 1;
			case DUPLA -> 2;
			case KARAKORUM -> Integer.MAX_VALUE;
		};
	}

	boolean cabeYurt() { return yurts.size() < capacidadeDeYurts(); }

	boolean temYurtDe(Jogador jogador) { return yurts.contains(jogador); }

	void receberYurt(Jogador jogador) {
		if (!cabeYurt()) throw new IllegalStateException("não cabe yurt em " + nome);
		yurts.add(jogador);
	}

	void setConselheiro(Conselheiro conselheiro) {
		if (tipo == Tipo.SIMPLES) throw new IllegalArgumentException("conselheiro só fica em parada dupla ou em Karakorum");
		this.conselheiro = conselheiro;
	}

	String getNome() { return nome; }

	Tipo getTipo() { return tipo; }

	Mapa.Regiao getRegiao() { return regiao; }

	Set<Parada> getAdjacentes() { return Collections.unmodifiableSet(adjacentes); }

	List<Jogador> getYurts() { return Collections.unmodifiableList(yurts); }

	/** O conselheiro desta parada; null numa parada simples. */
	Conselheiro getConselheiro() { return conselheiro; }

	@Override
	public String toString() { return nome; }
}