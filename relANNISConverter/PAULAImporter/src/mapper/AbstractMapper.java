package mapper;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;

import reader.AbstractReader;

/**
 * Die abstrakte Klasse AbstractMapper stellt das Bindeglied zwischen den Readern und
 * dem jeweiligen Korpusmodell dar. Diese Klasse bildet das Quellmodell auf das interne
 * Zielmodell ab.  
 * @author Florian Zipser
 * @version 1.0
 *
 */
public abstract class AbstractMapper 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"AbstractMapper";		//Name dieses Tools
	
	protected Logger logger= null;			//log4j-Logger
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
	protected static final String ERR_NOT_OVERRIDE=		MSG_ERR + "This methode has to be overriden by derived class. Method name: ";
//	 ============================================== Konstruktoren ==============================================
	/**
	 * Initialisiert ein Mapper Objekt und setzt den logger zur Nachrichtenausgabe.
	 * @param logger Logger - Logger zur Nachrichtenausgabe
	 */
	public AbstractMapper(Logger logger)
	{
		this.logger= logger;
	}
//	 ============================================== private Methoden ==============================================
//	 ============================================== protected Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	
	/**
	 * Bildet ein Quelldatenmodell auf eine Instanz des Internen Korpus Modells ab.
	 * @param srcFolder File - Quellverzeichnis, aus dem das zu mappende Korpus stammt
	 * @param dstFolder File - Zielverzeichniss, in das die Output-Dateien geschrieben werden
	 * @param tmpFolder File - temporäres Verzeichnis zum Zwischenspeichern
	 */
	public void map(	File srcFolder,
						File dstFolder,
						File tmpFolder, 
						boolean toDot) throws Exception
		{ throw new Exception(ERR_NOT_OVERRIDE + "map()"); }
	
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
//	 ============================================== main Methode ==============================================	


}
