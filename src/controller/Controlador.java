package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.Fachada;
import model.ObservadoIF;
import model.ObservadorIF;
import view.JanelaEscolha;
import view.JanelaHerdeiros;
import view.JanelaInicial;
import view.JanelaResultado;
import view.JanelaTabuleiro;
import view.PainelCartela;
import view.PainelTabuleiro;

// Organiza a sequencia de uma partida: janela inicial, escolha dos herdeiros,
// escolhas antes da 1a rodada, tabuleiro, jogadas, salvamento e fim. E um
// Singleton, como o Ctrl do Capitulo 17, e observa o model para tratar o fim.
// Tratamento de eventos sem expressoes lambda, como pede o enunciado.
public class Controlador implements ActionListener, MouseListener, ObservadorIF {

	public static final String NOVA = "nova";
	public static final String CONTINUAR = "continuar";
	public static final String QUANTIDADE = "quantidade";
	public static final String COMECAR = "comecar";
	public static final String DEFINIR = "definir";
	public static final String PASSAR = "passar";
	public static final String SALVAR = "salvar";
	public static final String YURT = "yurt";
	public static final String ENTREGAR = "entregar";
	public static final String TOPO = "topo";
	public static final String HABILIDADE = "habilidade";
	public static final String CARTELA = "cartela";
	public static final String CARTAS = "cartas";
	// comandos com parametros: "atacar:<cidade>:<tesouro>", "moeda:<cidade>:<tesouro>",
	// "melhoria:<espaco>:<coluna>" e "tesouro:<tipo>"
	public static final String ATACAR = "atacar:";
	public static final String MOEDA = "moeda:";
	public static final String MELHORIA = "melhoria:";
	public static final String TESOURO = "tesouro:";

	private static Controlador instancia;

	private final Fachada fachada = Fachada.getInstancia();
	private JanelaInicial janelaInicial;
	private JanelaHerdeiros janelaHerdeiros;
	private JanelaEscolha janelaEscolha;
	private JanelaTabuleiro janelaTabuleiro;

	private Controlador () {}

	public static Controlador getInstancia () {
		if (instancia == null) instancia = new Controlador();
		return instancia;
	}

	public void iniciar () {
		janelaInicial = new JanelaInicial();
		janelaInicial.setVisible(true);
	}

	// As janelas do jogo se registram como observadoras por aqui.
	public void registra (ObservadorIF o) {
		fachada.registra(o);
	}

	@Override
	public void actionPerformed (ActionEvent evento) {
		String comando = evento.getActionCommand();
		if (NOVA.equals(comando)) {
			janelaInicial.dispose();
			escolherHerdeiros();
		}
		else if (CONTINUAR.equals(comando)) continuarPartida();
		else if (QUANTIDADE.equals(comando)) janelaHerdeiros.atualizarQuantidade();
		else if (COMECAR.equals(comando)) comecarPartida();
		else if (DEFINIR.equals(comando)) fachada.definirMovimentos(janelaTabuleiro.getMovimentosEscolhidos());
		else if (YURT.equals(comando)) construirYurt();
		else if (ENTREGAR.equals(comando)) fachada.entregarAoConselheiro();
		else if (TOPO.equals(comando)) {
			if (fachada.escolheColunaDaMelhoria()) janelaTabuleiro.mostrarColunasParaOTopo();
			else fachada.comprarMelhoria(Fachada.TOPO_DA_PILHA, -1);
		}
		else if (HABILIDADE.equals(comando)) fachada.usarHabilidade();
		else if (CARTELA.equals(comando)) janelaTabuleiro.alternarCartela();
		else if (CARTAS.equals(comando)) janelaTabuleiro.alternarCartas();
		else if (SALVAR.equals(comando)) salvarPartida();
		else if (PASSAR.equals(comando)) fachada.passarVez();
		else if (comando.startsWith(TESOURO)) fachada.escolherTesouro(parametro(comando, 1));
		else if (comando.startsWith(ATACAR)) fachada.atacarCidade(parametro(comando, 1), parametro(comando, 2));
		else if (comando.startsWith(MOEDA)) fachada.tomarComMoeda(parametro(comando, 1), parametro(comando, 2));
		else if (comando.startsWith(MELHORIA)) fachada.comprarMelhoria(parametro(comando, 1), parametro(comando, 2));
	}

	// n-esimo numero de um comando como "atacar:<cidade>:<tesouro>".
	private static int parametro (String comando, int n) {
		return Integer.parseInt(comando.split(":")[n]);
	}

	private void escolherHerdeiros () {
		janelaHerdeiros = new JanelaHerdeiros();
		janelaHerdeiros.setVisible(true);
	}

	private void comecarPartida () {
		int[] herdeiros = janelaHerdeiros.getHerdeirosEscolhidos();
		if (herdeiros == null) return;   // a propria janela avisa que houve herdeiro repetido
		fachada.novaPartida(herdeiros, janelaHerdeiros.getNomes());
		janelaHerdeiros.dispose();
		janelaEscolha = new JanelaEscolha();
		proximaEscolha();
	}

	// Antes da 1a rodada cada jogador escolhe o conselheiro secreto (e Jochi a
	// melhoria inicial). Quando todos escolheram, o tabuleiro abre.
	private void proximaEscolha () {
		int jogador = fachada.getJogadorEscolhendo();
		if (jogador >= 0) {
			janelaEscolha.mostrar(jogador);
			return;
		}
		janelaEscolha.dispose();
		abrirTabuleiro();
	}

	private void abrirTabuleiro () {
		janelaTabuleiro = new JanelaTabuleiro();
		janelaTabuleiro.setVisible(true);
		fachada.registra(this);   // por ultimo: no fim, as janelas ja estao atualizadas
	}

