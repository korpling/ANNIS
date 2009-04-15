package annisservice.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import annisservice.importer.preImporter.ExtFileExtractor;

/**
 * In later versions the RelANNISImporter will manage the full import of relANNIS-data
 * into the relANNIS-Db, so that the ANNIS 2.0 system can work with them.
 * <br/>
 * <br/>
 * Note: For now the RelANNISImporter just extract external files and stores them with
 * help of ExternalFileMgr. 
 * 
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class RelANNISImporter 
{
	private Logger logger = Logger.getLogger(RelANNISImporter.class);
	private Properties props= null;
	
	private static final String FILE_LOG= "settings/raImporter_log4j.xml";//"settings/raImporter_log4j.xml"; 
	private static final String FILE_SET= "settings/raImporter_settings_local.xml";
	
	private static final String FLAG_HELP= 	"-h";	//Flag for help
	private static final String FLAG_DIR= 	"-d";	//Flag for directory
	
	
	private static final String TOOLNAME= "RelANNISImporter";
	// ----------------------- Fehlermeldungen -----------------------
	private static final String MSG_ERR=				"ERROR(" +TOOLNAME+ "):\t";
	private static final String ERR_EMPTY_FILE=			MSG_ERR + "The given directoy is empty.";
	private static final String ERR_FILE_NOT_FOUND=		MSG_ERR + "The given directoy does not exists: ";
	private static final String ERR_FILE_NO_DIR=		MSG_ERR + "The given file object is not a directoy: ";
	
	/**
	 * Creates a new RelANNISImporter- object with own logger.
	 */
	public RelANNISImporter(Properties props) throws FileNotFoundException, InvalidPropertiesFormatException, IOException
	{
		this(Logger.getLogger(RelANNISImporter.class), props);
	}
	
	/**
	 * Creates a new RelANNISImporter- object with given logger.
	 * @param logger Logger - given logger
	 */
	public RelANNISImporter(Logger logger, Properties props) 
	{
		this.logger= logger;
		this.props= props;
	}
	
	/**
	 * Starts with importing the given directory
	 * @param dir File - the directory which has to be imported 
	 */
	public void start(File dir) throws FileNotFoundException, IOException
	{
		if (dir== null) throw new FileNotFoundException(ERR_EMPTY_FILE);
		if (!dir.exists()) throw new FileNotFoundException(ERR_FILE_NOT_FOUND + dir.getAbsolutePath());
		if (!dir.isDirectory()) throw new FileNotFoundException(ERR_FILE_NO_DIR + dir.getAbsolutePath());
		
		//log-Ausgabe
		if (this.logger!= null) this.logger.info("importing directory: "+dir.getAbsolutePath());
		
		
		//start ExtFileExtraction
		ExtFileExtractor extractor= new ExtFileExtractor(this.props);
		extractor.setTMPPath(dir);
		for(File file : dir.listFiles())
		{
		if (file.isFile())
				extractExtFile(extractor, file);
		}
		extractor.kill();
		//End ExtFileExtraction
	}
	
	/**
	 * Extracts the reference to an external file and replaces it with returned id.
	 * @param file File - the file which shoul be extracted
	 * @param extractor ExtFileExtractor - the extractor with which the file should be extracted
	 */
	private void extractExtFile(ExtFileExtractor extractor, File file) throws IOException
	{
		String[] parts= file.getName().split("[.]");
		if (parts[parts.length-1].equalsIgnoreCase("tab"))
		{
			//log-Ausgabe
			if (this.logger!= null) this.logger.info("extracting file: "+file.getAbsolutePath());
			extractor.extract(file);
		}
		
	}
	
	public static String printInfo()
	{
		String retStr= "";
		retStr= "The RelANNISImporter will import a given directory to ANNIS 2.0 database" +
				"Note: At actual time importing means just extracting external files, store them" +
				"and put an reference to the relANNIS-files in the given directory.\n" +
				"Synopsis:\n"+
				"'-d' followed by the directory which shoul be imported\n"+
				"'-h' for help ";
		return(retStr);
	}
	
	/**
	 * Entrypoint for RelANNISImporter from outside. The given directory will be 
	 * imported. 
	 * Note: At actual time importing means just extracting external files, store them
	 * and put an reference to the relANNIS-files in the given directory. 
	 * 
	 * @param args - 	"-d" followed by the directory which shoul be imported
	 * 					"-h" for help 
	 */
	public static void main(String[] args)
	{
		//logger initialisieren
		Logger logger = Logger.getLogger(RelANNISImporter.class);
		DOMConfigurator.configure(FILE_LOG);
		
		logger.info("**********************  relANNISImport  **********************");
		
		boolean help= false;
		String srcDir= null;
		
		if (args.length< 1) help= true;
		//Eingabeparameter prüfen
		for (int i=0; i < args.length; i++)
		{
			// Hilfe ausgeben
			if (args[i].equalsIgnoreCase(FLAG_HELP)) help= true;
			// zu analysierendes Verzeichniss als Verzeichniss
			else if (args[i].equalsIgnoreCase(FLAG_DIR))
			{
				if (args.length < i+2) help= true;
				else
				{
					srcDir= args[i+1];
					i++;
				}
			}
		}
		
		try
		{
			if (help)
				logger.info(RelANNISImporter.printInfo());
			else
			{
				Properties props= new Properties();
				props.loadFromXML(new FileInputStream(FILE_SET));
				RelANNISImporter importer= new RelANNISImporter(logger, props);
				importer.start(new File(srcDir));
				
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.fatal(e.getMessage());
		}
		logger.info("**********************  relANNISImport  **********************");
	}
}
