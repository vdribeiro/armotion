
import genetic.BassMutation;
import jm.JMC;
import jm.music.data.CPhrase;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.music.tools.Mod;
import mozart.MozartDiceGame;
import automata.RhythmicAutomata;
import drumbeat.BasicBeat;
import drumbeat.DiscoBeat;
import drumbeat.DrumBeat;
import drumbeat.ShuffleBeat;

public class Algorithm implements JMC {

	/** Row Your Boat score */
	public static Score RowYourBoat() {
		//Create the data objects we want to use
		Score score = new Score("Row Your Boat");
		//Parts can have a name, instrument, and channel.
		Part flute = new Part("Flute", FLUTE, 0);
		Part trumpet = new Part("Trumpet", TRUMPET, 1);
		Part clarinet = new Part("Clarinet", CLARINET, 2);

		//Lets write the music in a convenient way.
		int[] pitchArray = {C4,C4,C4,D4,E4,E4,D4,E4,F4,G4,C5,C5,C5,G4,G4,G4,E4,E4,E4,
				C4,C4,C4,G4,F4,E4,D4,C4};
		double[] rhythmArray = {C,C,CT,QT,C,CT,QT,CT, QT,M, QT, QT, QT, QT, QT,
				QT, QT, QT, QT, QT, QT, QT, CT, QT, CT, QT,M};
		//add the notes to a phrase
		Phrase phrase1 = new Phrase(0.0);
		phrase1.addNoteList(pitchArray, rhythmArray);

		//Make two new phrases and change start times to make a round
		Phrase phrase2 = phrase1.copy();
		phrase2.setStartTime(4.0);
		Phrase phrase3 = phrase1.copy();
		phrase3.setStartTime(8.0);

		//Play different parts in different octaves
		Mod.transpose(phrase1, 12);
		Mod.transpose(phrase3, -12);

		//loop phrases once
		Mod.repeat(phrase1, 1);
		Mod.repeat(phrase2, 1);
		Mod.repeat(phrase3, 1);

		//add phrases to the parts
		flute.addPhrase(phrase1);		
		trumpet.addPhrase(phrase2);
		clarinet.addPhrase(phrase3);

		//add parts to the score
		score.addPart(flute);
		score.addPart(trumpet);	
		score.addPart(clarinet);		

		return score;
	}

	/** Generate a melody backwards, and flip the melody about the initial pitch */
	public static Score RetroInv(int numberOfNotes) {
		// make the original phrase
		Phrase melody = new Phrase("Original"); 
		for(int i=0;i<numberOfNotes;i++){
			Note n = new Note(C4+(int)(Math.random()*18), QUAVER);
			melody.addNote(n);
		}
		Score melScore = new Score();
		Part melPart = new Part();
		melPart.addPhrase(melody);
		melScore.addPart(melPart);

		//View.show(melScore);
		// reverse the phrase
		Phrase backwards = new Phrase("Retrograde");
		for(int i=melody.size();i>0;i--){
			backwards.addNote(melody.getNote(i-1));
		}

		Score backScore = new Score();
		Part backPart = new Part();
		backPart.addPhrase(backwards);
		backScore.addPart(backPart);

		// invert the melody
		// Make a copy of the melody
		Phrase flip = melody.copy();
		flip.setTitle("Inversion");
		// get the first pitch
		int firstNote = flip.getNote(0).getPitch();
		for(int i=0; i< flip.size();i++){
			// change the pitch to invert each around the first note
			flip.getNote(i).setPitch(firstNote - (melody.getNote(i).getPitch() - firstNote));
		}

		Score flipScore = new Score();
		Part flipPart = new Part();
		flipPart.addPhrase(flip);
		flipScore.addPart(flipPart);

		return flipScore;
	}

