package view;

import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import model.Fachada;

// Resultado da partida: votos do Kurultai, votos de influencia e o vencedor.
public class JanelaResultado extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String[] COLUNAS = { "Herdeiro", "Kurultai", "Influencia", "Total", "Tesouros" };

	public JanelaResultado () {
		super("Fim da partida");
		Fachada fachada = Fachada.getInstancia();
		int jogadores = fachada.getQuantidadeDeJogadores();

		JPanel tabela = new JPanel(new GridLayout(jogadores + 1, COLUNAS.length, 16, 6));
		for (String coluna : COLUNAS) tabela.add(negrito(new JLabel(coluna)));
		for (int j = 0; j < jogadores; j++) {
			JLabel nome = new JLabel(fachada.getHerdeiro(j));
			nome.setForeground(PainelTabuleiro.CORES[j]);
			tabela.add(negrito(nome));
			tabela.add(new JLabel(String.valueOf(fachada.getVotos(j))));
			tabela.add(new JLabel(String.valueOf(fachada.getVotosDeInfluencia(j))));
			tabela.add(new JLabel(String.valueOf(fachada.getVotosFinais(j))));
			tabela.add(new JLabel(String.valueOf(fachada.getTesouros(j))));
		}

		ArrayList<Integer> vencedores = fachada.getVencedores();
		StringBuilder texto = new StringBuilder(vencedores.size() > 1 ? "Empate: " : "Novo Khan: ");
		for (int i = 0; i < vencedores.size(); i++) {
			if (i > 0) texto.append(" e ");
			texto.append(fachada.getHerdeiro(vencedores.get(i)));
		}
		JLabel vencedor = new JLabel(texto.toString(), SwingConstants.CENTER);
		vencedor.setFont(vencedor.getFont().deriveFont(Font.BOLD, 22f));

		JPanel painel = new JPanel(new GridLayout(2, 1, 0, 16));
		painel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
		painel.add(tabela);
		painel.add(vencedor);
		add(painel);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
	}

	private static JLabel negrito (JLabel rotulo) {
		rotulo.setFont(rotulo.getFont().deriveFont(Font.BOLD));
		return rotulo;
	}
}
