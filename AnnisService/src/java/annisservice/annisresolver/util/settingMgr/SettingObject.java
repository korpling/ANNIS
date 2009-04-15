package annisservice.annisresolver.util.settingMgr;

import java.util.Hashtable;
import java.util.Vector;

import org.xml.sax.ext.DefaultHandler2;

/**
 * Klassen die dieses Interface implementieren müssen über einen DefaultHandler2 verfügen,
 * der passende Settings aus der Settingdatei verarbeiten kann.
 * @author Administrator
 *
 */
public interface SettingObject 
{
	//zu implementierende Methoden
	/**
	 * Die Methode getSetEntry() gibt eine Tabelle mit DefaultHandler2 (SettingListener) - Objekten zurück. 
	 * Jedem dieser DefaultHandler ist eine Liste mit SettingNamen zugeordnet, auf die Listener
	 * hören. Trifft der Parser in der XML-Setting-Datei auf den entsprechenden Settingnamen,
	 * so werden die SAX-Methoden an den dazugehörigen Settinglistener weitergeleitet.	
	 * @return Hashtable<DefaultHandler2, Vector<String>> - Zuordnungstabelle von Settinglistener zu ihren entsprechenden SettindNamen
	 */
	public Hashtable<DefaultHandler2, Vector<String>> getSetEntry() throws Exception;
	
	/**
	 * Die Methode readSettings() wird durch den SettingMgr aufgerufen, wenn die Setting-Datei
	 * eingelesen wurde und alle entsprechenden Aufrufe an den SettingListener weitergeleitet wurden.
	 * @throws Exception
	 */
	public void readSettings() throws Exception;
}
