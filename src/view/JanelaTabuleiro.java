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
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import controller.Controlador;
import model.Fachada;

// Janela do tabuleiro. Os componentes Swing aqui so cuidam da ativacao das
// colunas, do ajuste do testador e da troca de vez; o jogo em si e desenhado
// pelo PainelTabuleiro com Java2D.
public class JanelaTabuleiro extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String[] NOMES_DAS_COLUNAS = { "Vermelha", "Verde", "Amarela", "Branca" };

	private final Fachada fachada = Fachada.getInstancia();
	private final PainelTabuleiro painelTabuleiro = new PainelTabuleiro();
	private final JButton[] botoesDeColuna = new JButton[Fachada.NUM_COLUNAS];
	private final JLabel rotuloDaVez = new JLabel("", SwingConstants.CENTER);
	private final JLabel rotuloDeMovimentos = new JLabel("", SwingConstants.CENTER);
	private final JPanel painelDaVez = new JPanel(new GridLayout(2, 1));
	private final JComboBox<Integer> seletorDeMovimentos = new JComboBox<Integer>();
	private final JButton botaoDeDefinir = new JButton("Definir movimentos");
	private final JButton botaoDePassar = new JButton("Passar a vez");

	public JanelaTabuleiro () {
		super("Herdeiros de Khan");
		Controlador controlador = new Controlador(this);

		painelTabuleiro.addMouseListener(controlador);
		for (int coluna = 0; coluna < botoesDeColuna.length; coluna++) {
			botoesDeColuna[coluna] = new JButton();
			botoesDeColuna[coluna].setActionCommand(String.valueOf(coluna));
			botoesDeColuna[coluna].addActionListener(controlador);
		}
		for (int valor = 1; valor <= 6; valor++) seletorDeMovimentos.addItem(Integer.valueOf(valor));
		botaoDeDefinir.setActionCommand(Controlador.DEFINIR);
		botaoDeDefinir.addActionListener(controlador);
		botaoDePassar.setActionCommand(Controlador.PASSAR);
		botaoDePassar.addActionListener(controlador);

		setLayout(new BorderLayout());
		add(painelTabuleiro, BorderLayout.CENTER);
		add(montarPainelDeControle(), BorderLayout.EAST);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		atualizar();
	}

	private JPanel montarPainelDeControle () {
		JPanel controle = new JPanel();
		controle.setLayout(new BoxLayout(controle, BoxLayout.Y_AXIS));
		controle.setPreferredSize(new Dimension(380, 1005));
		controle.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

		rotuloDaVez.setFont(rotuloDaVez.getFont().deriveFont(Font.BOLD, 24f));
		rotuloDeMovimentos.setFont(rotuloDeMovimentos.getFont().deriveFont(Font.BOLD, 20f));
		// A area da vez e pintada com a cor do jogador, como pede o enunciado.
		painelDaVez.add(rotuloDaVez);
		painelDaVez.add(rotuloDeMovimentos);
		painelDaVez.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
		painelDaVez.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
		controle.add(painelDaVez);
		controle.add(Box.createVerticalStrut(24));

		controle.add(titulo("Ativar uma coluna da cartela"));
		for (JButton botao : botoesDeColuna) {
			botao.setAlignmentX(Component.CENTER_ALIGNMENT);
			botao.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
			controle.add(botao);
			controle.add(Box.createVerticalStrut(6));
		}

		controle.add(Box.createVerticalStrut(24));
		controle.add(titulo("Testador"));
		seletorDeMovimentos.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
		seletorDeMovimentos.setAlignmentX(Component.CENTER_ALIGNMENT);
		botaoDeDefinir.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
		botaoDeDefinir.setAlignmentX(Component.CENTER_ALIGNMENT);
		controle.add(seletorDeMovimentos);
		controle.add(Box.createVerticalStrut(6));
		controle.add(botaoDeDefinir);

		controle.add(Box.createVerticalGlue());
		botaoDePassar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
		botaoDePassar.setAlignmentX(Component.CENTER_ALIGNMENT);
		controle.add(botaoDePassar);
		return controle;
	}

	private JLabel titulo (String texto) {
		JLabel rotulo = new JLabel(texto);
		rotulo.setAlignmentX(Component.CENTER_ALIGNMENT);
		rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD, 14f));
		return rotulo;
	}

	public PainelTabuleiro getPainelTabuleiro () { return painelTabuleiro; }

	public int getMovimentosEscolhidos () {
		return ((Integer) seletorDeMovimentos.getSelectedItem()).intValue();
	}

	public void atualizar () {
		int vez = fachada.getVez();
		Color cor = PainelTabuleiro.CORES[vez % PainelTabuleiro.CORES.length];
		painelDaVez.setBackground(cor);
		rotuloDaVez.setForeground(Color.WHITE);
		rotuloDeMovimentos.setForeground(Color.WHITE);
		rotuloDaVez.setText("Vez de " + fachada.getHerdeiro(vez));
		rotuloDeMovimentos.setText(fachada.getMovimentos() + " movimento(s)");

		for (int coluna = 0; coluna < botoesDeColuna.length; coluna++) {
			botoesDeColuna[coluna].setText(NOMES_DAS_COLUNAS[coluna]
					+ "  -  " + fachada.getCavalosDaColuna(coluna) + " cavalo(s)");
			botoesDeColuna[coluna].setEnabled(fachada.getColunaAtivada() == Fachada.NENHUMA_COLUNA
					&& fachada.colunaDisponivel(coluna));
		}
		painelTabuleiro.repaint();
	}
}
