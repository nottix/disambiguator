import chaos.XDG.*;
import chaos.processors.*;
import chaos.textstructure.*;
import chaos.alternatives.*;

import java.io.*;
import java.util.*;


public class AI {
	public static void main(String [] argv) {
		try {
			chaos.ConfigurationHandler.initialize();
			chaos.ConfigurationHandler.parseKBPropFile("it","tut");
			Parser p = new Parser();
			p.initialize();
			String dir = argv[0];
			if ((new File(dir)).isDirectory()) {
				File [] files = (new File(dir)).listFiles();
				for (int i=0; i < files.length; i++) {
					Text t =Text.load_new(files[i]);

					Vector <Paragraph> prs = t.getParagraphs();
					for (Paragraph pr:prs) {
						Vector <XDG> xdgs = pr.getXdgs();
						for (int k = 0; k < xdgs.size(); k ++ ) {
							XDG xdg_bis = new XDG();
							System.out.print(".");
							xdg_bis.setSetOfConstituents(xdgs.elementAt(k).getSetOfConstituents());
							xdg_bis = p.run(xdg_bis,"VSA,SSA");
							xdgs.setElementAt(xdg_bis,k);
						}
						
					}
					t.save( new File (new File("./chaos"),files[i].getName() ), AvailableOutputFormat.valueOf("xml"), true);
				}
			} else {
				System.out.println( "Error: " + dir + " is not a directory");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}