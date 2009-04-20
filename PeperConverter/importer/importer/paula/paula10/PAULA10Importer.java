package importer.paula.paula10;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import de.corpling.peper.impl.ImporterImpl;
import de.corpling.salt.SaltConcrete.SDocument;

import importer.paula.paula10.analyzer.paulaAnalyzer.PAULAAnalyzer;
import importer.paula.paula10.importer.mapperV1.MapperV1;
import importer.paula.paula10.structureAnalyzer.structureAnalyzer.PAULAStructAnalyzer;

public class PAULA10Importer extends ImporterImpl
{
	Logger logger= Logger.getLogger(PAULA10Importer.class);
	private static final String FILE_PROP= "/paula10/paula10.properties";
	protected Properties props= null;
	private File tmpPath= null;
	private boolean printDot= false;
	private boolean subDir= false;
	private String paSetFile= null;
	private static final String MSG_ERR= "ERROR("+PAULA10Importer.class+"): ";
	
	public PAULA10Importer()
	{
		logger= Logger.getLogger(PAULA10Importer.class);
//		init();
		logger.info("PAULA10Importer is initialized");
	}

	private void init() 
	{
		//this.getClass().getResource(PAULA10Importer.class);
		//Property-Datei erzeugen
		File propFile= new File(this.settingDir+ "/"+FILE_PROP);
		this.props= new Properties();
		try{
			this.props.load(new FileInputStream(propFile));
		}catch (Exception e)
		{throw new RuntimeException(MSG_ERR + "Cannot find input file for properties: "+propFile+"\n nested exception: "+ e.getMessage());}
		
//		//StructAnalyzer:temprorary path auslesen
//		String tmpPathStr= this.props.getProperty("paulaStructAnalyzer.tmp");
//		this.tmpPath= new File(tmpPathStr);
//		if (!tmpPath.exists())
//			throw new NullPointerException(MSG_ERR+ "The given temprorary folder does not exists: "+ this.tmpPath);
//		if (!tmpPath.isDirectory())
//			throw new NullPointerException(MSG_ERR+ "The given temprorary folder is not a directory: "+ this.tmpPath);
		
		//StructAnalyzer: anderen Kram auslesen
		if (this.props.getProperty("paulaStructAnalyzer.dot").equalsIgnoreCase("t"))
			this.printDot= true;
		if (this.props.getProperty("paulaStructAnalyzer.subDir").equalsIgnoreCase("t"))
			this.subDir= true;
		
		//Analyzer: anderen Kram auslesen
		this.paSetFile= this.props.getProperty("paulaAnalyzer.paFile");
	}
	
	private void prepare4Import()
	{		
		//StructAnalyzer:temprorary path auslesen
		String tmpPathStr= "./"+ "_TMP/"+this.inputDir.getName();
		this.tmpPath= new File(tmpPathStr);
		if (!this.tmpPath.exists())
			if (!this.tmpPath.mkdirs())
				throw new NullPointerException(MSG_ERR+ "Can not create following directory: "+ this.tmpPath);
//		if (!tmpPath.exists())
//			throw new NullPointerException(MSG_ERR+ "The given temprorary folder does not exists: "+ this.tmpPath);
//		if (!tmpPath.isDirectory())
//			throw new NullPointerException(MSG_ERR+ "The given temprorary folder is not a directory: "+ this.tmpPath);
		
		
		File tmpCorpusPath= null;
		
		//structure analyzer
		try
		{
			logger.info("analyzing corpus structure...............");
			PAULAStructAnalyzer analyzer= new PAULAStructAnalyzer();
			analyzer.setSettingDir(this.getSettingDir());
			tmpCorpusPath= analyzer.analyze(this.getInputDir(), this.tmpPath, this.printDot, this.subDir);
		}
		catch (Exception e)
		{ e.printStackTrace();
			throw new RuntimeException(MSG_ERR+ "Cannot analyze the given structure: "+this.getInputDir().getAbsolutePath()+"\n reason is: "+ e.getMessage());}
		
		//corpus analyzer
		try
		{
			logger.info("analyzing corpus ..........................");
			PAULAAnalyzer paulaAna= new PAULAAnalyzer();
			paulaAna.setSettingDir(this.getSettingDir());
			paulaAna.loadAnalyzers(this.paSetFile);
			paulaAna.analyze(tmpCorpusPath, this.printDot);
		}
		catch (Exception e)
		{ e.printStackTrace();
		throw new RuntimeException(MSG_ERR+ "Cannot analyze the given corpus: "+this.getInputDir().getAbsolutePath()+"\n reason is: "+ e.getMessage());}
	}
	
	private MapperV1 mapper= null;
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void importCorpusStructure() 
	{
		logger.info("initializing importer..........................");
		this.init();
		logger.info("prparing for importing ..........................");
		this.prepare4Import();
		logger.info("importing corpus structure..........................");
		
		//corpus importer
		try
		{
			logger.info("converting corpus .........................");
			//File corpusPath= new File(tmpCorpusPath.getCanonicalFile()+"/"+ this.getImportDefinition().getImportPath().getName());
			this.mapper= new MapperV1();
			this.mapper.setSaltProject(this.getSaltProject());
			//mapper.map(corpusPath);
//			mapper.setImportReceiver(importReceiver);
			this.mapper.map(this.tmpPath);
			this.mapper.importCorpusStructure();
		}
		catch (Exception e)
		{ e.printStackTrace(); throw new RuntimeException(MSG_ERR+ "Cannot analyze the given corpus: "+this.getInputDir()+"\n reason is: "+ e.getMessage());}
	}
	
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void importDocument(SDocument sDocument) 
	{
		this.mapper.importDocument(sDocument);
	}
	
