package view;

import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import controller.Controlador;

// 1a janela: nova partida ou continuar uma partida salva.
public class JanelaInicial extends JFrame {

	private static final long serialVersionUID = 1L;

	public JanelaInicial () {
		super("Herdeiros de Khan");
		JLabel titulo = new JLabel("Herdeiros de Khan", SwingConstants.CENTER);
		titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 26f));
		JButton nova = new JButton("Nova partida");
		nova.setActionCommand(Controlador.NOVA);
		nova.addActionListener(Controlador.getInstancia());
		// O salvamento e a recuperacao ficam para a 4a iteracao.
		JButton continuar = new JButton("Continuar partida salva");
		continuar.setEnabled(false);

		JPanel painel = new JPanel(new GridLayout(3, 1, 0, 12));
		painel.setBorder(BorderFactory.createEmptyBorder(24, 40, 24, 40));
		painel.add(titulo);
		painel.add(nova);
		painel.add(continuar);
		add(painel);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
	}
}
