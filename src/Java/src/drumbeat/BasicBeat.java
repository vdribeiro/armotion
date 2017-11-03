package drumbeat;
import jm.music.data.Phrase;

/** Provides a basic drum beat by subclassing DrumBeat and implementing the 
 * abstract methods.
 * @author Andrew Brown
 */
public class BasicBeat extends DrumBeat {

	static final DrumBeat beat = new BasicBeat();

	public static void main(final String[] args) {
		//beat.saveToFile();
		beat.play(5);
	}

	protected String getName() {
		return "Basic drum beat";
	}

	protected void composeKicks(final Phrase kicks) {
		kicks.addNote(KICK_CROTCHET);
		kicks.addNote(REST_CROTCHET); 
		kicks.addNote(KICK_CROTCHET);
		kicks.addNote(REST_CROTCHET);
	}

	protected void composeSnare(final Phrase snare) {
		snare.addNote(REST_CROTCHET);
		snare.addNote(SNARE_CROTCHET);
		snare.addNote(REST_CROTCHET);
		snare.addNote(SNARE_CROTCHET);
	}

	protected void composeHats(final Phrase hats) {
		hats.addNote(HAT_CROTCHET);
		hats.addNote(HAT_CROTCHET);
		hats.addNote(HAT_CROTCHET);
		hats.addNote(HAT_CROTCHET);
	}
}
