package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import controller.Controlador;
import model.Fachada;

// 2a janela: quantos jogadores e qual herdeiro cada cor vai assumir.
public class JanelaHerdeiros extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final String[] NOMES_DAS_CORES = { "Vermelho", "Azul", "Verde", "Amarelo", "Roxo" };

	private final JComboBox<Integer> seletorDeQuantidade = new JComboBox<Integer>();
	private final JComboBox<String>[] seletoresDeHerdeiro;
	private final JLabel aviso = new JLabel(" ", SwingConstants.CENTER);

	@SuppressWarnings("unchecked")
	public JanelaHerdeiros () {
		super("Escolha dos herdeiros");
		Controlador controlador = Controlador.getInstancia();
		for (int i = 2; i <= Fachada.MAX_JOGADORES; i++) seletorDeQuantidade.addItem(Integer.valueOf(i));
		seletorDeQuantidade.setSelectedItem(Integer.valueOf(3));
		seletorDeQuantidade.setActionCommand(Controlador.QUANTIDADE);
		seletorDeQuantidade.addActionListener(controlador);

		JPanel grade = new JPanel(new GridLayout(Fachada.MAX_JOGADORES + 1, 2, 8, 8));
		grade.add(new JLabel("Jogadores:"));
		grade.add(seletorDeQuantidade);
		seletoresDeHerdeiro = new JComboBox[Fachada.MAX_JOGADORES];
		for (int i = 0; i < seletoresDeHerdeiro.length; i++) {
			JLabel cor = new JLabel("Jogador " + NOMES_DAS_CORES[i]);
			cor.setForeground(PainelTabuleiro.CORES[i]);
			seletoresDeHerdeiro[i] = new JComboBox<String>(Fachada.getHerdeiros());
			seletoresDeHerdeiro[i].setSelectedIndex(i);
			grade.add(cor);
			grade.add(seletoresDeHerdeiro[i]);
		}

		JButton comecar = new JButton("Comecar");
		comecar.setActionCommand(Controlador.COMECAR);
		comecar.addActionListener(controlador);
		aviso.setForeground(Color.RED);

		JPanel painel = new JPanel(new BorderLayout(0, 12));
		painel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
		painel.add(grade, BorderLayout.CENTER);
		painel.add(aviso, BorderLayout.NORTH);
		painel.add(comecar, BorderLayout.SOUTH);
		add(painel);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		atualizarQuantidade();
		pack();
		setLocationRelativeTo(null);
	}

	public void atualizarQuantidade () {
		int quantidade = getQuantidade();
		for (int i = 0; i < seletoresDeHerdeiro.length; i++) seletoresDeHerdeiro[i].setEnabled(i < quantidade);
	}

	private int getQuantidade () {
		return ((Integer) seletorDeQuantidade.getSelectedItem()).intValue();
	}

	// Cada tabuleiro de jogador e unico, entao dois jogadores nao podem ter o mesmo herdeiro.
	// Devolve null nesse caso.
	public int[] getHerdeirosEscolhidos () {
		int[] herdeiros = new int[getQuantidade()];
		for (int i = 0; i < herdeiros.length; i++) {
			herdeiros[i] = seletoresDeHerdeiro[i].getSelectedIndex();
			for (int j = 0; j < i; j++)
				if (herdeiros[j] == herdeiros[i]) {
					aviso.setText("Cada jogador precisa de um herdeiro diferente.");
					return null;
				}
		}
		return herdeiros;
	}
}
