package annisservice.importer.preImporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import annisservice.extFiles.ExternalFileMgr;
import annisservice.extFiles.ExternalFileMgrImpl;
import annisservice.extFiles.exchangeObj.ExtFileObjectCom;
import annisservice.extFiles.exchangeObj.ExtFileObjectImpl;

/**
 * The ExtFileExtractor scans a given directory and searches relANNIS-files (tab-files) 
 * for entries which classify a link to an external file.
 * The external file will be send to the ExternalFileMgr and the returned id will be 
 * stored in the relANNIS-file. Therefore it is necessary to rewrite the relANNIS-files.  
 * 
 * 
 * @author Florian Zipser
 * @version 1.0
 *
 */
public class ExtFileExtractor 
{
	Properties props= null;
	
	private static final String TOOLNAME= "ExtFileExtractor";
	
	private static final String KW_ISTMP= 		"isTMP";
	private static final String KW_TMPPATH= 	"tmpPath";
	private static final String KW_TMPFOLDER=	"tmpFolder";
	private static final String KW_TABSEP= 		"tabSep";
	private static final String KW_EXTDIR=		"extDir";
		
	// ----------------------- Fehlermeldungen -----------------------
	private static final String MSG_ERR=				"ERROR(" +TOOLNAME+ "):\t";
	private static final String ERR_NO_PROPS=			MSG_ERR + "The given properties are empty.";
	private static final String ERR_DELETE=				MSG_ERR + "Cannot replace old file with new file, because I cannot delete the old file: ";
	private static final String ERR_RENAME=				MSG_ERR + "Cannot rename the given file: ";
	
	/**
	 * temprorary path which will be deleted afeter extracting
	 */
	private File tmpPath= null;
	
	private boolean isTmp= true;
	
	/**
	 * Creates a new ExtFileExtractor object.
	 * @param props Properties - the properties with which the ExtFileExtractor works
	 */
	public ExtFileExtractor(Properties props) throws NullPointerException
	{
		if (props== null) throw new NullPointerException(ERR_NO_PROPS);
		
		this.tmpPath= new File(props.getProperty(KW_TMPPATH)+ "/" + props.getProperty(KW_TMPFOLDER)+ "/");
		this.tmpPath.mkdir();
		if (props.getProperty(KW_ISTMP).equalsIgnoreCase("false")) 
			this.isTmp= false;
		else this.isTmp= true;
		
		this.props= props;
	}
	
	/**
	 * Sets a new temprorary path, for example to the working directory.
	 * @param tmpPath File - Reference to the temprorary path
	 */
	public void setTMPPath(File tmpPath)
	{
		String tmpFolder= props.getProperty(KW_TMPFOLDER);
		File newTMPPath= new File(tmpPath.getAbsoluteFile() + "/"+ tmpFolder+ "/");
		if (!newTMPPath.exists())
			newTMPPath.mkdir();
		this.tmpPath= newTMPPath; 
	}
	
	/**
	 * Returns the current path which is used for storing temprorary files.
	 * @return the tmprorary path
	 */
	public File getTMPPath()
	{ return(this.tmpPath); }
	
	/**
	 * Extracts one file. Maybe problems with UTF-8 Encoding 
	 * @param file
	 */
	public void extract(File file) throws IOException
	{
		String newFileName= this.tmpPath+"/" + file.getName();
		File newFile= new File(newFileName);
		FileReader fRead= new FileReader(file);
		//fRead.e
		BufferedReader fis = new BufferedReader(fRead);
		
		//!!!UTF-8 Problem !!!
		PrintStream fos= new PrintStream(newFile);//, "UTF-8");
		String line= fis.readLine();
		while (line != null) 
		{
			String newLine= this.extractLines(line, file.getParent());
			fos.println(newLine);
			line = fis.readLine();
		}
		fis.close();
		fos.close();
		
		//deleting old file and copying new file
		if (!file.delete())
			throw new IOException(ERR_DELETE+ file.getCanonicalPath());
		if(!newFile.renameTo(file)) 
			throw new IOException(ERR_RENAME+newFile.getName());
	}
	
	
	/**
	 * Checks every given string, if it contains an external file reference and returns
	 * this line with a referncial id made by ExtFileMgr.
	 * @param line String - the line in which the reference might be
	 * @param workDir - the working directory
	 */
	// XXX: Austausch der [ExtFile]-Werte
	private String extractLines(String line, String workDir)
	{
		String retStr= "";
		String sep= this.props.getProperty(KW_TABSEP);
		//line wird anhand des tabSeperators getrennt
		String[] atts= line.split(sep);
		
		String extPattern=	"(\\[ExtFile\\])";
		String strPattern= extPattern+".+";
		Pattern p = Pattern.compile(strPattern);
		//einzelne Attribute durchgehen
		for (int i= 0; i< atts.length; i++)
		{
			Matcher m = p.matcher(atts[i]);
			if (m.matches())
			{
				String[] parts= atts[i].replaceAll(extPattern, "").split("/");
				String branches= "";
				String fileName= "";
				for (int j= 0; j < parts.length-1;j++)
				{
					if (j== 0) branches= branches+ parts[j];
					else branches= branches+ "/"+ parts[j];
				}
				fileName= parts[parts.length-1];
				File extFile= new File(workDir + "/"+ this.props.getProperty(KW_EXTDIR) +"/"+branches+"/"+ fileName);
				if (i== 0) retStr= this.putFile(branches, extFile).toString();
				else retStr= retStr + "\t" + this.putFile(branches, extFile).toString();
			}
			else 
			{
				if (i== 0) retStr= atts[i];
				else retStr= retStr +"\t" + atts[i];
			}
		}
		
		return(retStr);
	}
	
	/**
	 * Puts a file into the ExternalFileMgr and returns its returned id.
	 * @param branch String -name of the branch in which the file will be stored
	 * @param file File - the file which should be stored
	 * @return
	 */
	private Long putFile(String branch, File file) throws RuntimeException
	{
		Long retId= null;
		try
		{
			//ExternalFileMgr extFileMgr= ExternalFileMgrImpl.getMgr(new File("e:/UniJob/eclipse/dddquery/settings/settings_ExtFileMgr_test.xml"), null);
			ExternalFileMgr extFileMgr= new ExternalFileMgrImpl();
			//branch erstellen
			if (!extFileMgr.hasBranch(branch))
				extFileMgr.createBranch(branch);
			
			//Datei einf�gen
			ExtFileObjectCom extFileObj= new ExtFileObjectImpl();
			extFileObj.setFile(file);
			extFileObj.setBranch(branch);
			extFileObj.setMime(file.toURI().toURL().openConnection().getContentType());
			retId= extFileMgr.putFile(extFileObj);
		}
		catch (Exception e)
		{ throw new RuntimeException(e.getMessage()); }
		return(retId);
	}
	
	/**
	 * Removes all external things created during running. This method is also
	 * called by finalize().
	 */
	public void kill()
	{
		//das tempor�re Verzeichniss samt Inhalt l�schen
		if (this.isTmp)
		{
			for (File file: this.tmpPath.listFiles())
			{
				file.delete();
			}
			this.tmpPath.delete();
		}
	}
	
	public void finalize()
	{
		this.kill();
	}
}
