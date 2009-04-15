package paulaAnalyzer;

import java.io.File;

/**
 * Diese Klasse stellt den Kernpunkt der Kommunikation zwischen PAULAAnalyzer, mainAnalyzer
 * und den specificAnalyzern dar. Dieses Objekt wird von jeder dieser Klassen um ein paar 
 * Daten erweitert. Nach dem Analyseprozess enthält dieses Objekt alle Analyseergebnisse über
 * die darin enthaltene PAULA-Datei.
 * 
 * Die Statusabfolge dieser Klasse: 	INITED --> SPECIFIED --> ORDERED --> TYPED 	--> COMMENTED	NOT_TYPED<br/>
 * <ol>
 * 	<li>INITED: PAULA-Dateiname gesetzt</li>
 * 	<li>SPECIFIED: PAULA-DTD, PAULA-ID und PAULA-Typ(muss nicht gesetzt sein) gesetzt</li>
 * 	<li>ORDERED: Reihenfolge des Analysers übertragen</li>
 * 	<li>TYPED: Analysetyp und abstrakter Analysetyp gesetzt</li>
 * 	<li>COMMENTED: Kommentar gesetzt</li>
 * 	<li>ABORDED: Der Versuch der Typisierung wurde abgebrochen</li>
 * </ol>
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public class AnalyzeContainer
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"AnalyzeContainer";		//Name dieses Tools
	private static final boolean DEBUG= false;		//DEBUG Schalter
	
	/**
	 * Gibt den status an, indem sich das Analyze-ContainerObjekt befindet
	 *
	 */
	public enum STATUS {INITED, SPECIFIED, ORDERED, TYPED, COMMENTED, ABORDED};
	
	/**
	 * Gibt den sog. abstrakten Typ einer Datei an, bzw. gibt an, ob es sich um Metadaten 
	 * oder Annotationsdaten handelt. Diser Typ ist nur wichtig für das Framework des 
	 * PAULA-Analyzers, während der Typ wichtig für den PAULAImporter ist.<br/>
	 * META_DATA:			normale Metadaten
	 * META_STRUCT_DATA:	Strukturdaten für die Metadaten (anno.xml)
	 * ANNO_DATA:			alle anderen Daten, (Primärtext, Token, Struktur, Annotationen)
	 */
	public enum ABS_ANA_TYPE {META_DATA, META_STRUCT_DATA, ANNO_DATA};	
	
	private STATUS status= STATUS.INITED;
	//wird vom PAULAAnalyzer hinzugefügt
	private File paulaFile= null;				//Quelldatei, um die es geht
	
	//wird vom mainAnalyzer hinzugefügt
	private String paulaID= null;					//PAULA-ID dieser Datei
	private String paulaType= null;				//PAULA-Typ dieser Datei
	private ABS_ANA_TYPE absAnaType= null;			//PAULA-Typ dieser Datei
	private File dtdFile= null;					//DTD zu der PAULA-Datei
	
	//wird vom specificAnalyzer hinzugefügt
	private String anaType= null;					//Analysetyp der Datei
	private String comment= null;					//Kommentar zu diesem Eintrag
	
	//wird vom mainAnalyzer hinzugefügt
	private Double order= null;					//Reihenfolge, in der die Datei importiert werden soll (im Bezug auf andere)
	private boolean toImport= true;				//gibt an, ob Datei zu importieren ist
	
							
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	private static final String MSG_START_FCT=		MSG_STD + "start method: ";
	private static final String MSG_END_FCT=		MSG_STD + "end method: ";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_PFILE=		MSG_ERR + "The given PAULA file is empty.";
	private static final String ERR_NOT_EXISTING_PFILE=	MSG_ERR + "The given PAULA file does not exist: ";
	private static final String ERR_EMPTY_DTD=			MSG_ERR + "The given DTD file is empty.";
	private static final String ERR_NOT_EXISTING_DTD=	MSG_ERR + "The given DTD file does not exist: ";
	private static final String ERR_EMPTY_ANATYPE=		MSG_ERR + "The given analyze type is empty.";
	private static final String ERR_NOT_INITED=			MSG_ERR + "Cannot set object into status SPECIFIED, before it is not in status INITED.";
	private static final String ERR_NOT_SPECIFIED=		MSG_ERR + "Cannot set object into status ORDERED, before it is not in status SPECIFIED.";
	private static final String ERR_NOT_ORDERED=		MSG_ERR + "Cannot set object into status TYPED, before it is not in status ORDERED.";
	private static final String ERR_NOT_TYPED=			MSG_ERR + "Cannot set object into status COMMENTED, before it is not in status TYPED.";
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Instanziiert ein AnalyzeContainer Objekt und initialisiert dieses mit der entsprechenden
	 * PAULA-Bezugsdatei
	 * @param paulaFile String - Paula Datei, auf die sich der Container bezieht.
	 */
	public AnalyzeContainer(File paulaFile) throws Exception
	{
		if (paulaFile== null) throw new Exception(ERR_EMPTY_PFILE);
		if (!paulaFile.exists()) throw new Exception(ERR_NOT_EXISTING_PFILE + paulaFile.getAbsoluteFile());
			
		this.paulaFile= paulaFile;
	}
	
