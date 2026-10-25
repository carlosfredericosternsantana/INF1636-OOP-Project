import javax.swing.SwingUtilities;

import model.Fachada;
import view.JanelaTabuleiro;

// As janelas iniciais ainda nao sao exigidas, entao a partida ja comeca no tabuleiro.
public class Principal implements Runnable {

	private static final int JOGADORES = 3;

	public static void main (String[] args) {
		SwingUtilities.invokeLater(new Principal());
	}

	@Override
	public void run () {
		Fachada.getInstancia().novaPartida(JOGADORES);
		new JanelaTabuleiro().setVisible(true);
	}
}