	/** Generates a chromatic melody which follows the curve of a sine wave */
	public static Score SineMelody(double density){
		Score s = new Score("Sine melody");
		Part p = new Part("Melody", MARIMBA, 0);
		Phrase phr = new Phrase(); 
		// the density is the resolution of notes over the sine wave function
		// a lower density produces a shorter melody - more course resolution.
		//double density = 25.0;

		// create a phrase of randomly pitched quavers over the full MIDI range.
		for(int i = 0; i < (2*3.14*density); i++) { /*3.14 is an approximate 
                                                      value for pi */
			int pitch = (int)(Math.sin(i/density) * 50 + 60);
			Note note = new Note(pitch, DSQ);
			phr.addNote(note);
		}

		// add the phrase to an instrument and that to a score
		p.addPhrase(phr);
		s.addPart(p);

		//display the output
		//View.show(s);

		return s;
	}

	/** Steve Reich's "Piano Phase" piece */
	public static Score PianoPhase() {
		//Create the data objects we want to use
		Score score = new Score("Piano Phase", 120.0);
		score.setTimeSignature(6, 8);
		Part p1 = new Part("Piano1", PIANO, 0);
		Part p2 = new Part("Piano2", EPIANO, 1);
		Phrase phrase1 = new Phrase();
		//Lets write the music in a convenient way.
		int[] pitchArray = {E4,FS4,B4,CS5,D5,FS4,E4,CS5,B4,FS4,D5,CS5};
		double[] rhythmArray = {0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25};

		//add the notes to the first phrase
		phrase1.addNoteList(pitchArray, rhythmArray);
		Phrase phrase2 = (Phrase) phrase1.copy();
		Phrase phrase3 = (Phrase) phrase1.copy();
		//Phrse four is played after the offset period
		Phrase phrase4 = (Phrase) phrase1.copy();

		// repeat the sstable piano part and add it to a part and score
		Mod.repeat(phrase1, 40);
		Mod.repeat(phrase2, 8); // stable
		phrase2.setStartTime(0.0);
		Mod.repeat(phrase3, 24); // accellerate
		phrase3.setTempo(120. * (6. * 24.) / (6. * 24. - 1.));
		System.out.println("End time = " + phrase3.getEndTime());
		Mod.repeat(phrase4, 8); //stable again

		// Phrase 3 is the one which extends phrase two at a different tempo
		phrase3.setStartTime(24.0);
		phrase4.setStartTime(32 * 3 - 1);
		//Play different parts in different octaves for more audible clarity
		Mod.transpose(phrase2, 12);
		Mod.transpose(phrase3, 12);
		Mod.transpose(phrase4, 12);

		//add phrases to the two instruments
		p1.addPhrase(phrase1);		
		p2.addPhrase(phrase2);
		p2.addPhrase(phrase3);
		p2.addPhrase(phrase4);
		// set pan
		p1.setPan(0.1);
		p2.setPan(0.9);
		//add parts to the score
		score.addPart(p1);
		score.addPart(p2);		

		return score;
	}

