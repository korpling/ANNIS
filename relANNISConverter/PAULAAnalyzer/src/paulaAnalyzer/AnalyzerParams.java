package paulaAnalyzer;

import specificAnalyzer.AbstractAnalyzer;

public class AnalyzerParams 
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"";		//Name dieses Tools
	
	protected String name= null;		//Name des Analysers
	protected String className= null;	//Name der Klassendatei des Analysers 
	protected double priority= 0;		//Priorität, mit der der Analyser analysiert
	protected double order= 0;			//Klasse der Reihenfolge, in die die analysierte Datei gehört
	protected AbstractAnalyzer analyzer= null;	//eigentlicher Analysierer
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_STD=			TOOLNAME + ">\t";
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "):\t";
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== Konstruktoren ==============================================
	
	/**
	 * Initialisiert ein neues AnalyzerParams-Objekt.
	 * @param name String - Name des Analyzers
	 * @param className String - Name der Klassendatei des Analyzers
	 * @param priority double - Priorität des Analyzers
	 * @param order double- Reihenfolgewert des Analyzers
	 */
	public AnalyzerParams(	String name, 
							String className, 
							double priority, 
							double order)
	{
		this.name= name;
		this.className= className;
		this.priority= priority;
		this.order= order;
	}

//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	/**
	 * Gibt den Namen dieses Analyzers zurück
	 */
	public String getName()
		{ return(this.name);}
	
	/**
	 * Gibt den Namen der Klassendatei des Analyzers zurück.
	 */
	public String getClassName()
		{ return(this.className);}
	
	/**
	 * Gibt die Priorität dieses Analyzers zurück
	 */
	public double getPriority()
		{ return(this.priority);}
	/**
	 * Gibt den Reihenfolgewert dieses Analyzers zurück
	 */
	public double getOrder()
		{ return(this.order);}
	
	/**
	 * Gibt den konkreten Analyzer zurück.
	 */
	public AbstractAnalyzer getAnalyzer()
		{ return(this.analyzer);}
	
	/**
	 * Setzt den konkreten Analyzer zurück.
	 */
	public void setAnalyzer(AbstractAnalyzer analyzer)
		{ this.analyzer = analyzer;}
	
	
	/**
	 * Gibt Informationen über dieses Objekt als String zurück. 
	 * @return String - Informationen über dieses Objekt
	 */
	public String toString()
	{	
		String retStr= "";
		retStr= "name: " + this.getName() + "; classname: "  + this.getClassName() + 
				"; priority: " + this.getPriority() + "; order: " + this.getOrder(); 
		return(retStr);
	}
}
