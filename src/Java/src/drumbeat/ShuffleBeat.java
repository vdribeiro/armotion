package drumbeat;
import jm.music.data.*;

/**
 * This class creates a one bar disco beat
 * @author Andrew Brown
 */
public class ShuffleBeat extends BasicBeat {

	static final DrumBeat beat = new ShuffleBeat();

	public static void main(final String[] args) {
		//beat.saveToFile();
		beat.play(5);
	}

	protected String getName() {
		return "Shuffle drum beat";
	}

	protected void composeHats(final Phrase hats) {
		for (int i = 0; i < 4; i++) {
			hats.addNote(new Note(CLOSED_HI_HAT, DOTTED_QUAVER));
			hats.addNote(new Note(CLOSED_HI_HAT, SEMI_QUAVER));
		}
	}
}