	/** Creates stochastic drum patterns */
	public static Score Jm808(int length, int loopNum) {
		//int length = 16;
		Score pattern1 = new Score("jm-808"); 
		// 25 = TR808 General MIDI kit. 9 = MIDI channel 10.
		Part drums = new Part("Drums",25, 9);
		Phrase phrBD = new Phrase(0.0);
		Phrase phrSD = new Phrase(0.0);
		Phrase phrHH = new Phrase(0.0);
		CPhrase phrEnd = new CPhrase();

		//clean out the score and part for reuse
		pattern1.empty();
		drums.empty();

		//create the appropriate length phrases filled up with note objects
		int pitch = 0;

		pitch = 36;
		for(int i=0;i<length;i++){
			Note note = new Note(pitch, SQ, (int)(Math.random()*70 + 50));
			phrBD.addNote(note);
		}

		pitch = 38;	
		for(int i=0;i<length;i++){
			Note note = new Note(pitch, SQ, (int)(Math.random()*70 + 50));
			phrSD.addNote(note);
		}

		pitch = 42;
		for(int i=0;i<length;i++){
			Note note = new Note(pitch, SQ, (int)(Math.random()*70 + 50));
			phrHH.addNote(note);
		}

		// make crash ending
		int[] pitchArray1a = {36,49}; // kick and crash cymbal
		phrEnd.addChord(pitchArray1a, SB);

		// get the on-off values
		for (int i=0;i<length;i++) {
			if (Math.random() > 0.3) {
				phrBD.getNote(i).setPitch(REST);
			} else phrBD.getNote(i).setPitch(36);
			if (Math.random() > 0.5) {
				phrSD.getNote(i).setPitch(REST);
			} else phrSD.getNote(i).setPitch(38);
			if (Math.random() > 0.8) {
				phrHH.getNote(i).setPitch(REST);
			} else phrHH.getNote(i).setPitch(42);
		}	

		// loop the drum pattern
		Mod.repeat(phrBD, loopNum);
		Mod.repeat(phrSD, loopNum);
		Mod.repeat(phrHH, loopNum);

		// add phrases to the instrument (part)
		drums.addPhrase(phrBD);
		drums.addPhrase(phrSD);
		drums.addPhrase(phrHH);
		drums.addCPhrase(phrEnd);

		// add the drum part to a score.
		pattern1.addPart(drums);

		return pattern1;
	}

	/** Creates a simple one bar drum pattern */
	public static Score AudioDrums(int length) {
		Score s = new Score("Drums");
		Part kickPart = new Part("Kick", 0, 9);
		Part snarePart = new Part("Snare", 1, 9);
		Part hatsPart = new Part("Hats", 2, 9);
		Part openHatsPart = new Part("OpenHats", 3, 9);
		Phrase kickPhrase = new Phrase();
		Phrase snarePhrase = new Phrase();
		Phrase hatsPhrase = new Phrase();
		Phrase openHatsPhrase = new Phrase();

		/*
		SimpleSampleInst kickInst = new SimpleSampleInst("Kick.au", FRQ[36]);
		SimpleSampleInst snareInst = new SimpleSampleInst("Snare.au", FRQ[38]);
		SimpleSampleInst hatsInst = new SimpleSampleInst("Hats.au", FRQ[42]);
		SimpleSampleInst openHatsInst = new SimpleSampleInst("HHOpen.au", FRQ[46]);
		Instrument[] drumKit = {kickInst, snareInst, hatsInst, openHatsInst};
		*/

		// kick 
		for(int i=0;i<length;i++) {
			Note n = new Note(36,2.0);
			kickPhrase.addNote(n);
		}

		//snare
		for(int i=0;i<length;i++) {
			Note r = new Note(REST,1.0);
			snarePhrase.addNote(r);
			Note n = new Note(38,1.0);
			n.setDuration(n.getRhythmValue());
			snarePhrase.addNote(n);
		}

		//hats
		for(int i=0;i<8*length;i++) {
			if(i%2 == 1 && Math.random() < 0.1) {
				Note n = new Note(46,0.25, (int)(Math.random()*127));
				n.setDuration(n.getRhythmValue());
				openHatsPhrase.addNote(n);
				Note r = new Note(REST, 0.25);
				hatsPhrase.addNote(r);
			} else {
				Note n = new Note(42,0.25, (int)(Math.random()*127));
				n.setDuration(n.getRhythmValue());
				hatsPhrase.addNote(n);
				Note r = new Note(REST, 0.25);
				openHatsPhrase.addNote(r);
			}
		}

		//pack
		kickPart.addPhrase(kickPhrase);
		snarePart.addPhrase(snarePhrase);
		hatsPart.addPhrase(hatsPhrase);
		openHatsPart.addPhrase(openHatsPhrase);

		s.addPart(kickPart);
		s.addPart(snarePart);
		s.addPart(hatsPart);
		s.addPart(openHatsPart);

		return s;
	}

