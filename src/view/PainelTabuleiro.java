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
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import controller.Controlador;
import model.Fachada;
import model.ObservadoIF;
import model.ObservadorIF;

// O tabuleiro e tudo o que fica sobre ele sao desenhados com Graphics2D; as
// imagens dos elementos de jogo, somente com drawImage(). Coordenadas em pixels
// da imagem do tabuleiro (2905 x 2919).
public class PainelTabuleiro extends JPanel implements ObservadorIF {

	private static final long serialVersionUID = 1L;

	public static final Color[] CORES = {
		new Color(200, 45, 45), new Color(45, 85, 195), new Color(45, 150, 70),
		new Color(230, 185, 45), new Color(130, 60, 175)
	};

	public static final String[] NOMES_DOS_RECURSOS = {
		"Voto", "Espada", "Punho", "Cavalo", "Khan", "Yurt", "Moeda", "Graos", "Tecido", "Ferro", "Pele", "Vaca"
	};

	// Centro e raio de cada parada, na ordem do model:
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

	// Icone de cada provincia (pote de tributo), na ordem do model.
	private static final int[][] POTES = {
		{ 626,  423}, { 871,  370}, { 784,  938}, {1949,  747}, {2160, 1051}, {1225,  911},
		{2065, 1822}, {1685, 1922}, {2398, 1563}, { 605, 1658}, { 510, 1202}, {1500, 1616}
	};

	// Icone do Khan; o indice e a posicao do Khan no model (0 = fora do tabuleiro).
	private static final int[][] KHAN = {
		null, { 104, 2799}, { 626,  568}, {1751,  911}, {2134, 2034}, { 824, 1722}
	};

	private static final int[][] CIDADES = {
		{ 304,  383}, {1083,  607}, {1743,  423}, {2562,  845}, {2073, 1479},
		{2443, 2245}, {1254, 2047}, { 264, 1690}, { 805, 1373}
	};

	// Quadro "1" de cada parada de conselheiro, onde fica a peca de bonus.
	private static final int[][] BONUS = {
		{27, 1392, 124}, {28, 2430, 317}, {29, 2747, 1690}, {30, 1690, 2350},
		{31, 713, 2047}, {32, 224, 1017}, {Fachada.KARAKORUM, 1442, 1373}
	};

	// Espacos 1 a 5 da area de melhorias (o indice 0 nao e usado).
	private static final int[] MELHORIAS_X = { 0, 304, 570, 830, 1094, 1356 };
	private static final int MELHORIAS_Y = 2799;

	// Casas 1 a 17 da trilha do Kurultai (o indice 0 nao e usado).
	private static final int[][] TRILHA = {
		null,
		{1146, 2635}, {1336, 2635}, {1520, 2635}, {1700, 2635}, {1890, 2635},
		{2070, 2635}, {2260, 2635}, {2444, 2635}, {2624, 2635}, {2810, 2635},
		{2720, 2795}, {2530, 2795}, {2344, 2795}, {2160, 2795}, {1980, 2795}, {1796, 2795}, {1616, 2795}
	};

	private static final int RAIO_DO_POTE = 55;
	private static final int RAIO_DO_KHAN = 65;
	private static final int RAIO_DA_CIDADE = 110;
	private static final int LARGURA_DA_MELHORIA = 220, ALTURA_DA_MELHORIA = 116;
	private static final int LARGURA_PEAO = 95;
	private static final int ALTURA_PEAO = 130;
	private static final int ESPACO_ENTRE_PIOES = 62;

	private final BufferedImage tabuleiro = carregar("tabuleiro.jpg");
	private final BufferedImage khan = carregar("peao_khan.png");
	private final BufferedImage[] pioes = new BufferedImage[CORES.length];
	private final BufferedImage[] recursos = new BufferedImage[NOMES_DOS_RECURSOS.length];
	private final BufferedImage[] bonus = new BufferedImage[7];
	private final BufferedImage[][] melhorias = new BufferedImage[NOMES_DOS_RECURSOS.length][3];
	private final Fachada fachada = Fachada.getInstancia();

	private double escala = 1;
	private int margemX = 0;
	private int margemY = 0;

	public PainelTabuleiro () {
		for (int i = 0; i < pioes.length; i++) pioes[i] = carregar("peao" + i + ".png");
		for (int i = 0; i < recursos.length; i++) recursos[i] = carregarSeExistir("recurso" + i + ".png");
		for (int i = 0; i < bonus.length; i++) bonus[i] = carregar("bonus" + i + ".jpg");
		carregarMelhorias(melhorias);
		setPreferredSize(new Dimension(1000, 1005));
		setBackground(new Color(30, 26, 22));
		addMouseListener(Controlador.getInstancia());
		Controlador.getInstancia().registra(this);
	}

	// Imagem de cada melhoria por [tipo][cor]; combinacoes que nao existem ficam null.
	static void carregarMelhorias (BufferedImage[][] imagens) {
		for (int tipo = 0; tipo < imagens.length; tipo++)
			for (int cor = 0; cor < 3; cor++)
				imagens[tipo][cor] = carregarSeExistir("melhoria" + tipo + "-" + cor + ".jpg");
	}

