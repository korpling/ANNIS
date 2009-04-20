/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.impl;

import de.corpling.peper.ConvertJob;
import de.corpling.peper.ExportObject;
import de.corpling.peper.ConvertProject;
import de.corpling.peper.ExportObjects;
import de.corpling.peper.ExportSet;
import de.corpling.peper.Exporter;
import de.corpling.peper.FormatDefinition;
import de.corpling.peper.ImExportSet;
import de.corpling.peper.ImExporter;
import de.corpling.peper.ImportObject;
import de.corpling.peper.ImportSet;
import de.corpling.peper.Importer;
import de.corpling.peper.PeperConverter;
import de.corpling.peper.PeperFactory;
import de.corpling.peper.PeperPackage;

import de.corpling.peper.PorterEmitter;
import de.corpling.salt.SaltConcrete.SaltConcretePackage;
import de.corpling.salt.SaltConcrete.impl.SaltConcretePackageImpl;
import de.corpling.salt.SaltProject;
import de.corpling.salt.model.ModelPackage;
import de.corpling.salt.model.core.CorePackage;
import de.corpling.salt.model.core.impl.CorePackageImpl;
import de.corpling.salt.model.impl.ModelPackageImpl;
import java.io.File;

import java.util.Properties;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class PeperPackageImpl extends EPackageImpl implements PeperPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass imExportSetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass exportSetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass importSetEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass importerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass exporterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass imExporterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass formatDefinitionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass convertJobEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass importObjectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass exportObjectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass peperConverterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass porterEmitterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType fileEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType propertiesEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType saltProjectEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see de.corpling.peper.PeperPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private PeperPackageImpl() {
		super(eNS_URI, PeperFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this
	 * model, and for any others upon which it depends.  Simple
	 * dependencies are satisfied by calling this method on all
	 * dependent packages before doing anything else.  This method drives
	 * initialization for interdependent packages directly, in parallel
	 * with this package, itself.
	 * <p>Of this package and its interdependencies, all packages which
	 * have not yet been registered by their URI values are first created
	 * and registered.  The packages are then initialized in two steps:
	 * meta-model objects for all of the packages are created before any
	 * are initialized, since one package's meta-model objects may refer to
	 * those of another.
	 * <p>Invocation of this method will not affect any packages that have
	 * already been initialized.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static PeperPackage init() {
		if (isInited) return (PeperPackage)EPackage.Registry.INSTANCE.getEPackage(PeperPackage.eNS_URI);

		// Obtain or create and register package
		PeperPackageImpl thePeperPackage = (PeperPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof PeperPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new PeperPackageImpl());

		isInited = true;

		// Obtain or create and register interdependencies
		SaltConcretePackageImpl theSaltConcretePackage = (SaltConcretePackageImpl)(EPackage.Registry.INSTANCE.getEPackage(SaltConcretePackage.eNS_URI) instanceof SaltConcretePackageImpl ? EPackage.Registry.INSTANCE.getEPackage(SaltConcretePackage.eNS_URI) : SaltConcretePackage.eINSTANCE);
		ModelPackageImpl theModelPackage = (ModelPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ModelPackage.eNS_URI) instanceof ModelPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ModelPackage.eNS_URI) : ModelPackage.eINSTANCE);
		CorePackageImpl theCorePackage = (CorePackageImpl)(EPackage.Registry.INSTANCE.getEPackage(CorePackage.eNS_URI) instanceof CorePackageImpl ? EPackage.Registry.INSTANCE.getEPackage(CorePackage.eNS_URI) : CorePackage.eINSTANCE);

		// Create package meta-data objects
		thePeperPackage.createPackageContents();
		theSaltConcretePackage.createPackageContents();
		theModelPackage.createPackageContents();
		theCorePackage.createPackageContents();

		// Initialize created meta-data
		thePeperPackage.initializePackageContents();
		theSaltConcretePackage.initializePackageContents();
		theModelPackage.initializePackageContents();
		theCorePackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		thePeperPackage.freeze();

		return thePeperPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getImExportSet() {
		return imExportSetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImExportSet_DataSourcePath() {
		return (EAttribute)imExportSetEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getImExportSet_FormatDefinition() {
		return (EReference)imExportSetEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getExportSet() {
		return exportSetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getImportSet() {
		return importSetEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getImporter() {
		return importerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImporter_InputDir() {
		return (EAttribute)importerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImporter_SettingDir() {
		return (EAttribute)importerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getExporter() {
		return exporterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getExporter_OutputDir() {
		return (EAttribute)exporterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getExporter_SettingDir() {
		return (EAttribute)exporterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getImExporter() {
		return imExporterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getImExporter_SupportedFormats() {
		return (EReference)imExporterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImExporter_Name() {
		return (EAttribute)imExporterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getImExporter_SaltProject() {
		return (EAttribute)imExporterEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getFormatDefinition() {
		return formatDefinitionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFormatDefinition_FormatName() {
		return (EAttribute)formatDefinitionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFormatDefinition_FormatVersion() {
		return (EAttribute)formatDefinitionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getFormatDefinition_FormatReference() {
		return (EAttribute)formatDefinitionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getConvertJob() {
		return convertJobEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getConvertJob_ImportObjects() {
		return (EReference)convertJobEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getConvertJob_ExportObjects() {
		return (EReference)convertJobEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getConvertJob_SaltProject() {
		return (EAttribute)convertJobEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getConvertJob_PorterEmitter() {
		return (EReference)convertJobEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getImportObject() {
		return importObjectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getImportObject_Importer() {
		return (EReference)importObjectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getImportObject_ImportSet() {
		return (EReference)importObjectEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getImportObject_ConvertJob() {
		return (EReference)importObjectEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getExportObject() {
		return exportObjectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getExportObject_ExportSet() {
		return (EReference)exportObjectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getExportObject_Exporter() {
		return (EReference)exportObjectEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getExportObject_ConvertJob() {
		return (EReference)exportObjectEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPeperConverter() {
		return peperConverterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getPeperConverter_ConvertJobs() {
		return (EReference)peperConverterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getPorterEmitter() {
		return porterEmitterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getPorterEmitter_Props() {
		return (EAttribute)porterEmitterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getFile() {
		return fileEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getProperties() {
		return propertiesEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getSaltProject() {
		return saltProjectEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PeperFactory getPeperFactory() {
		return (PeperFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		imExportSetEClass = createEClass(IM_EXPORT_SET);
		createEAttribute(imExportSetEClass, IM_EXPORT_SET__DATA_SOURCE_PATH);
		createEReference(imExportSetEClass, IM_EXPORT_SET__FORMAT_DEFINITION);

		exportSetEClass = createEClass(EXPORT_SET);

		importSetEClass = createEClass(IMPORT_SET);

		importerEClass = createEClass(IMPORTER);
		createEAttribute(importerEClass, IMPORTER__INPUT_DIR);
		createEAttribute(importerEClass, IMPORTER__SETTING_DIR);

		exporterEClass = createEClass(EXPORTER);
		createEAttribute(exporterEClass, EXPORTER__OUTPUT_DIR);
		createEAttribute(exporterEClass, EXPORTER__SETTING_DIR);

		imExporterEClass = createEClass(IM_EXPORTER);
		createEReference(imExporterEClass, IM_EXPORTER__SUPPORTED_FORMATS);
		createEAttribute(imExporterEClass, IM_EXPORTER__NAME);
		createEAttribute(imExporterEClass, IM_EXPORTER__SALT_PROJECT);

		formatDefinitionEClass = createEClass(FORMAT_DEFINITION);
		createEAttribute(formatDefinitionEClass, FORMAT_DEFINITION__FORMAT_NAME);
		createEAttribute(formatDefinitionEClass, FORMAT_DEFINITION__FORMAT_VERSION);
		createEAttribute(formatDefinitionEClass, FORMAT_DEFINITION__FORMAT_REFERENCE);

		convertJobEClass = createEClass(CONVERT_JOB);
		createEReference(convertJobEClass, CONVERT_JOB__IMPORT_OBJECTS);
		createEReference(convertJobEClass, CONVERT_JOB__EXPORT_OBJECTS);
		createEAttribute(convertJobEClass, CONVERT_JOB__SALT_PROJECT);
		createEReference(convertJobEClass, CONVERT_JOB__PORTER_EMITTER);

		importObjectEClass = createEClass(IMPORT_OBJECT);
		createEReference(importObjectEClass, IMPORT_OBJECT__IMPORTER);
		createEReference(importObjectEClass, IMPORT_OBJECT__IMPORT_SET);
		createEReference(importObjectEClass, IMPORT_OBJECT__CONVERT_JOB);

		exportObjectEClass = createEClass(EXPORT_OBJECT);
		createEReference(exportObjectEClass, EXPORT_OBJECT__EXPORT_SET);
		createEReference(exportObjectEClass, EXPORT_OBJECT__EXPORTER);
		createEReference(exportObjectEClass, EXPORT_OBJECT__CONVERT_JOB);

		peperConverterEClass = createEClass(PEPER_CONVERTER);
		createEReference(peperConverterEClass, PEPER_CONVERTER__CONVERT_JOBS);

		porterEmitterEClass = createEClass(PORTER_EMITTER);
		createEAttribute(porterEmitterEClass, PORTER_EMITTER__PROPS);

		// Create data types
		fileEDataType = createEDataType(FILE);
		propertiesEDataType = createEDataType(PROPERTIES);
		saltProjectEDataType = createEDataType(SALT_PROJECT);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		SaltConcretePackage theSaltConcretePackage = (SaltConcretePackage)EPackage.Registry.INSTANCE.getEPackage(SaltConcretePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		exportSetEClass.getESuperTypes().add(this.getImExportSet());
		importSetEClass.getESuperTypes().add(this.getImExportSet());
		importerEClass.getESuperTypes().add(this.getImExporter());
		exporterEClass.getESuperTypes().add(this.getImExporter());

		// Initialize classes and features; add operations and parameters
		initEClass(imExportSetEClass, ImExportSet.class, "ImExportSet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getImExportSet_DataSourcePath(), this.getFile(), "dataSourcePath", null, 0, 1, ImExportSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getImExportSet_FormatDefinition(), this.getFormatDefinition(), null, "formatDefinition", null, 1, 1, ImExportSet.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(exportSetEClass, ExportSet.class, "ExportSet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(importSetEClass, ImportSet.class, "ImportSet", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(importerEClass, Importer.class, "Importer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getImporter_InputDir(), this.getFile(), "inputDir", null, 0, 1, Importer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getImporter_SettingDir(), this.getFile(), "settingDir", null, 0, 1, Importer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(importerEClass, null, "importCorpusStructure", 0, 1, IS_UNIQUE, IS_ORDERED);

		EOperation op = addEOperation(importerEClass, null, "importDocument", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theSaltConcretePackage.getSDocument(), "sDocument", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(importerEClass, null, "close", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(exporterEClass, Exporter.class, "Exporter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getExporter_OutputDir(), this.getFile(), "outputDir", null, 0, 1, Exporter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getExporter_SettingDir(), this.getFile(), "settingDir", null, 0, 1, Exporter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(exporterEClass, null, "export", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theSaltConcretePackage.getSCorpus(), "sCorpus", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(exporterEClass, null, "export", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theSaltConcretePackage.getSDocument(), "sDocument", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(exporterEClass, null, "close", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(imExporterEClass, ImExporter.class, "ImExporter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getImExporter_SupportedFormats(), this.getFormatDefinition(), null, "supportedFormats", null, 1, -1, ImExporter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getImExporter_Name(), ecorePackage.getEString(), "name", null, 0, 1, ImExporter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getImExporter_SaltProject(), this.getSaltProject(), "saltProject", null, 0, 1, ImExporter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(imExporterEClass, ecorePackage.getEBoolean(), "isSupported", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getImExportSet(), "ImExportSet", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(formatDefinitionEClass, FormatDefinition.class, "FormatDefinition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFormatDefinition_FormatName(), ecorePackage.getEString(), "formatName", null, 0, 1, FormatDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFormatDefinition_FormatVersion(), ecorePackage.getEString(), "formatVersion", null, 0, 1, FormatDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFormatDefinition_FormatReference(), ecorePackage.getEString(), "formatReference", null, 0, 1, FormatDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(formatDefinitionEClass, ecorePackage.getEBoolean(), "equals", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getFormatDefinition(), "otherObject", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(convertJobEClass, ConvertJob.class, "ConvertJob", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getConvertJob_ImportObjects(), this.getImportObject(), this.getImportObject_ConvertJob(), "importObjects", null, 0, -1, ConvertJob.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getConvertJob_ExportObjects(), this.getExportObject(), this.getExportObject_ConvertJob(), "exportObjects", null, 0, -1, ConvertJob.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getConvertJob_SaltProject(), this.getSaltProject(), "saltProject", null, 0, 1, ConvertJob.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getConvertJob_PorterEmitter(), this.getPorterEmitter(), null, "porterEmitter", null, 0, 1, ConvertJob.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(convertJobEClass, null, "start", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(convertJobEClass, null, "addImportSet", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getImportSet(), "importSet", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(convertJobEClass, null, "addExportSet", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getExportSet(), "exportSet", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(importObjectEClass, ImportObject.class, "ImportObject", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getImportObject_Importer(), this.getImporter(), null, "importer", null, 1, 1, ImportObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getImportObject_ImportSet(), this.getImportSet(), null, "importSet", null, 1, 1, ImportObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getImportObject_ConvertJob(), this.getConvertJob(), this.getConvertJob_ImportObjects(), "convertJob", null, 1, 1, ImportObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(exportObjectEClass, ExportObject.class, "ExportObject", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getExportObject_ExportSet(), this.getExportSet(), null, "exportSet", null, 1, 1, ExportObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getExportObject_Exporter(), this.getExporter(), null, "exporter", null, 1, 1, ExportObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getExportObject_ConvertJob(), this.getConvertJob(), this.getConvertJob_ExportObjects(), "convertJob", null, 1, 1, ExportObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(peperConverterEClass, PeperConverter.class, "PeperConverter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getPeperConverter_ConvertJobs(), this.getConvertJob(), null, "convertJobs", null, 0, -1, PeperConverter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(peperConverterEClass, null, "start", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(peperConverterEClass, null, "addJob", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "importDescription", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getEString(), "exportDescription", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(porterEmitterEClass, PorterEmitter.class, "PorterEmitter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPorterEmitter_Props(), this.getProperties(), "props", null, 0, 1, PorterEmitter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(porterEmitterEClass, this.getImporter(), "emitImporter", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getFormatDefinition(), "formatDefinition", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(porterEmitterEClass, this.getExporter(), "emitExporter", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getFormatDefinition(), "formatDefinition", 0, 1, IS_UNIQUE, IS_ORDERED);

		// Initialize data types
		initEDataType(fileEDataType, File.class, "File", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(propertiesEDataType, Properties.class, "Properties", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(saltProjectEDataType, SaltProject.class, "SaltProject", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //PeperPackageImpl
