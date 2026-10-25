package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import model.Fachada;

// O tabuleiro e os pioes sao desenhados somente com drawImage(), da Graphics2D.
// Nenhum componente Swing e usado para exibir imagens do jogo.
public class PainelTabuleiro extends JPanel {

	private static final long serialVersionUID = 1L;

	public static final Color[] CORES = {
		new Color(200, 45, 45), new Color(45, 85, 195), new Color(45, 150, 70),
		new Color(230, 185, 45), new Color(130, 60, 175)
	};

	// Centro de cada parada em pixels da imagem do tabuleiro, na ordem do model:
	// 0 a 26 paradas simples, 27 a 32 paradas duplas e 33 Karakorum.
	private static final int[][] PARADAS = {
		{ 134,  541,  70}, { 496,  258,  70}, { 477,  540,  70},   // Kiev
		{ 862,  558,  70}, {1279,  499,  70}, {1090,  846,  70},   // Moscou
		{1566,  674,  70}, {1761,  231,  70}, {1899,  595,  70},   // Sarai
		{2503,  660,  70}, {2778,  889,  70}, {2533, 1082,  70},   // Kaesong
		{2154, 1259,  70}, {2177, 1686,  70}, {1890, 1600,  70},   // Pequim
		{2538, 2036,  70}, {2607, 2423,  70}, {2239, 2237,  70},   // Cantao
		{1483, 2061,  70}, {1221, 2240,  70}, {1280, 1824,  70},   // Cabul
		{ 459, 1647,  70}, { 186, 1916,  70}, { 223, 1496,  70},   // Bagda
		{ 579, 1417,  70}, { 963, 1559,  70}, { 848, 1196,  70},   // Samarcanda
		{ 897,  214, 105}, {2244,  500, 105}, {2571, 1568, 105},   // duplas 27, 28, 29
		{1841, 2232, 105}, { 731, 1890, 105}, { 468,  898, 105},   // duplas 30, 31, 32
		{1443, 1414, 160}                                          // Karakorum
	};

	private static final int LARGURA_PEAO = 95;
	private static final int ALTURA_PEAO = 130;
	private static final int ESPACO_ENTRE_PIOES = 62;

	private final BufferedImage tabuleiro;
	private final BufferedImage[] pioes;
	private final Fachada fachada = Fachada.getInstancia();

	private double escala = 1;
	private int margemX = 0;
	private int margemY = 0;

	public PainelTabuleiro () {
		tabuleiro = carregar("tabuleiro.jpg");
		pioes = new BufferedImage[CORES.length];
		for (int i = 0; i < pioes.length; i++) pioes[i] = carregar("peao" + i + ".png");
		setPreferredSize(new Dimension(1000, 1005));
		setBackground(new Color(30, 26, 22));
	}

	private BufferedImage carregar (String arquivo) {
		try {
			return ImageIO.read(PainelTabuleiro.class.getResource("imagens/" + arquivo));
		} catch (IOException erro) {
			throw new IllegalStateException("nao foi possivel carregar imagens/" + arquivo, erro);
		}
	}

	@Override
	protected void paintComponent (Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		escala = Math.min((double) getWidth() / tabuleiro.getWidth(),
		                  (double) getHeight() / tabuleiro.getHeight());
		int largura = (int) (tabuleiro.getWidth() * escala);
		int altura = (int) (tabuleiro.getHeight() * escala);
		margemX = (getWidth() - largura) / 2;
		margemY = (getHeight() - altura) / 2;

		g2.drawImage(tabuleiro, margemX, margemY, largura, altura, null);
		desenharAlcancaveis(g2);
		desenharPioes(g2);
	}

	private void desenharAlcancaveis (Graphics2D g2) {
		ArrayList<Integer> alcancaveis = fachada.getParadasAlcancaveis();
		g2.setStroke(new BasicStroke((float) Math.max(2, 10 * escala)));
		g2.setColor(new Color(255, 255, 255, 190));
		for (int parada : alcancaveis) {
			int raio = (int) (PARADAS[parada][2] * escala);
			g2.drawOval(telaX(PARADAS[parada][0]) - raio, telaY(PARADAS[parada][1]) - raio, raio * 2, raio * 2);
		}
	}

	private void desenharPioes (Graphics2D g2) {
		for (int parada = 0; parada < Fachada.NUM_PARADAS; parada++) {
			ArrayList<Integer> naParada = new ArrayList<Integer>();
			for (int jogador = 0; jogador < fachada.getQuantidadeDeJogadores(); jogador++)
				if (fachada.getParada(jogador) == parada) naParada.add(jogador);
			for (int i = 0; i < naParada.size(); i++) {
				int deslocamento = (int) ((i - (naParada.size() - 1) / 2.0) * ESPACO_ENTRE_PIOES);
				desenharPeao(g2, naParada.get(i), PARADAS[parada][0] + deslocamento, PARADAS[parada][1]);
			}
		}
	}

	// O pe do piao fica sobre o centro da parada.
	private void desenharPeao (Graphics2D g2, int jogador, int x, int y) {
		int largura = (int) (LARGURA_PEAO * escala);
		int altura = (int) (ALTURA_PEAO * escala);
		g2.drawImage(pioes[jogador % pioes.length],
				telaX(x) - largura / 2, telaY(y) - altura + altura / 6, largura, altura, null);
	}

	private int telaX (int x) { return margemX + (int) (x * escala); }

	private int telaY (int y) { return margemY + (int) (y * escala); }

	// Devolve a parada clicada, ou -1 se o clique caiu fora de todas elas.
	public int paradaEm (int telaX, int telaY) {
		if (escala <= 0) return -1;
		double x = (telaX - margemX) / escala;
		double y = (telaY - margemY) / escala;
		int maisProxima = -1;
		double menorDistancia = Double.MAX_VALUE;
		for (int parada = 0; parada < PARADAS.length; parada++) {
			double dx = x - PARADAS[parada][0];
			double dy = y - PARADAS[parada][1];
			double distancia = Math.sqrt(dx * dx + dy * dy);
			if (distancia <= PARADAS[parada][2] && distancia < menorDistancia) {
				menorDistancia = distancia;
				maisProxima = parada;
			}
		}
		return maisProxima;
	}
}
