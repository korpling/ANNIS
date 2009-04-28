/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.impl;

import de.corpling.peper.ConvertJob;
import de.corpling.peper.ExportObject;
import de.corpling.peper.ExportSet;
import de.corpling.peper.FormatDefinition;
import de.corpling.peper.ImportObject;
import de.corpling.peper.ImportSet;
import de.corpling.peper.PeperConverter;
import de.corpling.peper.PeperFactory;
import de.corpling.peper.PeperPackage;
import de.corpling.peper.PorterEmitter;
import de.util.timer.Timer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Converter</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.impl.PeperConverterImpl#getConvertJobs <em>Convert Jobs</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PeperConverterImpl extends EObjectImpl implements PeperConverter 
{
	private static final String MSG_ERR= "Error("+PeperConverterImpl.class+")";
	private static final String KW_IMEXPORT_SET_SEPERATOR=	"::";
	private static final String IMPORT_SET= "sourcedirectory"+KW_IMEXPORT_SET_SEPERATOR+"formatname"+KW_IMEXPORT_SET_SEPERATOR+"formatversion";
	private static final String EXPORT_SET= "sourcedirectory"+KW_IMEXPORT_SET_SEPERATOR+"formatname"+KW_IMEXPORT_SET_SEPERATOR+"formatversion";
	
	
	private Logger logger= Logger.getLogger(PeperConverterImpl.class);
	/**
	 * The cached value of the '{@link #getConvertJobs() <em>Convert Jobs</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConvertJobs()
	 * @generated
	 * @ordered
	 */
	protected EList<ConvertJob> convertJobs;
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	protected PeperConverterImpl() {
		super();
		this.init();
	}
	
	/**
	 * Initializes this object
	 */
	private void init()
	{
		
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PeperPackage.Literals.PEPER_CONVERTER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ConvertJob> getConvertJobs() {
		if (convertJobs == null) {
			convertJobs = new EObjectContainmentEList<ConvertJob>(ConvertJob.class, this, PeperPackage.PEPER_CONVERTER__CONVERT_JOBS);
		}
		return convertJobs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void start() 
	{
		try
		{
			int i= 1;
			for (ConvertJob job: this.getConvertJobs())
			{
				this.logger.info("===== Starting with job("+i+") ==============================");
				this.logger.info("settings:");
				//print info about all importing sets
				for (ImportObject importObj: job.getImportObjects())
				{
					this.logger.info("import set:");
					this.logger.info("\tsource:\t"+importObj.getImportSet().getDataSourcePath().getCanonicalPath());
					this.logger.info("\tformat:\t"+importObj.getImportSet().getFormatDefinition().getFormatName());
					this.logger.info("\tversion:\t"+importObj.getImportSet().getFormatDefinition().getFormatVersion());
				}	
				//print info about all importing sets
				for (ExportObject exportObj: job.getExportObjects())
				{
					this.logger.info("export set:");
					this.logger.info("\ttarget:\t"+exportObj.getExportSet().getDataSourcePath().getCanonicalPath());
					this.logger.info("\tformat:\t"+exportObj.getExportSet().getFormatDefinition().getFormatName());
					this.logger.info("\tversion:\t"+exportObj.getExportSet().getFormatDefinition().getFormatVersion());
				}	
				this.logger.info("-----------------------------------------------------------");
				this.logger.info("converting:");
				job.start();
				
				this.logger.info("=====Ending with job("+i+") ================================");
				i++;
			}
		}
		catch (Exception e) 
		{
			throw new NullPointerException(e.getMessage());
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void addJob(String importDescription, String exportDescription) 
	{
		ConvertJob job= PeperFactory.eINSTANCE.createConvertJob();
		//setting emitter to Convert job
		PorterEmitter pEmitter= PeperFactory.eINSTANCE.createPorterEmitter();
		job.setPorterEmitter(pEmitter);
		Properties props= new Properties();
		File propFile= new File("./settings/importer_exporter.properties");
		
		try {
			props.load(new FileInputStream(propFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		pEmitter.setProps(props);
		
		EList<ImportSet> importSets= new BasicEList<ImportSet>();
		String[] importParamSets= importDescription.split(";");
		for (String importParamSet: importParamSets)
		{	
			//dealing with Import sets
			String[] parts;
			parts= importParamSet.split(KW_IMEXPORT_SET_SEPERATOR);
			if (parts.length!= 3)
				throw new NullPointerException(MSG_ERR + "There are to less parameter. IMPORT_SET has to look like this: "+ IMPORT_SET);
			else if (	(parts[0].equalsIgnoreCase("")) ||
						(parts[1].equalsIgnoreCase(""))||
						(parts[2].equalsIgnoreCase("")))
				throw new NullPointerException(MSG_ERR + "There are empty parameters. IMPORT_SET has to look like this: "+ IMPORT_SET);
			else
			{
				FormatDefinition importFormatDef= PeperFactory.eINSTANCE.createFormatDefinition();
				importFormatDef.setFormatName(parts[1]);
				importFormatDef.setFormatVersion(parts[2]); 
				
				ImportSet importSet= PeperFactory.eINSTANCE.createImportSet();
				importSet.setFormatDefinition(importFormatDef);
				importSet.setDataSourcePath(new File(parts[0]));
				importSets.add(importSet);
			}
		}
		
		EList<ExportSet> exportSets= new BasicEList<ExportSet>();
		String[] exportParamSets= exportDescription.split(";");
		for (String exportParamSet: exportParamSets)
		{	
			//dealing with export sets
			String[] parts;
			parts= exportParamSet.split(KW_IMEXPORT_SET_SEPERATOR);
			if (parts.length!= 3)
				throw new NullPointerException(MSG_ERR + "There are to less parameter. EXPORT_SET has to look like this: "+ EXPORT_SET);
			else if (	(parts[0].equalsIgnoreCase("")) ||
						(parts[1].equalsIgnoreCase(""))||
						(parts[2].equalsIgnoreCase("")))
				throw new NullPointerException(MSG_ERR + "There are empty parameters. EXPORT_SET has to look like this: "+ EXPORT_SET);
			else
			{
				FormatDefinition exportFormatDef= PeperFactory.eINSTANCE.createFormatDefinition();
				exportFormatDef.setFormatName(parts[1]);
				exportFormatDef.setFormatVersion(parts[2]); 
				
				ExportSet exportSet= PeperFactory.eINSTANCE.createExportSet();
				exportSet.setFormatDefinition(exportFormatDef);
				File exportDir= new File(parts[0]);
				if (!exportDir.exists())
				{	
					if (!exportDir.mkdirs())
						try {
							throw new NullPointerException(MSG_ERR + "Cannot create the export directory: "+exportDir.getCanonicalPath());
						} catch (IOException e) {
							throw new NullPointerException(MSG_ERR + e);
						}
				}	
				exportSet.setDataSourcePath(exportDir);
				exportSets.add(exportSet);
			}
			//add import sets to job
			for (ImportSet importSet: importSets)
				job.addImportSet(importSet);
			//add import sets to job
			for (ExportSet exportSet: exportSets)
				job.addExportSet(exportSet);
		}
		//adding jobs to peperConverter
//		for (ConvertJob job: jobs)
		this.getConvertJobs().add(job);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case PeperPackage.PEPER_CONVERTER__CONVERT_JOBS:
				return ((InternalEList<?>)getConvertJobs()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PeperPackage.PEPER_CONVERTER__CONVERT_JOBS:
				return getConvertJobs();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case PeperPackage.PEPER_CONVERTER__CONVERT_JOBS:
				getConvertJobs().clear();
				getConvertJobs().addAll((Collection<? extends ConvertJob>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case PeperPackage.PEPER_CONVERTER__CONVERT_JOBS:
				getConvertJobs().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case PeperPackage.PEPER_CONVERTER__CONVERT_JOBS:
				return convertJobs != null && !convertJobs.isEmpty();
		}
		return super.eIsSet(featureID);
	}

//====================================== main methode ======================================
	@SuppressWarnings("unused")
	private static Logger staticLogger= null;
	
	public static String getSynopsis()
	{
		String retStr= null;
		retStr= retStr + "Synopsis:\tPeperConverterImpl -s IMPORT_SETS -t EXPORT_SETS \n";
		retStr= retStr + "\n";
		retStr= retStr + "\t\tIMPORT_SETS\t\t(IMPORT_SET;)*\n";
		retStr= retStr + "\t\tIMPORT_SET\t\t"+IMPORT_SET+"\n";
		retStr= retStr + "\t\tEXPORT_SETS\t\t(EXPORT_SET;)*\n";
		retStr= retStr + "\t\tEXPORT_SET\t\t"+EXPORT_SET+"\n";
		return(retStr);
	}
	public static void main(String args[])
	{
		PropertyConfigurator.configureAndWatch("settings/peperConverter_log4j.properties", 60*1000 );
		Logger staticLogger= Logger.getLogger(PeperConverterImpl.class);
		staticLogger.info("************************************************************************");
		staticLogger.info("***                         Peper Converter                          ***");
		staticLogger.info("************************************************************************");
		staticLogger.info("* Peper converter is a salt model based converter for a lot of         *");
		staticLogger.info("* linguistical formats.                                                *");
		staticLogger.info("* for contact write an eMail to: zipser@informatik.hu-berlin.de        *");
		staticLogger.info("************************************************************************");
		staticLogger.info("\n");
	
		Timer timer= new Timer();
		timer.start();
		//Variablen fï¿½r Parameter
		String importStrs= null;				//Name des Import-Korpusverzeichnis
		String exportStrs= null;				//Name des Export-Korpusverzeichnis
		
		//Eingabeparameter prï¿½fen
		for (int i=0; i < args.length; i++)
		{
			// zu analysierendes Verzeichniss als Verzeichniss
			if (args[i].equalsIgnoreCase("-s"))
			{
				if (args.length> i+1)
					importStrs= args[i+1];
				i++;
			}	
			
			// zu analysierendes Verzeichniss als Verzeichniss
			else if (args[i].equalsIgnoreCase("-t"))
			{
				if (args.length> i+1)
					exportStrs= args[i+1];
				i++;
			}	
		}
		//marks if parameter for program call are ok
		PeperConverter peperConverter= PeperFactory.eINSTANCE.createPeperConverter();
		boolean paramsOk= false;
		try 
		{
			if ((importStrs== null) || (importStrs.equalsIgnoreCase("")))
				throw new NullPointerException("No arguments for importers are given.");
			else if ((exportStrs== null) || (exportStrs.equalsIgnoreCase("")))
				throw new NullPointerException("No arguments for exporters are given.");	
			//program call
			else
			{
				//TODO has to be fixed, you can only take one job, for more it doesn´t work yet
				peperConverter.addJob(importStrs, exportStrs);
				paramsOk= true;
			}
			 
		} catch (Exception e) 
		{
			staticLogger.error("Error in program call:");
			staticLogger.error("\t"+e);
			staticLogger.error("Please call:");
			staticLogger.error(getSynopsis());
		}
		//starts converting
		if (paramsOk)
		{
			peperConverter.start();
			staticLogger.info("CONVERSION ENDED SUCCESSFULL, REQUIRED TIME: "+timer.toString());
		}
		else 
			staticLogger.info("CONVERSION ENDED WITH ERRORS, REQUIRED TIME: "+timer.toString());
		staticLogger.info("************************************************************************");
	}
} //PeperConverterImpl
