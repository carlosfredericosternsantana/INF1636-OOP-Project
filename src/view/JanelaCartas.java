package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import controller.Controlador;
import model.Fachada;
import model.ObservadoIF;
import model.ObservadorIF;

// Janela de cartas, aberta e fechada pelo botao "Cartas" e pelo Observer: conselheiros abertos
// ainda em jogo, o conselheiro secreto e as pecas de bonus do jogador da vez.
// As imagens sao achadas pelo identificador que o model da a cada carta.
public class JanelaCartas extends JFrame implements ObservadorIF {

	private static final long serialVersionUID = 1L;

	private final PainelCartas painel = new PainelCartas();

	public JanelaCartas () {
		super("Cartas");
		add(painel);
		setResizable(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		Controlador.getInstancia().registra(this);
	}

	// Observer: a janela reflete o estado da partida. Uma entrega ao conselheiro
	// (evento CARTAS) muda as cartas e as pecas de bonus, entao a janela se abre
	// para mostra-las. No novo turno ela se fecha, para o conselheiro secreto do
	// proximo jogador nao aparecer sem ele pedir; no fim da partida tambem.
	@Override
	public void notify (ObservadoIF o) {
		int evento = o.get(ObservadoIF.EVENTO);
		if (evento == ObservadoIF.CARTAS) setVisible(true);
		else if (evento == ObservadoIF.NOVO_TURNO || evento == ObservadoIF.FIM) setVisible(false);
		painel.repaint();
	}

	private static class PainelCartas extends JPanel {

		private static final long serialVersionUID = 1L;

		private static final int LARGURA_DA_CARTA = 200, ALTURA_DA_CARTA = 314;
		private static final int MARGEM = 20, TOPO = 40, TAMANHO_DO_BONUS = 110;

		private final BufferedImage[] abertas = new BufferedImage[14];
		private final BufferedImage[] secretas = new BufferedImage[10];
		private final BufferedImage[] bonus = new BufferedImage[7];
		private final Fachada fachada = Fachada.getInstancia();

		PainelCartas () {
			for (int i = 0; i < abertas.length; i++) abertas[i] = PainelTabuleiro.carregar("aberto" + i + ".jpg");
			for (int i = 0; i < secretas.length; i++) secretas[i] = PainelTabuleiro.carregar("secreto" + i + ".jpg");
			for (int i = 0; i < bonus.length; i++) bonus[i] = PainelTabuleiro.carregar("bonus" + i + ".jpg");
			setPreferredSize(new Dimension(6 * MARGEM + 5 * LARGURA_DA_CARTA, TOPO + ALTURA_DA_CARTA + 60 + TAMANHO_DO_BONUS + MARGEM));
			setBackground(new Color(40, 34, 28));
		}

		@Override
		protected void paintComponent (Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setColor(Color.WHITE);
			g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
			int vez = fachada.getVez();

			g2.drawString("Conselheiros abertos", MARGEM, TOPO - 12);
			ArrayList<Integer> cartas = fachada.getCartasAbertas();
			for (int i = 0; i < cartas.size(); i++)
				g2.drawImage(abertas[cartas.get(i)], MARGEM + i * (LARGURA_DA_CARTA + MARGEM), TOPO,
						LARGURA_DA_CARTA, ALTURA_DA_CARTA, null);

			int xSecreta = MARGEM + 4 * (LARGURA_DA_CARTA + MARGEM) + MARGEM;
			g2.drawString("Secreto de " + fachada.getHerdeiro(vez), xSecreta, TOPO - 12);
			g2.drawImage(secretas[fachada.getConselheiroSecreto(vez)], xSecreta, TOPO, LARGURA_DA_CARTA, ALTURA_DA_CARTA, null);

			int yBonus = TOPO + ALTURA_DA_CARTA + 50;
			g2.drawString("Pecas de bonus de " + fachada.getHerdeiro(vez), MARGEM, yBonus - 12);
			ArrayList<Integer> pecas = fachada.getPecasDeBonus(vez);
			for (int i = 0; i < pecas.size(); i++)
				g2.drawImage(bonus[pecas.get(i)], MARGEM + i * (TAMANHO_DO_BONUS + MARGEM), yBonus,
						TAMANHO_DO_BONUS, TAMANHO_DO_BONUS, null);
		}
	}
}