	/** Generates a random chromatic melody where each note's pitch is randomly determined */
	public static Score Stochastic(int length){
		Score stochScore = new Score("Stochastic");
		Part inst = new Part("Piano", PIANO, 0);
		Phrase phr = new Phrase(0.0); 

		// create a phrase of randomly pitched quavers over the full MIDI range.
		for(int i=0; i<length; i++){
			Note note = new Note((int)(Math.random()*127), QUAVER);
			phr.addNote(note);
		}

		// add the phrase to an instrument and that to a score
		inst.addPhrase(phr);
		stochScore.addPart(inst);

		return stochScore;
	}

	/** Generates a random melody and constrains it so that it only contain notes found in the C major scale. */
	public static Score ModeMelody(int length){
		Score modeScore = new Score("Mode Melody");
		Part inst = new Part("Guitar", SGUITAR, 0);
		Phrase phr = new Phrase(); 
		int temp; //variable to store random number
		int[] mode = {0,2,4,5,7,9,11,12}; //C major scale degrees

		// create a phrase of 'length' randomly pitched quavers within C major.
		for(short i=0;i<length;){
			// generate a random number between 30 and 85 (avoids excessively high and low notes).
			temp = (int)(Math.random()*55 + 30);
			//check that it is a note in the mode (C major scale)
			for (short j=0; j< mode.length; j++) {
				if ( temp%12 == mode[j]) {
					// if it is then add it to the phrase and move to the next note in the phrase
					Note note = new Note(temp, Q);
					phr.addNote(note);
					i++; // only move onto the next note when a valid pitch is found.
				}
			}
		}

		// add the phrase to an instrument and that to a score
		inst.addPhrase(phr);
		modeScore.addPart(inst);

		return modeScore;
	}

	/** Generates a melody based on random walk (drunk) pitches which are constrained to the C minor scale */
	public static Score DrunkWalk(int length){
		Score modeScore = new Score("Drunk walk");
		Part inst = new Part("Bass", SYNTH_BASS, 0);
		Phrase phr = new Phrase();
		int pitch = C3; // variable to store the calculated pitch (initialized with a start pitch value)
		int offset;

		// create a phrase of length quavers following a random walk within C minor.
		for(short i=0;i<length;) {
			// next note within plus or minus a 5th.
			offset = 0;
			while(offset == 0) {
				offset = (int)((Math.random()*14) - 7); 
			}
			pitch += offset; // add the offset to the pitch to find the next pitch
			// check that it is a note in the mode using the isScale method.
			// several other scale constants are available in the JMC
			if (pitch>=0){
				Note note = new Note(pitch, Q);
				if(note.isScale(MINOR_SCALE)) {
					phr.addNote(note);
					i++;
				}
			}
		}

		// add the phrase to an instrument and that to a score
		inst.addPhrase(phr);
		modeScore.addPart(inst);

		return modeScore;
	}

	/** Generates a melody on Mozart's Dice Game */
	public static Score Mozart(){
		return MozartDiceGame.getScore();
	}

	/** Creates Drum pattern */
	public static Score DrumPatt(int patt, int times){
		DrumBeat beat = null;

		switch (patt) {
		case 0:
			beat = new DiscoBeat();
			break;
		case 1:
			beat = new ShuffleBeat();
			break;
		default:
			beat = new BasicBeat();
			break;
		}

		Score score = beat.getScore();
		Mod.repeat(score, times);

		return score;
	}

