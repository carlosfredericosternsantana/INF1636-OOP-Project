package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// Unica porta de entrada do model (Singleton + Facade): a view e o controller
// so enxergam esta classe e as interfaces do Observer.
public class Fachada {

	public static final int NUM_PARADAS = Mapa.NUM_PARADAS;
	public static final int KARAKORUM = Mapa.KARAKORUM;
	public static final int NUM_POTES = Mapa.NUM_POTES;
	public static final int NUM_CIDADES = Mapa.NUM_CIDADES;
	public static final int NUM_COLUNAS = Melhorias.NUM_COLUNAS;
	public static final int LINHAS_DA_CARTELA = Melhorias.LINHAS;
	public static final int ESPACOS_DE_MELHORIA = Melhorias.TAMANHO_TRILHA;
	public static final int TOPO_DA_PILHA = Melhorias.EMBARALHADA;
	public static final int SEM_PEAO = Jogador.SEM_PEAO;
	public static final int MAX_JOGADORES = Manager.MAX_JOGADORES;
	public static final int VERMELHA = Jogador.VERMELHA;
	public static final int VERDE = Jogador.VERDE;
	public static final int AMARELA = Jogador.AMARELA;
	public static final int BRANCA = Jogador.BRANCA;
	public static final int NENHUMA_COLUNA = Manager.NENHUMA_COLUNA;
	public static final int FORA_DO_MAPA = Mapa.FORA_DO_MAPA;
	public static final int KHAN_NAS_MELHORIAS = Mapa.TRILHA_DE_MELHORIAS;
	public static final int ESPADA = Mapa.ESPADA, MAOZINHA = Mapa.MAOZINHA, YURT = Mapa.YURT, MOEDA = Mapa.MOEDA;
	public static final int GRAOS = Mapa.GRAOS, TECIDO = Mapa.TECIDO, FERRO = Mapa.FERRO, PELE = Mapa.PELE, VACA = Mapa.VACA;

	private static Fachada instancia;

	private Manager manager;

	private Fachada () {}

	public static Fachada getInstancia () {
		if (instancia == null) instancia = new Fachada();
		return instancia;
	}

	public static String[] getHerdeiros () { return Jogador.HERDEIROS.clone(); }

	public void novaPartida (int[] herdeiros, String[] nomes) {
		manager = new Manager(herdeiros);
		for (int j = 0; j < nomes.length; j++) manager.getJogador(j).setNome(nomes[j]);
	}

	// ---------- salvamento e recuperacao (arquivo texto ASCII) ----------

	public boolean podeSalvar () { return manager.podeSalvar(); }

	public void salvarPartida (File arquivo) throws IOException {
		try (PrintWriter out = new PrintWriter(arquivo, "US-ASCII")) {
			manager.salvar(out);
			if (out.checkError()) throw new IOException("erro ao gravar " + arquivo.getName());
		}
	}

	// Se o arquivo for invalido, a partida atual nao muda.
	public void carregarPartida (File arquivo) throws IOException {
		try (BufferedReader in = new BufferedReader(new FileReader(arquivo))) {
			manager = Manager.carregar(in);
		} catch (RuntimeException erro) {
			throw new IOException("arquivo de partida invalido", erro);
		}
	}

	// ---------- escolhas antes da 1a rodada ----------

	public int getJogadorEscolhendo () { return manager.getJogadorEscolhendo(); }

	public int getOpcaoDeSecreto (int jogador, int opcao) { return manager.getOpcaoDeSecreto(jogador, opcao); }

	public int getTipoDaOpcaoDeMelhoria (int opcao) { return manager.getOpcaoDeMelhoria(opcao).getTipo(); }

	public int getCorDaOpcaoDeMelhoria (int opcao) { return manager.getOpcaoDeMelhoria(opcao).getCor(); }

	public boolean escolherNoInicio (int opcao) { return manager.escolherNoInicio(opcao); }

	public void registra (ObservadorIF o) { manager.add(o); }

	public void remove (ObservadorIF o) { manager.remove(o); }

	// ---------- jogadores ----------

	public int getQuantidadeDeJogadores () { return manager.getQuantidadeDeJogadores(); }

	public int getVez () { return manager.getVez(); }

	public String getHerdeiro (int jogador) { return manager.getJogador(jogador).getHerdeiro(); }

	public String getNome (int jogador) { return manager.getJogador(jogador).getNome(); }

	public int getIndiceDoHerdeiro (int jogador) { return manager.getJogador(jogador).getIndiceDoHerdeiro(); }

	public int getParada (int jogador) { return manager.getJogador(jogador).getParada(); }

	// Parada do 2o peao de Altani, ou SEM_PEAO.
	public int getOutraParada (int jogador) { return manager.getJogador(jogador).getOutraParada(); }

	public int getRecurso (int jogador, int tipo) { return manager.getJogador(jogador).getRecurso().getItem(tipo); }

	public int getVotos (int jogador) { return manager.getJogador(jogador).getRecurso().getItem(Mapa.VOTO); }

	public int getTotalDeVotos () { return manager.totalDeVotos(); }

	public int getYurtsRestantes (int jogador) { return manager.getJogador(jogador).getYurtsRestantes(); }

	public int getConselheiroSecreto (int jogador) { return manager.getJogador(jogador).getConselheiroSecreto(); }

	public ArrayList<Integer> getPecasDeBonus (int jogador) { return manager.getJogador(jogador).getBonusRecebidos(); }

	// ---------- turno ----------

