package annis.resolver;

import java.io.File;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class AnnisResolverServer extends UnicastRemoteObject implements AnnisResolverService {
	private static final long serialVersionUID = -7429860809599810639L;

//	private Properties propsDb;
	//private Connection dbConn;
	
	public AnnisResolverServer (File propertiesFile) throws RemoteException {
        super();
//        propsDb = new Properties();
        /*
		try {
			propsDb.load(new FileInputStream(propertiesFile));
			
			Class.forName(propsDb.getProperty("dbDriver")).newInstance(); 
//			dbConn = DriverManager.getConnection(propsDb.getProperty("dbURL"), propsDb.getProperty("dbUser"), propsDb.getProperty("dbPassword") );
			
			//TODO Here is where initialization routines might wanna go...
			
		} catch (FileNotFoundException e) {
			throw new RemoteException(e.getMessage());
		} catch (IOException e) {
			throw new RemoteException(e.getMessage());
//		} catch (SQLException e) {
//			throw new RemoteException(e.getMessage());
		} catch (ClassNotFoundException e) {
			throw new RemoteException("Class Not Found: " + e.getMessage());
		} catch (InstantiationException e) {
			throw new RemoteException(e.getMessage());
		} catch (IllegalAccessException e) {
			throw new RemoteException(e.getMessage());
		}	
		*/	
    }
	
	public static void main(String args[]) throws Exception {
        //Start RMIRegistry
    	try {
    		Registry registry = LocateRegistry.createRegistry(4712);
    		if (System.getSecurityManager() == null)
                System.setSecurityManager ( new RMISecurityManager() );
    		 AnnisResolverServer svr = new AnnisResolverServer(new File("db.properties"));
    		 registry.bind ("AnnisResolverService", svr);
    		 try {
    			 if("-d".equals(args[0])) {
    				 //Run as Daemon
    				 System.out.close();
    				 System.err.close();
    			 }
    		 } catch (ArrayIndexOutOfBoundsException e) {
    			 System.out.println ("AnnisResolverServer is now waiting for requests...\n");
    		 }
    	} catch(Exception e) {
    		e.printStackTrace();
    	}        
    }
	
	public void ping() throws RemoteException {
		// TODO Auto-generated method stub
	}
	
	/**
	 * This method returns the name of the tool which should visualize the
	 * computed Viz-type for the given annotation level and corpus.
	 * @param corpusID Long - unique identifier for a corpus
	 * @param annoLevel String - Name of the annotation level
	 * @return name of tool for visualization
	 * @throws Exception
	 */
	public String getVizualizationTool(	Long corpusID, 
										String annoLevel) throws RemoteException
	{
		ANNISResolver annisResolver= new ANNISResolver();
		try
		{
			return(annisResolver.getVizualizationTool(corpusID, annoLevel));
		}
		catch (Exception e)
		{  throw new RemoteException(e.getMessage());}
	}
	
	/**
	 * This method returns a visualization type. The visualization type is taken
	 * from data base and is identified by unique identifier for corpus (corpus_ID) 
	 * and annotation level (annoLevel).
	 * @param corpusID Long - unique identifier for a corpus
	 * @param annoLevel String - Name of the annotation level
	 * @return visualization type
	 * @throws Exception
	 */
	public VisualizationType getVizualizationType(	Long corpusId, 
													String annoLevel) throws RemoteException 
	{
		ANNISResolver annisResolver= new ANNISResolver();
		try
		{
			return(annisResolver.getVizualizationType(corpusId, annoLevel));
		}
		catch (Exception e)
		{  throw new RemoteException(e.getMessage());}
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
		ANNISResolver annisResolver= new ANNISResolver();
		try
		{
			return(annisResolver.getVizualizationType(corpusId, annoLevel, annotation));
		}
		catch (Exception e)
		{  throw new RemoteException(e.getMessage());}
	}
}