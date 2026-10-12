package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class QuestTest {

	@Test
	public void soOPrimeiroJogadorACumprirLevaAQuest() {
		Quest quest = new Quest(Quest.TipoCondicao.ESPADAS, 4);
		Jogador primeiro = new Jogador();
		Jogador segundo = new Jogador();
		primeiro.r.setItem(Mapa.ESPADA, 3);
		segundo.r.setItem(Mapa.ESPADA, 4);
		assertFalse(quest.checarQuests(primeiro, 0));
		assertTrue(quest.checarQuests(segundo, 1));
		assertEquals(1, quest.getCumpridaPor());
		primeiro.r.setItem(Mapa.ESPADA, 4);
		assertFalse(quest.checarQuests(primeiro, 0));
		assertEquals(1, quest.getCumpridaPor());
	}

	@Test
	public void questsDeMelhoriaLeemAsColunasDoJogador() {
		Jogador jogador = new Jogador();
		jogador.melhoriasPorColuna[0] = 2;
		jogador.melhoriasPorColuna[1] = 2;
		jogador.melhoriasPorColuna[2] = 1;
		assertTrue(new Quest(Quest.TipoCondicao.COLUNAS_COM_DUAS_MELHORIAS, 2).checarQuests(jogador, 0));
		assertFalse(new Quest(Quest.TipoCondicao.MELHORIAS_NA_MESMA_COLUNA, 3).checarQuests(jogador, 0));
		assertFalse(new Quest(Quest.TipoCondicao.LINHAS_DE_MELHORIA, 1).checarQuests(jogador, 0));
		jogador.melhoriasPorColuna[3] = 1;
		assertTrue(new Quest(Quest.TipoCondicao.LINHAS_DE_MELHORIA, 1).checarQuests(jogador, 0));
	}
}
