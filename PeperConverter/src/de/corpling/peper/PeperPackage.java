/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see de.corpling.peper.PeperFactory
 * @model kind="package"
 * @generated
 */
public interface PeperPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "peper";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "www.corpling.de/peper";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "peper";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	PeperPackage eINSTANCE = de.corpling.peper.impl.PeperPackageImpl.init();

	/**
	 * The meta object id for the '{@link de.corpling.peper.impl.ImExportSetImpl <em>Im Export Set</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.impl.ImExportSetImpl
	 * @see de.corpling.peper.impl.PeperPackageImpl#getImExportSet()
	 * @generated
	 */
	int IM_EXPORT_SET = 0;

	/**
	 * The feature id for the '<em><b>Data Source Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IM_EXPORT_SET__DATA_SOURCE_PATH = 0;

	/**
	 * The feature id for the '<em><b>Format Definition</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IM_EXPORT_SET__FORMAT_DEFINITION = 1;

	/**
	 * The number of structural features of the '<em>Im Export Set</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IM_EXPORT_SET_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link de.corpling.peper.impl.ExportSetImpl <em>Export Set</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.impl.ExportSetImpl
	 * @see de.corpling.peper.impl.PeperPackageImpl#getExportSet()
	 * @generated
	 */
	int EXPORT_SET = 1;

	/**
	 * The feature id for the '<em><b>Data Source Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORT_SET__DATA_SOURCE_PATH = IM_EXPORT_SET__DATA_SOURCE_PATH;

	/**
	 * The feature id for the '<em><b>Format Definition</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORT_SET__FORMAT_DEFINITION = IM_EXPORT_SET__FORMAT_DEFINITION;

	/**
	 * The number of structural features of the '<em>Export Set</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORT_SET_FEATURE_COUNT = IM_EXPORT_SET_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.corpling.peper.impl.ImportSetImpl <em>Import Set</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.impl.ImportSetImpl
	 * @see de.corpling.peper.impl.PeperPackageImpl#getImportSet()
	 * @generated
	 */
	int IMPORT_SET = 2;

	/**
	 * The feature id for the '<em><b>Data Source Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORT_SET__DATA_SOURCE_PATH = IM_EXPORT_SET__DATA_SOURCE_PATH;

	/**
	 * The feature id for the '<em><b>Format Definition</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORT_SET__FORMAT_DEFINITION = IM_EXPORT_SET__FORMAT_DEFINITION;

	/**
	 * The number of structural features of the '<em>Import Set</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORT_SET_FEATURE_COUNT = IM_EXPORT_SET_FEATURE_COUNT + 0;

	/**
	 * The meta object id for the '{@link de.corpling.peper.impl.ImExporterImpl <em>Im Exporter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.impl.ImExporterImpl
	 * @see de.corpling.peper.impl.PeperPackageImpl#getImExporter()
	 * @generated
	 */
	int IM_EXPORTER = 5;

	/**
	 * The feature id for the '<em><b>Supported Formats</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IM_EXPORTER__SUPPORTED_FORMATS = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IM_EXPORTER__NAME = 1;

	/**
	 * The feature id for the '<em><b>Salt Project</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IM_EXPORTER__SALT_PROJECT = 2;

	/**
	 * The number of structural features of the '<em>Im Exporter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IM_EXPORTER_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link de.corpling.peper.impl.ImporterImpl <em>Importer</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.impl.ImporterImpl
	 * @see de.corpling.peper.impl.PeperPackageImpl#getImporter()
	 * @generated
	 */
	int IMPORTER = 3;

	/**
	 * The feature id for the '<em><b>Supported Formats</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORTER__SUPPORTED_FORMATS = IM_EXPORTER__SUPPORTED_FORMATS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORTER__NAME = IM_EXPORTER__NAME;

	/**
	 * The feature id for the '<em><b>Salt Project</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORTER__SALT_PROJECT = IM_EXPORTER__SALT_PROJECT;

	/**
	 * The feature id for the '<em><b>Input Dir</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORTER__INPUT_DIR = IM_EXPORTER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Setting Dir</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORTER__SETTING_DIR = IM_EXPORTER_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Importer</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORTER_FEATURE_COUNT = IM_EXPORTER_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link de.corpling.peper.impl.ExporterImpl <em>Exporter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.impl.ExporterImpl
	 * @see de.corpling.peper.impl.PeperPackageImpl#getExporter()
	 * @generated
	 */
	int EXPORTER = 4;

	/**
	 * The feature id for the '<em><b>Supported Formats</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORTER__SUPPORTED_FORMATS = IM_EXPORTER__SUPPORTED_FORMATS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORTER__NAME = IM_EXPORTER__NAME;

	/**
	 * The feature id for the '<em><b>Salt Project</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORTER__SALT_PROJECT = IM_EXPORTER__SALT_PROJECT;

	/**
	 * The feature id for the '<em><b>Output Dir</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORTER__OUTPUT_DIR = IM_EXPORTER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Setting Dir</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORTER__SETTING_DIR = IM_EXPORTER_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>Exporter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORTER_FEATURE_COUNT = IM_EXPORTER_FEATURE_COUNT + 2;

	/**
	 * The meta object id for the '{@link de.corpling.peper.impl.FormatDefinitionImpl <em>Format Definition</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.impl.FormatDefinitionImpl
	 * @see de.corpling.peper.impl.PeperPackageImpl#getFormatDefinition()
	 * @generated
	 */
	int FORMAT_DEFINITION = 6;

	/**
	 * The feature id for the '<em><b>Format Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORMAT_DEFINITION__FORMAT_NAME = 0;

	/**
	 * The feature id for the '<em><b>Format Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORMAT_DEFINITION__FORMAT_VERSION = 1;

	/**
	 * The feature id for the '<em><b>Format Reference</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORMAT_DEFINITION__FORMAT_REFERENCE = 2;

	/**
	 * The number of structural features of the '<em>Format Definition</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int FORMAT_DEFINITION_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link de.corpling.peper.impl.ConvertJobImpl <em>Convert Job</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.impl.ConvertJobImpl
	 * @see de.corpling.peper.impl.PeperPackageImpl#getConvertJob()
	 * @generated
	 */
	int CONVERT_JOB = 7;

	/**
	 * The feature id for the '<em><b>Import Objects</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONVERT_JOB__IMPORT_OBJECTS = 0;

	/**
	 * The feature id for the '<em><b>Export Objects</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONVERT_JOB__EXPORT_OBJECTS = 1;

	/**
	 * The feature id for the '<em><b>Salt Project</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONVERT_JOB__SALT_PROJECT = 2;

	/**
	 * The feature id for the '<em><b>Porter Emitter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONVERT_JOB__PORTER_EMITTER = 3;

	/**
	 * The number of structural features of the '<em>Convert Job</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int CONVERT_JOB_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link de.corpling.peper.impl.ImportObjectImpl <em>Import Object</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.impl.ImportObjectImpl
	 * @see de.corpling.peper.impl.PeperPackageImpl#getImportObject()
	 * @generated
	 */
	int IMPORT_OBJECT = 8;

	/**
	 * The feature id for the '<em><b>Importer</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORT_OBJECT__IMPORTER = 0;

	/**
	 * The feature id for the '<em><b>Import Set</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORT_OBJECT__IMPORT_SET = 1;

	/**
	 * The feature id for the '<em><b>Convert Job</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORT_OBJECT__CONVERT_JOB = 2;

	/**
	 * The number of structural features of the '<em>Import Object</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IMPORT_OBJECT_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link de.corpling.peper.impl.ExportObjectImpl <em>Export Object</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.impl.ExportObjectImpl
	 * @see de.corpling.peper.impl.PeperPackageImpl#getExportObject()
	 * @generated
	 */
	int EXPORT_OBJECT = 9;

	/**
	 * The feature id for the '<em><b>Export Set</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORT_OBJECT__EXPORT_SET = 0;

	/**
	 * The feature id for the '<em><b>Exporter</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORT_OBJECT__EXPORTER = 1;

	/**
	 * The feature id for the '<em><b>Convert Job</b></em>' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORT_OBJECT__CONVERT_JOB = 2;

	/**
	 * The number of structural features of the '<em>Export Object</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EXPORT_OBJECT_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link de.corpling.peper.impl.PeperConverterImpl <em>Converter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.impl.PeperConverterImpl
	 * @see de.corpling.peper.impl.PeperPackageImpl#getPeperConverter()
	 * @generated
	 */
	int PEPER_CONVERTER = 10;

	/**
	 * The feature id for the '<em><b>Convert Jobs</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PEPER_CONVERTER__CONVERT_JOBS = 0;

	/**
	 * The number of structural features of the '<em>Converter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PEPER_CONVERTER_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link de.corpling.peper.impl.PorterEmitterImpl <em>Porter Emitter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.impl.PorterEmitterImpl
	 * @see de.corpling.peper.impl.PeperPackageImpl#getPorterEmitter()
	 * @generated
	 */
	int PORTER_EMITTER = 11;

	/**
	 * The feature id for the '<em><b>Props</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PORTER_EMITTER__PROPS = 0;

	/**
	 * The number of structural features of the '<em>Porter Emitter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int PORTER_EMITTER_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '<em>File</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.io.File
	 * @see de.corpling.peper.impl.PeperPackageImpl#getFile()
	 * @generated
	 */
	int FILE = 12;


	/**
	 * The meta object id for the '<em>Properties</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.util.Properties
	 * @see de.corpling.peper.impl.PeperPackageImpl#getProperties()
	 * @generated
	 */
	int PROPERTIES = 13;


	/**
	 * The meta object id for the '<em>Salt Project</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.salt.SaltProject
	 * @see de.corpling.peper.impl.PeperPackageImpl#getSaltProject()
	 * @generated
	 */
	int SALT_PROJECT = 14;


	/**
	 * Returns the meta object for class '{@link de.corpling.peper.ImExportSet <em>Im Export Set</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Im Export Set</em>'.
	 * @see de.corpling.peper.ImExportSet
	 * @generated
	 */
	EClass getImExportSet();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.ImExportSet#getDataSourcePath <em>Data Source Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Data Source Path</em>'.
	 * @see de.corpling.peper.ImExportSet#getDataSourcePath()
	 * @see #getImExportSet()
	 * @generated
	 */
	EAttribute getImExportSet_DataSourcePath();

	/**
	 * Returns the meta object for the containment reference '{@link de.corpling.peper.ImExportSet#getFormatDefinition <em>Format Definition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Format Definition</em>'.
	 * @see de.corpling.peper.ImExportSet#getFormatDefinition()
	 * @see #getImExportSet()
	 * @generated
	 */
	EReference getImExportSet_FormatDefinition();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.ExportSet <em>Export Set</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Export Set</em>'.
	 * @see de.corpling.peper.ExportSet
	 * @generated
	 */
	EClass getExportSet();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.ImportSet <em>Import Set</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Import Set</em>'.
	 * @see de.corpling.peper.ImportSet
	 * @generated
	 */
	EClass getImportSet();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.Importer <em>Importer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Importer</em>'.
	 * @see de.corpling.peper.Importer
	 * @generated
	 */
	EClass getImporter();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.Importer#getInputDir <em>Input Dir</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Input Dir</em>'.
	 * @see de.corpling.peper.Importer#getInputDir()
	 * @see #getImporter()
	 * @generated
	 */
	EAttribute getImporter_InputDir();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.Importer#getSettingDir <em>Setting Dir</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Setting Dir</em>'.
	 * @see de.corpling.peper.Importer#getSettingDir()
	 * @see #getImporter()
	 * @generated
	 */
	EAttribute getImporter_SettingDir();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.Exporter <em>Exporter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Exporter</em>'.
	 * @see de.corpling.peper.Exporter
	 * @generated
	 */
	EClass getExporter();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.Exporter#getOutputDir <em>Output Dir</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Output Dir</em>'.
	 * @see de.corpling.peper.Exporter#getOutputDir()
	 * @see #getExporter()
	 * @generated
	 */
	EAttribute getExporter_OutputDir();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.Exporter#getSettingDir <em>Setting Dir</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Setting Dir</em>'.
	 * @see de.corpling.peper.Exporter#getSettingDir()
	 * @see #getExporter()
	 * @generated
	 */
	EAttribute getExporter_SettingDir();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.ImExporter <em>Im Exporter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Im Exporter</em>'.
	 * @see de.corpling.peper.ImExporter
	 * @generated
	 */
	EClass getImExporter();

	/**
	 * Returns the meta object for the containment reference list '{@link de.corpling.peper.ImExporter#getSupportedFormats <em>Supported Formats</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Supported Formats</em>'.
	 * @see de.corpling.peper.ImExporter#getSupportedFormats()
	 * @see #getImExporter()
	 * @generated
	 */
	EReference getImExporter_SupportedFormats();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.ImExporter#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see de.corpling.peper.ImExporter#getName()
	 * @see #getImExporter()
	 * @generated
	 */
	EAttribute getImExporter_Name();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.ImExporter#getSaltProject <em>Salt Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Salt Project</em>'.
	 * @see de.corpling.peper.ImExporter#getSaltProject()
	 * @see #getImExporter()
	 * @generated
	 */
	EAttribute getImExporter_SaltProject();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.FormatDefinition <em>Format Definition</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Format Definition</em>'.
	 * @see de.corpling.peper.FormatDefinition
	 * @generated
	 */
	EClass getFormatDefinition();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.FormatDefinition#getFormatName <em>Format Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Format Name</em>'.
	 * @see de.corpling.peper.FormatDefinition#getFormatName()
	 * @see #getFormatDefinition()
	 * @generated
	 */
	EAttribute getFormatDefinition_FormatName();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.FormatDefinition#getFormatVersion <em>Format Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Format Version</em>'.
	 * @see de.corpling.peper.FormatDefinition#getFormatVersion()
	 * @see #getFormatDefinition()
	 * @generated
	 */
	EAttribute getFormatDefinition_FormatVersion();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.FormatDefinition#getFormatReference <em>Format Reference</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Format Reference</em>'.
	 * @see de.corpling.peper.FormatDefinition#getFormatReference()
	 * @see #getFormatDefinition()
	 * @generated
	 */
	EAttribute getFormatDefinition_FormatReference();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.ConvertJob <em>Convert Job</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Convert Job</em>'.
	 * @see de.corpling.peper.ConvertJob
	 * @generated
	 */
	EClass getConvertJob();

	/**
	 * Returns the meta object for the containment reference list '{@link de.corpling.peper.ConvertJob#getImportObjects <em>Import Objects</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Import Objects</em>'.
	 * @see de.corpling.peper.ConvertJob#getImportObjects()
	 * @see #getConvertJob()
	 * @generated
	 */
	EReference getConvertJob_ImportObjects();

	/**
	 * Returns the meta object for the containment reference list '{@link de.corpling.peper.ConvertJob#getExportObjects <em>Export Objects</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Export Objects</em>'.
	 * @see de.corpling.peper.ConvertJob#getExportObjects()
	 * @see #getConvertJob()
	 * @generated
	 */
	EReference getConvertJob_ExportObjects();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.ConvertJob#getSaltProject <em>Salt Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Salt Project</em>'.
	 * @see de.corpling.peper.ConvertJob#getSaltProject()
	 * @see #getConvertJob()
	 * @generated
	 */
	EAttribute getConvertJob_SaltProject();

	/**
	 * Returns the meta object for the reference '{@link de.corpling.peper.ConvertJob#getPorterEmitter <em>Porter Emitter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Porter Emitter</em>'.
	 * @see de.corpling.peper.ConvertJob#getPorterEmitter()
	 * @see #getConvertJob()
	 * @generated
	 */
	EReference getConvertJob_PorterEmitter();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.ImportObject <em>Import Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Import Object</em>'.
	 * @see de.corpling.peper.ImportObject
	 * @generated
	 */
	EClass getImportObject();

	/**
	 * Returns the meta object for the containment reference '{@link de.corpling.peper.ImportObject#getImporter <em>Importer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Importer</em>'.
	 * @see de.corpling.peper.ImportObject#getImporter()
	 * @see #getImportObject()
	 * @generated
	 */
	EReference getImportObject_Importer();

	/**
	 * Returns the meta object for the containment reference '{@link de.corpling.peper.ImportObject#getImportSet <em>Import Set</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Import Set</em>'.
	 * @see de.corpling.peper.ImportObject#getImportSet()
	 * @see #getImportObject()
	 * @generated
	 */
	EReference getImportObject_ImportSet();

	/**
	 * Returns the meta object for the container reference '{@link de.corpling.peper.ImportObject#getConvertJob <em>Convert Job</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Convert Job</em>'.
	 * @see de.corpling.peper.ImportObject#getConvertJob()
	 * @see #getImportObject()
	 * @generated
	 */
	EReference getImportObject_ConvertJob();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.ExportObject <em>Export Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Export Object</em>'.
	 * @see de.corpling.peper.ExportObject
	 * @generated
	 */
	EClass getExportObject();

	/**
	 * Returns the meta object for the containment reference '{@link de.corpling.peper.ExportObject#getExportSet <em>Export Set</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Export Set</em>'.
	 * @see de.corpling.peper.ExportObject#getExportSet()
	 * @see #getExportObject()
	 * @generated
	 */
	EReference getExportObject_ExportSet();

	/**
	 * Returns the meta object for the containment reference '{@link de.corpling.peper.ExportObject#getExporter <em>Exporter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Exporter</em>'.
	 * @see de.corpling.peper.ExportObject#getExporter()
	 * @see #getExportObject()
	 * @generated
	 */
	EReference getExportObject_Exporter();

	/**
	 * Returns the meta object for the container reference '{@link de.corpling.peper.ExportObject#getConvertJob <em>Convert Job</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the container reference '<em>Convert Job</em>'.
	 * @see de.corpling.peper.ExportObject#getConvertJob()
	 * @see #getExportObject()
	 * @generated
	 */
	EReference getExportObject_ConvertJob();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.PeperConverter <em>Converter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Converter</em>'.
	 * @see de.corpling.peper.PeperConverter
	 * @generated
	 */
	EClass getPeperConverter();

	/**
	 * Returns the meta object for the containment reference list '{@link de.corpling.peper.PeperConverter#getConvertJobs <em>Convert Jobs</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Convert Jobs</em>'.
	 * @see de.corpling.peper.PeperConverter#getConvertJobs()
	 * @see #getPeperConverter()
	 * @generated
	 */
	EReference getPeperConverter_ConvertJobs();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.PorterEmitter <em>Porter Emitter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Porter Emitter</em>'.
	 * @see de.corpling.peper.PorterEmitter
	 * @generated
	 */
	EClass getPorterEmitter();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.PorterEmitter#getProps <em>Props</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Props</em>'.
	 * @see de.corpling.peper.PorterEmitter#getProps()
	 * @see #getPorterEmitter()
	 * @generated
	 */
	EAttribute getPorterEmitter_Props();

	/**
	 * Returns the meta object for data type '{@link java.io.File <em>File</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>File</em>'.
	 * @see java.io.File
	 * @model instanceClass="java.io.File"
	 * @generated
	 */
	EDataType getFile();

	/**
	 * Returns the meta object for data type '{@link java.util.Properties <em>Properties</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Properties</em>'.
	 * @see java.util.Properties
	 * @model instanceClass="java.util.Properties"
	 * @generated
	 */
	EDataType getProperties();

	/**
	 * Returns the meta object for data type '{@link de.corpling.salt.SaltProject <em>Salt Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Salt Project</em>'.
	 * @see de.corpling.salt.SaltProject
	 * @model instanceClass="de.corpling.salt.SaltProject"
	 * @generated
	 */
	EDataType getSaltProject();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	PeperFactory getPeperFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link de.corpling.peper.impl.ImExportSetImpl <em>Im Export Set</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.impl.ImExportSetImpl
		 * @see de.corpling.peper.impl.PeperPackageImpl#getImExportSet()
		 * @generated
		 */
		EClass IM_EXPORT_SET = eINSTANCE.getImExportSet();

		/**
		 * The meta object literal for the '<em><b>Data Source Path</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IM_EXPORT_SET__DATA_SOURCE_PATH = eINSTANCE.getImExportSet_DataSourcePath();

		/**
		 * The meta object literal for the '<em><b>Format Definition</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IM_EXPORT_SET__FORMAT_DEFINITION = eINSTANCE.getImExportSet_FormatDefinition();

		/**
		 * The meta object literal for the '{@link de.corpling.peper.impl.ExportSetImpl <em>Export Set</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.impl.ExportSetImpl
		 * @see de.corpling.peper.impl.PeperPackageImpl#getExportSet()
		 * @generated
		 */
		EClass EXPORT_SET = eINSTANCE.getExportSet();

		/**
		 * The meta object literal for the '{@link de.corpling.peper.impl.ImportSetImpl <em>Import Set</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.impl.ImportSetImpl
		 * @see de.corpling.peper.impl.PeperPackageImpl#getImportSet()
		 * @generated
		 */
		EClass IMPORT_SET = eINSTANCE.getImportSet();

		/**
		 * The meta object literal for the '{@link de.corpling.peper.impl.ImporterImpl <em>Importer</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.impl.ImporterImpl
		 * @see de.corpling.peper.impl.PeperPackageImpl#getImporter()
		 * @generated
		 */
		EClass IMPORTER = eINSTANCE.getImporter();

		/**
		 * The meta object literal for the '<em><b>Input Dir</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMPORTER__INPUT_DIR = eINSTANCE.getImporter_InputDir();

		/**
		 * The meta object literal for the '<em><b>Setting Dir</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IMPORTER__SETTING_DIR = eINSTANCE.getImporter_SettingDir();

		/**
		 * The meta object literal for the '{@link de.corpling.peper.impl.ExporterImpl <em>Exporter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.impl.ExporterImpl
		 * @see de.corpling.peper.impl.PeperPackageImpl#getExporter()
		 * @generated
		 */
		EClass EXPORTER = eINSTANCE.getExporter();

		/**
		 * The meta object literal for the '<em><b>Output Dir</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EXPORTER__OUTPUT_DIR = eINSTANCE.getExporter_OutputDir();

		/**
		 * The meta object literal for the '<em><b>Setting Dir</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EXPORTER__SETTING_DIR = eINSTANCE.getExporter_SettingDir();

		/**
		 * The meta object literal for the '{@link de.corpling.peper.impl.ImExporterImpl <em>Im Exporter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.impl.ImExporterImpl
		 * @see de.corpling.peper.impl.PeperPackageImpl#getImExporter()
		 * @generated
		 */
		EClass IM_EXPORTER = eINSTANCE.getImExporter();

		/**
		 * The meta object literal for the '<em><b>Supported Formats</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IM_EXPORTER__SUPPORTED_FORMATS = eINSTANCE.getImExporter_SupportedFormats();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IM_EXPORTER__NAME = eINSTANCE.getImExporter_Name();

		/**
		 * The meta object literal for the '<em><b>Salt Project</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IM_EXPORTER__SALT_PROJECT = eINSTANCE.getImExporter_SaltProject();

		/**
		 * The meta object literal for the '{@link de.corpling.peper.impl.FormatDefinitionImpl <em>Format Definition</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.impl.FormatDefinitionImpl
		 * @see de.corpling.peper.impl.PeperPackageImpl#getFormatDefinition()
		 * @generated
		 */
		EClass FORMAT_DEFINITION = eINSTANCE.getFormatDefinition();

		/**
		 * The meta object literal for the '<em><b>Format Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FORMAT_DEFINITION__FORMAT_NAME = eINSTANCE.getFormatDefinition_FormatName();

		/**
		 * The meta object literal for the '<em><b>Format Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FORMAT_DEFINITION__FORMAT_VERSION = eINSTANCE.getFormatDefinition_FormatVersion();

		/**
		 * The meta object literal for the '<em><b>Format Reference</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute FORMAT_DEFINITION__FORMAT_REFERENCE = eINSTANCE.getFormatDefinition_FormatReference();

		/**
		 * The meta object literal for the '{@link de.corpling.peper.impl.ConvertJobImpl <em>Convert Job</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.impl.ConvertJobImpl
		 * @see de.corpling.peper.impl.PeperPackageImpl#getConvertJob()
		 * @generated
		 */
		EClass CONVERT_JOB = eINSTANCE.getConvertJob();

		/**
		 * The meta object literal for the '<em><b>Import Objects</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CONVERT_JOB__IMPORT_OBJECTS = eINSTANCE.getConvertJob_ImportObjects();

		/**
		 * The meta object literal for the '<em><b>Export Objects</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CONVERT_JOB__EXPORT_OBJECTS = eINSTANCE.getConvertJob_ExportObjects();

		/**
		 * The meta object literal for the '<em><b>Salt Project</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute CONVERT_JOB__SALT_PROJECT = eINSTANCE.getConvertJob_SaltProject();

		/**
		 * The meta object literal for the '<em><b>Porter Emitter</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference CONVERT_JOB__PORTER_EMITTER = eINSTANCE.getConvertJob_PorterEmitter();

		/**
		 * The meta object literal for the '{@link de.corpling.peper.impl.ImportObjectImpl <em>Import Object</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.impl.ImportObjectImpl
		 * @see de.corpling.peper.impl.PeperPackageImpl#getImportObject()
		 * @generated
		 */
		EClass IMPORT_OBJECT = eINSTANCE.getImportObject();

		/**
		 * The meta object literal for the '<em><b>Importer</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IMPORT_OBJECT__IMPORTER = eINSTANCE.getImportObject_Importer();

		/**
		 * The meta object literal for the '<em><b>Import Set</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IMPORT_OBJECT__IMPORT_SET = eINSTANCE.getImportObject_ImportSet();

		/**
		 * The meta object literal for the '<em><b>Convert Job</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference IMPORT_OBJECT__CONVERT_JOB = eINSTANCE.getImportObject_ConvertJob();

		/**
		 * The meta object literal for the '{@link de.corpling.peper.impl.ExportObjectImpl <em>Export Object</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.impl.ExportObjectImpl
		 * @see de.corpling.peper.impl.PeperPackageImpl#getExportObject()
		 * @generated
		 */
		EClass EXPORT_OBJECT = eINSTANCE.getExportObject();

		/**
		 * The meta object literal for the '<em><b>Export Set</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EXPORT_OBJECT__EXPORT_SET = eINSTANCE.getExportObject_ExportSet();

		/**
		 * The meta object literal for the '<em><b>Exporter</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EXPORT_OBJECT__EXPORTER = eINSTANCE.getExportObject_Exporter();

		/**
		 * The meta object literal for the '<em><b>Convert Job</b></em>' container reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EXPORT_OBJECT__CONVERT_JOB = eINSTANCE.getExportObject_ConvertJob();

		/**
		 * The meta object literal for the '{@link de.corpling.peper.impl.PeperConverterImpl <em>Converter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.impl.PeperConverterImpl
		 * @see de.corpling.peper.impl.PeperPackageImpl#getPeperConverter()
		 * @generated
		 */
		EClass PEPER_CONVERTER = eINSTANCE.getPeperConverter();

		/**
		 * The meta object literal for the '<em><b>Convert Jobs</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference PEPER_CONVERTER__CONVERT_JOBS = eINSTANCE.getPeperConverter_ConvertJobs();

		/**
		 * The meta object literal for the '{@link de.corpling.peper.impl.PorterEmitterImpl <em>Porter Emitter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.impl.PorterEmitterImpl
		 * @see de.corpling.peper.impl.PeperPackageImpl#getPorterEmitter()
		 * @generated
		 */
		EClass PORTER_EMITTER = eINSTANCE.getPorterEmitter();

		/**
		 * The meta object literal for the '<em><b>Props</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute PORTER_EMITTER__PROPS = eINSTANCE.getPorterEmitter_Props();

		/**
		 * The meta object literal for the '<em>File</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.io.File
		 * @see de.corpling.peper.impl.PeperPackageImpl#getFile()
		 * @generated
		 */
		EDataType FILE = eINSTANCE.getFile();

		/**
		 * The meta object literal for the '<em>Properties</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.util.Properties
		 * @see de.corpling.peper.impl.PeperPackageImpl#getProperties()
		 * @generated
		 */
		EDataType PROPERTIES = eINSTANCE.getProperties();

		/**
		 * The meta object literal for the '<em>Salt Project</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.salt.SaltProject
		 * @see de.corpling.peper.impl.PeperPackageImpl#getSaltProject()
		 * @generated
		 */
		EDataType SALT_PROJECT = eINSTANCE.getSaltProject();

	}

} //PeperPackage
