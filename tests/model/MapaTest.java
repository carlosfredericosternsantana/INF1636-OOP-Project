package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import model.Mapa.Peca;
import model.Mapa.Regiao;

/**
 * Casos de teste do mapa, sobre um mapa pequeno:
 *
 * <pre>
 *   K(Karakorum) ── A ── B ── C          Samarcanda (China) ao lado de B
 *                   │                    Sarai (Rússia) ao lado de C
 *                   D (dupla, conselheiro)  Bagdá (Pérsia) ao lado de D
 *                                        Herat (Pérsia) ao lado de K, fica na pilha
 *   Potes: Kiev (moeda) ao lado de C; Moscou (ficha de yurt) ao lado de D;
 *          Ordos (espada, do Khan) ao lado de B, com vizinhos Kiev e Moscou.
 * </pre>
 */
public class MapaTest {

	private Mapa mapa;
	private Jogador jogador;
	private Jogador outro;

	/** No padrão do Recurso: pega a cópia, altera e devolve ao jogador. */
	private static void dar(Jogador jogador, Peca peca, int quantidade) {
		Recurso mao = jogador.getRecurso();
		peca.somar(mao, quantidade);
		jogador.setRecurso(mao);
	}

	private static int qtd(Jogador jogador, Peca peca) {
		return peca.em(jogador.getRecurso());
	}

	@Before
	public void montarMapa() {
		mapa = new Mapa();
		mapa.adicionarParada("K", Parada.Tipo.KARAKORUM, Regiao.NENHUMA);
		mapa.adicionarParada("A", Parada.Tipo.SIMPLES, Regiao.CHINA);
		mapa.adicionarParada("B", Parada.Tipo.SIMPLES, Regiao.CHINA);
		mapa.adicionarParada("C", Parada.Tipo.SIMPLES, Regiao.RUSSIA);
		mapa.adicionarParada("D", Parada.Tipo.DUPLA, Regiao.PERSIA);
		mapa.conectar("K", "A");
		mapa.conectar("A", "B");
		mapa.conectar("B", "C");
		mapa.conectar("A", "D");
		mapa.colocarConselheiro("D", Map.of(Peca.PELE, 2), 1);

		mapa.adicionarPote("Kiev", Peca.MOEDA, false, "C");
		mapa.adicionarPote("Moscou", Peca.FICHA_DE_YURT, false, "D");
		mapa.adicionarPote("Ordos", Peca.ESPADA, true, "B");
		mapa.definirVizinhosDoKhan("Ordos", "Kiev", "Moscou");

		mapa.adicionarCidade("Samarcanda", Regiao.CHINA, "B");
		mapa.adicionarCidade("Sarai", Regiao.RUSSIA, "C");
		mapa.adicionarCidade("Bagda", Regiao.PERSIA, "D");
		mapa.adicionarCidade("Herat", Regiao.PERSIA, "K");

		List<Peca> tesouros = List.of(
				Peca.GRAOS, Peca.TECIDO, Peca.FERRO, Peca.PELE,   // Samarcanda
				Peca.VACA, Peca.VACA, Peca.VACA, Peca.VACA,       // Sarai
				Peca.TECIDO, Peca.TECIDO, Peca.TECIDO, Peca.TECIDO, // Bagda
				Peca.GRAOS, Peca.GRAOS, Peca.GRAOS, Peca.GRAOS);  // Herat
		mapa.prepararCom(List.of("Samarcanda", "Sarai", "Bagda", "Herat"), tesouros);

		jogador = new Jogador();
		outro = new Jogador();
		mapa.colocarPeao(jogador);
		mapa.colocarPeao(outro);
		mapa.iniciarTurno(jogador);
	}

	/** Um passo para uma parada vizinha custa 1 movimento; pular paradas não vale. */
	@Test
	public void soSeAndaParaParadaAdjacente() {
		assertEquals(-1, mapa.mover(jogador, List.of("B")));
		assertSame(mapa.getParada("K"), mapa.getPosicao(jogador));

		assertEquals(2, mapa.mover(jogador, List.of("A", "B")));
		assertSame(mapa.getParada("B"), mapa.getPosicao(jogador));
	}

