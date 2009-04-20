package importer.paula.paula10.util.timer;


/**
 * Die Klasse Timer realisiert eine Stoppuhr, sie kann genutzt werden, um die Laufzeit eines
 * Programmes zu messen.
 * 
 * @author Flo
 * @version 1.0
 *
 */
public class Timer 
{
//	 ============================================== private Variablen ==============================================
	private long millis= 0L;
	private long tempMillis= 0L;		//temporäre Zeit, zum erneuten starten
	
	private int hours= 0;
	private int mins= 0;
	private int secs= 0;
	private long msecs= 0;
	private boolean stopped= false;

//	 ============================================== Konstruktoren ==============================================
	/**
	 * Instanziiert ein Timer-Objekt und startet die Zeitmessung.
	 */
	public Timer()
	{
		this.start();
	}
	
//	 ============================================== private Methoden ==============================================
	/**
	 * Belegt die Werte hours, mins, secs und msecs mit einer Zeit berechnet aus this.millis. 
	 */
	private void compTime()
	{
		long milliSecs= 0L;
		if (stopped) milliSecs= this.millis;
		else milliSecs= System.currentTimeMillis() - this.millis;
		
		this.hours= (int)Math.floor(milliSecs /(1000 * 60 * 60));
		milliSecs= milliSecs - this.hours* (1000 * 60 * 60);
		this.mins= (int)Math.floor(milliSecs /(1000 * 60));
		milliSecs= milliSecs - this.mins* (1000 * 60);
		this.secs= (int)Math.floor(milliSecs /(1000));
		milliSecs= milliSecs - this.secs* (1000);
		this.msecs= milliSecs;
	}
//	 ============================================== öffentl. Methoden ==============================================

	/**
	 * Startet den Zeitmessvorgang
	 */
	public void start()
		{ this.millis= System.currentTimeMillis(); }
	
	/**
	 * Startet die Zeitmessung erneut. beim stoppen wird die neu Zeit dann auf die alte addiert
	 */
	public void startAgain()
	{ 
		this.tempMillis = this.millis;
		this.millis= System.currentTimeMillis();
	}
	
	/**
	 * Stoppt den Messvorgang
	 */
	public void stop()
	{ 
		this.millis= (System.currentTimeMillis() - this.millis) + this.tempMillis;
		this.stopped= true;
		//this.millis= 3661001L;
	}
	
	/**
	 * Wenn die Zeit gestoppt wurde, wird die vergangene Zeit bis zum Stoppen zurückgegeben,
	 * ansonsten die Zeit die bis zum Aufruf von toString vergangen ist.
	 * Format: Seconds:Minutes:Hours
	 */
	public String toString()
	{
		String retStr= "";
		
		long milliSecs= 0L;
		if (stopped) milliSecs= this.millis;
		else milliSecs= System.currentTimeMillis() - this.millis;
		
		this.compTime();
		
		String phours= null;
		String pmins= null;
		String psecs= null;
		String pmillis= this.msecs + "";
		
		if (this.hours < 10L) phours= "0"+ hours;
		else phours= hours +"";
		if (this.mins < 10L) pmins= "0"+ mins;
		else pmins= mins +"";
		if (this.secs < 10L) psecs= "0"+ secs;
		else psecs= secs +"";
		
		retStr= phours + ":" + pmins + ":" + psecs + "." + pmillis;
		//retStr= retStr + "milli secs:\t " +	milliSecs;
		
		return(retStr);
	}
	
	/**
	 * Aufruf des XLinkInjectors.
	 * @param args Argumente für das Arbeiten des XMLPrinter
	 */
	public static void main(String[] args) 
	{
		Timer timer= new Timer();
		timer.start();
		System.out.println(timer);
		for (int i = 0; i < 500000000L; i++);
		timer.stop();
		System.out.println(timer);
		
		System.out.println(Math.floor(1.5));
	}
}
