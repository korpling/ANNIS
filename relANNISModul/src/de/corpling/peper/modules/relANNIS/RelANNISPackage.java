/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS;

import de.corpling.peper.PeperPackage;
import de.corpling.salt.model.saltCore.SaltCorePackage;
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
 * @see de.corpling.peper.modules.relANNIS.RelANNISFactory
 * @model kind="package"
 * @generated
 */
public interface RelANNISPackage extends EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "relANNIS";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "relANNIS";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "ra";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	RelANNISPackage eINSTANCE = de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl.init();

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.RAExporterImpl <em>RA Exporter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.RAExporterImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRAExporter()
	 * @generated
	 */
	int RA_EXPORTER = 0;

	/**
	 * The feature id for the '<em><b>Supported Formats</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EXPORTER__SUPPORTED_FORMATS = PeperPackage.EXPORTER__SUPPORTED_FORMATS;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EXPORTER__NAME = PeperPackage.EXPORTER__NAME;

	/**
	 * The feature id for the '<em><b>Salt Project</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EXPORTER__SALT_PROJECT = PeperPackage.EXPORTER__SALT_PROJECT;

	/**
	 * The feature id for the '<em><b>Output Dir</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EXPORTER__OUTPUT_DIR = PeperPackage.EXPORTER__OUTPUT_DIR;

	/**
	 * The feature id for the '<em><b>Setting Dir</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EXPORTER__SETTING_DIR = PeperPackage.EXPORTER__SETTING_DIR;

	/**
	 * The feature id for the '<em><b>Ra Mapper</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EXPORTER__RA_MAPPER = PeperPackage.EXPORTER_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Ra DAO</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EXPORTER__RA_DAO = PeperPackage.EXPORTER_FEATURE_COUNT + 1;

	/**
	 * The number of structural features of the '<em>RA Exporter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EXPORTER_FEATURE_COUNT = PeperPackage.EXPORTER_FEATURE_COUNT + 2;


	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.RAMapperImpl <em>RA Mapper</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.RAMapperImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRAMapper()
	 * @generated
	 */
	int RA_MAPPER = 1;

	/**
	 * The feature id for the '<em><b>Ra DAO</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_MAPPER__RA_DAO = SaltCorePackage.STRAVERSAL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Curr Ta Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_MAPPER__CURR_TA_ID = SaltCorePackage.STRAVERSAL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Ra Exporter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_MAPPER__RA_EXPORTER = SaltCorePackage.STRAVERSAL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Coherent Components</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_MAPPER__COHERENT_COMPONENTS = SaltCorePackage.STRAVERSAL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>Sub Connected Components</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_MAPPER__SUB_CONNECTED_COMPONENTS = SaltCorePackage.STRAVERSAL_OBJECT_FEATURE_COUNT + 4;

	/**
	 * The number of structural features of the '<em>RA Mapper</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_MAPPER_FEATURE_COUNT = SaltCorePackage.STRAVERSAL_OBJECT_FEATURE_COUNT + 5;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.RADAOImpl <em>RADAO</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.RADAOImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRADAO()
	 * @generated
	 */
	int RADAO = 2;

	/**
	 * The feature id for the '<em><b>Output Dir</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RADAO__OUTPUT_DIR = SaltCorePackage.STRAVERSAL_OBJECT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Tuple Writer Entries</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RADAO__TUPLE_WRITER_ENTRIES = SaltCorePackage.STRAVERSAL_OBJECT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Ta Entries</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RADAO__TA_ENTRIES = SaltCorePackage.STRAVERSAL_OBJECT_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Unique Values</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RADAO__UNIQUE_VALUES = SaltCorePackage.STRAVERSAL_OBJECT_FEATURE_COUNT + 3;

	/**
	 * The number of structural features of the '<em>RADAO</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RADAO_FEATURE_COUNT = SaltCorePackage.STRAVERSAL_OBJECT_FEATURE_COUNT + 4;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.RACorpusImpl <em>RA Corpus</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.RACorpusImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRACorpus()
	 * @generated
	 */
	int RA_CORPUS = 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_CORPUS__ID = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_CORPUS__NAME = 1;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_CORPUS__TYPE = 2;

	/**
	 * The feature id for the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_CORPUS__VERSION = 3;

	/**
	 * The feature id for the '<em><b>Pre</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_CORPUS__PRE = 4;

	/**
	 * The feature id for the '<em><b>Post</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_CORPUS__POST = 5;

	/**
	 * The number of structural features of the '<em>RA Corpus</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_CORPUS_FEATURE_COUNT = 6;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.TupleWriterEntryImpl <em>Tuple Writer Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.TupleWriterEntryImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getTupleWriterEntry()
	 * @generated
	 */
	int TUPLE_WRITER_ENTRY = 4;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TUPLE_WRITER_ENTRY__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TUPLE_WRITER_ENTRY__VALUE = 1;

	/**
	 * The number of structural features of the '<em>Tuple Writer Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TUPLE_WRITER_ENTRY_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.TupleWriterContainerImpl <em>Tuple Writer Container</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.TupleWriterContainerImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getTupleWriterContainer()
	 * @generated
	 */
	int TUPLE_WRITER_CONTAINER = 5;

	/**
	 * The feature id for the '<em><b>Tuple Writer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TUPLE_WRITER_CONTAINER__TUPLE_WRITER = 0;

	/**
	 * The number of structural features of the '<em>Tuple Writer Container</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TUPLE_WRITER_CONTAINER_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.TaEntryImpl <em>Ta Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.TaEntryImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getTaEntry()
	 * @generated
	 */
	int TA_ENTRY = 6;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TA_ENTRY__KEY = 0;

	/**
	 * The feature id for the '<em><b>Value</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TA_ENTRY__VALUE = 1;

	/**
	 * The number of structural features of the '<em>Ta Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TA_ENTRY_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.TAObjectImpl <em>TA Object</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.TAObjectImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getTAObject()
	 * @generated
	 */
	int TA_OBJECT = 7;

	/**
	 * The feature id for the '<em><b>Ta Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TA_OBJECT__TA_ID = 0;

	/**
	 * The feature id for the '<em><b>Tw Writer Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TA_OBJECT__TW_WRITER_KEY = 1;

	/**
	 * The number of structural features of the '<em>TA Object</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TA_OBJECT_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.RACorpusAnnotationImpl <em>RA Corpus Annotation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.RACorpusAnnotationImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRACorpusAnnotation()
	 * @generated
	 */
	int RA_CORPUS_ANNOTATION = 8;

	/**
	 * The feature id for the '<em><b>Corpus ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_CORPUS_ANNOTATION__CORPUS_REF = 0;

	/**
	 * The feature id for the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_CORPUS_ANNOTATION__NAMESPACE = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_CORPUS_ANNOTATION__NAME = 2;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_CORPUS_ANNOTATION__VALUE = 3;

	/**
	 * The number of structural features of the '<em>RA Corpus Annotation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_CORPUS_ANNOTATION_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.UniqueValueEntryImpl <em>Unique Value Entry</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.UniqueValueEntryImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getUniqueValueEntry()
	 * @generated
	 */
	int UNIQUE_VALUE_ENTRY = 9;

	/**
	 * The feature id for the '<em><b>Value</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UNIQUE_VALUE_ENTRY__VALUE = 0;

	/**
	 * The feature id for the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UNIQUE_VALUE_ENTRY__KEY = 1;

	/**
	 * The number of structural features of the '<em>Unique Value Entry</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UNIQUE_VALUE_ENTRY_FEATURE_COUNT = 2;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.UniqueValueImpl <em>Unique Value</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.UniqueValueImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getUniqueValue()
	 * @generated
	 */
	int UNIQUE_VALUE = 10;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UNIQUE_VALUE__VALUE = 0;

	/**
	 * The number of structural features of the '<em>Unique Value</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int UNIQUE_VALUE_FEATURE_COUNT = 1;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.RATextImpl <em>RA Text</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.RATextImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRAText()
	 * @generated
	 */
	int RA_TEXT = 11;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_TEXT__ID = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_TEXT__NAME = 1;

	/**
	 * The feature id for the '<em><b>Text</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_TEXT__TEXT = 2;

	/**
	 * The number of structural features of the '<em>RA Text</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_TEXT_FEATURE_COUNT = 3;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.RANodeImpl <em>RA Node</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.RANodeImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRANode()
	 * @generated
	 */
	int RA_NODE = 12;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE__ID = 0;

	/**
	 * The feature id for the '<em><b>Text ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE__TEXT_REF = 1;

	/**
	 * The feature id for the '<em><b>Corpus ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE__CORPUS_REF = 2;

	/**
	 * The feature id for the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE__NAMESPACE = 3;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE__NAME = 4;

	/**
	 * The feature id for the '<em><b>Left</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE__LEFT = 5;

	/**
	 * The feature id for the '<em><b>Right</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE__RIGHT = 6;

	/**
	 * The feature id for the '<em><b>Token index</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE__TOKEN_INDEX = 7;

	/**
	 * The feature id for the '<em><b>Continuous</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE__CONTINUOUS = 8;

	/**
	 * The feature id for the '<em><b>Span</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE__SPAN = 9;

	/**
	 * The number of structural features of the '<em>RA Node</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE_FEATURE_COUNT = 10;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeImpl <em>RA Edge</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.RAEdgeImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRAEdge()
	 * @generated
	 */
	int RA_EDGE = 13;

	/**
	 * The feature id for the '<em><b>Pre</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EDGE__PRE = 0;

	/**
	 * The feature id for the '<em><b>Post</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EDGE__POST = 1;

	/**
	 * The feature id for the '<em><b>Node ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EDGE__NODE_REF = 2;

	/**
	 * The feature id for the '<em><b>Component ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EDGE__COMPONENT_REF = 3;

	/**
	 * The feature id for the '<em><b>Parent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EDGE__PARENT = 4;

	/**
	 * The number of structural features of the '<em>RA Edge</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EDGE_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.RANodeAnnotationImpl <em>RA Node Annotation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.RANodeAnnotationImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRANodeAnnotation()
	 * @generated
	 */
	int RA_NODE_ANNOTATION = 14;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE_ANNOTATION__ID = 0;

	/**
	 * The feature id for the '<em><b>Node ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE_ANNOTATION__NODE_REF = 1;

	/**
	 * The feature id for the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE_ANNOTATION__NAMESPACE = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE_ANNOTATION__NAME = 3;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE_ANNOTATION__VALUE = 4;

	/**
	 * The number of structural features of the '<em>RA Node Annotation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_NODE_ANNOTATION_FEATURE_COUNT = 5;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.RAComponentImpl <em>RA Component</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.RAComponentImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRAComponent()
	 * @generated
	 */
	int RA_COMPONENT = 15;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_COMPONENT__ID = 0;

	/**
	 * The feature id for the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_COMPONENT__TYPE = 1;

	/**
	 * The feature id for the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_COMPONENT__NAMESPACE = 2;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_COMPONENT__NAME = 3;

	/**
	 * The number of structural features of the '<em>RA Component</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_COMPONENT_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeAnnotationImpl <em>RA Edge Annotation</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.impl.RAEdgeAnnotationImpl
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRAEdgeAnnotation()
	 * @generated
	 */
	int RA_EDGE_ANNOTATION = 16;

	/**
	 * The feature id for the '<em><b>Edge ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EDGE_ANNOTATION__EDGE_REF = 0;

	/**
	 * The feature id for the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EDGE_ANNOTATION__NAMESPACE = 1;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EDGE_ANNOTATION__NAME = 2;

	/**
	 * The feature id for the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EDGE_ANNOTATION__VALUE = 3;

	/**
	 * The number of structural features of the '<em>RA Edge Annotation</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int RA_EDGE_ANNOTATION_FEATURE_COUNT = 4;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE <em>RA CORPUS TYPE</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRA_CORPUS_TYPE()
	 * @generated
	 */
	int RA_CORPUS_TYPE = 17;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.EXPORT_FILE <em>EXPORT FILE</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.EXPORT_FILE
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getEXPORT_FILE()
	 * @generated
	 */
	int EXPORT_FILE = 18;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.DAOOBJECT <em>DAOOBJECT</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.DAOOBJECT
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getDAOOBJECT()
	 * @generated
	 */
	int DAOOBJECT = 19;

	/**
	 * The meta object id for the '{@link de.corpling.peper.modules.relANNIS.UNIQUE_VALUES <em>UNIQUE VALUES</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.peper.modules.relANNIS.UNIQUE_VALUES
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getUNIQUE_VALUES()
	 * @generated
	 */
	int UNIQUE_VALUES = 20;

	/**
	 * The meta object id for the '<em>Salt Project</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.salt.saltFW.SaltProject
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getSaltProject()
	 * @generated
	 */
	int SALT_PROJECT = 21;


	/**
	 * The meta object id for the '<em>File</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.io.File
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getFile()
	 * @generated
	 */
	int FILE = 22;


	/**
	 * The meta object id for the '<em>Tuple Writer</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.dataconnector.tupleconnector.ITupleWriter
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getTupleWriter()
	 * @generated
	 */
	int TUPLE_WRITER = 23;


	/**
	 * The meta object id for the '<em>SElement</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see de.corpling.salt.model.saltCore.SElement
	 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getSElement()
	 * @generated
	 */
	int SELEMENT = 24;


	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.RAExporter <em>RA Exporter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>RA Exporter</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAExporter
	 * @generated
	 */
	EClass getRAExporter();

	/**
	 * Returns the meta object for the reference '{@link de.corpling.peper.modules.relANNIS.RAExporter#getRaMapper <em>Ra Mapper</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Ra Mapper</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAExporter#getRaMapper()
	 * @see #getRAExporter()
	 * @generated
	 */
	EReference getRAExporter_RaMapper();

	/**
	 * Returns the meta object for the containment reference '{@link de.corpling.peper.modules.relANNIS.RAExporter#getRaDAO <em>Ra DAO</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Ra DAO</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAExporter#getRaDAO()
	 * @see #getRAExporter()
	 * @generated
	 */
	EReference getRAExporter_RaDAO();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.RAMapper <em>RA Mapper</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>RA Mapper</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAMapper
	 * @generated
	 */
	EClass getRAMapper();

	/**
	 * Returns the meta object for the reference '{@link de.corpling.peper.modules.relANNIS.RAMapper#getRaDAO <em>Ra DAO</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Ra DAO</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAMapper#getRaDAO()
	 * @see #getRAMapper()
	 * @generated
	 */
	EReference getRAMapper_RaDAO();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAMapper#getCurrTaId <em>Curr Ta Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Curr Ta Id</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAMapper#getCurrTaId()
	 * @see #getRAMapper()
	 * @generated
	 */
	EAttribute getRAMapper_CurrTaId();

	/**
	 * Returns the meta object for the reference '{@link de.corpling.peper.modules.relANNIS.RAMapper#getRaExporter <em>Ra Exporter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Ra Exporter</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAMapper#getRaExporter()
	 * @see #getRAMapper()
	 * @generated
	 */
	EReference getRAMapper_RaExporter();

	/**
	 * Returns the meta object for the attribute list '{@link de.corpling.peper.modules.relANNIS.RAMapper#getCoherentComponents <em>Coherent Components</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Coherent Components</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAMapper#getCoherentComponents()
	 * @see #getRAMapper()
	 * @generated
	 */
	EAttribute getRAMapper_CoherentComponents();

	/**
	 * Returns the meta object for the attribute list '{@link de.corpling.peper.modules.relANNIS.RAMapper#getSubConnectedComponents <em>Sub Connected Components</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Sub Connected Components</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAMapper#getSubConnectedComponents()
	 * @see #getRAMapper()
	 * @generated
	 */
	EAttribute getRAMapper_SubConnectedComponents();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.RADAO <em>RADAO</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>RADAO</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RADAO
	 * @generated
	 */
	EClass getRADAO();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RADAO#getOutputDir <em>Output Dir</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Output Dir</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RADAO#getOutputDir()
	 * @see #getRADAO()
	 * @generated
	 */
	EAttribute getRADAO_OutputDir();

	/**
	 * Returns the meta object for the map '{@link de.corpling.peper.modules.relANNIS.RADAO#getTupleWriterEntries <em>Tuple Writer Entries</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Tuple Writer Entries</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RADAO#getTupleWriterEntries()
	 * @see #getRADAO()
	 * @generated
	 */
	EReference getRADAO_TupleWriterEntries();

	/**
	 * Returns the meta object for the map '{@link de.corpling.peper.modules.relANNIS.RADAO#getTaEntries <em>Ta Entries</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Ta Entries</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RADAO#getTaEntries()
	 * @see #getRADAO()
	 * @generated
	 */
	EReference getRADAO_TaEntries();

	/**
	 * Returns the meta object for the map '{@link de.corpling.peper.modules.relANNIS.RADAO#getUniqueValues <em>Unique Values</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the map '<em>Unique Values</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RADAO#getUniqueValues()
	 * @see #getRADAO()
	 * @generated
	 */
	EReference getRADAO_UniqueValues();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.RACorpus <em>RA Corpus</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>RA Corpus</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RACorpus
	 * @generated
	 */
	EClass getRACorpus();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RACorpus#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RACorpus#getId()
	 * @see #getRACorpus()
	 * @generated
	 */
	EAttribute getRACorpus_Id();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RACorpus#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RACorpus#getName()
	 * @see #getRACorpus()
	 * @generated
	 */
	EAttribute getRACorpus_Name();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RACorpus#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RACorpus#getType()
	 * @see #getRACorpus()
	 * @generated
	 */
	EAttribute getRACorpus_Type();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RACorpus#getVersion <em>Version</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Version</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RACorpus#getVersion()
	 * @see #getRACorpus()
	 * @generated
	 */
	EAttribute getRACorpus_Version();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RACorpus#getPre <em>Pre</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pre</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RACorpus#getPre()
	 * @see #getRACorpus()
	 * @generated
	 */
	EAttribute getRACorpus_Pre();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RACorpus#getPost <em>Post</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Post</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RACorpus#getPost()
	 * @see #getRACorpus()
	 * @generated
	 */
	EAttribute getRACorpus_Post();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>Tuple Writer Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tuple Writer Entry</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="de.corpling.peper.modules.relANNIS.DAOOBJECT"
	 *        valueType="de.corpling.peper.modules.relANNIS.TupleWriterContainer" valueContainment="true"
	 * @generated
	 */
	EClass getTupleWriterEntry();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getTupleWriterEntry()
	 * @generated
	 */
	EAttribute getTupleWriterEntry_Key();

	/**
	 * Returns the meta object for the containment reference '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getTupleWriterEntry()
	 * @generated
	 */
	EReference getTupleWriterEntry_Value();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.TupleWriterContainer <em>Tuple Writer Container</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Tuple Writer Container</em>'.
	 * @see de.corpling.peper.modules.relANNIS.TupleWriterContainer
	 * @generated
	 */
	EClass getTupleWriterContainer();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.TupleWriterContainer#getTupleWriter <em>Tuple Writer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tuple Writer</em>'.
	 * @see de.corpling.peper.modules.relANNIS.TupleWriterContainer#getTupleWriter()
	 * @see #getTupleWriterContainer()
	 * @generated
	 */
	EAttribute getTupleWriterContainer_TupleWriter();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>Ta Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Ta Entry</em>'.
	 * @see java.util.Map.Entry
	 * @model keyDataType="org.eclipse.emf.ecore.ELongObject"
	 *        valueType="de.corpling.peper.modules.relANNIS.TAObject" valueContainment="true" valueMany="true"
	 * @generated
	 */
	EClass getTaEntry();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getTaEntry()
	 * @generated
	 */
	EAttribute getTaEntry_Key();

	/**
	 * Returns the meta object for the containment reference list '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getTaEntry()
	 * @generated
	 */
	EReference getTaEntry_Value();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.TAObject <em>TA Object</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>TA Object</em>'.
	 * @see de.corpling.peper.modules.relANNIS.TAObject
	 * @generated
	 */
	EClass getTAObject();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.TAObject#getTaId <em>Ta Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Ta Id</em>'.
	 * @see de.corpling.peper.modules.relANNIS.TAObject#getTaId()
	 * @see #getTAObject()
	 * @generated
	 */
	EAttribute getTAObject_TaId();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.TAObject#getTwWriterKey <em>Tw Writer Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Tw Writer Key</em>'.
	 * @see de.corpling.peper.modules.relANNIS.TAObject#getTwWriterKey()
	 * @see #getTAObject()
	 * @generated
	 */
	EAttribute getTAObject_TwWriterKey();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation <em>RA Corpus Annotation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>RA Corpus Annotation</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RACorpusAnnotation
	 * @generated
	 */
	EClass getRACorpusAnnotation();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getCorpus_ref <em>Corpus ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Corpus ref</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getCorpus_ref()
	 * @see #getRACorpusAnnotation()
	 * @generated
	 */
	EAttribute getRACorpusAnnotation_Corpus_ref();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getNamespace <em>Namespace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Namespace</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getNamespace()
	 * @see #getRACorpusAnnotation()
	 * @generated
	 */
	EAttribute getRACorpusAnnotation_Namespace();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getName()
	 * @see #getRACorpusAnnotation()
	 * @generated
	 */
	EAttribute getRACorpusAnnotation_Name();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getValue()
	 * @see #getRACorpusAnnotation()
	 * @generated
	 */
	EAttribute getRACorpusAnnotation_Value();

	/**
	 * Returns the meta object for class '{@link java.util.Map.Entry <em>Unique Value Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Unique Value Entry</em>'.
	 * @see java.util.Map.Entry
	 * @model features="value key" 
	 *        valueType="de.corpling.peper.modules.relANNIS.UniqueValue"
	 *        keyDataType="de.corpling.peper.modules.relANNIS.UNIQUE_VALUES"
	 * @generated
	 */
	EClass getUniqueValueEntry();

	/**
	 * Returns the meta object for the reference '{@link java.util.Map.Entry <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Value</em>'.
	 * @see java.util.Map.Entry
	 * @see #getUniqueValueEntry()
	 * @generated
	 */
	EReference getUniqueValueEntry_Value();

	/**
	 * Returns the meta object for the attribute '{@link java.util.Map.Entry <em>Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Key</em>'.
	 * @see java.util.Map.Entry
	 * @see #getUniqueValueEntry()
	 * @generated
	 */
	EAttribute getUniqueValueEntry_Key();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.UniqueValue <em>Unique Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Unique Value</em>'.
	 * @see de.corpling.peper.modules.relANNIS.UniqueValue
	 * @generated
	 */
	EClass getUniqueValue();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.UniqueValue#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see de.corpling.peper.modules.relANNIS.UniqueValue#getValue()
	 * @see #getUniqueValue()
	 * @generated
	 */
	EAttribute getUniqueValue_Value();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.RAText <em>RA Text</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>RA Text</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAText
	 * @generated
	 */
	EClass getRAText();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAText#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAText#getId()
	 * @see #getRAText()
	 * @generated
	 */
	EAttribute getRAText_Id();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAText#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAText#getName()
	 * @see #getRAText()
	 * @generated
	 */
	EAttribute getRAText_Name();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAText#getText <em>Text</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Text</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAText#getText()
	 * @see #getRAText()
	 * @generated
	 */
	EAttribute getRAText_Text();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.RANode <em>RA Node</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>RA Node</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANode
	 * @generated
	 */
	EClass getRANode();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANode#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANode#getId()
	 * @see #getRANode()
	 * @generated
	 */
	EAttribute getRANode_Id();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANode#getText_ref <em>Text ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Text ref</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANode#getText_ref()
	 * @see #getRANode()
	 * @generated
	 */
	EAttribute getRANode_Text_ref();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANode#getCorpus_ref <em>Corpus ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Corpus ref</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANode#getCorpus_ref()
	 * @see #getRANode()
	 * @generated
	 */
	EAttribute getRANode_Corpus_ref();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANode#getNamespace <em>Namespace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Namespace</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANode#getNamespace()
	 * @see #getRANode()
	 * @generated
	 */
	EAttribute getRANode_Namespace();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANode#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANode#getName()
	 * @see #getRANode()
	 * @generated
	 */
	EAttribute getRANode_Name();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANode#getLeft <em>Left</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Left</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANode#getLeft()
	 * @see #getRANode()
	 * @generated
	 */
	EAttribute getRANode_Left();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANode#getRight <em>Right</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Right</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANode#getRight()
	 * @see #getRANode()
	 * @generated
	 */
	EAttribute getRANode_Right();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANode#getToken_index <em>Token index</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Token index</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANode#getToken_index()
	 * @see #getRANode()
	 * @generated
	 */
	EAttribute getRANode_Token_index();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANode#isContinuous <em>Continuous</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Continuous</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANode#isContinuous()
	 * @see #getRANode()
	 * @generated
	 */
	EAttribute getRANode_Continuous();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANode#getSpan <em>Span</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Span</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANode#getSpan()
	 * @see #getRANode()
	 * @generated
	 */
	EAttribute getRANode_Span();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.RAEdge <em>RA Edge</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>RA Edge</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAEdge
	 * @generated
	 */
	EClass getRAEdge();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAEdge#getPre <em>Pre</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pre</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAEdge#getPre()
	 * @see #getRAEdge()
	 * @generated
	 */
	EAttribute getRAEdge_Pre();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAEdge#getPost <em>Post</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Post</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAEdge#getPost()
	 * @see #getRAEdge()
	 * @generated
	 */
	EAttribute getRAEdge_Post();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAEdge#getNode_ref <em>Node ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ref</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAEdge#getNode_ref()
	 * @see #getRAEdge()
	 * @generated
	 */
	EAttribute getRAEdge_Node_ref();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAEdge#getComponent_ref <em>Component ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Component ref</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAEdge#getComponent_ref()
	 * @see #getRAEdge()
	 * @generated
	 */
	EAttribute getRAEdge_Component_ref();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAEdge#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Parent</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAEdge#getParent()
	 * @see #getRAEdge()
	 * @generated
	 */
	EAttribute getRAEdge_Parent();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.RANodeAnnotation <em>RA Node Annotation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>RA Node Annotation</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANodeAnnotation
	 * @generated
	 */
	EClass getRANodeAnnotation();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANodeAnnotation#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANodeAnnotation#getId()
	 * @see #getRANodeAnnotation()
	 * @generated
	 */
	EAttribute getRANodeAnnotation_Id();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANodeAnnotation#getNode_ref <em>Node ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Node ref</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANodeAnnotation#getNode_ref()
	 * @see #getRANodeAnnotation()
	 * @generated
	 */
	EAttribute getRANodeAnnotation_Node_ref();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANodeAnnotation#getNamespace <em>Namespace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Namespace</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANodeAnnotation#getNamespace()
	 * @see #getRANodeAnnotation()
	 * @generated
	 */
	EAttribute getRANodeAnnotation_Namespace();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANodeAnnotation#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANodeAnnotation#getName()
	 * @see #getRANodeAnnotation()
	 * @generated
	 */
	EAttribute getRANodeAnnotation_Name();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RANodeAnnotation#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RANodeAnnotation#getValue()
	 * @see #getRANodeAnnotation()
	 * @generated
	 */
	EAttribute getRANodeAnnotation_Value();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.RAComponent <em>RA Component</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>RA Component</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAComponent
	 * @generated
	 */
	EClass getRAComponent();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAComponent#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAComponent#getId()
	 * @see #getRAComponent()
	 * @generated
	 */
	EAttribute getRAComponent_Id();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAComponent#getType <em>Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Type</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAComponent#getType()
	 * @see #getRAComponent()
	 * @generated
	 */
	EAttribute getRAComponent_Type();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAComponent#getNamespace <em>Namespace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Namespace</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAComponent#getNamespace()
	 * @see #getRAComponent()
	 * @generated
	 */
	EAttribute getRAComponent_Namespace();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAComponent#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAComponent#getName()
	 * @see #getRAComponent()
	 * @generated
	 */
	EAttribute getRAComponent_Name();

	/**
	 * Returns the meta object for class '{@link de.corpling.peper.modules.relANNIS.RAEdgeAnnotation <em>RA Edge Annotation</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>RA Edge Annotation</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAEdgeAnnotation
	 * @generated
	 */
	EClass getRAEdgeAnnotation();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAEdgeAnnotation#getEdge_ref <em>Edge ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Edge ref</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAEdgeAnnotation#getEdge_ref()
	 * @see #getRAEdgeAnnotation()
	 * @generated
	 */
	EAttribute getRAEdgeAnnotation_Edge_ref();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAEdgeAnnotation#getNamespace <em>Namespace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Namespace</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAEdgeAnnotation#getNamespace()
	 * @see #getRAEdgeAnnotation()
	 * @generated
	 */
	EAttribute getRAEdgeAnnotation_Namespace();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAEdgeAnnotation#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAEdgeAnnotation#getName()
	 * @see #getRAEdgeAnnotation()
	 * @generated
	 */
	EAttribute getRAEdgeAnnotation_Name();

	/**
	 * Returns the meta object for the attribute '{@link de.corpling.peper.modules.relANNIS.RAEdgeAnnotation#getValue <em>Value</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Value</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RAEdgeAnnotation#getValue()
	 * @see #getRAEdgeAnnotation()
	 * @generated
	 */
	EAttribute getRAEdgeAnnotation_Value();

	/**
	 * Returns the meta object for enum '{@link de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE <em>RA CORPUS TYPE</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>RA CORPUS TYPE</em>'.
	 * @see de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE
	 * @generated
	 */
	EEnum getRA_CORPUS_TYPE();

	/**
	 * Returns the meta object for enum '{@link de.corpling.peper.modules.relANNIS.EXPORT_FILE <em>EXPORT FILE</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>EXPORT FILE</em>'.
	 * @see de.corpling.peper.modules.relANNIS.EXPORT_FILE
	 * @generated
	 */
	EEnum getEXPORT_FILE();

	/**
	 * Returns the meta object for enum '{@link de.corpling.peper.modules.relANNIS.DAOOBJECT <em>DAOOBJECT</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>DAOOBJECT</em>'.
	 * @see de.corpling.peper.modules.relANNIS.DAOOBJECT
	 * @generated
	 */
	EEnum getDAOOBJECT();

	/**
	 * Returns the meta object for enum '{@link de.corpling.peper.modules.relANNIS.UNIQUE_VALUES <em>UNIQUE VALUES</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>UNIQUE VALUES</em>'.
	 * @see de.corpling.peper.modules.relANNIS.UNIQUE_VALUES
	 * @generated
	 */
	EEnum getUNIQUE_VALUES();

	/**
	 * Returns the meta object for data type '{@link de.corpling.salt.saltFW.SaltProject <em>Salt Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Salt Project</em>'.
	 * @see de.corpling.salt.saltFW.SaltProject
	 * @model instanceClass="de.corpling.salt.saltFW.SaltProject"
	 * @generated
	 */
	EDataType getSaltProject();

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
	 * Returns the meta object for data type '{@link de.dataconnector.tupleconnector.ITupleWriter <em>Tuple Writer</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>Tuple Writer</em>'.
	 * @see de.dataconnector.tupleconnector.ITupleWriter
	 * @model instanceClass="de.dataconnector.tupleconnector.ITupleWriter"
	 * @generated
	 */
	EDataType getTupleWriter();

	/**
	 * Returns the meta object for data type '{@link de.corpling.salt.model.saltCore.SElement <em>SElement</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for data type '<em>SElement</em>'.
	 * @see de.corpling.salt.model.saltCore.SElement
	 * @model instanceClass="de.corpling.salt.model.saltCore.SElement"
	 * @generated
	 */
	EDataType getSElement();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	RelANNISFactory getRelANNISFactory();

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
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.RAExporterImpl <em>RA Exporter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.RAExporterImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRAExporter()
		 * @generated
		 */
		EClass RA_EXPORTER = eINSTANCE.getRAExporter();
		/**
		 * The meta object literal for the '<em><b>Ra Mapper</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RA_EXPORTER__RA_MAPPER = eINSTANCE.getRAExporter_RaMapper();
		/**
		 * The meta object literal for the '<em><b>Ra DAO</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RA_EXPORTER__RA_DAO = eINSTANCE.getRAExporter_RaDAO();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.RAMapperImpl <em>RA Mapper</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.RAMapperImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRAMapper()
		 * @generated
		 */
		EClass RA_MAPPER = eINSTANCE.getRAMapper();
		/**
		 * The meta object literal for the '<em><b>Ra DAO</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RA_MAPPER__RA_DAO = eINSTANCE.getRAMapper_RaDAO();
		/**
		 * The meta object literal for the '<em><b>Curr Ta Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_MAPPER__CURR_TA_ID = eINSTANCE.getRAMapper_CurrTaId();
		/**
		 * The meta object literal for the '<em><b>Ra Exporter</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RA_MAPPER__RA_EXPORTER = eINSTANCE.getRAMapper_RaExporter();
		/**
		 * The meta object literal for the '<em><b>Coherent Components</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_MAPPER__COHERENT_COMPONENTS = eINSTANCE.getRAMapper_CoherentComponents();
		/**
		 * The meta object literal for the '<em><b>Sub Connected Components</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_MAPPER__SUB_CONNECTED_COMPONENTS = eINSTANCE.getRAMapper_SubConnectedComponents();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.RADAOImpl <em>RADAO</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.RADAOImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRADAO()
		 * @generated
		 */
		EClass RADAO = eINSTANCE.getRADAO();
		/**
		 * The meta object literal for the '<em><b>Output Dir</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RADAO__OUTPUT_DIR = eINSTANCE.getRADAO_OutputDir();
		/**
		 * The meta object literal for the '<em><b>Tuple Writer Entries</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RADAO__TUPLE_WRITER_ENTRIES = eINSTANCE.getRADAO_TupleWriterEntries();
		/**
		 * The meta object literal for the '<em><b>Ta Entries</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RADAO__TA_ENTRIES = eINSTANCE.getRADAO_TaEntries();
		/**
		 * The meta object literal for the '<em><b>Unique Values</b></em>' map feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference RADAO__UNIQUE_VALUES = eINSTANCE.getRADAO_UniqueValues();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.RACorpusImpl <em>RA Corpus</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.RACorpusImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRACorpus()
		 * @generated
		 */
		EClass RA_CORPUS = eINSTANCE.getRACorpus();
		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_CORPUS__ID = eINSTANCE.getRACorpus_Id();
		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_CORPUS__NAME = eINSTANCE.getRACorpus_Name();
		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_CORPUS__TYPE = eINSTANCE.getRACorpus_Type();
		/**
		 * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_CORPUS__VERSION = eINSTANCE.getRACorpus_Version();
		/**
		 * The meta object literal for the '<em><b>Pre</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_CORPUS__PRE = eINSTANCE.getRACorpus_Pre();
		/**
		 * The meta object literal for the '<em><b>Post</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_CORPUS__POST = eINSTANCE.getRACorpus_Post();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.TupleWriterEntryImpl <em>Tuple Writer Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.TupleWriterEntryImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getTupleWriterEntry()
		 * @generated
		 */
		EClass TUPLE_WRITER_ENTRY = eINSTANCE.getTupleWriterEntry();
		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TUPLE_WRITER_ENTRY__KEY = eINSTANCE.getTupleWriterEntry_Key();
		/**
		 * The meta object literal for the '<em><b>Value</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TUPLE_WRITER_ENTRY__VALUE = eINSTANCE.getTupleWriterEntry_Value();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.TupleWriterContainerImpl <em>Tuple Writer Container</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.TupleWriterContainerImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getTupleWriterContainer()
		 * @generated
		 */
		EClass TUPLE_WRITER_CONTAINER = eINSTANCE.getTupleWriterContainer();
		/**
		 * The meta object literal for the '<em><b>Tuple Writer</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TUPLE_WRITER_CONTAINER__TUPLE_WRITER = eINSTANCE.getTupleWriterContainer_TupleWriter();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.TaEntryImpl <em>Ta Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.TaEntryImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getTaEntry()
		 * @generated
		 */
		EClass TA_ENTRY = eINSTANCE.getTaEntry();
		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TA_ENTRY__KEY = eINSTANCE.getTaEntry_Key();
		/**
		 * The meta object literal for the '<em><b>Value</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TA_ENTRY__VALUE = eINSTANCE.getTaEntry_Value();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.TAObjectImpl <em>TA Object</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.TAObjectImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getTAObject()
		 * @generated
		 */
		EClass TA_OBJECT = eINSTANCE.getTAObject();
		/**
		 * The meta object literal for the '<em><b>Ta Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TA_OBJECT__TA_ID = eINSTANCE.getTAObject_TaId();
		/**
		 * The meta object literal for the '<em><b>Tw Writer Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TA_OBJECT__TW_WRITER_KEY = eINSTANCE.getTAObject_TwWriterKey();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.RACorpusAnnotationImpl <em>RA Corpus Annotation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.RACorpusAnnotationImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRACorpusAnnotation()
		 * @generated
		 */
		EClass RA_CORPUS_ANNOTATION = eINSTANCE.getRACorpusAnnotation();
		/**
		 * The meta object literal for the '<em><b>Corpus ref</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_CORPUS_ANNOTATION__CORPUS_REF = eINSTANCE.getRACorpusAnnotation_Corpus_ref();
		/**
		 * The meta object literal for the '<em><b>Namespace</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_CORPUS_ANNOTATION__NAMESPACE = eINSTANCE.getRACorpusAnnotation_Namespace();
		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_CORPUS_ANNOTATION__NAME = eINSTANCE.getRACorpusAnnotation_Name();
		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_CORPUS_ANNOTATION__VALUE = eINSTANCE.getRACorpusAnnotation_Value();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.UniqueValueEntryImpl <em>Unique Value Entry</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.UniqueValueEntryImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getUniqueValueEntry()
		 * @generated
		 */
		EClass UNIQUE_VALUE_ENTRY = eINSTANCE.getUniqueValueEntry();
		/**
		 * The meta object literal for the '<em><b>Value</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference UNIQUE_VALUE_ENTRY__VALUE = eINSTANCE.getUniqueValueEntry_Value();
		/**
		 * The meta object literal for the '<em><b>Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UNIQUE_VALUE_ENTRY__KEY = eINSTANCE.getUniqueValueEntry_Key();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.UniqueValueImpl <em>Unique Value</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.UniqueValueImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getUniqueValue()
		 * @generated
		 */
		EClass UNIQUE_VALUE = eINSTANCE.getUniqueValue();
		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute UNIQUE_VALUE__VALUE = eINSTANCE.getUniqueValue_Value();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.RATextImpl <em>RA Text</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.RATextImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRAText()
		 * @generated
		 */
		EClass RA_TEXT = eINSTANCE.getRAText();
		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_TEXT__ID = eINSTANCE.getRAText_Id();
		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_TEXT__NAME = eINSTANCE.getRAText_Name();
		/**
		 * The meta object literal for the '<em><b>Text</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_TEXT__TEXT = eINSTANCE.getRAText_Text();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.RANodeImpl <em>RA Node</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.RANodeImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRANode()
		 * @generated
		 */
		EClass RA_NODE = eINSTANCE.getRANode();
		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE__ID = eINSTANCE.getRANode_Id();
		/**
		 * The meta object literal for the '<em><b>Text ref</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE__TEXT_REF = eINSTANCE.getRANode_Text_ref();
		/**
		 * The meta object literal for the '<em><b>Corpus ref</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE__CORPUS_REF = eINSTANCE.getRANode_Corpus_ref();
		/**
		 * The meta object literal for the '<em><b>Namespace</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE__NAMESPACE = eINSTANCE.getRANode_Namespace();
		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE__NAME = eINSTANCE.getRANode_Name();
		/**
		 * The meta object literal for the '<em><b>Left</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE__LEFT = eINSTANCE.getRANode_Left();
		/**
		 * The meta object literal for the '<em><b>Right</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE__RIGHT = eINSTANCE.getRANode_Right();
		/**
		 * The meta object literal for the '<em><b>Token index</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE__TOKEN_INDEX = eINSTANCE.getRANode_Token_index();
		/**
		 * The meta object literal for the '<em><b>Continuous</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE__CONTINUOUS = eINSTANCE.getRANode_Continuous();
		/**
		 * The meta object literal for the '<em><b>Span</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE__SPAN = eINSTANCE.getRANode_Span();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeImpl <em>RA Edge</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.RAEdgeImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRAEdge()
		 * @generated
		 */
		EClass RA_EDGE = eINSTANCE.getRAEdge();
		/**
		 * The meta object literal for the '<em><b>Pre</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_EDGE__PRE = eINSTANCE.getRAEdge_Pre();
		/**
		 * The meta object literal for the '<em><b>Post</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_EDGE__POST = eINSTANCE.getRAEdge_Post();
		/**
		 * The meta object literal for the '<em><b>Node ref</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_EDGE__NODE_REF = eINSTANCE.getRAEdge_Node_ref();
		/**
		 * The meta object literal for the '<em><b>Component ref</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_EDGE__COMPONENT_REF = eINSTANCE.getRAEdge_Component_ref();
		/**
		 * The meta object literal for the '<em><b>Parent</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_EDGE__PARENT = eINSTANCE.getRAEdge_Parent();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.RANodeAnnotationImpl <em>RA Node Annotation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.RANodeAnnotationImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRANodeAnnotation()
		 * @generated
		 */
		EClass RA_NODE_ANNOTATION = eINSTANCE.getRANodeAnnotation();
		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE_ANNOTATION__ID = eINSTANCE.getRANodeAnnotation_Id();
		/**
		 * The meta object literal for the '<em><b>Node ref</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE_ANNOTATION__NODE_REF = eINSTANCE.getRANodeAnnotation_Node_ref();
		/**
		 * The meta object literal for the '<em><b>Namespace</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE_ANNOTATION__NAMESPACE = eINSTANCE.getRANodeAnnotation_Namespace();
		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE_ANNOTATION__NAME = eINSTANCE.getRANodeAnnotation_Name();
		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_NODE_ANNOTATION__VALUE = eINSTANCE.getRANodeAnnotation_Value();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.RAComponentImpl <em>RA Component</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.RAComponentImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRAComponent()
		 * @generated
		 */
		EClass RA_COMPONENT = eINSTANCE.getRAComponent();
		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_COMPONENT__ID = eINSTANCE.getRAComponent_Id();
		/**
		 * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_COMPONENT__TYPE = eINSTANCE.getRAComponent_Type();
		/**
		 * The meta object literal for the '<em><b>Namespace</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_COMPONENT__NAMESPACE = eINSTANCE.getRAComponent_Namespace();
		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_COMPONENT__NAME = eINSTANCE.getRAComponent_Name();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeAnnotationImpl <em>RA Edge Annotation</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.impl.RAEdgeAnnotationImpl
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRAEdgeAnnotation()
		 * @generated
		 */
		EClass RA_EDGE_ANNOTATION = eINSTANCE.getRAEdgeAnnotation();
		/**
		 * The meta object literal for the '<em><b>Edge ref</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_EDGE_ANNOTATION__EDGE_REF = eINSTANCE.getRAEdgeAnnotation_Edge_ref();
		/**
		 * The meta object literal for the '<em><b>Namespace</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_EDGE_ANNOTATION__NAMESPACE = eINSTANCE.getRAEdgeAnnotation_Namespace();
		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_EDGE_ANNOTATION__NAME = eINSTANCE.getRAEdgeAnnotation_Name();
		/**
		 * The meta object literal for the '<em><b>Value</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute RA_EDGE_ANNOTATION__VALUE = eINSTANCE.getRAEdgeAnnotation_Value();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE <em>RA CORPUS TYPE</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getRA_CORPUS_TYPE()
		 * @generated
		 */
		EEnum RA_CORPUS_TYPE = eINSTANCE.getRA_CORPUS_TYPE();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.EXPORT_FILE <em>EXPORT FILE</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.EXPORT_FILE
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getEXPORT_FILE()
		 * @generated
		 */
		EEnum EXPORT_FILE = eINSTANCE.getEXPORT_FILE();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.DAOOBJECT <em>DAOOBJECT</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.DAOOBJECT
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getDAOOBJECT()
		 * @generated
		 */
		EEnum DAOOBJECT = eINSTANCE.getDAOOBJECT();
		/**
		 * The meta object literal for the '{@link de.corpling.peper.modules.relANNIS.UNIQUE_VALUES <em>UNIQUE VALUES</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.peper.modules.relANNIS.UNIQUE_VALUES
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getUNIQUE_VALUES()
		 * @generated
		 */
		EEnum UNIQUE_VALUES = eINSTANCE.getUNIQUE_VALUES();
		/**
		 * The meta object literal for the '<em>Salt Project</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.salt.saltFW.SaltProject
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getSaltProject()
		 * @generated
		 */
		EDataType SALT_PROJECT = eINSTANCE.getSaltProject();
		/**
		 * The meta object literal for the '<em>File</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.io.File
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getFile()
		 * @generated
		 */
		EDataType FILE = eINSTANCE.getFile();
		/**
		 * The meta object literal for the '<em>Tuple Writer</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.dataconnector.tupleconnector.ITupleWriter
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getTupleWriter()
		 * @generated
		 */
		EDataType TUPLE_WRITER = eINSTANCE.getTupleWriter();
		/**
		 * The meta object literal for the '<em>SElement</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see de.corpling.salt.model.saltCore.SElement
		 * @see de.corpling.peper.modules.relANNIS.impl.RelANNISPackageImpl#getSElement()
		 * @generated
		 */
		EDataType SELEMENT = eINSTANCE.getSElement();

	}

} //RelANNISPackage
