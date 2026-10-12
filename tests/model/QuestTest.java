package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QuestTest {

	private static final int TEMPO = 1000;

	@Test(timeout = TEMPO)
	public void soOPrimeiroJogadorACumprirLevaAQuest() {
		Quest quest = new Quest(Quest.TipoCondicao.ESPADAS, 4);
		Jogador primeiro = new Jogador();
		Jogador segundo = new Jogador();
		primeiro.r.setItem(Mapa.ESPADA, 3);
		segundo.r.setItem(Mapa.ESPADA, 4);
		assertFalse("3 espadas nao cumprem a quest", quest.checarQuests(primeiro, 0));
		assertTrue("4 espadas cumprem a quest", quest.checarQuests(segundo, 1));
		assertEquals("indice de quem cumpriu", 1, quest.getCumpridaPor());
		primeiro.r.setItem(Mapa.ESPADA, 4);
		assertFalse("quest ja cumprida por outro jogador", quest.checarQuests(primeiro, 0));
		assertEquals("quem cumpriu nao muda", 1, quest.getCumpridaPor());
	}

	@Test(timeout = TEMPO)
	public void questsDeMelhoriaLeemAsColunasDoJogador() {
		Jogador jogador = new Jogador();
		jogador.melhoriasPorColuna.set(0, 2);
		jogador.melhoriasPorColuna.set(1, 2);
		jogador.melhoriasPorColuna.set(2, 1);
		assertTrue("2 colunas com 2 melhorias",
				new Quest(Quest.TipoCondicao.COLUNAS_COM_DUAS_MELHORIAS, 2).checarQuests(jogador, 0));
		assertFalse("nenhuma coluna tem 3 melhorias",
				new Quest(Quest.TipoCondicao.MELHORIAS_NA_MESMA_COLUNA, 3).checarQuests(jogador, 0));
		assertFalse("coluna 3 vazia impede a linha completa",
				new Quest(Quest.TipoCondicao.LINHAS_DE_MELHORIA, 1).checarQuests(jogador, 0));
		jogador.melhoriasPorColuna.set(3, 1);
		assertTrue("todas as colunas com melhoria completam a linha",
				new Quest(Quest.TipoCondicao.LINHAS_DE_MELHORIA, 1).checarQuests(jogador, 0));
	}
}
