package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import model.Fachada;
import model.ObservadorIF;
import view.JanelaHerdeiros;
import view.JanelaInicial;
import view.JanelaTabuleiro;
import view.PainelCartela;
import view.PainelTabuleiro;

// Organiza a sequencia de uma partida: janela inicial, escolha dos herdeiros,
// tabuleiro e jogadas. E um Singleton, como o Ctrl do Capitulo 17.
// Tratamento de eventos sem expressoes lambda, como pede o enunciado.
public class Controlador implements ActionListener, MouseListener {

	public static final String NOVA = "nova";
	public static final String QUANTIDADE = "quantidade";
	public static final String COMECAR = "comecar";
	public static final String DEFINIR = "definir";
	public static final String PASSAR = "passar";
	public static final String YURT = "yurt";
	public static final String ENTREGAR = "entregar";
	public static final String CARTELA = "cartela";
	public static final String CARTAS = "cartas";
	// comandos com parametros: "atacar:<cidade>:<tesouro>" e "tesouro:<tipo>"
	public static final String ATACAR = "atacar:";
	public static final String TESOURO = "tesouro:";

	private static Controlador instancia;

	private final Fachada fachada = Fachada.getInstancia();
	private JanelaInicial janelaInicial;
	private JanelaHerdeiros janelaHerdeiros;
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
		if (NOVA.equals(comando)) escolherHerdeiros();
		else if (QUANTIDADE.equals(comando)) janelaHerdeiros.atualizarQuantidade();
		else if (COMECAR.equals(comando)) comecarPartida();
		else if (DEFINIR.equals(comando)) fachada.definirMovimentos(janelaTabuleiro.getMovimentosEscolhidos());
		else if (YURT.equals(comando)) fachada.construirYurt();
		else if (ENTREGAR.equals(comando)) fachada.entregarAoConselheiro();
		else if (CARTELA.equals(comando)) janelaTabuleiro.alternarCartela();
		else if (CARTAS.equals(comando)) janelaTabuleiro.alternarCartas();
		else if (PASSAR.equals(comando)) fachada.passarVez();
		else if (comando.startsWith(TESOURO)) fachada.escolherTesouro(Integer.parseInt(comando.substring(TESOURO.length())));
		else if (comando.startsWith(ATACAR)) {
			String[] partes = comando.split(":");
			fachada.atacarCidade(Integer.parseInt(partes[1]), Integer.parseInt(partes[2]));
		}
	}

	private void escolherHerdeiros () {
		janelaInicial.dispose();
		janelaHerdeiros = new JanelaHerdeiros();
		janelaHerdeiros.setVisible(true);
	}

	private void comecarPartida () {
		int[] herdeiros = janelaHerdeiros.getHerdeirosEscolhidos();
		if (herdeiros == null) return;   // a propria janela avisa que houve herdeiro repetido
		janelaHerdeiros.dispose();
		fachada.novaPartida(herdeiros);
		janelaTabuleiro = new JanelaTabuleiro();
		janelaTabuleiro.setVisible(true);
	}

	@Override
	public void mouseClicked (MouseEvent evento) {
		Object origem = evento.getSource();
		if (origem instanceof PainelCartela) {
			int coluna = ((PainelCartela) origem).colunaEm(evento.getX(), evento.getY());
			if (coluna >= 0) fachada.ativarColuna(coluna);
		} else if (origem instanceof PainelTabuleiro) {
			cliqueNoTabuleiro((PainelTabuleiro) origem, evento.getX(), evento.getY());
		}
	}

	// Cada clique aciona no maximo uma regra. Se ela nao puder ser aplicada,
	// nada acontece, como no deslocamento dos pioes.
	private void cliqueNoTabuleiro (PainelTabuleiro painel, int x, int y) {
		if (!fachada.getTesourosParaEscolher().isEmpty()) {
			janelaTabuleiro.mostrarEscolhaDeTesouro();
			return;
		}
		int khan = painel.khanEm(x, y);
		int pote = painel.poteEm(x, y);
		int espaco = painel.espacoDeMelhoriaEm(x, y);
		int cidade = painel.cidadeEm(x, y);
		if (khan >= 0) fachada.colocarKhan(khan);
		else if (pote >= 0) fachada.pegarTributo(pote);
		else if (espaco >= 0) fachada.comprarMelhoria(espaco);
		else if (cidade >= 0) {
			if (fachada.podeAtacar(cidade)) painel.mostrarAtaque(cidade, x, y);
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
