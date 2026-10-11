package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import model.Coletavel.TipoPeca;

import model.Quest.TipoCondicao;

/** Casos de teste da carta de conselheiro aberto. */
public class QuestTest {

	/** Quem ainda não atingiu a demanda não cumpre a carta, e ela continua em jogo. */
	@Test
	public void naoCumpreAbaixoDaDemanda() {
		Quest quatroEspadas = new Quest(TipoCondicao.ESPADAS, 4);
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.ESPADA, 3);

		assertFalse(quatroEspadas.checarQuests(jogador));
		assertFalse(quatroEspadas.foiCumprida());
		assertNull(quatroEspadas.getCumpridaPor());
	}

	/** Atingida a demanda, a carta é cumprida e fica registrada no nome do jogador. */
	@Test
	public void cumpreAoAtingirADemanda() {
		Quest quatroEspadas = new Quest(TipoCondicao.ESPADAS, 4);
		Jogador jogador = new Jogador();
		jogador.receber(TipoPeca.ESPADA, 4);

		assertTrue(quatroEspadas.checarQuests(jogador));
		assertTrue(quatroEspadas.foiCumprida());
		assertSame(jogador, quatroEspadas.getCumpridaPor());
		assertEquals(1, Quest.VOTOS);
		assertEquals(4, quatroEspadas.getQuantidadeExigida(TipoCondicao.ESPADAS));
	}

	/** Só o PRIMEIRO a cumprir ganha o voto: o segundo não leva a carta, mesmo sobrando demanda. */
	@Test
	public void segundoJogadorNaoLevaCartaJaCumprida() {
		Quest quatroEspadas = new Quest(TipoCondicao.ESPADAS, 4);
		Jogador primeiro = new Jogador();
		primeiro.receber(TipoPeca.ESPADA, 4);
		Jogador segundo = new Jogador();
		segundo.receber(TipoPeca.ESPADA, 10);

		assertTrue(quatroEspadas.checarQuests(primeiro));
		assertFalse(quatroEspadas.checarQuests(segundo));
		assertSame(primeiro, quatroEspadas.getCumpridaPor());
	}

	/** Checar de novo o mesmo jogador não pode render um segundo voto pela mesma carta. */
	@Test
	public void naoPontuaDuasVezesParaOMesmoJogador() {
		Quest duasCidades = new Quest(TipoCondicao.CIDADES_CONQUISTADAS, 2);
		Jogador jogador = new Jogador();
		jogador.cidadesConquistadas = 3;

		assertTrue(duasCidades.checarQuests(jogador));
		assertFalse(duasCidades.checarQuests(jogador));
	}

	/** Cada condição tem de ler o contador certo do jogador, e não outro qualquer. */
	@Test
	public void cadaCondicaoLeOContadorCorrespondente() {
		Jogador jogador = new Jogador();
		jogador.cidadesConquistadas = 2;
		jogador.pecasDeBonus = 1;

		assertTrue(new Quest(TipoCondicao.CIDADES_CONQUISTADAS, 2).checarQuests(jogador));
		assertFalse(new Quest(TipoCondicao.PECAS_DE_BONUS, 2).checarQuests(jogador));
	}

	/** A carta "1 yurt em cada região" só é cumprida quando as TRÊS regiões estão atendidas. */
	@Test
	public void demandaCompostaExigeTodasAsPartes() {
		Quest umYurtEmCadaRegiao = new Quest(Map.of(
				TipoCondicao.YURTS_NA_CHINA, 1,
				TipoCondicao.YURTS_NA_RUSSIA, 1,
				TipoCondicao.YURTS_NA_PERSIA, 1));
		Jogador jogador = new Jogador();
		jogador.yurtsNaChina = 5;
		jogador.yurtsNaRussia = 5;

		assertFalse(umYurtEmCadaRegiao.checarQuests(jogador)); // falta a Pérsia
		assertEquals(1, umYurtEmCadaRegiao.getQuantidadeExigida(TipoCondicao.YURTS_NA_PERSIA));

		jogador.yurtsNaPersia = 1;
		assertTrue(umYurtEmCadaRegiao.checarQuests(jogador));
	}

	/** Carta sem nenhuma exigência não existe no jogo. */
	@Test(expected = IllegalArgumentException.class)
	public void demandaVaziaEhRecusada() {
		new Quest(Map.of());
	}

	/** Carta com demanda não positiva não existe no jogo e tem de ser recusada na criação. */
	@Test(expected = IllegalArgumentException.class)
	public void demandaNaoPositivaEhRecusada() {
		new Quest(TipoCondicao.ESPADAS, 0);
	}

}
