package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

import controller.Controlador;
import model.Fachada;

// Antes da 1a rodada: um jogador por vez clica numa das suas 2 cartas de
// conselheiro secreto; Jochi depois clica numa das 2 melhorias para a coluna
// branca. Quem escolhe e o que escolher vem do model.
public class JanelaEscolha extends JFrame {

	private static final long serialVersionUID = 1L;

	private final PainelEscolha painel = new PainelEscolha();

	public JanelaEscolha () {
		super("Escolhas antes da 1a rodada");
		add(painel);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
	}

	public void mostrar (int jogador) {
		painel.jogador = jogador;
		painel.repaint();
		setVisible(true);
	}

	public static class PainelEscolha extends JPanel {

		private static final long serialVersionUID = 1L;

		private static final int LARGURA = 250, ALTURA = 397;            // tamanho da carta
		private static final int ALTURA_DA_MELHORIA = LARGURA * 100 / 190;
		private static final int MARGEM = 30, TOPO = 60;

		private final BufferedImage[] secretas = new BufferedImage[10];
		private final BufferedImage[][] melhorias = new BufferedImage[PainelTabuleiro.NOMES_DOS_RECURSOS.length][3];
		private final Fachada fachada = Fachada.getInstancia();
		private int jogador;

		PainelEscolha () {
			for (int i = 0; i < secretas.length; i++) secretas[i] = PainelTabuleiro.carregar("secreto" + i + ".jpg");
			PainelTabuleiro.carregarMelhorias(melhorias);
			setPreferredSize(new Dimension(3 * MARGEM + 2 * LARGURA, TOPO + ALTURA + MARGEM));
			setBackground(new Color(40, 34, 28));
			addMouseListener(Controlador.getInstancia());
		}

		private boolean escolhendoSecreto () {
			return fachada.getConselheiroSecreto(jogador) < 0;
		}

		@Override
		protected void paintComponent (Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g2.setColor(PainelTabuleiro.CORES[jogador % PainelTabuleiro.CORES.length]);
			g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
			String pedido = escolhendoSecreto() ? "escolha seu conselheiro secreto" : "escolha a melhoria da coluna branca";
			g2.drawString(PainelTabuleiro.nomeDoJogador(jogador) + ", " + pedido, MARGEM, TOPO - 22);
			for (int opcao = 0; opcao < 2; opcao++) {
				int x = MARGEM + opcao * (LARGURA + MARGEM);
				if (escolhendoSecreto()) {
					g2.drawImage(secretas[fachada.getOpcaoDeSecreto(jogador, opcao)], x, TOPO, LARGURA, ALTURA, null);
				} else {
					BufferedImage imagem = melhorias[fachada.getTipoDaOpcaoDeMelhoria(opcao)][fachada.getCorDaOpcaoDeMelhoria(opcao)];
					g2.drawImage(imagem, x, TOPO + (ALTURA - ALTURA_DA_MELHORIA) / 2, LARGURA, ALTURA_DA_MELHORIA, null);
				}
			}
		}

		// Opcao clicada (0 ou 1), ou -1.
		public int opcaoEm (int x, int y) {
			if (y < TOPO || y > TOPO + ALTURA) return -1;
			for (int opcao = 0; opcao < 2; opcao++) {
				int esquerda = MARGEM + opcao * (LARGURA + MARGEM);
				if (x >= esquerda && x <= esquerda + LARGURA) return opcao;
			}
			return -1;
		}
	}
}
