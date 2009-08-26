package annis.resolver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;


/**
 * This class offers objects for getting a visualization type for
 * a special corpus and a special annotation level. A second function
 * is getting a visualization type for a special corpus, a special level 
 * and a special annotation 
 * @author Florian Zipser
 * @version 1.0
 */
public class ANNISResolver implements AnnisResolverService
{
	private Logger log = Logger.getLogger(this.getClass());
	
	public ANNISResolver() {
		log.info("AnnisResolverService loaded.");
	}
	
	
	/**
	 * Inserts a new visualization type value into database. The methode
	 * checks if the given value already exists.
	 * @param vizType String - the new value which should be inserted.
	 * @exception throws an error if the given value already exists
	 */
	public void newVizType(String vizType) throws Exception
	{
		//Daten-Access-Objekt erzeugen
		AnnisVizDAO dao= new AnnisVizDAO(null);
		//Datenzugriff �ffnen
		dao.open();
		//Visualisierungstyp holen
		dao.insertVizType(vizType);
		dao.close();
	}
	
	/**
	 * Returns a visualization type for a special corpus and a special annotation level.
	 * @param corpusId String - a global unique id for the special corpus
	 * @param annoLevel String - a special annotation level 
	 * @return the visualization type
	 * @throws RemoteException 
	 * @throws RemoteException if there is no annotation type for corpusId and and annoLevel 
	 */
	public VisualizationType getVizualizationType(	Long corpusId, 
													String annoLevel) throws RemoteException 
	{
		VisualizationType vizType= VisualizationType.NONE; 
		try
		{
			//Daten-Access-Objekt erzeugen
			AnnisVizDAO dao= new AnnisVizDAO(null);
			//Datenzugriff �ffnen
			dao.open();
			//Visualisierungstyp holen
			String vizTypeStr= dao.getVizType(corpusId, annoLevel);
			try
			{
				//Visualisierungstyp auf enum transformieren
				vizType= VisualizationType.valueOf(vizTypeStr);
		}
		//wenn es keinen enum Typen gibt
		catch (Exception e)
			{vizType= VisualizationType.PARTITURE;}
		//Datenzugriff schlie�en
		dao.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
		return(vizType);
	}
	
	/**
	 * Returns a visualization type for a special corpus and a special annotation level.
	 * If the annotation is null or empty, the other method will be called.
	 * @param corpusId String - a global unique id for the special corpus
	 * @param annoLevel String - a special annotation level 
	 * @return the visualization type
	 * @throws RemoteException if there is no annotation type for corpusId and and annoLevel 
	 */
	public VisualizationType getVizualizationType(	Long corpusId, 
													String annoLevel,
													String annotation) throws RemoteException 
	{
		//wenn keine Annotation gegeben wurde andere Methode aufrufen
		if ((annotation== null) || (annotation.equalsIgnoreCase("")))
				return(this.getVizualizationType(corpusId, annoLevel));
		else 
		{
			VisualizationType vizType= VisualizationType.NONE; 
			try
			{
			//Daten-Access-Objekt erzeugen
			AnnisVizDAO dao= new AnnisVizDAO(null);
			//Datenzugriff �ffnen
			dao.open();
			//Visualisierungstyp holen
			String vizTypeStr= dao.getVizType(corpusId, annoLevel, annotation);
			try
			{
			//Visualisierungstyp auf enum transformieren
			vizType= VisualizationType.valueOf(vizTypeStr);
			}
			//wenn es keinen enum Typen gibt
			catch (Exception e)
			{vizType= VisualizationType.PARTITURE;}
			//Datenzugriff schlie�en
			dao.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new RemoteException(e.getMessage());
			}
			return(vizType);
		}
	}
	
	/**
	 * This methode returns the name of the tool wich should visualize the
	 * computed Viz-type for the given annotation level and corpus.
	 * @param corpusID Long - unique identifier for a corpus
	 * @param annoLevel String - Name of the annotation level
	 * @return name of tool for visualization
	 * @throws Exception
	 */
	public String getVizualizationTool(	Long corpusID, 
										String annoLevel) throws RemoteException
	{
		VisualizationType vizType= null;
		vizType= this.getVizualizationType(corpusID, annoLevel);
		String toolName= null;
		
		//Ermitteln des Toolnamens zu dem gesuchten VizType
		if (vizType.equals(VisualizationType.TREE))
				toolName= "annis.frontend.servlets.visualizers.TreeVisualizer";
		else if (vizType.equals(VisualizationType.PARTITURE))
			toolName= "annis.frontend.servlets.visualizers.PartiturVisualizer";
		else if (vizType.equals(VisualizationType.MMAX))
			toolName= "annis.frontend.servlets.visualizers.MmaxVisualizer";
		return(toolName);
	}
	
	public static void main(String[] args) 
	{
		System.out.println("========================== Test for getting vizualization type ==========================");
		try
		{
			BufferedReader in = null;
			System.out.print("please enter the corpus id:\t");
			in = new BufferedReader(new InputStreamReader(System.in));
			Long corp_id = new Long(in.readLine());
			System.out.print("please enter the annotation level:\t");
			in = new BufferedReader(new InputStreamReader(System.in));
			String annoLevel= in.readLine();
			System.out.print("please enter the annotation:\t");
			in = new BufferedReader(new InputStreamReader(System.in));
			String annotation= in.readLine();
			System.out.println("given values:");
			System.out.println("corpus id: "+ corp_id + "\t annotation level: "+ annoLevel + "\t annotation: "+ annotation);
			
			ANNISResolver annisResolver= new ANNISResolver();
			System.out.println("visualization type: "+ annisResolver.getVizualizationType(corp_id, annoLevel, annotation) + "\t vizTOOL: "+ annisResolver.getVizualizationTool(corp_id, annoLevel));
		}
		catch (Exception e)
		{
			System.out.println("ERROR in program :");
			e.printStackTrace();
		}
		System.out.println("========================== End Test for getting vizualization type ==========================");
	}

	public void ping() throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}