	/** Generates a chromatic fractal melody */
	public static Score Fractal(int npts) {
		Score score = new Score("Fractal");
		Part part = new Part("Piano", PIANO);
		Phrase phr = new Phrase();

		float sum;
		float[] rg = new float[16];
		int k, kg, ng, threshold;
		int np = 1;
		int nbits = 1;
		//int npts = 48; //number of notes
		float nr = (float)(npts);

		part.addPhrase(phr);
		score.addPart(part);

		// setup math
		nr = nr/2;
		// create a phrase of fractal pitches quavers over the full MIDI range.
		System.out.println("Calculating fractal melody. . .");
		while (nr > 1) {
			nbits++;
			np = 2 * np;
			nr = nr/2;
		}
		for(kg=0; kg<nbits; kg++) {
			rg[kg] = (float)(Math.random());
		}

		// calc notes
		for(k=0; k<npts; k++) {
			threshold = np;
			ng = nbits;
			while(k%threshold != 0) {
				ng--;
				threshold = threshold / 2;
			}
			sum = 0;
			for(kg=0; kg<nbits; kg++) {
				if(kg<ng) {rg[kg]=(float)(Math.random());}
				sum += rg[kg];
			}
			Note note = new Note((int)(sum/nbits*127), Q);
			phr.addNote(note);
		}

		return score;
	}

	/** Generates a chaotic chromatic melody */
	public static Score Chaos(int limit) {
		Score score = new Score("Chaos", 130);
		Part inst = new Part("Piano", PIANO, 0);
		Phrase phr = new Phrase(0.0);
		double xold = 0.0; // initial x position
		double x, y; // temp variables
		double yold = 0.0; // initial y position

		//---------------
		// Do chaotic things
		//----------------
		double a = 1.4; // first constant. For oscillation try 1.04
		double b = 0.3; //second constant. For oscillation try 0.3

		// create a phrase of chaotically pitched quavers over a limited MIDI range.
		for(short i=0;i<limit;i++){
			x = 1 + yold - a * xold * xold;
			y = b * xold;
			// map the x value across a few octaves
			Note note = new Note((int)(x*36)+48, Q);
			phr.addNote(note);
			xold = x;
			yold = y;
		}
		inst.addPhrase(phr);

		// add the part that to a score
		score.addPart(inst);

		return score;
	}

	/** Generates a two part chaotic chromatic melody */
	public static Score MuchChaos(int limit){
		Score score = new Score("JMDemo - Chaos", 120);
		Part guitarPart = new Part("Guitar", NYLON_GUITAR, 0);
		Part pianoPart = new Part("Piano", PIANO, 1);
		Phrase phr = new Phrase(0.0);
		Phrase phr2 = new Phrase(0.0);
		double xold = 0.0; // initial x position
		double x, y; // temp variables
		double yold = 0.0; // initial y position

		//---------------
		// first phrase
		//----------------
		double a = 1.4; // first constant. For oscillation try 1.04
		double b = 0.3; //second constant. For oscillation try 0.3

		// create a phrase of chaotically pitched quavers over a limited MIDI range.
		for(short i=0;i<limit;i++){
			x = 1 + yold - a * xold * xold;
			y = b * xold;
			// map the x value across a few octaves
			Note note = new Note((int)((x*24)+48), C);
			phr.addNote(note);
			xold = x;
			yold = y;
		}
		guitarPart.addPhrase(phr);

		//---------------
		// Second phrase
		//----------------
		a = 1.401; // first constant. For oscillation try 1.04
		b = 0.3; // second constant. For oscillation try 0.3
		xold = 0.0;
		yold = 0.0;

		// create a phrase of chaotically pitched quavers over a limited MIDI range.
		for(short i=0;i<limit;i++){
			x = 1 + yold - a * xold * xold;
			y = b * xold;
			// map the x value across a few octaves
			Note note = new Note((int)((x*24)+48), C);
			phr2.addNote(note);
			xold = x;
			yold = y;
		}
		pianoPart.addPhrase(phr2);

		// add the part that to a score
		score.addPart(guitarPart);
		score.addPart(pianoPart);

		return score;
	}
	
	/** Generates a simple one dimensional Cellular Automata that is applied to 
	 * generate an evolutionary series of bars each with 'length' pulses */
	public static Score RhythmicAutomata(int length) {
		return RhythmicAutomata.getScore(length);
	}
	
	/** Generates a phrase and then mutates it every second bar 
	 * by randomly changing one note */
	public static Score BassMutation(int nmut) {
		return BassMutation.getScore(nmut);
	}
}
