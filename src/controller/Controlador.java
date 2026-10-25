package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import model.Fachada;
import view.JanelaTabuleiro;

// Tratamento de eventos sem expressoes lambda, como pede o enunciado.
public class Controlador implements ActionListener, MouseListener {

	public static final String DEFINIR = "definir";
	public static final String PASSAR = "passar";

	private final JanelaTabuleiro janela;
	private final Fachada fachada = Fachada.getInstancia();

	public Controlador (JanelaTabuleiro janela) {
		this.janela = janela;
	}

	@Override
	public void actionPerformed (ActionEvent evento) {
		String comando = evento.getActionCommand();
		if (DEFINIR.equals(comando)) fachada.definirMovimentos(janela.getMovimentosEscolhidos());
		else if (PASSAR.equals(comando)) fachada.passarVez();
		else fachada.ativarColuna(Integer.parseInt(comando));
		janela.atualizar();
	}

	// Um clique numa parada desloca o piao da vez se o deslocamento for possivel.
	// Se nao for, nada acontece: nenhuma mensagem e a vez nao muda.
	@Override
	public void mouseClicked (MouseEvent evento) {
		int parada = janela.getPainelTabuleiro().paradaEm(evento.getX(), evento.getY());
		if (parada < 0) return;
		if (fachada.mover(parada)) janela.atualizar();
	}

	@Override
	public void mousePressed (MouseEvent evento) {}

	@Override
	public void mouseReleased (MouseEvent evento) {}

	@Override
	public void mouseEntered (MouseEvent evento) {}

	@Override
	public void mouseExited (MouseEvent evento) {}
}
