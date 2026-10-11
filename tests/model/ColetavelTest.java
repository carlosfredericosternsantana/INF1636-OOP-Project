package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

import model.Coletavel.TipoPeca;
import model.Conselheiro.PecaBonus;

/** Casos de teste dos elementos coletáveis do mapa: cidade, província e conselheiro. */
public class ColetavelTest {

	private static Cidade cidadeComDoisTesouros() {
		Cidade cidade = new Cidade("Samarcanda");
		cidade.adicionar(TipoPeca.TESOURO_A, 1);
		cidade.adicionar(TipoPeca.TESOURO_B, 1);
		return cidade;
	}

	private static Conselheiro conselheiroDePeles() {
		return Conselheiro.deParDeTesouros("Conselheiro do norte", TipoPeca.TESOURO_A, PecaBonus.DUAS_MOEDAS);
	}

	// ---------------------------------------------------------------- Cidade

	/** O 1º tesouro da cidade custa 1 espada e o seguinte, no mesmo turno, custa 2. */
	@Test
	public void segundoTesouroDaMesmaCidadeCustaODobro() {
		Cidade cidade = cidadeComDoisTesouros();
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.ESPADA, 3);

		assertTrue(cidade.coletar(jogador, TipoPeca.TESOURO_A));
		assertEquals(2, jogador.getEspadas());
		assertTrue(cidade.coletar(jogador, TipoPeca.TESOURO_B));
		assertEquals(0, jogador.getEspadas());
		assertEquals(2, jogador.getTesouros());
	}

	/** Passado o turno, o ataque recomeça custando 1 espada. */
	@Test
	public void novoTurnoZeraOEscalonamento() {
		Cidade cidade = cidadeComDoisTesouros();
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.ESPADA, 2);

		cidade.coletar(jogador, TipoPeca.TESOURO_A);
		cidade.novoTurno();

		assertEquals(Map.of(TipoPeca.ESPADA, 1), cidade.getCusto());
		assertTrue(cidade.coletar(jogador, TipoPeca.TESOURO_B));
	}

	/** Sem espada não se toma tesouro, e nada é cobrado nem retirado. */
	@Test
	public void semEspadaNaoSaqueiaENaoCobra() {
		Cidade cidade = cidadeComDoisTesouros();
		Jogador semEspadas = new Jogador();

		assertFalse(cidade.coletar(semEspadas, TipoPeca.TESOURO_A));
		assertEquals(0, semEspadas.getTesouros());
		assertEquals(2, cidade.total());
	}

	/** Pedir um tesouro que a cidade não tem não cobra espada nem conta para o escalonamento. */
	@Test
	public void tesouroAusenteNaoCobra() {
		Cidade cidade = cidadeComDoisTesouros();
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.ESPADA, 1);

		assertFalse(cidade.coletar(jogador, TipoPeca.TESOURO_E));
		assertEquals(1, jogador.getEspadas());
		assertEquals(Map.of(TipoPeca.ESPADA, 1), cidade.getCusto());
	}

	/** Tomar o último tesouro conquista a cidade e credita isso ao jogador. */
	@Test
	public void ultimoTesouroConquistaACidade() {
		Cidade cidade = cidadeComDoisTesouros();
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.ESPADA, 3);

		cidade.coletar(jogador, TipoPeca.TESOURO_A);
		assertFalse(cidade.foiConquistada());
		assertEquals(0, jogador.getCidadesConquistadas());

		cidade.coletar(jogador, TipoPeca.TESOURO_B);
		assertTrue(cidade.estaVazio());
		assertSame(jogador, cidade.getConquistador());
		assertEquals(1, jogador.getCidadesConquistadas());
	}

	/** Cidade guarda tesouros, não tributos. */
	@Test(expected = IllegalArgumentException.class)
	public void cidadeSoAceitaTesouro() {
		new Cidade("Bagdá").adicionar(TipoPeca.MOEDA, 1);
	}

	// ---------------------------------------------------------------- Província

	/** A província cobra 1 punho, que é ação da coluna ativada, e entrega o tributo dela. */
	@Test
	public void provinciaCobraPunho() {
		Provincia provincia = new Provincia("Kiev", TipoPeca.MOEDA);
		provincia.receberTributo();
		provincia.receberTributo();
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.PUNHO, 1);

		assertTrue(provincia.coletar(jogador));
		assertEquals(1, jogador.getQuantidade(TipoPeca.MOEDA));
		assertEquals(0, jogador.getQuantidade(TipoPeca.PUNHO));
		assertFalse(provincia.coletar(jogador)); // acabaram os punhos
		assertEquals(1, provincia.total());
	}

	/** O Khan não passa do limite de 3 peças: na província cheia, nada é acrescentado. */
	@Test
	public void provinciaNaoPassaDeTresTributos() {
		Provincia provincia = new Provincia("Kiev", TipoPeca.YURT, true);
		assertTrue(provincia.receberTributo());
		assertTrue(provincia.receberTributo());
		assertTrue(provincia.receberTributo());

		assertFalse(provincia.receberTributo());
		assertEquals(Provincia.MAX_TRIBUTOS, provincia.total());
		assertTrue(provincia.ehDoKhan());
	}

	/** Província rende espada, yurt ou moeda; tesouro só vem de cidade. */
	@Test(expected = IllegalArgumentException.class)
	public void provinciaNaoRendeTesouro() {
		new Provincia("Moscou", TipoPeca.TESOURO_C);
	}

	// ---------------------------------------------------------------- Conselheiro

	/** Entrega certa rende votos; o bônus sai só na primeira, e a demanda não se esgota. */
	@Test
	public void entregaRendeVotosEOBonusSaiUmaVezSo() {
		Conselheiro conselheiro = conselheiroDePeles();
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.TESOURO_A, 4);

		assertEquals(1, conselheiro.entregar(jogador, Map.of(TipoPeca.TESOURO_A, 2)));
		assertEquals(1, jogador.getVotos());
		assertEquals(1, jogador.getPecasDeBonus());
		assertNull(conselheiro.getBonus());

		assertEquals(1, conselheiro.entregar(jogador, Map.of(TipoPeca.TESOURO_A, 2)));
		assertEquals(2, jogador.getVotos());
		assertEquals(1, jogador.getPecasDeBonus()); // o bônus não volta
		assertFalse(conselheiro.temBonus());
		assertEquals(0, jogador.getTesouros());
	}

	/** Pagamento que não atende à demanda não é cobrado e não dá voto. */
	@Test
	public void entregaInsuficienteNaoCobra() {
		Conselheiro conselheiro = conselheiroDePeles();
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.TESOURO_A, 1);

		assertEquals(0, conselheiro.entregar(jogador, Map.of(TipoPeca.TESOURO_A, 1)));
		assertEquals(0, jogador.getVotos());
		assertEquals(1, jogador.getTesouros());
		assertTrue(conselheiro.temBonus());
	}

	/** Mandar tesouros a mais não é aceito: o conselheiro nunca cobra além da demanda. */
	@Test
	public void entregaAcimaDaDemandaEhRecusada() {
		Conselheiro conselheiro = conselheiroDePeles();
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.TESOURO_A, 3);

		assertEquals(0, conselheiro.entregar(jogador, Map.of(TipoPeca.TESOURO_A, 3)));
		assertEquals(3, jogador.getTesouros());
		assertEquals(0, jogador.getVotos());
	}

	/** A parada que pede "3 tesouros de qualquer tipo" aceita tipos misturados, e só 3. */
	@Test
	public void conselheiroDeTipoLivreAceitaTesourosMisturados() {
		Conselheiro conselheiro = Conselheiro.deTesourosQuaisquer("Qualquer tipo", PecaBonus.VOTO);
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.TESOURO_A, 1);
		jogador.receber(TipoPeca.TESOURO_B, 1);
		jogador.receber(TipoPeca.TESOURO_C, 1);

		assertEquals(0, conselheiro.entregar(jogador, Map.of(TipoPeca.TESOURO_A, 1, TipoPeca.TESOURO_B, 1)));
		assertEquals(1, conselheiro.entregar(jogador,
				Map.of(TipoPeca.TESOURO_A, 1, TipoPeca.TESOURO_B, 1, TipoPeca.TESOURO_C, 1)));
		assertEquals(0, jogador.getTesouros());
	}

	/** Karakorum pede os 3 tipos sorteados na preparação e dá 2 votos por entrega. */
	@Test
	public void karakorumDaDoisVotos() {
		Conselheiro karakorum = Conselheiro.karakorum(
				Set.of(TipoPeca.TESOURO_A, TipoPeca.TESOURO_C, TipoPeca.TESOURO_E), PecaBonus.DUAS_ESPADAS);
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.TESOURO_A, 1);
		jogador.receber(TipoPeca.TESOURO_C, 1);
		jogador.receber(TipoPeca.TESOURO_E, 1);
		jogador.receber(TipoPeca.TESOURO_B, 1);

		// um dos tipos errado: recusado
		assertEquals(0, karakorum.entregar(jogador,
				Map.of(TipoPeca.TESOURO_A, 1, TipoPeca.TESOURO_B, 1, TipoPeca.TESOURO_E, 1)));
		assertEquals(2, karakorum.entregar(jogador,
				Map.of(TipoPeca.TESOURO_A, 1, TipoPeca.TESOURO_C, 1, TipoPeca.TESOURO_E, 1)));
		assertEquals(2, jogador.getVotos());
		assertEquals(1, jogador.getTesouros()); // sobrou o TESOURO_B
	}
}
