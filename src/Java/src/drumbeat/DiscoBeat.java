package drumbeat;
import jm.music.data.*;

/**
 * This class creates a one bar disco beat
 * @author Andrew Brown
 */
public class DiscoBeat extends BasicBeat {

	static final DrumBeat beat = new DiscoBeat();

	public static void main(final String[] args) {
		//beat.saveToFile();
		beat.play(5);
	}

	protected String getName() {
		return "Disco drum beat";
	}

	protected void composeHats(final Phrase hats) {
		for (int i = 0; i < 4; i++) {
			hats.addNote(new Note(CLOSED_HI_HAT, QUAVER));
			hats.addNote(new Note(OPEN_HI_HAT, QUAVER));
		}
	}
}
