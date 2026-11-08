package view;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import controller.Controlador;
import model.Fachada;

// Cartela do jogador da vez, desenhada com drawImage(): a imagem do herdeiro,
// as melhorias compradas e as pecas de ativacao nas colunas usadas.
// Coordenadas em pixels da imagem da cartela (1300 x 707).
public class PainelCartela extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final int[] COLUNA_X = { 435, 663, 893, 1148 };   // centro de cada coluna
	private static final int[] LINHA_Y = { 467, 352, 237, 119 };     // centro de cada espaco, de baixo para cima
	private static final int BASE_Y = 587;                           // acoes impressas na base da coluna
	private static final int TOPO = 68, FUNDO = 637, LARGURA_DA_COLUNA = 195;
	private static final int LARGURA_DA_MELHORIA = 190, ALTURA_DA_MELHORIA = 100;
	private static final int TAMANHO_DA_ATIVACAO = 95;

	private final BufferedImage[] cartelas = new BufferedImage[5];
	private final BufferedImage[] ativacoes = new BufferedImage[PainelTabuleiro.CORES.length];
	private final BufferedImage[][] melhorias = new BufferedImage[PainelTabuleiro.NOMES_DOS_RECURSOS.length][3];
	private final Fachada fachada = Fachada.getInstancia();

	public PainelCartela () {
		for (int i = 0; i < cartelas.length; i++) cartelas[i] = PainelTabuleiro.carregar("cartela" + i + ".jpg");
		for (int i = 0; i < ativacoes.length; i++) ativacoes[i] = PainelTabuleiro.carregar("ativacao" + i + ".png");
		PainelTabuleiro.carregarMelhorias(melhorias);
		setPreferredSize(new Dimension(cartelas[0].getWidth(), cartelas[0].getHeight()));
		addMouseListener(Controlador.getInstancia());
	}

	@Override
	protected void paintComponent (Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		int vez = fachada.getVez();
		g2.drawImage(cartelas[fachada.getIndiceDoHerdeiro(vez)], 0, 0, null);
		for (int coluna = 0; coluna < Fachada.NUM_COLUNAS; coluna++) {
			for (int linha = 0; linha < fachada.getMelhoriasNaColuna(coluna); linha++) {
				BufferedImage imagem = melhorias[fachada.getTipoDaMelhoria(coluna, linha)][fachada.getCorDaMelhoria(coluna, linha)];
				g2.drawImage(imagem, COLUNA_X[coluna] - LARGURA_DA_MELHORIA / 2, LINHA_Y[linha] - ALTURA_DA_MELHORIA / 2,
						LARGURA_DA_MELHORIA, ALTURA_DA_MELHORIA, null);
			}
			if (!fachada.colunaDisponivel(coluna) || fachada.getColunaAtivada() == coluna)
				g2.drawImage(ativacoes[vez % ativacoes.length], COLUNA_X[coluna] - TAMANHO_DA_ATIVACAO / 2,
						BASE_Y - TAMANHO_DA_ATIVACAO / 2, TAMANHO_DA_ATIVACAO, TAMANHO_DA_ATIVACAO, null);
		}
	}

	// Coluna clicada (0 a 3), ou -1.
	public int colunaEm (int x, int y) {
		if (y < TOPO || y > FUNDO) return -1;
		for (int coluna = 0; coluna < COLUNA_X.length; coluna++)
			if (Math.abs(x - COLUNA_X[coluna]) <= LARGURA_DA_COLUNA / 2) return coluna;
		return -1;
	}
}
