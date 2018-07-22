import java.awt.Dimension;
import java.util.Random;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.UIManager;


public class JBrainTetris extends JTetris{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JPanel little;
	JCheckBox brainMode;
	JSlider adversary;
	JLabel adversaryLabel;
	
	DefaultBrain brain;
	Brain.Move bestMove;
	
	private int curCount;
	
	JBrainTetris(int pixels) {
		super(pixels);
		this.curCount = 0;
		brain = new DefaultBrain();
	}
	//surcharge de la methode createControlPanel()
	
	public JComponent createControlPanel() {
		JComponent panel = super.createControlPanel();
		
		panel.add(new JLabel("Brain: "));
		brainMode = new JCheckBox("Brain active");
		panel.add(brainMode);
		
		little = new JPanel();
		little.add(new JLabel("Souleyman: "));
		adversary = new JSlider(0, 100, 0);
		adversary.setPreferredSize(new Dimension(100, 15));
		adversaryLabel = new JLabel("ok");
		little.add(adversary);
		little.add(adversaryLabel);
		panel.add(little);
		
		return panel;
	}
	// methode Stick
	
	@Override
	public void tick(int verb) {
		if (brainMode.isSelected() && verb == DOWN) {	// play a brain tick
			board.undo();
			if (curCount != count) {
				curCount = count;
				bestMove = brain.bestMove(board, currentPiece, HEIGHT,null);
			}
			
			if (bestMove != null) {
				if(!currentPiece.equals(bestMove.piece)) {
					super.tick(ROTATE);
				}
			
				if (bestMove.x < currentX) {
					super.tick(LEFT);
				} else if (bestMove.x > currentX){
					super.tick(RIGHT);
				}
			}
			// the piece must move down following any rotation and/or left/right move
			super.tick(DOWN);
		} else {	// play a standard tick
			super.tick(verb);
		}
	}
	
	// Creation d'une Frame dans main
	
	public static void main(String[] args){
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) { }
		
		JTetris tetris = new JBrainTetris(16);
		JFrame frame = JBrainTetris.createFrame(tetris);
		frame.setVisible(true);
	}
	
	@Override
	public Piece pickNextPiece() {
		Piece piece = null;
		// get a random number from 1 to 99
		Random r = new Random();
		int randVal = r.nextInt(98) + 1;
		int sliderVal = adversary.getValue();
		
		if (randVal < sliderVal) {	// adversary intervenes whenever sliderVal is greater than randVal
			adversaryLabel.setText("*ok*");
			double worstScore = 0;
			
			// iterate through the pieces to find the worst piece
			for(Piece p : super.pieces) {
				Brain.Move currMove = brain.bestMove(board, p, HEIGHT, null);
				// if no bestMove is found, default to the super's pick piece
				if (currMove == null) return super.pickNextPiece();
				if (currMove.score > worstScore) {
					worstScore = currMove.score;
					piece = p;
				}
			}
		} else {	// adversary does not intervene
			adversaryLabel.setText("ok");
			piece = super.pickNextPiece();
		}
		return piece;
	}
	
}
