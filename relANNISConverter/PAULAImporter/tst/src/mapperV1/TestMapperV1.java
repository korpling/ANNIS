package mapperV1;


import java.util.Collection;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.junit.Before;

import paulaReader_1_0.PAULAMapperInterface;

/**
 * Diese Klasse tested die Klasse MapperV1.
 * @author Florian Zipser
 * @version 1.0
 */
public class TestMapperV1 extends TestCase
{
	private static final String TOOLNAME=		"TestPAULAConnector";
	private static final String MSG_TST=		TOOLNAME + "> Testing ";
	private static final String MSG_OK= 		"OK";
	private static final String MSG_FAILED= 	"FAILED";
	
	private static final String KW_PATH_SEP=	"/";
	
	private static final String MSG_CORP_DN=		MSG_TST + "creating a corpus node.............";
	private static final String MSG_DOC_DN=			MSG_TST + "creating a document node.......";
	
	/**
	 * The tested object.
	 */
	private PAULAMapperInterface mapper= null;
	
	/**
	 * The corpus names wich should be created 
	 */
	private String[] corpPath= {"rootCorpus", "subcorpus1", "subcorpus1a", "subcorpus1b", "subcorpus2"};
	
	
	@Before
	public void setUp() throws Exception 
	{
		mapper= new MapperV1(null);
	}
		
	/**
	 * Tested das erzeugen eines Corpus-Knoten.
	 */
	public void testCreateCorpDN() throws Exception
	{
		try
		{
			System.out.print(MSG_CORP_DN);
			
			//ersten Corpus einfügen
			String corpName= "rootCorpus";
			String corpPath= corpName;
			mapper.startCorpusData(corpPath, corpName);
			
			//ersten Corpus einfügen
			corpName= "subCorpus1";
			corpPath= corpPath + KW_PATH_SEP +corpName;
			mapper.startCorpusData(corpPath, corpName);
			
		}
		catch (AssertionFailedError e)
		{
			System.out.println(MSG_FAILED);
			throw new Exception(e);
		}
		catch (Exception e)
		{
			System.out.println(MSG_FAILED);
			throw new Exception(e);
		}
		System.out.println(MSG_OK);
	}
	
	/**
	 * Prüft die Methode insertStructEdgeDN(). Dabei wird geprüft, ob eine beliebige
	 * Liste mit StructEdge-Objekten in die richtige Reihenfolge gebracht und korrekt in 
	 * den Korpusgraphen eingefügt wird.
	 */
	public void testInsertStructEdgeDN()
	{
		//prüfen ob der Fehler ein Kreis bemerkt wird
		//prüfen ob der Fehler, es wird ein nicht existierendes const-Element referenziert bemerkt wird
		//
	}

}
