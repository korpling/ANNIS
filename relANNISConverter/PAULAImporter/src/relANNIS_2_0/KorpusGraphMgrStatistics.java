package relANNIS_2_0;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Ein Objekt dieser Klasse nimmt ein paar statistische Auswertungen anderer Objekt, in
 * diesem Fall eines KorpusGraphMgr-Objektes vor.
 * @author Administrator
 *
 */
public class KorpusGraphMgrStatistics 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"KorpusGraphMgrStatistics";		//Name dieses Tools

	private static Hashtable<String, Long> nodeReached= null;
	private static Hashtable<String, Long> nodeLeft= null;
	private static Hashtable<String, Long> checkConstraint= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== Konstruktoren ==============================================
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	public static void graphTraversal(String VAL_NAME, String FCT_NAME)
	{
		if (nodeReached== null) nodeReached= new Hashtable<String, Long>();
		if (nodeLeft== null) nodeLeft= new Hashtable<String, Long>();
		if (checkConstraint== null) checkConstraint= new Hashtable<String, Long>();
		if (FCT_NAME.equalsIgnoreCase("nodeReached"))
		{
			Long tmpCounter= (long)1;
			if (nodeReached.get(VAL_NAME)!= null) 
			{
				tmpCounter= nodeReached.get(VAL_NAME);
				tmpCounter++;
				nodeReached.remove(VAL_NAME);
			}
			nodeReached.put(VAL_NAME, tmpCounter);
		}
		else if (FCT_NAME.equalsIgnoreCase("nodeLeft"))
		{
			Long tmpCounter= (long)1;
			if (nodeLeft.get(VAL_NAME)!= null) 
			{
				tmpCounter= nodeLeft.get(VAL_NAME);
				tmpCounter++;
				nodeLeft.remove(VAL_NAME);
			}
			nodeLeft.put(VAL_NAME, tmpCounter);
			
		}
		else if (FCT_NAME.equalsIgnoreCase("checkConstraint"))
		{
			Long tmpCounter= (long)1;
			if (checkConstraint.get(VAL_NAME)!= null) 
			{
				tmpCounter= checkConstraint.get(VAL_NAME);
				tmpCounter++;
				checkConstraint.remove(VAL_NAME);
			}
			checkConstraint.put(VAL_NAME, tmpCounter);
		}
	}
	
	public static void printStatistics()
	{
		Enumeration keys= nodeReached.keys();
		System.out.println(MSG_STD + "NodeReached: ");
		while (keys.hasMoreElements())
		{
			String key= (String)keys.nextElement();
			System.out.println(MSG_STD + "number of '"+key+"'-elements: "+ nodeReached.get(key));
		}
		keys= nodeLeft.keys();
		System.out.println(MSG_STD + "nodeLeft: ");
		while (keys.hasMoreElements())
		{
			String key= (String)keys.nextElement();
			System.out.println(MSG_STD + "number of '"+key+"'-elements: "+ nodeLeft.get(key));
		}
		keys= checkConstraint.keys();
		System.out.println(MSG_STD + "checkConstraint: ");
		while (keys.hasMoreElements())
		{
			String key= (String)keys.nextElement();
			System.out.println(MSG_STD + "number of '"+key+"'-elements: "+ checkConstraint.get(key));
		}
	}
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		 retStr= "this method isn´t implemented";
		return(retStr);
	}
}