	public void close()
	{
		if(!deleteDir(this.tmpPath))
			this.logger.warn("Can´t delete the temprorary folder: "+this.tmpPath);

	}
	
	@Override
	protected void finalize() throws Throwable 
	{
	    try {
	        close();        // close open files
	    } finally {
	        super.finalize();
	    }
	}

	
	// Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    private static boolean deleteDir(File dir) 
    {
    	boolean retVal= true;
    	if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children!= null)
            {
	            for (int i=0; i<children.length; i++) {
	                boolean success = deleteDir(new File(dir, children[i]));
	                if (!success) {
	                    return false;
	                }
	            }
            }
        }
    
        // The directory is now empty so delete it
        retVal= dir.delete();
    	return(retVal);
    }

    
//	
//	protected ImportReceiver importReceiver= null;
//	@Override
//	public void setImportReceiver(ImportReceiver importReceiver) 
//	{
//		this.importReceiver= importReceiver;
//	}
//	@Override
//	public ImportReceiver getImportReceiver() 
//	{
//		return(this.importReceiver);
//	}
////-------------- End: import receiver
//	
//
//	@Override
//	public void start() 
//	{
//		if (this.importReceiver== null)
//			throw new NullPointerException(MSG_ERR + "Cannot start importing, because importReceiver is not set.");
//		
//		if (this.importReceiver.getSaltModel()== null)
//			throw new NullPointerException(MSG_ERR+ "Cannot start analyze, because no slat model is given.");
//		
//		File tmpCorpusPath= null;
//		
//		//structure analyzer
//		try
//		{
//			logger.info("analyzing corpus structure...............");
//			PAULAStructAnalyzer analyzer= new PAULAStructAnalyzer();
//			tmpCorpusPath= analyzer.analyze(this.getImportDefinition().getImportPath(), this.tmpPath, this.printDot, this.subDir);
//		}
//		catch (Exception e)
//		{ e.printStackTrace(); throw new RuntimeException(MSG_ERR+ "Cannot analyze the given structure: "+this.getImportDefinition().getImportPath()+"\n reason is: "+ e.getMessage());}
//		
//		//corpus analyzer
//		try
//		{
//			
//			PAULAAnalyzer paulaAna= new PAULAAnalyzer();
//			paulaAna.loadAnalyzers(this.paSetFile);
//			paulaAna.analyze(tmpCorpusPath, this.printDot);
//		}
//		catch (Exception e)
//		{ e.printStackTrace(); throw new RuntimeException(MSG_ERR+ "Cannot analyze the given corpus: "+this.getImportDefinition().getImportPath()+"\n reason is: "+ e.getMessage());}
//		
//		//corpus importer
//		try
//		{
//			logger.info("converting corpus .........................");
//			logger.info("analyzing corpus ..........................");
//			//File corpusPath= new File(tmpCorpusPath.getCanonicalFile()+"/"+ this.getImportDefinition().getImportPath().getName());
//			MapperV1 mapper= new MapperV1();
//			//mapper.map(corpusPath);
//			mapper.setImportReceiver(importReceiver);
//			mapper.map(this.tmpPath);
//		}
//		catch (Exception e)
//		{ e.printStackTrace(); throw new RuntimeException(MSG_ERR+ "Cannot analyze the given corpus: "+this.getImportDefinition().getImportPath()+"\n reason is: "+ e.getMessage());}
//	}
//	
//	public static void main(String[] args) throws Exception
//	{
//		System.out.println("************************** start of Test for PAULA10Importer **************************");
//		
//		//Variablen fï¿½r Parameter
//		String srcPathStr= "";				//Name des Korpusverzeichnis
//		
//		//Eingabeparameter prï¿½fen
//		for (int i=0; i < args.length; i++)
//		{
//			// zu analysierendes Verzeichniss als Verzeichniss
//			if (args[i].equalsIgnoreCase("-s"))
//			{
//				srcPathStr= args[i+1];
//				i++;
//			}			
//		}
//		//System.out.println("Canonical name:"+PAULA10Importer.class.getCanonicalName());
//		//PAULA10Importer importer= (PAULA10Importer) Class.forName("paula10.PAULA10Importer").newInstance();
//		PAULA10Importer importer= new PAULA10Importer();
//		PropertyConfigurator.configureAndWatch("importer/paula10/settings/log4j.properties", 60*1000 );
//		
//		//importer.setSource(new File(srcPathStr));
//		//importer.setSaltModel(new SaltFactoryImpl().createSaltModel());
//		importer.start();
//		
//		System.out.println("************************** end of Test for PAULA10Importer **************************");
//	}
//
//	@Override
//	public String getName() 
//	{
//		return("PAULA10Importer");
//	}
//
//	//----- Start: exportDefinition
//	protected ImportDefinition importDef= null;
//	@Override
//	public ImportDefinition getImportDefinition() 
//	{
//		return(this.importDef);
//	}
//
//	@Override
//	public void setImportDefinition(ImportDefinition importDefinition) 
//	{
//		if (importDefinition== null)
//			throw new NullPointerException(MSG_ERR + "The given importDefinition is empty.");
//		this.importDef= importDefinition;
//	}
////----- End: exportDefinition
//
//	@Override
//	public String getDescription() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Collection<String> getFormats() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Collection<String> getVersions() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setDescription(String value) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setFormats(Collection<String> formats) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setName(String value) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setVersions(Collection<String> value) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setFormats(String value) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setVersions(String value) {
//		// TODO Auto-generated method stub
//		
//	}
}
