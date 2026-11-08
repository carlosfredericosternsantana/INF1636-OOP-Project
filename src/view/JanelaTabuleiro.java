package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import controller.Controlador;
import model.Fachada;
import model.ObservadoIF;
import model.ObservadorIF;

// Janela do tabuleiro. Os componentes Swing aqui so mostram texto e disparam
// acoes; o jogo em si e desenhado pelo PainelTabuleiro com Java2D. A janela
// observa o model e se atualiza a cada notificacao.
public class JanelaTabuleiro extends JFrame implements ObservadorIF {

	private static final long serialVersionUID = 1L;

	private static final String[] NOMES = PainelTabuleiro.NOMES_DOS_RECURSOS;

	private final Fachada fachada = Fachada.getInstancia();
	private final PainelTabuleiro painelTabuleiro = new PainelTabuleiro();
	private final JanelaCartela janelaCartela = new JanelaCartela();
	private final JanelaCartas janelaCartas = new JanelaCartas();
	private final JLabel rotuloDaVez = new JLabel("", SwingConstants.CENTER);
	private final JLabel rotuloDeMovimentos = new JLabel("", SwingConstants.CENTER);
	private final JPanel painelDaVez = new JPanel(new GridLayout(2, 1));
	private final JLabel rotuloDoTurno = new JLabel();
	private final JLabel rotuloDoJogador = new JLabel();
	private final JLabel rotuloDoKurultai = new JLabel();
	private final JButton botaoDeYurt = botao("Construir yurt aqui", Controlador.YURT);
	private final JButton botaoDeEntrega = botao("Entregar ao conselheiro", Controlador.ENTREGAR);
	private final JButton botaoDaCartela = botao("Cartela", Controlador.CARTELA);
	private final JButton botaoDeCartas = botao("Cartas", Controlador.CARTAS);
	private final JComboBox<Integer> seletorDeMovimentos = new JComboBox<Integer>();
	private final JButton botaoDeDefinir = botao("Definir movimentos", Controlador.DEFINIR);
	private final JButton botaoDePassar = botao("Passar a vez", Controlador.PASSAR);

	public JanelaTabuleiro () {
		super("Herdeiros de Khan");
		for (int valor = 1; valor <= 6; valor++) seletorDeMovimentos.addItem(Integer.valueOf(valor));
		setLayout(new BorderLayout());
		add(painelTabuleiro, BorderLayout.CENTER);
		add(montarPainelDeControle(), BorderLayout.EAST);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		atualizar();
		pack();
		setLocationRelativeTo(null);
		Controlador.getInstancia().registra(this);
		janelaCartela.setVisible(true);
	}

	private static JButton botao (String texto, String comando) {
		JButton botao = new JButton(texto);
		botao.setActionCommand(comando);
		botao.addActionListener(Controlador.getInstancia());
		botao.setAlignmentX(Component.CENTER_ALIGNMENT);
		botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		return botao;
	}

	private JPanel montarPainelDeControle () {
		JPanel controle = new JPanel();
		controle.setLayout(new BoxLayout(controle, BoxLayout.Y_AXIS));
		controle.setPreferredSize(new Dimension(380, 1005));
		controle.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

		rotuloDaVez.setFont(rotuloDaVez.getFont().deriveFont(Font.BOLD, 24f));
		rotuloDeMovimentos.setFont(rotuloDeMovimentos.getFont().deriveFont(Font.BOLD, 20f));
		rotuloDaVez.setForeground(Color.WHITE);
		rotuloDeMovimentos.setForeground(Color.WHITE);
		// A area da vez e pintada com a cor do jogador, como pede o enunciado.
		painelDaVez.add(rotuloDaVez);
		painelDaVez.add(rotuloDeMovimentos);
		painelDaVez.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
		painelDaVez.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
		controle.add(painelDaVez);

		for (JLabel rotulo : new JLabel[] { rotuloDoTurno, rotuloDoJogador, rotuloDoKurultai }) {
			controle.add(Box.createVerticalStrut(14));
			rotulo.setAlignmentX(Component.CENTER_ALIGNMENT);
			controle.add(rotulo);
		}

		controle.add(Box.createVerticalStrut(18));
		for (JButton botao : new JButton[] { botaoDeYurt, botaoDeEntrega, botaoDaCartela, botaoDeCartas }) {
			controle.add(botao);
			controle.add(Box.createVerticalStrut(6));
		}

		controle.add(Box.createVerticalStrut(18));
		JLabel testador = new JLabel("Testador");
		testador.setAlignmentX(Component.CENTER_ALIGNMENT);
		testador.setFont(testador.getFont().deriveFont(Font.BOLD, 14f));
		controle.add(testador);
		seletorDeMovimentos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
		seletorDeMovimentos.setAlignmentX(Component.CENTER_ALIGNMENT);
		controle.add(seletorDeMovimentos);
		controle.add(Box.createVerticalStrut(6));
		controle.add(botaoDeDefinir);

		controle.add(Box.createVerticalGlue());
		botaoDePassar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
		controle.add(botaoDePassar);
		return controle;
	}