	/** Pode-se passar por uma parada simples ocupada, mas não terminar o movimento nela. */
	@Test
	public void naoTerminaEmParadaSimplesOcupada() {
		mapa.mover(outro, List.of("A"));

		assertEquals(-1, mapa.custoDoCaminho(jogador, List.of("A")));
		assertEquals(2, mapa.custoDoCaminho(jogador, List.of("A", "B")));
	}

	/** Na parada dupla cabem 2 peões; o 3º não pode terminar o movimento nela. */
	@Test
	public void paradaDuplaCabeDoisPeoes() {
		Jogador terceiro = new Jogador();
		mapa.colocarPeao(terceiro);
		mapa.mover(jogador, List.of("A", "D"));
		mapa.mover(outro, List.of("A", "D"));

		assertEquals(-1, mapa.custoDoCaminho(terceiro, List.of("A", "D")));
	}

	/** Yurt só em parada feita no turno, com espaço, e gastando a ficha de yurt; nunca em Karakorum. */
	@Test
	public void regrasParaConstruirYurt() {
		dar(jogador, Peca.FICHA_DE_YURT, 2);
		assertFalse(mapa.construirYurt(jogador, "K")); // Karakorum não aceita yurt
		assertFalse(mapa.construirYurt(jogador, "A")); // ainda não parou em A

		mapa.mover(jogador, List.of("A"));
		assertTrue(mapa.construirYurt(jogador, "A"));
		assertFalse(mapa.construirYurt(jogador, "A")); // parada simples já tem yurt

		assertEquals(1, qtd(jogador, Peca.FICHA_DE_YURT));
		assertEquals(1, jogador.getYurtsPorRegiao(Regiao.CHINA.ordinal()));
	}

	/** Pular o próprio yurt não gasta movimento, mas também não conta como parada feita. */
	@Test
	public void pularOProprioYurtNaoCustaENaoContaComoParada() {
		dar(jogador, Peca.FICHA_DE_YURT, 1);
		mapa.mover(jogador, List.of("A", "B"));
		mapa.construirYurt(jogador, "B");
		mapa.mover(jogador, List.of("A"));
		mapa.encerrarTurno(jogador);

		mapa.iniciarTurno(jogador);
		assertEquals(1, mapa.mover(jogador, List.of("B", "C"))); // B foi pulada
		assertFalse(mapa.fezParadaEm(jogador, mapa.getParada("B")));
		assertTrue(mapa.fezParadaEm(jogador, mapa.getParada("C")));
	}

	/** Tributo só de pote adjacente a uma parada feita no turno, pagando 1 maozinha. */
	@Test
	public void tributoSoDePoteAdjacente() {
		mapa.receberTributo("Kiev");
		dar(jogador, Peca.MAOZINHA, 1);

		assertFalse(mapa.pegarTributo(jogador, "Kiev"));
		mapa.mover(jogador, List.of("A", "B", "C"));
		assertTrue(mapa.pegarTributo(jogador, "Kiev"));
		assertEquals(1, qtd(jogador, Peca.MOEDA));
		assertEquals(0, qtd(jogador, Peca.MAOZINHA));
		assertEquals(0, mapa.quantidadeNoPote("Kiev"));
	}

	/** O Khan põe tributo no seu pote e nos 2 vizinhos, sem passar de 3, e não pode ficar onde está. */
	@Test
	public void khanDistribuiTributoENaoFicaParado() {
		assertTrue(mapa.moverKhanParaPote("Ordos"));
		assertEquals(1, mapa.quantidadeNoPote("Ordos"));
		assertEquals(1, mapa.quantidadeNoPote("Kiev"));
		assertEquals(1, mapa.quantidadeNoPote("Moscou"));

		assertFalse(mapa.moverKhanParaPote("Ordos"));
		assertFalse(mapa.moverKhanParaPote("Kiev")); // não é pote do Khan
		assertTrue(mapa.moverKhanParaMelhorias());
		assertFalse(mapa.moverKhanParaMelhorias());

		mapa.receberTributo("Kiev");
		mapa.receberTributo("Kiev");
		assertFalse(mapa.receberTributo("Kiev")); // máximo de 3
		assertEquals(Mapa.MAX_TRIBUTOS_POR_POTE, mapa.quantidadeNoPote("Kiev"));
	}