//	 ============================================== private Methoden ==============================================
	/**
	 * Überprüft, ob alle Daten des Objektes ausreichen um den Status zu erhöhen und erhöht
	 * diesen ggf.
	 * Statuslogik
	 * start:	INITED --> SPECIFIED --> ORDERED --> TYPED 	--> COMMENTED 
	 */
	private void incStatus()
	{
		if (DEBUG) System.out.println(MSG_STD+ "call inStatus(), old status: " + this.status);
		//wenn Status gleich INITED und dtdFile gesetzt, PAULAID gesetzt und PAULAType gesetzt
		// auf SPECIFIED setzen
		if (this.status.equals(STATUS.INITED)) 
		{
			//alt, nur wenn Type gesetzt sein muss
			//if ((this.dtdFile != null) && 
			//		((this.paulaID != null)&& (!this.paulaID.equalsIgnoreCase(""))) &&
			//		((this.paulaType != null) && (!this.paulaType.equalsIgnoreCase(""))))
			if ((this.dtdFile != null) && 
					((this.paulaID != null)&& (!this.paulaID.equalsIgnoreCase("")))) 
			this.status= STATUS.SPECIFIED;
		}
		//wenn Status gleich SPECIFIED
		// auf ORDRED setzen
		else if (this.status.equals(STATUS.SPECIFIED)) 
		{
			if (this.order != null) this.status= STATUS.ORDERED;
		}
		//wenn Status gleich ORDERED
		// auf TYPED setzen
		else if (this.status.equals(STATUS.ORDERED)) 
		{
			if ((this.absAnaType != null) && (this.anaType != null) && (!this.anaType.equalsIgnoreCase("")))
			{
				this.status= STATUS.TYPED;
			}
		}
		//wenn Status gleich TYPED
		else if (this.status.equals(STATUS.TYPED)) 
		{
			//wennn Kommentar gesetzt
			if ((this.comment != null) && (!this.comment.equalsIgnoreCase("")))
				this.status= STATUS.COMMENTED;
		}
		if (DEBUG) System.out.println(MSG_STD+ "call inStatus(), new status: " + this.status);
	}