	// ---------- salvamento e recuperacao ----------

	// O nome e o local do arquivo sao livres; so a extensao .txt e fixa.
	private static JFileChooser seletorDeArquivo () {
		JFileChooser seletor = new JFileChooser();
		seletor.setFileFilter(new FileNameExtensionFilter("Partida de Herdeiros de Khan (*.txt)", "txt"));
		return seletor;
	}

	private void continuarPartida () {
		JFileChooser seletor = seletorDeArquivo();
		if (seletor.showOpenDialog(janelaInicial) != JFileChooser.APPROVE_OPTION) return;
		try {
			fachada.carregarPartida(seletor.getSelectedFile());
		} catch (IOException erro) {
			JOptionPane.showMessageDialog(janelaInicial, "Nao foi possivel carregar a partida: " + erro.getMessage(),
					"Continuar partida", JOptionPane.ERROR_MESSAGE);
			return;
		}
		janelaInicial.dispose();
		abrirTabuleiro();
	}

	private void salvarPartida () {
		JFileChooser seletor = seletorDeArquivo();
		if (seletor.showSaveDialog(janelaTabuleiro) != JFileChooser.APPROVE_OPTION) return;
		File arquivo = seletor.getSelectedFile();
		if (!arquivo.getName().toLowerCase().endsWith(".txt")) arquivo = new File(arquivo.getPath() + ".txt");
		if (arquivo.exists() && JOptionPane.showConfirmDialog(janelaTabuleiro, "O arquivo " + arquivo.getName()
				+ " ja existe. Substituir?", "Salvar partida", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
		try {
			fachada.salvarPartida(arquivo);
		} catch (IOException erro) {
			JOptionPane.showMessageDialog(janelaTabuleiro, "Nao foi possivel salvar a partida: " + erro.getMessage(),
					"Salvar partida", JOptionPane.ERROR_MESSAGE);
		}
	}

	// ---------- fim da partida ----------

	// Mostra o vencedor e pergunta se os jogadores querem outra partida.
	@Override
	public void notify (ObservadoIF o) {
		if (o.get(ObservadoIF.EVENTO) != ObservadoIF.FIM) return;
		JanelaResultado resultado = new JanelaResultado();
		resultado.setVisible(true);
		int resposta = JOptionPane.showConfirmDialog(resultado, "Desejam iniciar uma nova partida?",
				"Fim da partida", JOptionPane.YES_NO_OPTION);
		if (resposta != JOptionPane.YES_OPTION) System.exit(0);
		resultado.dispose();
		janelaTabuleiro.dispose();
		escolherHerdeiros();
	}

	// ---------- cliques ----------

	// Com mais de uma parada possivel (Tolui), o jogador clica numa das destacadas.
	private void construirYurt () {
		ArrayList<Integer> paradas = fachada.getParadasParaYurt();
		if (paradas.size() == 1) fachada.construirYurt(paradas.get(0));
		else if (paradas.size() > 1) janelaTabuleiro.escolherParadaDoYurt();
	}

	@Override
	public void mouseClicked (MouseEvent evento) {
		Object origem = evento.getSource();
		if (origem instanceof PainelCartela) {
			int coluna = ((PainelCartela) origem).colunaEm(evento.getX(), evento.getY());
			if (coluna >= 0) fachada.ativarColuna(coluna);
		} else if (origem instanceof PainelTabuleiro) {
			cliqueNoTabuleiro((PainelTabuleiro) origem, evento.getX(), evento.getY());
		} else if (origem instanceof JanelaEscolha.PainelEscolha) {
			int opcao = ((JanelaEscolha.PainelEscolha) origem).opcaoEm(evento.getX(), evento.getY());
			if (opcao >= 0 && fachada.escolherNoInicio(opcao)) proximaEscolha();
		}
	}

	// Cada clique aciona no maximo uma regra. Se ela nao puder ser aplicada,
	// nada acontece, como no deslocamento dos pioes.
	private void cliqueNoTabuleiro (PainelTabuleiro painel, int x, int y) {
		if (!fachada.getTesourosParaEscolher().isEmpty()) {
			janelaTabuleiro.mostrarEscolhaDeTesouro();
			return;
		}
		if (painel.isEscolhendoYurt()) {
			painel.setEscolhendoYurt(false);
			int parada = painel.paradaEm(x, y);
			if (parada >= 0) fachada.construirYurt(parada);
			return;
		}
		int khan = painel.khanEm(x, y);
		int pote = painel.poteEm(x, y);
		int espaco = painel.espacoDeMelhoriaEm(x, y);
		int cidade = painel.cidadeEm(x, y);
		if (khan >= 0) fachada.colocarKhan(khan);
		else if (pote >= 0) fachada.pegarTributo(pote);
		else if (espaco >= 0) {
			if (fachada.escolheColunaDaMelhoria() && fachada.getTipoDaMelhoriaNaArea(espaco) >= 0)
				painel.mostrarColunas(espaco, x, y);
			else fachada.comprarMelhoria(espaco, -1);
		}
		else if (cidade >= 0) {
			if (fachada.podeAtacar(cidade) || fachada.podeTomarComMoeda(cidade)) painel.mostrarAtaque(cidade, x, y);
		} else {
			int parada = painel.paradaEm(x, y);
			if (parada >= 0) fachada.mover(parada);
		}
	}

	@Override
	public void mousePressed (MouseEvent evento) {}

	@Override
	public void mouseReleased (MouseEvent evento) {}

	@Override
	public void mouseEntered (MouseEvent evento) {}

	@Override
	public void mouseExited (MouseEvent evento) {}
}