	/** 1 espada pelo 1º tesouro da cidade no turno, 2 pelos seguintes; o turno novo zera. */
	@Test
	public void ataqueEscalonaNoMesmoTurno() {
		dar(jogador, Peca.ESPADA, 3);
		mapa.mover(jogador, List.of("A", "B"));

		assertTrue(mapa.atacarCidade(jogador, "Samarcanda", Peca.GRAOS));
		assertEquals(2, mapa.custoDeAtaque("Samarcanda")); // o próximo custa 2
		assertTrue(mapa.atacarCidade(jogador, "Samarcanda", Peca.TECIDO));
		assertEquals(0, qtd(jogador, Peca.ESPADA));
		assertEquals(2, Peca.tesouros(jogador.getRecurso()));

		mapa.encerrarTurno(jogador);
		assertEquals(1, mapa.custoDeAtaque("Samarcanda"));
	}

	/** Conquistar uma cidade põe o yurt no centro dela, conta para a região e revela a próxima. */
	@Test
	public void conquistaColocaYurtERevelaProximaCidade() {
		dar(jogador, Peca.ESPADA, 7); // 1 + 2 + 2 + 2
		mapa.mover(jogador, List.of("A", "B"));
		assertFalse(mapa.estaRevelada("Herat"));

		assertTrue(mapa.atacarCidade(jogador, "Samarcanda", Peca.GRAOS));
		assertTrue(mapa.atacarCidade(jogador, "Samarcanda", Peca.TECIDO));
		assertTrue(mapa.atacarCidade(jogador, "Samarcanda", Peca.FERRO));
		assertTrue(mapa.atacarCidade(jogador, "Samarcanda", Peca.PELE));

		assertEquals(1, jogador.getCidadesConquistadas());
		assertFalse(mapa.estaRevelada("Samarcanda"));
		assertTrue(mapa.estaRevelada("Herat"));
		assertEquals(Map.of(Regiao.CHINA, 1), mapa.yurtsPorRegiao(jogador));
		assertEquals(1, jogador.getYurtsPorRegiao(Regiao.CHINA.ordinal()));
	}

	/** Só se ataca cidade adjacente a uma parada feita no turno, e nada é cobrado se não puder. */
	@Test
	public void naoAtacaCidadeDistante() {
		dar(jogador, Peca.ESPADA, 1);
		mapa.mover(jogador, List.of("A"));

		assertFalse(mapa.atacarCidade(jogador, "Sarai", Peca.VACA));
		assertEquals(1, qtd(jogador, Peca.ESPADA));
		assertEquals(4, mapa.tesourosNaCidade("Sarai"));
	}

	/** Entrega só onde parou, só com a demanda exata; o bônus sai na primeira entrega. */
	@Test
	public void entregaAoConselheiro() {
		dar(jogador, Peca.PELE, 5);
		Recurso duasPeles = new Recurso();
		duasPeles.setPele(2);
		Recurso tresPeles = new Recurso();
		tresPeles.setPele(3);

		assertEquals(0, mapa.entregar(jogador, "D", duasPeles)); // ainda não parou em D
		mapa.mover(jogador, List.of("A", "D"));
		assertEquals(0, mapa.entregar(jogador, "D", tresPeles)); // acima da demanda
		assertEquals(1, mapa.entregar(jogador, "D", duasPeles));
		assertEquals(1, jogador.getPecasDeBonus());
		assertFalse(mapa.temBonus("D"));

		assertEquals(1, mapa.entregar(jogador, "D", duasPeles)); // a demanda não se esgota
		assertEquals(2, jogador.getRecurso().getVoto());
		assertEquals(1, jogador.getPecasDeBonus());               // o bônus não volta
		assertEquals(1, qtd(jogador, Peca.PELE));
	}

	/** A maior rede conta só yurts ligados por rotas (carta secreta Viajante). */
	@Test
	public void maiorRedeDeYurtsConectados() {
		dar(jogador, Peca.FICHA_DE_YURT, 3);
		mapa.mover(jogador, List.of("A", "B", "C"));
		mapa.construirYurt(jogador, "A");
		mapa.construirYurt(jogador, "B");
		mapa.mover(jogador, List.of("B", "A", "D")); // B e A pulados (yurts dele)
		mapa.construirYurt(jogador, "D");

		assertEquals(3, mapa.maiorRedeDeYurts(jogador)); // A-B e A-D estão ligados
	}
}
