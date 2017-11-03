package genetic;
import jm.music.data.*;
import jm.JMC;
import jm.music.tools.*;

public class MakeParts implements JMC{
	static int length = 32;
	
	public static CPhrase drumPattern() {
		//clean out the score and part for reuse
		CPhrase pattern1 = new CPhrase();
		Phrase phrBD = new Phrase(0.0);
		Phrase phrSD = new Phrase(0.0);
		Phrase phrHH = new Phrase(0.0);
			
		//create the appropriate length phrases filled up with note objects
		phrBD = kickPattern();
		phrSD = phraseFill();
		phrHH = phraseFill();

		// turn the SQ either on or off at random
		for (int i=0;i<length;i++) {
			if (Math.random() > 0.5) phrSD.getNote(i).setPitch(38);
			if (Math.random() > 0.2) phrHH.getNote(i).setPitch(42);
		}	
		
		// add phrases to the CPhrase
		pattern1.addPhrase(phrBD);
		pattern1.addPhrase(phrSD);
		pattern1.addPhrase(phrHH);
		
		Mod.repeat(pattern1, 3);
		
		return pattern1;
	}
	
	private static Phrase phraseFill() {
		Phrase phrase = new Phrase(0.0);
		for(int i=0;i<length;i++){
			Note note = new Note(REST, SQ, (int)(Math.random()*70 + 50));
			phrase.addNote(note);
		}
		return phrase;
	}
	
	private static Phrase kickPattern() {
		Phrase phrase = new Phrase(0.0);
		for(int i=0;i<length/4;i++){
			Note note = new Note(36, C, 110);
			phrase.addNote(note);
		}
		return phrase;
	}
	
	public static Phrase bassPattern() {
		//clean out the score and part for reuse
		Phrase pattern1 = new Phrase();
		
		int[] pitchSelection = {36, 40, 43, 45, 48, 52, 55, 60};
		int pitch;

		// turn the SQ either on or off at random
		for (int i=0;i<length;i++) {
			if (Math.random() > 0.5) {
				pitch = pitchSelection[(int)(Math.random()*pitchSelection.length)];
			} else pitch = REST;
			Note note = new Note(pitch, SQ, (int)(Math.random()*70 + 50));
			pattern1.addNote(note);
		}
		
		Mod.repeat(pattern1, 3);
		
		return pattern1;
	}
}
