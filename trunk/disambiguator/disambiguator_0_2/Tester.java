package disambiguator_0_2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Tester {

	/**
	 * Serve per dare una dimostrazione delle funzionalità di tutto il progetto.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			printMenu();
			InputStreamReader isr = new InputStreamReader( System.in );
			BufferedReader stdin = new BufferedReader( isr );
			String input = stdin.readLine();
			switch(Integer.valueOf(input).intValue()) {
				case 1:
					break;
				case 2:
					break;
				case 3:
					break;
				case 4:
					break;
			
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void printMenu() {
		System.out.println("Progetto di Intelligenza Artificiale 2006/2007\n\n" +
			"1: Caricare il corpus di training nel DB\n" +
			"2: Rimuovere il corpus di training dal DB\n" +
			"3: Rimuovere il DB chaos\n" +
			"------------------------------------------\n" +
			"4: Avviare la disambiguazione\n\n>");
	}

}
