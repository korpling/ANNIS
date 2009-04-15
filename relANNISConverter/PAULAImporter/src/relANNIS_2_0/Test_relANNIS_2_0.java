package relANNIS_2_0;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Diese Klasse dient dem Testen des Packages relANNIS_1_0.
 * @author Florian Zipser
 *
 */
public class Test_relANNIS_2_0 
{
	public static void main(String args[])
	{
		//log4j einrichten
		Logger logger= Logger.getLogger(Test_relANNIS_2_0.class);	//log4j initialisieren
		DOMConfigurator.configure("./settings/log4j.xml");			//log-FileEinstellungen
		System.out.println("******************** start Test for relANNIS_1_0 ********************");
		
		try
		{
			CorpusGraphMgr korpGraph= new CorpusGraphMgr(new File("Ausgabeordner"), logger);
			CorpDN korpDN;
			CorpDN korpDN1;
			korpDN= new CorpDN("korp1", "");
			korpGraph.addCorpDN(korpDN);
				
			//korp2 an korp1 hängen
			korpDN= new CorpDN("korp2", "korp1/");
			korpGraph.addCorpDN(korpDN);
			
			//korp 3 an korp 2 hängen
			korpDN= new CorpDN("korp3", "korp1/korp2/");
			korpGraph.addCorpDN(korpDN);
			
			//korp4 an korp1 hängen
			korpDN= new CorpDN("korp4", "korp1/");
			korpGraph.addCorpDN(korpDN, (CorpDN)korpGraph.getDN("korp1"));
			
			//Documente einfügen
			DocDN docDN;
			// doc1 und doc 2 in korp4 einfügen
			docDN= new DocDN("korp1/korp4", "doc1", korpDN);
			korpGraph.addDocDN(docDN);
			docDN= new DocDN("korp1/korp4", "doc2", korpDN);
			korpGraph.addDocDN(docDN);
			
			// doc3 in korp2 einfügen
			docDN= new DocDN("korp1/korp2", "doc3", korpDN);
			korpGraph.addDocDN(docDN, (CorpDN)korpGraph.getDN("korp2"));
			
			korpGraph.printGraph("test/korpusGraph");
		}
		catch (Exception e)
		{e.printStackTrace();}
		
		System.out.println("******************** end Test for relANNIS_1_0 ********************");
	}
}
