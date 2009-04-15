package paulaAnalyzer;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Diese Klasse ist der SettingHandler der Klasse PAULAAnalyzer, es werden alle nötigen Daten
 * eingelesen um die EInstellungen der KLasse PAULAnalyzer vorzunehmen.
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class PASettingHandler extends DefaultHandler2 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"PAULAANalyzerSettingHandler";		//Name dieses Tools
	
	//TAG Namen
	private static final String TAG_ANALYZERS= "analyzers";		
	private static final String TAG_COLLECTION= "collection";		
	private static final String TAG_ANALYZER= 	"analyzer";		
	
	//Attribut Namen
	private static final String ATT_COL_PATH= 	"path";
	private static final String ATT_ANA_NAME= 	"name";
	private static final String ATT_ANA_CNAME= 	"className";
	private static final String ATT_ANA_PRIO= 	"priority";
	private static final String ATT_ANA_ORDER= 	"order";
	
	private Vector<AnalyzerParams> analyzers= null;			//ausgelesene Parameter der Analyser
	private String currPath= "";							//aktueller Pfadname
	private boolean tagAnasRead=	false;					//Tag Analyzers gelesen
	private boolean tagColRead=	false;						//Tag Collection gelesen
	private boolean tagAnaRead=	false;						//Tag Analyzer gelesen
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_XML_ERR=		MSG_ERR + "There´s an error in the xml file. ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_ANAS_NOT_READ=	MSG_XML_ERR + "The tag '"+TAG_ANALYZERS+"' has not been seen.";
	private static final String ERR_COL_NOT_READ=	MSG_XML_ERR + "The tag '"+TAG_COLLECTION+"' has not been seen.";
	private static final String ERR_FORMAT=		MSG_XML_ERR + "The values '"+ATT_ANA_PRIO+"' and '"+ATT_ANA_ORDER+"' must be vonvertable to double.";
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Initalisiert ein PASettingHandler-Objekt.
	 */
	public PASettingHandler()
	{
		this.analyzers= new Vector<AnalyzerParams>();
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	
	/**
	 * Gibt eine Liste zurück mit Analyzer Parameteren, diese wurden zuvor aus der Setting
	 * Datei ausgelesen.
	 * @return Liste mit Analyzer Parameteren, null wenn noch nicht eingelesen wurde
	 */
	public Vector<AnalyzerParams> getAnalyzerParams()	
		{ return(this.analyzers); }
	
	// ------------------------------------------ SAX-Methoden
	public void startElement(	String uri,
								String localName,
								String qName,
								Attributes attributes) throws SAXException
	{
		if (qName.equalsIgnoreCase(TAG_ANALYZERS)) this.tagAnasRead= true;
		else if (qName.equalsIgnoreCase(TAG_COLLECTION)) 
		{
			if (!this.tagAnasRead) throw new SAXException(ERR_ANAS_NOT_READ);
			this.tagColRead= true;
			//lese Attribute 
			for (int i= 0; i < attributes.getLength(); i++)
			{
				if (ATT_COL_PATH.equalsIgnoreCase(attributes.getQName(i))) this.currPath= attributes.getValue(i);
			}
		}
		else if (qName.equalsIgnoreCase(TAG_ANALYZER)) 
		{
			if (!this.tagAnasRead) throw new SAXException(ERR_ANAS_NOT_READ);
			if (!this.tagColRead) throw new SAXException(ERR_COL_NOT_READ);
			this.tagAnaRead= true;
			
			String name= "";
			String cName= "";
			String prio= "";
			String order= "";
			
			//lese Attribute 
			for (int i= 0; i < attributes.getLength(); i++)
			{
				if (ATT_ANA_NAME.equalsIgnoreCase(attributes.getQName(i))) name= attributes.getValue(i);
				if (ATT_ANA_CNAME.equalsIgnoreCase(attributes.getQName(i))) cName= attributes.getValue(i);
				if (ATT_ANA_PRIO.equalsIgnoreCase(attributes.getQName(i))) prio= attributes.getValue(i);
				if (ATT_ANA_ORDER.equalsIgnoreCase(attributes.getQName(i))) order= attributes.getValue(i);
			}
			Double dPrio= 0.0;
			Double dOrder= 0.0;
			try
			{
				dPrio= new Double(prio);
				dOrder= new Double(order);
			}
			catch (NumberFormatException e)
				{throw new SAXException(ERR_FORMAT); }
			
			if ((this.currPath!= null) && (!this.currPath.equalsIgnoreCase("")))
				cName= this.currPath + cName;
			AnalyzerParams aParam= new AnalyzerParams(name, cName, dPrio, dOrder);
			this.analyzers.add(aParam);
		}
	}
	
	public void endElement(	String uri,
            				String localName,
            				String qName) throws SAXException
    {
		if (qName.equalsIgnoreCase(TAG_ANALYZERS)) 
		{
			this.tagAnasRead= false;
			this.tagColRead= false;
			this.tagAnaRead= false;
		}
		else if (qName.equalsIgnoreCase(TAG_COLLECTION)) 
		{
			this.tagColRead= false;
			this.tagAnaRead= false;
		}
		else if (qName.equalsIgnoreCase(TAG_ANALYZER)) this.tagAnaRead= false;
    }
	// ------------------------------------------ Ende SAX-Methoden
}
