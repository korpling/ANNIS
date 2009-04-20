package importer.paula.paula10.util.xPointer;

/**
 * Die Klasse XPtrRef stellt Objekte zur Verfügung, die Informationen über das Refernzziel 
 * eines XPointers enthalten. Dabei wird in zwei Arten eines referenzziels unterschieden.
 * Dies sind zum einen Einzelelemente, die sich dadurch auszeichnen das sie nur ein Element
 * als Ziel enthalten. Zum anderen sind dies Elementbereiche. Referenzen dieser Art 
 * enthalten mehrere Elemente als Ziel. Ein Objekt dieser Klasse hält dazu den id-Wert
 * der linken Grenze und den der rechten Grenze des Elementbereichs.
 * @author Florian Zipser
 * @version 1.0
 */
public class XPtrRef 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"XPtrRef";		//Name dieses Tools
	
	/**
	 *	Ein Pointer kann Elementknoten, oder Textknoten als Ziel haben.
	 *	ELEMENT= Elementknoten, TEXT= Textknoten, NOTSET= nicht gesetzt
	 */
	public enum POINTERTYPE{ELEMENT, TEXT, XMLFILE, NOTSET};
	
	private POINTERTYPE type= POINTERTYPE.NOTSET;
	private boolean isRange= true;			//gibt an, ob es sich bei dieser XPointeradresse um einen Bereich handelt
	private String docName= null;			//Name des Dokuments, auf das sie Referenz zeigt
	private String id= null;				//ID-Wert, auf den die Refrenz zeigt (nur Einzelelement)
	
	private String left= null;				//ID-Wert, linke Grenze eines Elementbereichs
	private String right= null;				//ID-Wert, rechte Grenze eines Elementbereichs
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	private static final String ERR_EMPTY_DOCNAME=	MSG_ERR + "The given document name is empty.";
	private static final String ERR_EMPTY_ID=		MSG_ERR + "The given id value for target of the reference is empty.";
	private static final String ERR_EMPTY_LEFT=		MSG_ERR + "The given value for left border id of the reference is empty.";
	private static final String ERR_EMPTY_RIGHT=	MSG_ERR + "The given value for right border id of the reference is empty.";
	private static final String ERR_NO_RANGE=		MSG_ERR + "The target of this XPointer reference is a simple element, not a range.";
	private static final String ERR_IS_RANGE=		MSG_ERR + "The target of this XPointer reference is a range, not a simple element.";
	private static final String ERR_INCORRECT_TYPE=	MSG_ERR + "Pointer type could not be 'NOTSET'.";
//	 ============================================== Konstruktoren ==============================================

	/**
	 * Erzeugt ein XPointer-Refernzziel Element. Dieses Element ist vom Typ Einzelelement.
	 * @param docName String - Name des Dokumentes, aus dem das Zielelement stammt
	 * @param id String - ID des Zielelementes 
	 */
	public XPtrRef(String docName, String id) throws Exception
	{
		if ((docName == null) || (docName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_DOCNAME);
		if ((id == null) || (id.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_ID);
		
		this.id= id;
		this.docName= docName;
		this.isRange= false;
	}
	
	/**
	 * Erzeugt ein XPointer-Refernzziel Element. Dieses Element ist vom Typ Elementbereich.
	 * @param docName String - Name des Dokumentes, aus dem das Zielelement stammt
	 * @param left String - id des Elementes, das die linke Grenze bildet
	 * @param right String - id des Elementes, das die rechte Grenze bildet
	 */
	public XPtrRef(String docName, String left, String right) throws Exception
	{
		if ((docName == null) || (docName.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_DOCNAME);
		if ((left == null) || (left.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_LEFT);
		if ((right == null) || (right.equalsIgnoreCase(""))) throw new Exception(ERR_EMPTY_RIGHT);
		
		this.left= left;
		this.right= right;
		this.docName= docName;
		this.isRange= true;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Setzt den Typ dieses Pointers.
	 * @param type POINTERTYPE - Typ dieses Pointers, muss ungleich NOTSET sein
	 */
	public void setType(POINTERTYPE type) throws Exception
	{
		if (type == POINTERTYPE.NOTSET) throw new Exception (ERR_INCORRECT_TYPE);
		this.type= type;
	}
	
	/**
	 * Gibt den Typ dieses Pointers zurück.
	 * @return Typ dieses Pointers.
	 */
	public POINTERTYPE getType()
	{ return(this.type);}
	
	
	/**
	 * Gibt an, ob es sich bei dieser XPointer-Adresse um ein Einzelement oder einen 
	 * Elementbereich handelt.
	 * @return true, wenn Adresse ein Elementbereich
	 */
	public boolean isRange()
		{ return(this.isRange); }
	
	/**
	 * Gibt den Namen des Dokumentes zurück, auf das der XPointer zeigt. Dieses Element
	 * enthält das Element mit der id bzw. Elemente mit linker und rechter Bereichsgrenze.
	 * @return Dokumentname des Zieldokuments
	 */
	public String getDoc()
		{return(this.docName); }
	
	/**
	 * Gibt das Ziel dieser XPointer-Referenz zurück, wenn es sich bei diesem 
	 * XPointer-Referenz um ein Einzelelement handelt.
	 * @return ID-Wert des Ziels dieser Referenz
	 * @throws Exception Fehler, wenn Referenz kein Einzelelement
	 */
	public String getID() throws Exception
	{ 
		if (this.isRange) throw new Exception(ERR_IS_RANGE); 
		return(this.id); 
	}
	
	/**
	 * Gibt die linke Bereichsgrenze des Ziels dieser XPointer-Referenz zurück, 
	 * wenn es sich bei dieser XPointer-Referenz um ein Elementbereich handelt.
	 * @return ID-Wert des linken Rand des Ziels dieser Referenz
	 * @throws Exception Fehler, wenn Referenz kein Elementbereich
	 */
	public String getLeft() throws Exception
	{ 
		if (!this.isRange) throw new Exception(ERR_NO_RANGE);
		return(this.left); 
	}
	
	/**
	 * Gibt die rechte Bereichsgrenze des Ziels dieser XPointer-Referenz zurück, 
	 * wenn es sich bei dieser XPointer-Referenz um ein Elementbereich handelt.
	 * @return ID-Wert des rechten Rand des Ziels dieser Referenz
	 * @throws Exception Fehler, wenn Referenz kein Elementbereich
	 */
	public String getRight() throws Exception
	{ 
		if (!this.isRange) throw new Exception(ERR_NO_RANGE);
		return(this.right); 
	}
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		
		retStr= MSG_STD + "document name: "+ this.docName+", type: ";
		if (this.isRange)
		{
			retStr= retStr + "range" + ", (left: " + this.left + ", right: " + this.right + ")";
		}
		else retStr= retStr + "simple node" + ", id" + this.id;
		
		return(retStr);
	}
}
