import javax.swing.SwingUtilities;

import controller.Controlador;

public class Principal implements Runnable {

	public static void main (String[] args) {
		SwingUtilities.invokeLater(new Principal());
	}

	@Override
	public void run () {
		Controlador.getInstancia().iniciar();
	}
}