//	 ============================================== öffentliche Methoden ==============================================
	
	/**
	 * Setzt die DTD der PAULA-Datei auf die übergebene.
	 * @param dtdFile File - DTD zu der PAULA-Datei
	 * @exception Fehler, wenn Datei leer
	 */
	public void setDTD(File dtdFile) throws Exception
	{
		if (DEBUG) System.out.println(MSG_STD + "call function setDTD()");
		
		// wenn status nicht INITED
		if (!this.status.equals(STATUS.INITED)) throw new Exception(ERR_NOT_INITED);
		if (dtdFile== null) throw new Exception(ERR_EMPTY_DTD);
		if (!dtdFile.exists()) throw new Exception(ERR_NOT_EXISTING_DTD + dtdFile.getAbsoluteFile());
		
		this.dtdFile= dtdFile;
		this.incStatus();
	}
	
	/**
	 * Setzt den PAULA-Typ der PAULA-Datei auf den übergebenen.
	 * @param paulaType String - PAULA-Typ zu der PAULA-Datei
	 */
	public void setPAULAType(String paulaType) throws Exception
	{ 
		if (DEBUG) System.out.println(MSG_STD + "call function setPAULAType()");
		
		// alt, da PAULA-TYp nicht angegeben werden muss nach Paula_header.dtd wenn status nicht INITED
		//if (!this.status.equals(STATUS.INITED)) throw new Exception(ERR_NOT_INITED);
		this.paulaType= paulaType;
		//this.incStatus();
	}
	
	/**
	 * Setzt die PAULA-ID der PAULA-Datei auf die übergebene.
	 * @param paulaID String - PAULA-ID zu der PAULA-Datei
	 */
	public void setPAULAID(String paulaID) throws Exception
	{ 
		if (DEBUG) System.out.println(MSG_STD + "call function setPAULAID()");
		
		// wenn status nicht INITED
		if (!this.status.equals(STATUS.INITED)) throw new Exception(ERR_NOT_INITED);
		this.paulaID= paulaID;
		this.incStatus();
	}
	
	/**
	 * Setzt die Reihenfolge (vielmehr den Platz in der Reihenfolge) in der die
	 * analysierte Datei später importiert werden soll.
	 * @param order double - Reihenfolge der analysierten Datei
	 * @throws Exception 
	 */
	public void setOrder(double order) throws Exception
	{
		if (DEBUG) System.out.println(MSG_STD + "call function setOrder");
		// wenn status nicht SPECIFIED
		if (!this.status.equals(STATUS.SPECIFIED)) throw new Exception(ERR_NOT_SPECIFIED);
		this.order= order;
		this.incStatus();
	}
	
	/**
	 * Setzt den Analyse-Typ der PAULA-Datei auf die übergebene.
	 * @param anaType String - Analyse-Typ zu der PAULA-Datei
	 */
	public void setAnaType(String anaType) throws Exception
	{ 
		if (DEBUG) System.out.println(MSG_STD + "call function setANAType");
		
		// wenn status nicht ORDERED
		if (!this.status.equals(STATUS.ORDERED)) throw new Exception(ERR_NOT_ORDERED);
		if ((anaType== null) || (anaType.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ANATYPE);
		this.anaType= anaType; 
		this.incStatus();
	}
	
	/**
	 * Setzt den abstrakten Analyse-Typ der PAULA-Datei auf den übergebenen.
	 * @param absAnaType String - abstrakter Analyse-Typ zu der PAULA-Datei
	 */
	public void setAbsAnaType(ABS_ANA_TYPE absAnaType) throws Exception
	{ 
		if (DEBUG) System.out.println(MSG_STD + "call function setAbsANAType");
		
		// wenn status nicht ORDERED
		if (!this.status.equals(STATUS.ORDERED)) throw new Exception(ERR_NOT_ORDERED);
		this.absAnaType= absAnaType;
		this.incStatus();
	}
	
	/**
	 * Setzt den Kommentar zu dem Analyse-Typ der PAULA-Datei auf die übergebene.
	 * @param comment String - Kommentar zu der PAULA-Datei
	 */
	public void setComment(String comment) throws Exception
	{ 
		if (DEBUG) System.out.println(MSG_STD + "call function setComment");
		
		// wenn status nicht TYPED
		if (!this.status.equals(STATUS.TYPED)) throw new Exception(ERR_NOT_TYPED);
		if ((anaType== null) || (anaType.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ANATYPE);
		if (this.status != STATUS.TYPED) throw new Exception(ERR_NOT_TYPED);
		this.comment= comment; 
		this.incStatus();
	}
	
	/**
	 * Gibt an, ob die entsprechende PAULA-Datei importiert werden soll.
	 * @param toImport boolean - flag, das angibt ob importiert werden soll
	 */
	public void setToImport(boolean toImport)
		{ this.toImport= toImport; }
	
	/**
	 * Setzt den Status dies Objektes auf aborted, eine weitere Typisierung dieses
	 * Objektes ist dann nicht mehr möglich.
	 */
	public void setAbborded()
		{ this.status= STATUS.ABORDED; }
	
	// ------------------------- Get-Methoden -------------------------
	/**
	 * Gibt die PAULA-Quell-Datei zurück.
	 * @return PAULA-Quell-Datei
	 */
	public File getPAULAFile()
		{ return(this.paulaFile); }
	
	/**
	 * Gibt die DTD der PAULA-Quell-Datei zurück.
	 * @return DTD der PAULA-Quell-Datei
	 */
	public File getDTD()
		{ return(this.dtdFile); }
	
	/**
	 * Gibt den PAULA-Typ der PAULA-Quell-Datei zurück.
	 * @return PAULA-Typ der PAULA-Quell-Datei
	 */
	public String getPAULAType()
		{ return(this.paulaType); }
	
	/**
	 * Gibt den abstrakten PAULA-Typ der PAULA-Quell-Datei zurück.
	 * @return abstrakter Analyse-Typ der PAULA-Quell-Datei
	 */
	public ABS_ANA_TYPE getAbsAnaType()
		{ return(this.absAnaType); }
	
	/**
	 * Gibt den abstrakten Analyse-Typ der PAULA-Quell-Datei zurück.
	 * @return PAULA-ID der PAULA-Quell-Datei
	 */
	public String getPAULAID()
		{ return(this.paulaID); }
	
	/**
	 * Gibt den Analyse-Typ der PAULA-Quell-Datei zurück.
	 * @return Analyse-Typ der PAULA-Quell-Datei
	 */
	public String getAnaType()
		{ return(this.anaType); }
	
	/**
	 * Gibt den Kommentar zu dem Analyse-Typ der PAULA-Quell-Datei zurück.
	 * @return Kommentar des Analyse-Typ der PAULA-Quell-Datei
	 */
	public String getComment()
		{ return(this.comment); }
	
	/**
	 * Gibt die Reihenfolge (vielmehr den Platz in der Reihenfolge) der zu analysierenden 
	 * PAULA-Datei zurück, in der diese später importiert werden soll.
	 * @return Reihenfolge der PAULA-Datei
	 */
	public Double getOrder()
		{ return(this.order); }
	
	/**
	 * Gibt den Status zurück in dem sich das Objekt gerade befindet.
	 * @return Status des Objektes
	 */
	public STATUS getStatus()
		{ return(this.status); }
	
	/**
	 * Gibt zurück, ob die entsprechende PAULA-Datei importiert werden soll.
	 * @return wenn true, dann soll importiert werden, sonst nicht
	 * @return true, wenn Datei importiert werden soll, sonst false
	 */
	public boolean getToImport()
		{ return(this.toImport); }
	/**
	 * Erzeugt eine exakte Kopie dieses AnalyzeContainer-Objektes und gibt diese zurück.
	 * @return eine Kopie dieses Objektes
	 */
	public AnalyzeContainer clone2() throws Exception
	{
		if (DEBUG) System.out.println(MSG_START_FCT + "clone2()");
		//erzeuge neues Objekt und initialisiere es
		AnalyzeContainer newCon= new AnalyzeContainer(this.getPAULAFile());
		
		//setzt die DTD der Analyse Datei
		if (this.getDTD() != null) newCon.setDTD(this.getDTD());
		//setzt die PAULA-ID der zu analysiereden Datei
		if (this.getPAULAID() != null) newCon.setPAULAID(this.getPAULAID());
		//setzt den PAULA-Typ der zu analysiereden Datei		
		if (this.getPAULAType() != null) newCon.setPAULAType(this.getPAULAType());
		//Analyse Typ setzen
		if (this.getAnaType() != null) newCon.setAnaType(this.getAnaType());
		//Analyse Typ setzen
		if (this.getAbsAnaType() != null) newCon.setAbsAnaType(this.getAbsAnaType());
		//Analyse-Kommentar setzen
		if (this.getComment() != null) newCon.setComment(this.getComment());
		if (DEBUG) System.out.println(MSG_END_FCT + "clone2()");
		return(newCon);
	}
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= "status: "		+ this.status;
		try{ 
			retStr= retStr + ", paula file: " 	+ this.getPAULAFile().getCanonicalPath().replace("\\", "/");
			if (this.getDTD()!= null) retStr= retStr + ", dtd file: " 	+ this.getDTD().getCanonicalPath().replace("\\", "/");
		}
		catch (Exception e)
			{retStr= retStr + ", paula file: EMPTY";}
		
		if (this.getPAULAID()!= null) retStr= retStr + ", paula id: " 	+ this.getPAULAID();
		if (this.getPAULAType()!= null) retStr= retStr + ", paula type: " + this.getPAULAType();
		if (this.getAbsAnaType()!= null) retStr= retStr + ", abstract analyze type: " + this.getAbsAnaType();
		if (this.getAnaType()!= null) retStr= retStr + ", analyze type: " + this.getAnaType();
		if (this.getOrder()!= null) retStr= retStr + ", order: " + this.getOrder();
		return(retStr);
	}
//	 ============================================== main Methode ==============================================	


}
