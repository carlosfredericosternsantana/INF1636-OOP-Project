package view;

import javax.swing.JFrame;

import controller.Controlador;
import model.Fachada;
import model.ObservadoIF;
import model.ObservadorIF;

// Janela da cartela. Abre sozinha a cada novo turno, para o jogador escolher a
// coluna, e se fecha depois que a coluna e ativada (o botao "Cartela" reabre).
public class JanelaCartela extends JFrame implements ObservadorIF {

	private static final long serialVersionUID = 1L;

	private final PainelCartela painel = new PainelCartela();

	public JanelaCartela () {
		add(painel);
		setResizable(false);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		atualizarTitulo();
		Controlador.getInstancia().registra(this);
	}

	private void atualizarTitulo () {
		Fachada fachada = Fachada.getInstancia();
		setTitle("Cartela de " + fachada.getHerdeiro(fachada.getVez()) + " - clique numa coluna para ativa-la");
	}

	@Override
	public void notify (ObservadoIF o) {
		int evento = o.get(ObservadoIF.EVENTO);
		atualizarTitulo();
		if (evento == ObservadoIF.NOVO_TURNO) setVisible(true);
		else if (evento == ObservadoIF.COLUNA_ATIVADA || evento == ObservadoIF.FIM) setVisible(false);
		painel.repaint();
	}
}
