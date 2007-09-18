package disambiguator;

import java.net.URL;

import chaos.XDG.*;
import chaos.XDG.XDG;
import chaos.processors.DependencyProcessor;
import chaos.processors.ProcessorException;

public class Disambiguator extends DependencyProcessor {
	IcdList icdList;
	
	/**
	 * Instanzia un oggetto di tipo Disambiguator
	 */
	public Disambiguator() {
		
	}

	public XDG run(XDG arg0, String type) throws ProcessorException {
		if(type!=null && type.equals("SHORTEST")) {
			arg0.disambiguateIcds();
			return arg0;
		}
		return run(arg0);
	}
	
	/**
	 * Avvia le operazioni del Disambiguatore
	 */
	public XDG run(XDG arg0) throws ProcessorException {
		icdList = arg0.getSetOfIcds();
		for(int i=0; i<icdList.size(); i++) {
			System.out.print(icdList.getIcd(i).getPlausibilityAsString()+", ");
		}
		System.out.println();
		return arg0;
	}

	/**
	 * Elimina le variabili allocate
	 */
	public void finalize() throws ProcessorException {
		System.out.println("Disambiguator stopped");
	}

	/**
	 * Inizializza le variabili del Disambiguatore
	 */
	public void initialize() throws ProcessorException {
		System.out.println("Disambiguator started");
	}

	/**
	 * Inizializza le variabili del Disambiguatore
	 */
	public void initialize(URL arg0) throws ProcessorException {
		this.initialize();
	}

	/**
	 * @param args
	 */
	/*public static void main(String[] args) {

	}*/

}