	private static BufferedImage carregarSeExistir (String arquivo) {
		if (PainelTabuleiro.class.getResource("imagens/" + arquivo) == null) return null;
		return carregar(arquivo);
	}

	static BufferedImage carregar (String arquivo) {
		try {
			return ImageIO.read(PainelTabuleiro.class.getResource("imagens/" + arquivo));
		} catch (IOException erro) {
			throw new IllegalStateException("nao foi possivel carregar imagens/" + arquivo, erro);
		}
	}

	@Override
	public void notify (ObservadoIF o) {
		repaint();
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
		desenharTributos(g2);
		desenharCidades(g2);
		desenharBonus(g2);
		desenharMelhorias(g2);
		desenharYurts(g2);
		desenharKurultai(g2);
		desenharAlcancaveis(g2);
		desenharPioes(g2);
		desenharKhan(g2);
	}

	// Desenha uma imagem centrada num ponto do tabuleiro.
	private void desenhar (Graphics2D g2, BufferedImage imagem, int x, int y, int largura, int altura) {
		int l = (int) (largura * escala), a = (int) (altura * escala);
		g2.drawImage(imagem, telaX(x) - l / 2, telaY(y) - a / 2, l, a, null);
	}

	// Disco colorido (yurt ou ficha da trilha) desenhado com Java2D.
	private void desenharFicha (Graphics2D g2, Color cor, int x, int y, int raio) {
		int r = Math.max(3, (int) (raio * escala));
		g2.setColor(cor);
		g2.fillOval(telaX(x) - r, telaY(y) - r, 2 * r, 2 * r);
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(1.5f));
		g2.drawOval(telaX(x) - r, telaY(y) - r, 2 * r, 2 * r);
	}

	private void desenharTributos (Graphics2D g2) {
		for (int pote = 0; pote < Fachada.NUM_POTES; pote++) {
			BufferedImage imagem = recursos[fachada.getTipoDoPote(pote)];
			int quantidade = fachada.getTributosNoPote(pote);
			for (int i = 0; i < quantidade; i++)
				desenhar(g2, imagem, POTES[pote][0] + 28 * i, POTES[pote][1] - 28 * i, 95, 95);
		}
	}

	// Cidade revelada mostra seus tesouros; cidade conquistada, o yurt do dono no centro.
	private void desenharCidades (Graphics2D g2) {
		for (int cidade = 0; cidade < Fachada.NUM_CIDADES; cidade++) {
			int dono = fachada.getDonoDaCidade(cidade);
			if (dono >= 0) desenharFicha(g2, CORES[dono], CIDADES[cidade][0], CIDADES[cidade][1], 45);
			if (!fachada.cidadeRevelada(cidade)) continue;
			ArrayList<Integer> tesouros = new ArrayList<Integer>();
			for (int tipo = Fachada.GRAOS; tipo <= Fachada.VACA; tipo++)
				for (int i = 0; i < fachada.getTesourosDaCidade(cidade, tipo); i++) tesouros.add(tipo);
			for (int i = 0; i < tesouros.size(); i++) {
				int x = CIDADES[cidade][0] + (int) ((i - (tesouros.size() - 1) / 2.0) * 70);
				desenhar(g2, recursos[tesouros.get(i)], x, CIDADES[cidade][1] + 95, 70, 70);
			}
		}
	}

	private void desenharBonus (Graphics2D g2) {
		for (int[] b : BONUS) {
			int peca = fachada.getBonusNaParada(b[0]);
			if (peca >= 0) desenhar(g2, bonus[peca], b[1], b[2], 120, 120);
		}
	}

	private void desenharMelhorias (Graphics2D g2) {
		for (int espaco = 1; espaco <= Fachada.ESPACOS_DE_MELHORIA; espaco++) {
			int tipo = fachada.getTipoDaMelhoriaNaArea(espaco);
			if (tipo < 0) continue;
			desenhar(g2, melhorias[tipo][fachada.getCorDaMelhoriaNaArea(espaco)],
					MELHORIAS_X[espaco], MELHORIAS_Y, LARGURA_DA_MELHORIA, ALTURA_DA_MELHORIA);
		}
	}

	// Yurts ficam um pouco abaixo do centro da parada, para nao sumirem sob os pioes.
	private void desenharYurts (Graphics2D g2) {
		for (int parada = 0; parada < Fachada.NUM_PARADAS; parada++) {
			ArrayList<Integer> yurts = fachada.getYurtsNaParada(parada);
			for (int i = 0; i < yurts.size(); i++) {
				int x = PARADAS[parada][0] + (int) ((i - (yurts.size() - 1) / 2.0) * 60);
				desenharFicha(g2, CORES[yurts.get(i)], x, PARADAS[parada][1] + 60, 28);
			}
		}
	}

	// Fichas dos jogadores e a ficha branca (lotacao do conselho) na trilha.
	private void desenharKurultai (Graphics2D g2) {
		int jogadores = fachada.getQuantidadeDeJogadores();
		for (int jogador = 0; jogador <= jogadores; jogador++) {
			boolean neutra = jogador == jogadores;
			int votos = Math.min(TRILHA.length - 1, neutra ? fachada.getTotalDeVotos() : fachada.getVotos(jogador));
			if (votos < 1) continue;
			double angulo = 2 * Math.PI * jogador / (jogadores + 1);
			int x = TRILHA[votos][0] + (int) (40 * Math.cos(angulo));
			int y = TRILHA[votos][1] + (int) (40 * Math.sin(angulo));
			desenharFicha(g2, neutra ? Color.WHITE : CORES[jogador], x, y, 26);
		}
	}

	private void desenharAlcancaveis (Graphics2D g2) {
		g2.setStroke(new BasicStroke((float) Math.max(2, 10 * escala)));
		g2.setColor(new Color(255, 255, 255, 190));
		for (int parada : fachada.getParadasAlcancaveis()) {
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
				desenharPeao(g2, pioes[naParada.get(i) % pioes.length], PARADAS[parada][0] + deslocamento, PARADAS[parada][1]);
			}
		}
	}

	private void desenharKhan (Graphics2D g2) {
		int posicao = fachada.getPosicaoDoKhan();
		if (posicao != Fachada.FORA_DO_MAPA) desenharPeao(g2, khan, KHAN[posicao][0] + 70, KHAN[posicao][1] + 40);
	}

	// O pe do piao fica sobre o ponto indicado.
	private void desenharPeao (Graphics2D g2, BufferedImage imagem, int x, int y) {
		int largura = (int) (LARGURA_PEAO * escala);
		int altura = (int) (ALTURA_PEAO * escala);
		g2.drawImage(imagem, telaX(x) - largura / 2, telaY(y) - altura + altura / 6, largura, altura, null);
	}

	private int telaX (int x) { return margemX + (int) (x * escala); }

	private int telaY (int y) { return margemY + (int) (y * escala); }

	private double tabuleiroX (int telaX) { return (telaX - margemX) / escala; }

	private double tabuleiroY (int telaY) { return (telaY - margemY) / escala; }

	// Indice do ponto mais proximo do clique dentro do raio, ou -1.
	private int maisProximo (int[][] pontos, int raio, int telaX, int telaY) {
		if (escala <= 0) return -1;
		int achado = -1;
		double menor = Double.MAX_VALUE;
		for (int i = 0; i < pontos.length; i++) {
			if (pontos[i] == null) continue;
			double distancia = Math.hypot(tabuleiroX(telaX) - pontos[i][0], tabuleiroY(telaY) - pontos[i][1]);
			int alcance = pontos[i].length > 2 ? pontos[i][2] : raio;
			if (distancia <= alcance && distancia < menor) {
				menor = distancia;
				achado = i;
			}
		}
		return achado;
	}

	// Devolve a parada clicada, ou -1 se o clique caiu fora de todas elas.
	public int paradaEm (int telaX, int telaY) { return maisProximo(PARADAS, 0, telaX, telaY); }

	public int poteEm (int telaX, int telaY) { return maisProximo(POTES, RAIO_DO_POTE, telaX, telaY); }

	// Posicao do Khan (1 a 5) clicada, ou -1.
	public int khanEm (int telaX, int telaY) { return maisProximo(KHAN, RAIO_DO_KHAN, telaX, telaY); }

	public int cidadeEm (int telaX, int telaY) { return maisProximo(CIDADES, RAIO_DA_CIDADE, telaX, telaY); }

	// Espaco da area de melhorias (1 a 5) clicado, ou -1.
	public int espacoDeMelhoriaEm (int telaX, int telaY) {
		double x = tabuleiroX(telaX), y = tabuleiroY(telaY);
		if (Math.abs(y - MELHORIAS_Y) > ALTURA_DA_MELHORIA / 2) return -1;
		for (int espaco = 1; espaco < MELHORIAS_X.length; espaco++)
			if (Math.abs(x - MELHORIAS_X[espaco]) <= LARGURA_DA_MELHORIA / 2) return espaco;
		return -1;
	}

	// Menu com os tesouros que podem ser tomados da cidade.
	public void mostrarAtaque (int cidade, int telaX, int telaY) {
		JPopupMenu menu = new JPopupMenu();
		int custo = fachada.getCustoDoAtaque(cidade);
		for (int tipo = Fachada.GRAOS; tipo <= Fachada.VACA; tipo++) {
			if (fachada.getTesourosDaCidade(cidade, tipo) <= 0) continue;
			JMenuItem item = new JMenuItem("Tomar " + NOMES_DOS_RECURSOS[tipo] + " (" + custo + " espada(s))");
			item.setActionCommand(Controlador.ATACAR + cidade + ":" + tipo);
			item.addActionListener(Controlador.getInstancia());
			menu.add(item);
		}
		menu.show(this, telaX, telaY);
	}
}
