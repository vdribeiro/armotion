import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import jm.JMC;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;
import jm.util.Play;

import org.json.JSONObject;

public class ARMotion implements JMC{

	// socket
	public static DatagramSocket socket =null;
	public static ServerSocket serverSocket = null;
	public static Socket clientSocket = null;
	public static int timeout = 600000;
	public static int port = 2012;
	public static boolean serverLoop = true;

	// music
	public static Score mainScore = new Score();
	public static Score currentScore = new Score();
	public static boolean beat=false;
	public static int rthm = 0;
	public static int inst1=0;
	public static int inst2=0;
	public static int index1=0;
	public static int index2=0;
	public static Note[] notes1;
	public static Note[] notes2;

	// ar
	public static double x = 0;
	public static double y = 0;
	public static double z = 0;

	public static void main(String[] args) {
		//runTCPServer();
		runUDPServer();
	}

	private static void playScore(boolean cmd) {
		if (cmd) {
			Play.midi(mainScore,false); 
		} else {
			Play.stopMidi();
		}
	}

	public static void setup(JSONObject json) throws Exception {
		int type = json.getInt("type");
		mainScore = new Score();
		inst1 = json.getInt("inst1");
		inst2 = json.getInt("inst2");
		beat = Int2Boolean(json.getInt("beat"));
		index1 = 0;
		index2 = 0;
		notes1 = new Note[1];
		notes2 = new Note[1];
		switch (type) {
		default:
			break;
		case 0:
			//notes1 = new Note[1];
			//notes1[0] = new Note();
			break;
		case 1:
			mainScore=Algorithm.AudioDrums(json.getInt("length"));
			break;
		case 2:
			mainScore=Algorithm.BassMutation(json.getInt("length"));
			break;
		case 3:
			mainScore=Algorithm.Chaos(json.getInt("length"));
			break;
		case 4:
			mainScore=Algorithm.DrumPatt(0, json.getInt("length"));
			break;
		case 5:
			mainScore=Algorithm.DrunkWalk(json.getInt("length"));
			break;
		case 6:
			mainScore=Algorithm.Fractal(json.getInt("length"));
			break;
		case 7:
			mainScore=Algorithm.Jm808(json.getInt("length"), 1);
			break;
		case 8:
			mainScore=Algorithm.ModeMelody(json.getInt("length"));
			break;
		case 9:
			mainScore=Algorithm.Mozart();
			break;
		case 10:
			mainScore=Algorithm.MuchChaos(json.getInt("length"));
			break;
		case 11:
			mainScore=Algorithm.PianoPhase();
			break;
		case 12:
			mainScore=Algorithm.RetroInv(json.getInt("length"));
			break;
		case 13:
			mainScore=Algorithm.RhythmicAutomata(json.getInt("length"));
			break;
		case 14:
			mainScore=Algorithm.RowYourBoat();
			break;
		case 15:
			mainScore=Algorithm.SineMelody(json.getInt("length"));
			break;
		case 16:
			mainScore=Algorithm.Stochastic(json.getInt("length"));
			break;
		}

		Part[] parts = mainScore.getPartArray();

		for (int i=0;i<parts.length;i++) {
			Phrase[] phrase = parts[i].getPhraseArray();
			if (i==0) {
				notes1 = phrase[0].getNoteArray();
			} else if (i==1) {
				notes2 = phrase[0].getNoteArray();
			} 
		}

		System.out.println("setup ok");
	}