	public int getMovimentos () { return manager.getMovimentos(); }

	public int getColunaAtivada () { return manager.getColunaAtivada(); }

	public int getRecursoDoTurno (int tipo) { return manager.getRecursoDoTurno(tipo); }

	public boolean isKhanPendente () { return manager.isKhanPendente(); }

	public int getMelhoriasGratis () { return manager.getJogadorDaVez().melhoriasGratis; }

	public boolean colunaDisponivel (int coluna) { return manager.getJogadorDaVez().colunaDisponivel(coluna); }

	public ArrayList<Integer> getParadasAlcancaveis () { return manager.paradasAlcancaveis(); }

	public ArrayList<Integer> getTesourosParaEscolher () { return manager.getTesourosParaEscolher(); }

	public ArrayList<Integer> getParadasParaYurt () { return manager.paradasParaYurt(); }

	public boolean escolheColunaDaMelhoria () { return manager.escolheColunaDaMelhoria(); }

	public boolean podeComprarDoTopo () { return manager.podeComprarDoTopo(); }

	public boolean podeUsarHabilidade () { return manager.podeUsarHabilidade(); }

	public boolean podeTomarComMoeda (int cidade) { return manager.podeTomarComMoeda(cidade); }

	public boolean podeEntregar () { return manager.podeEntregar(); }

	public boolean podeAtacar (int cidade) { return manager.podeAtacar(cidade); }

	public int getCustoDoAtaque (int cidade) { return manager.custoDoAtaque(cidade); }

	public boolean podePassarVez () { return manager.podePassarVez(); }

	// ---------- tabuleiro ----------

	public int getTipoDoPote (int pote) { return manager.getPote(pote).getTipo(); }

	public int getTributosNoPote (int pote) { return manager.getPote(pote).getQuantidade(); }

	public int getPosicaoDoKhan () { return manager.getMapa().getPosKhan(); }

	public ArrayList<Integer> getYurtsNaParada (int parada) {
		return new ArrayList<Integer>(manager.getMapa().yurtsEm(parada));
	}

	public boolean cidadeRevelada (int cidade) { return manager.cidadeRevelada(cidade); }

	public int getTesourosDaCidade (int cidade, int tipo) { return manager.getCidade(cidade).getTesouro(tipo); }

	public int getDonoDaCidade (int cidade) { return manager.getCidade(cidade).getDono(); }

	public int getBonusNaParada (int parada) { return manager.getBonusNaParada(parada); }

	// Tipo e cor da melhoria num espaco da area (1 a 5), ou -1 se estiver vazio.
	public int getTipoDaMelhoriaNaArea (int espaco) {
		Melhorias m = manager.melhoriaNaPosicao(espaco);
		return m == null ? -1 : m.getTipo();
	}

	public int getCorDaMelhoriaNaArea (int espaco) {
		Melhorias m = manager.melhoriaNaPosicao(espaco);
		return m == null ? -1 : m.getCor();
	}

	// ---------- cartela e cartas do jogador da vez ----------

	public int getMelhoriasNaColuna (int coluna) {
		return manager.getJogadorDaVez().melhoriasNaCartela.get(coluna).size();
	}

	public int getTipoDaMelhoria (int coluna, int linha) {
		return manager.getJogadorDaVez().melhoriasNaCartela.get(coluna).get(linha).getTipo();
	}

	public int getCorDaMelhoria (int coluna, int linha) {
		return manager.getJogadorDaVez().melhoriasNaCartela.get(coluna).get(linha).getCor();
	}

	public ArrayList<Integer> getCartasAbertas () { return manager.getCartasAbertas(); }

	// ---------- acoes ----------

	public boolean ativarColuna (int coluna) { return manager.ativarColuna(coluna); }

	public void definirMovimentos (int quantidade) { manager.definirMovimentos(quantidade); }

	public boolean mover (int destino) { return manager.mover(destino); }

	public boolean pegarTributo (int pote) { return manager.pegarTributo(pote); }

	public boolean colocarKhan (int posicao) { return manager.colocarKhan(posicao); }

	// espaco 1 a 5 ou TOPO_DA_PILHA; colunaDeJochi so vale para Jochi.
	public boolean comprarMelhoria (int espaco, int colunaDeJochi) { return manager.comprarMelhoria(espaco, colunaDeJochi); }

	public boolean atacarCidade (int cidade, int tesouro) { return manager.atacarCidade(cidade, tesouro); }

	public boolean construirYurt (int parada) { return manager.construirYurt(parada); }

	public boolean usarHabilidade () { return manager.usarHabilidade(); }

	public boolean tomarComMoeda (int cidade, int tesouro) { return manager.tomarComMoeda(cidade, tesouro); }

	public boolean entregarAoConselheiro () { return manager.entregarAoConselheiro(); }

	public boolean escolherTesouro (int tipo) { return manager.escolherTesouro(tipo); }

	public void passarVez () { if (manager.podePassarVez()) manager.passarVez(); }

	// ---------- fim ----------

	public boolean isTerminada () { return manager.isTerminada(); }

	public int getVotosDeInfluencia (int jogador) { return manager.votosDeInfluencia(jogador); }

	public int getVotosDoSecreto (int jogador) { return manager.votosDoSecreto(jogador); }

	public int getVotosFinais (int jogador) { return manager.votosFinais(jogador); }

	public int getTesouros (int jogador) { return manager.getJogador(jogador).getTesouros(); }

	public ArrayList<Integer> getVencedores () { return manager.vencedores(); }
}
