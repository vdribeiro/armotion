package drumbeat;
import jm.JMC;
import jm.music.data.*;
import jm.music.tools.Mod;
import jm.util.Play;
import jm.util.Write;

/** Provides an abstract represention of drum beat patterns. Subclasses must 
 * complete the methods which produce the kick, snare and hat parts. This class
 * includes a common method which writes the drum beat pattern to a MIDI file.
 *
 * @author Andrew Brown
 */
public abstract class DrumBeat implements JMC {

	/** Name of the drum beat. Will also be used as a filename when saving.
	 */
	protected abstract String getName();

	/** Used by subclasses to specify the kick portion of the drum beat. 
	 * Subclasses should add KICK_NOTE and REST notes in their 
	 * implementations to create the desired beat.
	 */
	protected abstract void composeKicks(final Phrase kicks);

	/** Used by subclasses to specify the snare portion of the drum beat. 
	 * Subclasses should add SNARE_NOTE and REST notes in their 
	 * implementations to create the desired beat.
	 */
	protected abstract void composeSnare(final Phrase snare);

	/** Used by subclasses to specify the hat portion of the drum beat. 
	 * Subclasses should add HAT_NOTE and REST notes in their 
	 * implementations to create the desired beat.
	 */
	protected abstract void composeHats(final Phrase hats);

	/** Saves the drum beat to the hard drive */
	public final void saveToFile() {
		Write.midi(getScore(), getName()+".mid");  
	}
	
	/** Plays the drum beat n times */
	public final void play(int times) {
		Score score = getScore();
		Mod.repeat(score, times);
		Play.midi(score);
	}
	
	public Score getScore() {
		final Score score = new Score(getName(), ALLEGRO);
		final Phrase kicksPhrase = new Phrase("Kicks");
		composeKicks(kicksPhrase);
		addToBeat(kicksPhrase, score);

		final Phrase snarePhrase = new Phrase("Snare");
		composeSnare(snarePhrase);
		addToBeat(snarePhrase, score);

		final Phrase hatsPhrase  = new Phrase("Hats");
		composeHats(hatsPhrase);
		addToBeat(hatsPhrase, score);
		
		return score;
	}

	/** Adds the specified phrase to the specified score. */
	private static final void addToBeat(final Phrase phrase,
			final Score score) {
		Part part = new Part(phrase.getTitle(), STANDARD_KIT, 
				DRUM_CHANNEL);
		part.addPhrase(phrase);
		score.addPart(part);
	}

	// Convenience fields for use in subclasses
	protected static final Note KICK_CROTCHET  = new Note(BASS_DRUM_1, 
			CROTCHET);
	protected static final Note SNARE_CROTCHET = new Note(ACOUSTIC_SNARE, 
			CROTCHET);
	protected static final Note HAT_CROTCHET   = new Note(CLOSED_HI_HAT,    
			CROTCHET);
	protected static final Note REST_CROTCHET  = new Note(REST, CROTCHET);

	// Constants used in this class
	private static final double ALLEGRO      = 130.0;
	private static final int    DRUM_CHANNEL = 9;
	private static final int    STANDARD_KIT = 0;
}