	public static void play(JSONObject json) throws Exception {
		boolean note = false;

		int mark = json.getInt("mark");
		
		if (x>=0) {
			x = json.getDouble("x");
			if (x<0) {
				note=true;
			}
		} else {
			x = json.getDouble("x");
			if (x>=0) {
				note=true;
			}
		}

		if (y>=0) {
			y = json.getDouble("y");
			if (y<0) {
				if (mark==1) rthm++;
			}
		} else {
			y = json.getDouble("y");
			if (y>=0) {
				if (mark==1) rthm++;
			}
		}
		z = json.getDouble("z");

		System.out.println("play:" + note);
		if (note) {
			Note rnote = new Note();
			int inst=0;

			if (mark == 1) {
				rnote = notes1[index1];
				index1++;
				if (index1>=notes1.length) {
					index1=0;
				}
				inst = inst1;
			} else if (mark == 2) {
				rnote = notes2[index2];
				index2++;
				if (index2>=notes2.length) {
					index2=0;
				}
				inst = inst2;
			}

			Phrase phrase = new Phrase();
			phrase.addNote(rnote);

			Part part = new Part(inst, inst);
			part.addPhrase(phrase);

			final Score score = new Score();
			score.addPart(part);
			Play.midi(score);
		}

		System.out.println("beat" + rthm);
		if (mark == 1 && rthm>=2 && beat) {
			rthm=0;
			Note rnote = new Note(35, 0.5);

			Phrase phrase = new Phrase();
			phrase.addNote(rnote);

			Part part = new Part(0, 9);
			part.addPhrase(phrase);

			final Score score = new Score();
			score.addPart(part);

			Play.midi(score);
		}

	}

	public static void runUDPServer() {
		createUDPSocket();

		while (serverLoop) {
			System.out.print("Waiting... ");
			try{
				byte[] bufReceiver = new byte[2048];
				DatagramPacket receivePacket = new DatagramPacket(bufReceiver, bufReceiver.length);
				socket.receive(receivePacket);
				String recv = new String(receivePacket.getData());
				JSONObject json = new JSONObject(recv);
				System.out.println("Received: " + recv);

				if (json.has("type")) {
					setup(json);
				} else if (json.has("play")) {
					playScore(Int2Boolean(json.getInt("play")));
				} else {
					play(json);
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}

		closeUDPSockets();

		System.out.println("Exit");
	}

	public static boolean createUDPSocket() {
		try {
			socket = new DatagramSocket(null);
			socket.setSoTimeout((int) timeout);
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(port));
		} catch (Exception e) {
			System.err.println("UDP: Could not create UDP socket on port: " + port);
			return false;
		}
		System.out.println("UDP Socket Created: " + socket.toString() + " : " + socket.getLocalSocketAddress());
		return true;
	}

	public static boolean closeUDPSockets() {

		try {
			socket.close();
		} catch (Exception e) {
			System.err.println(e.getMessage() + " UDP: Could not close port: " + port);
			return false;
		}

		return true;
	}

	public static void runTCPServer() {
		createTCPSockets();

		while (serverLoop) {
			System.out.println("Waiting...");
			try{

				String recv = new String();
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

				/*
				int read = 0;
				char[] rec = new char[1024];
				do {
					read = in.read(rec);
					if (read > 0) recv += new String(rec, 0, read);
				} while (read != -1);
				 */
				recv = in.readLine();

				System.out.println("Received: " + recv);
				if ( (recv.compareToIgnoreCase("exit")==0) || 
						(recv.compareToIgnoreCase("Connection reset")==0) ) {
					break;
				}

			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

		}

		closeTCPSockets();
	}

	public static boolean createTCPSockets() {
		try {
			serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(timeout);
			/*serverSocket.setReuseAddress(false);
			serverSocket.bind(new InetSocketAddress(port));*/
		} catch (Exception e) {
			System.err.println("Could not create TCP socket on port: " + port);
			return false;
		}
		System.out.println("TCP Connection Socket Created: " + serverSocket.toString() + " : " + serverSocket.getLocalSocketAddress());

		try {
			clientSocket = serverSocket.accept();
			clientSocket.setSoTimeout(timeout);
		} catch (Exception e) {
			System.err.println("TCP: Accept failed: " + port);
			return false;
		}

		System.out.println("\nTCP: Connection accepted from: " + clientSocket.getInetAddress());

		return true;
	}

	public static boolean closeTCPSockets() {

		if (clientSocket != null && !clientSocket.isClosed()) {
			try {
				clientSocket.close();
			} catch (IOException e) {
				System.err.println("TCP: Failed to close ClientSocket: " + e.getMessage());
				return false;
			}
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			System.err.println(e.getMessage() + " TCP: Could not close port: " + port);
			return false;
		}

		return true;
	}

	public static int Boolean2Int(boolean b) {
		return (b ? 1 : 0);
	}

	public static boolean Int2Boolean(int i) {
		return (i==0 ? false : true);
	}
}