	public int getMovimentosEscolhidos () {
		return ((Integer) seletorDeMovimentos.getSelectedItem()).intValue();
	}

	public void alternarCartela () { janelaCartela.setVisible(!janelaCartela.isVisible()); }

	public void alternarCartas () { janelaCartas.setVisible(!janelaCartas.isVisible()); }

	// Menu com os tesouros sacados pela peca de bonus; o jogador fica com um.
	public void mostrarEscolhaDeTesouro () {
		JPopupMenu menu = new JPopupMenu("Escolha um tesouro");
		for (int tipo : fachada.getTesourosParaEscolher()) {
			JMenuItem item = new JMenuItem("Ficar com " + NOMES[tipo]);
			item.setActionCommand(Controlador.TESOURO + tipo);
			item.addActionListener(Controlador.getInstancia());
			menu.add(item);
		}
		menu.show(painelTabuleiro, painelTabuleiro.getWidth() / 2, painelTabuleiro.getHeight() / 2);
	}

	@Override
	public void notify (ObservadoIF o) {
		int evento = o.get(ObservadoIF.EVENTO);
		atualizar();
		if (evento == ObservadoIF.ESCOLHA) mostrarEscolhaDeTesouro();
		else if (evento == ObservadoIF.FIM) new JanelaResultado().setVisible(true);
	}

	private void atualizar () {
		int vez = fachada.getVez();
		painelDaVez.setBackground(PainelTabuleiro.CORES[vez % PainelTabuleiro.CORES.length]);
		rotuloDaVez.setText("Vez de " + fachada.getHerdeiro(vez));
		rotuloDeMovimentos.setText(fachada.getMovimentos() + " movimento(s)");

		String coluna = fachada.getColunaAtivada() == Fachada.NENHUMA_COLUNA ? "escolha uma coluna na cartela" : "coluna ativada";
		rotuloDoTurno.setText("<html><b>Turno:</b> " + coluna
				+ "<br>Punhos (tributos): " + fachada.getRecursoDoTurno(Fachada.MAOZINHA)
				+ "<br>Khan: " + (fachada.isKhanPendente() ? "falta colocar" : "-")
				+ (fachada.getMelhoriasGratis() > 0 ? "<br>Melhoria gratis: clique numa melhoria" : "")
				+ "<br>Virtuais: " + listar(true, Fachada.ESPADA, Fachada.YURT, Fachada.MOEDA,
						Fachada.GRAOS, Fachada.TECIDO, Fachada.FERRO, Fachada.PELE, Fachada.VACA) + "</html>");

		rotuloDoJogador.setText("<html><b>Recursos:</b> " + listar(false, Fachada.ESPADA, Fachada.YURT, Fachada.MOEDA)
				+ "<br><b>Tesouros:</b> " + listar(false, Fachada.GRAOS, Fachada.TECIDO, Fachada.FERRO, Fachada.PELE, Fachada.VACA)
				+ "<br>Yurts no estoque: " + fachada.getYurtsRestantes(vez) + "</html>");

		StringBuilder kurultai = new StringBuilder("<html><b>Kurultai</b> (" + fachada.getTotalDeVotos() + " votos)");
		for (int j = 0; j < fachada.getQuantidadeDeJogadores(); j++)
			kurultai.append("<br>").append(fachada.getHerdeiro(j)).append(": ").append(fachada.getVotos(j));
		rotuloDoKurultai.setText(kurultai.append("</html>").toString());

		boolean emJogo = !fachada.isTerminada();
		botaoDeYurt.setEnabled(fachada.podeConstruirYurt());
		botaoDeEntrega.setEnabled(fachada.podeEntregar());
		botaoDeDefinir.setEnabled(emJogo);
		botaoDePassar.setEnabled(fachada.podePassarVez());
	}

	// "Espada 2, Moeda 1" com os recursos que o jogador tem (do turno ou da mao).
	private String listar (boolean doTurno, int... tipos) {
		StringBuilder texto = new StringBuilder();
		for (int tipo : tipos) {
			int quantidade = doTurno ? fachada.getRecursoDoTurno(tipo) : fachada.getRecurso(fachada.getVez(), tipo);
			if (quantidade == 0 && doTurno) continue;
			if (texto.length() > 0) texto.append(", ");
			texto.append(NOMES[tipo]).append(' ').append(quantidade);
		}
		return texto.length() == 0 ? "-" : texto.toString();
	}
}
