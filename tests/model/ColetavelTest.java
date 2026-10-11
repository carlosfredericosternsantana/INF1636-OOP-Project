package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import model.Coletavel.Tipo;
import model.Coletavel.TipoPeca;

/** Casos de teste dos elementos coletáveis do mapa. */
public class ColetavelTest {

	private static Coletavel cidadeComDoisTesouros() {
		Coletavel cidade = new Coletavel("Samarcanda", Tipo.CIDADE);
		cidade.adicionar(TipoPeca.TESOURO_A, 1);
		cidade.adicionar(TipoPeca.TESOURO_B, 1);
		return cidade;
	}

	/** O 1º tesouro da cidade custa 1 espada e o seguinte, no mesmo turno, custa 2. */
	@Test
	public void segundoTesouroDaMesmaCidadeCustaODobro() {
		Coletavel cidade = cidadeComDoisTesouros();
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
		Coletavel cidade = cidadeComDoisTesouros();
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
		Coletavel cidade = cidadeComDoisTesouros();
		Jogador semEspadas = new Jogador();

		assertFalse(cidade.coletar(semEspadas, TipoPeca.TESOURO_A));
		assertEquals(0, semEspadas.getTesouros());
		assertEquals(2, cidade.total());
	}

	/** Tomar o último tesouro conquista a cidade e credita isso ao jogador. */
	@Test
	public void ultimoTesouroConquistaACidade() {
		Coletavel cidade = cidadeComDoisTesouros();
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.ESPADA, 3);

		cidade.coletar(jogador, TipoPeca.TESOURO_A);
		assertEquals(0, jogador.getCidadesConquistadas());

		cidade.coletar(jogador, TipoPeca.TESOURO_B);
		assertTrue(cidade.estaVazio());
		assertEquals(1, jogador.getCidadesConquistadas());
	}

	/** A província cobra 1 punho, que é ação da coluna ativada, e entrega o tributo. */
	@Test
	public void provinciaCobraPunho() {
		Coletavel provincia = new Coletavel("Kiev", Tipo.PROVINCIA);
		provincia.adicionar(TipoPeca.MOEDA, 2);
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.PUNHO, 1);

		assertTrue(provincia.coletar(jogador, TipoPeca.MOEDA));
		assertEquals(1, jogador.getQuantidade(TipoPeca.MOEDA));
		assertEquals(0, jogador.getQuantidade(TipoPeca.PUNHO));
		assertFalse(provincia.coletar(jogador, TipoPeca.MOEDA)); // acabaram os punhos
		assertEquals(1, provincia.total());
	}

	/** Uma província comporta no máximo 3 peças de tributo. */
	@Test(expected = IllegalStateException.class)
	public void provinciaNaoPassaDeTresTributos() {
		Coletavel provincia = new Coletavel("Kiev", Tipo.PROVINCIA);
		provincia.adicionar(TipoPeca.YURT, 3);
		provincia.adicionar(TipoPeca.YURT, 1);
	}

	/** Entrega certa rende votos; o bônus sai só na primeira, e a demanda não se esgota. */
	@Test
	public void entregaRendeVotosEOBonusSaiUmaVezSo() {
		Coletavel conselheiro = new Coletavel("Conselheiro do norte", 1, Map.of(TipoPeca.TESOURO_A, 2), 0);
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.TESOURO_A, 4);

		assertEquals(1, conselheiro.entregar(jogador, Map.of(TipoPeca.TESOURO_A, 2)));
		assertEquals(1, jogador.getVotos());
		assertEquals(1, jogador.getPecasDeBonus());

		assertEquals(1, conselheiro.entregar(jogador, Map.of(TipoPeca.TESOURO_A, 2)));
		assertEquals(2, jogador.getVotos());
		assertEquals(1, jogador.getPecasDeBonus()); // o bônus não volta
		assertFalse(conselheiro.temBonus());
		assertEquals(0, jogador.getTesouros());
	}

	/** Pagamento que não atende à demanda não é cobrado e não dá voto. */
	@Test
	public void entregaInsuficienteNaoCobra() {
		Coletavel conselheiro = new Coletavel("Conselheiro do norte", 1, Map.of(TipoPeca.TESOURO_A, 2), 0);
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.TESOURO_A, 1);

		assertEquals(0, conselheiro.entregar(jogador, Map.of(TipoPeca.TESOURO_A, 1)));
		assertEquals(0, jogador.getVotos());
		assertEquals(1, jogador.getTesouros());
		assertTrue(conselheiro.temBonus());
	}

	/** A parada que pede "3 tesouros de qualquer tipo" aceita tipos misturados. */
	@Test
	public void conselheiroDeTipoLivreAceitaTesourosMisturados() {
		Coletavel conselheiro = new Coletavel("Qualquer tipo", 1, Map.of(), 3);
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.TESOURO_A, 1);
		jogador.receber(TipoPeca.TESOURO_B, 1);
		jogador.receber(TipoPeca.TESOURO_C, 1);

		assertEquals(0, conselheiro.entregar(jogador, Map.of(TipoPeca.TESOURO_A, 1, TipoPeca.TESOURO_B, 1)));
		assertEquals(1, conselheiro.entregar(jogador,
				Map.of(TipoPeca.TESOURO_A, 1, TipoPeca.TESOURO_B, 1, TipoPeca.TESOURO_C, 1)));
		assertEquals(0, jogador.getTesouros());
	}

	/** Conselheiro recebe entrega, não é saqueado. */
	@Test(expected = IllegalStateException.class)
	public void naoSeColetaDeUmConselheiro() {
		new Coletavel("Karakorum", 2, Map.of(TipoPeca.TESOURO_C, 1), 0)
				.coletar(new Jogador(), TipoPeca.TESOURO_C);
	}

	/** Cidade é saqueada, não recebe entrega. */
	@Test(expected = IllegalStateException.class)
	public void naoSeEntregaAUmaCidade() {
		cidadeComDoisTesouros().entregar(new Jogador(), Map.of(TipoPeca.TESOURO_A, 1));
	}
}
