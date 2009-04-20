/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.impl;

import de.corpling.peper.ConvertJob;
import de.corpling.peper.ExportObject;
import de.corpling.peper.ExportObjects;
import de.corpling.peper.ExportSet;
import de.corpling.peper.Exporter;
import de.corpling.peper.ImportObject;
import de.corpling.peper.ImportSet;
import de.corpling.peper.Importer;
import de.corpling.peper.PeperFactory;
import de.corpling.peper.PeperPackage;
import de.corpling.peper.PorterEmitter;
import de.corpling.salt.SaltFactory;
import de.corpling.salt.SaltProject;
import de.corpling.salt.SaltGraph;
import de.corpling.salt.SaltConcrete.SCorpus;
import de.corpling.salt.SaltConcrete.SDocument;
import de.corpling.salt.model.SElement;

import importer.paula.paula10.PAULA10Importer;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentWithInverseEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Convert Job</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.impl.ConvertJobImpl#getImportObjects <em>Import Objects</em>}</li>
 *   <li>{@link de.corpling.peper.impl.ConvertJobImpl#getExportObjects <em>Export Objects</em>}</li>
 *   <li>{@link de.corpling.peper.impl.ConvertJobImpl#getSaltProject <em>Salt Project</em>}</li>
 *   <li>{@link de.corpling.peper.impl.ConvertJobImpl#getPorterEmitter <em>Porter Emitter</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ConvertJobImpl extends EObjectImpl implements ConvertJob 
{
	private Logger logger= Logger.getLogger(ConvertJobImpl.class);
	private static final String MSG_ERR=	"Error("+ConvertJobImpl.class+"): ";
	/**
	 * The cached value of the '{@link #getImportObjects() <em>Import Objects</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImportObjects()
	 * @generated
	 * @ordered
	 */
	protected EList<ImportObject> importObjects;

	/**
	 * The cached value of the '{@link #getExportObjects() <em>Export Objects</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExportObjects()
	 * @generated
	 * @ordered
	 */
	protected EList<ExportObject> exportObjects;

	/**
	 * The default value of the '{@link #getSaltProject() <em>Salt Project</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSaltProject()
	 * @generated
	 * @ordered
	 */
	protected static final SaltProject SALT_PROJECT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSaltProject() <em>Salt Project</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSaltProject()
	 * @generated
	 * @ordered
	 */
	protected SaltProject saltProject = SALT_PROJECT_EDEFAULT;

	/**
	 * The cached value of the '{@link #getPorterEmitter() <em>Porter Emitter</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPorterEmitter()
	 * @generated
	 * @ordered
	 */
	protected PorterEmitter porterEmitter;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ConvertJobImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PeperPackage.Literals.CONVERT_JOB;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ImportObject> getImportObjects() {
		if (importObjects == null) {
			importObjects = new EObjectContainmentWithInverseEList<ImportObject>(ImportObject.class, this, PeperPackage.CONVERT_JOB__IMPORT_OBJECTS, PeperPackage.IMPORT_OBJECT__CONVERT_JOB);
		}
		return importObjects;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<ExportObject> getExportObjects() {
		if (exportObjects == null) {
			exportObjects = new EObjectContainmentWithInverseEList<ExportObject>(ExportObject.class, this, PeperPackage.CONVERT_JOB__EXPORT_OBJECTS, PeperPackage.EXPORT_OBJECT__CONVERT_JOB);
		}
		return exportObjects;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SaltProject getSaltProject() {
		return saltProject;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSaltProject(SaltProject newSaltProject) {
		SaltProject oldSaltProject = saltProject;
		saltProject = newSaltProject;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.CONVERT_JOB__SALT_PROJECT, oldSaltProject, saltProject));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PorterEmitter getPorterEmitter() {
		if (porterEmitter != null && porterEmitter.eIsProxy()) {
			InternalEObject oldPorterEmitter = (InternalEObject)porterEmitter;
			porterEmitter = (PorterEmitter)eResolveProxy(oldPorterEmitter);
			if (porterEmitter != oldPorterEmitter) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, PeperPackage.CONVERT_JOB__PORTER_EMITTER, oldPorterEmitter, porterEmitter));
			}
		}
		return porterEmitter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PorterEmitter basicGetPorterEmitter() {
		return porterEmitter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPorterEmitter(PorterEmitter newPorterEmitter) {
		PorterEmitter oldPorterEmitter = porterEmitter;
		porterEmitter = newPorterEmitter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.CONVERT_JOB__PORTER_EMITTER, oldPorterEmitter, porterEmitter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void start() 
	{
		if ((this.getImportObjects()== null) || (this.getImportObjects().size()< 1))
			throw new NullPointerException(MSG_ERR + "Cannot start with converting, because no importSet is given.");
		if ((this.getExportObjects()== null) || (this.getExportObjects().size()< 1))
			throw new NullPointerException(MSG_ERR + "Cannot start with converting, because no exportSet is given.");

		//TODO has to be fixed for more than one import Object
		if (this.getImportObjects().size()> 1)
			throw new NullPointerException(MSG_ERR + "Cannot convert import corpora, because cannot yet deal with more than one import corpus.");
		
		this.setSaltProject(SaltFactory.eINSTANCE.createSaltProject());
		
		//try catch has to be done, because of finalize does not seems to work
		try {
			//import corpora
			for (ImportObject importObject: this.getImportObjects())
			{
				logger.info("importing corpus structure");
				//set settings to importer
				importObject.getImporter().setSaltProject(this.getSaltProject());
				importObject.getImporter().importCorpusStructure();
			}
			
			
			//setting project to all exporters
			for (ExportObject exportObject: this.getExportObjects())
			{
				exportObject.getExporter().setSaltProject(this.getSaltProject());
			}	
			
			//import and export all documents
			for (SaltGraph sGraph: this.getSaltProject().getSaltGraphs())
			{
				if (sGraph.getSDocuments()!= null)
				{	
					for (SDocument sDocument: sGraph.getSDocuments())
					{
						//importing all documents with all importers
						for (ImportObject importObject: this.getImportObjects())
						{
							importObject.getImporter().importDocument(sDocument);
						}
						//exporting document with all exporters
						for (ExportObject exportObject: this.getExportObjects())
						{
							exportObject.getExporter().export(sDocument);
						}
						//removing document-graph from document
						sDocument.setSDocumentGraph(null);
					}
				}
			}
			
			//export corpora
			for (ExportObject exportObject: this.getExportObjects())
			{
				//set settings to importer
//				exportObject.getExporter().setSaltProject(this.getSaltProject());
				//export every saltGraph
				for (SaltGraph sGraph: this.getSaltProject().getSaltGraphs())
				{
					EList<SElement> roots= sGraph.getSRoots(); 
					if (roots== null)
						throw new NullPointerException(MSG_ERR + "No corpora to export.");
					for (SElement sCorpus: roots)
					{
						//export every corpus structure
						logger.info("exporting corpus structure starting at corpus: "+ sCorpus.getId());
						exportObject.getExporter().export((SCorpus)sCorpus);
					}
				}
			}
		} catch (RuntimeException e) {
			throw e;
		}
		finally
		{
			//close all importers (same as finalize, but this doesn´work)
			for (ImportObject importObject: this.getImportObjects())
			{
				importObject.getImporter().close();
			}
			//close all exporters (same as finalize, but this doesn´work)
			for (ExportObject exportObject: this.getExportObjects())
			{
				exportObject.getExporter().close();
			}
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void addImportSet(ImportSet importSet) 
	{
		if (importSet== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an empty import set.");
		if (importSet.getDataSourcePath()== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an import set which has no data source path to import.");
		if (importSet.getFormatDefinition()== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an import set which has no import set.");
		if (this.porterEmitter== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an import set, you have to set porter emitter first.");
		Importer importer= this.porterEmitter.emitImporter(importSet.getFormatDefinition());
		
		if (importer== null)
			throw new NullPointerException(MSG_ERR+ "Cannot add an import set, because no matching importer was found for given import set: "+ importSet);
		
		//setting input directory to importer
		importer.setInputDir(importSet.getDataSourcePath());
		
		//get directory, where settings of modules are stored
		URL dirUrl = ConvertJobImpl.class.getResource("./"); 
		String settingPath= dirUrl.getFile() + "../../../../../settings/modules/";
		importer.setSettingDir(new File(settingPath));
		
		//create a new ImportObject
		ImportObject importObject= PeperFactory.eINSTANCE.createImportObject();
		importObject.setImporter(importer);
		importObject.setImportSet(importSet);
		this.getImportObjects().add(importObject);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void addExportSet(ExportSet exportSet) 
	{
		if (exportSet== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an empty export set.");
		if (exportSet.getDataSourcePath()== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an export set which has no data source path to export.");
		if (exportSet.getFormatDefinition()== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an export set which has no export set.");
		if (this.porterEmitter== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an export set, you have to set porter emitter first.");
		Exporter exporter= this.porterEmitter.emitExporter(exportSet.getFormatDefinition());
		if (exporter== null)
			throw new NullPointerException(MSG_ERR+ "Cannot add an export set, because no matching exporter was found for given export set: "+ exportSet);
		
		//set settings to exporters
		exporter.setOutputDir(exportSet.getDataSourcePath());
		//create a new ImportObject
		ExportObject exportObject= PeperFactory.eINSTANCE.createExportObject();
		exportObject.setExporter(exporter);
		exportObject.setExportSet(exportSet);
		this.getExportObjects().add(exportObject);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case PeperPackage.CONVERT_JOB__IMPORT_OBJECTS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getImportObjects()).basicAdd(otherEnd, msgs);
			case PeperPackage.CONVERT_JOB__EXPORT_OBJECTS:
				return ((InternalEList<InternalEObject>)(InternalEList<?>)getExportObjects()).basicAdd(otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case PeperPackage.CONVERT_JOB__IMPORT_OBJECTS:
				return ((InternalEList<?>)getImportObjects()).basicRemove(otherEnd, msgs);
			case PeperPackage.CONVERT_JOB__EXPORT_OBJECTS:
				return ((InternalEList<?>)getExportObjects()).basicRemove(otherEnd, msgs);
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
			case PeperPackage.CONVERT_JOB__IMPORT_OBJECTS:
				return getImportObjects();
			case PeperPackage.CONVERT_JOB__EXPORT_OBJECTS:
				return getExportObjects();
			case PeperPackage.CONVERT_JOB__SALT_PROJECT:
				return getSaltProject();
			case PeperPackage.CONVERT_JOB__PORTER_EMITTER:
				if (resolve) return getPorterEmitter();
				return basicGetPorterEmitter();
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
			case PeperPackage.CONVERT_JOB__IMPORT_OBJECTS:
				getImportObjects().clear();
				getImportObjects().addAll((Collection<? extends ImportObject>)newValue);
				return;
			case PeperPackage.CONVERT_JOB__EXPORT_OBJECTS:
				getExportObjects().clear();
				getExportObjects().addAll((Collection<? extends ExportObject>)newValue);
				return;
			case PeperPackage.CONVERT_JOB__SALT_PROJECT:
				setSaltProject((SaltProject)newValue);
				return;
			case PeperPackage.CONVERT_JOB__PORTER_EMITTER:
				setPorterEmitter((PorterEmitter)newValue);
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
			case PeperPackage.CONVERT_JOB__IMPORT_OBJECTS:
				getImportObjects().clear();
				return;
			case PeperPackage.CONVERT_JOB__EXPORT_OBJECTS:
				getExportObjects().clear();
				return;
			case PeperPackage.CONVERT_JOB__SALT_PROJECT:
				setSaltProject(SALT_PROJECT_EDEFAULT);
				return;
			case PeperPackage.CONVERT_JOB__PORTER_EMITTER:
				setPorterEmitter((PorterEmitter)null);
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
			case PeperPackage.CONVERT_JOB__IMPORT_OBJECTS:
				return importObjects != null && !importObjects.isEmpty();
			case PeperPackage.CONVERT_JOB__EXPORT_OBJECTS:
				return exportObjects != null && !exportObjects.isEmpty();
			case PeperPackage.CONVERT_JOB__SALT_PROJECT:
				return SALT_PROJECT_EDEFAULT == null ? saltProject != null : !SALT_PROJECT_EDEFAULT.equals(saltProject);
			case PeperPackage.CONVERT_JOB__PORTER_EMITTER:
				return porterEmitter != null;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (saltProject: ");
		result.append(saltProject);
		result.append(')');
		return result.toString();
	}

} //ConvertJobImpl
