/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.impl;

import de.corpling.peper.modules.relANNIS.*;

import de.corpling.salt.model.saltCore.SElement;

import de.corpling.salt.saltFW.SaltProject;

import de.dataconnector.tupleconnector.ITupleWriter;

import java.io.File;

import java.util.Map;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class RelANNISFactoryImpl extends EFactoryImpl implements RelANNISFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static RelANNISFactory init() {
		try {
			RelANNISFactory theRelANNISFactory = (RelANNISFactory)EPackage.Registry.INSTANCE.getEFactory("relANNIS"); 
			if (theRelANNISFactory != null) {
				return theRelANNISFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new RelANNISFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RelANNISFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case RelANNISPackage.RA_EXPORTER: return createRAExporter();
			case RelANNISPackage.RA_MAPPER: return createRAMapper();
			case RelANNISPackage.RADAO: return createRADAO();
			case RelANNISPackage.RA_CORPUS: return createRACorpus();
			case RelANNISPackage.TUPLE_WRITER_ENTRY: return (EObject)createTupleWriterEntry();
			case RelANNISPackage.TUPLE_WRITER_CONTAINER: return createTupleWriterContainer();
			case RelANNISPackage.TA_ENTRY: return (EObject)createTaEntry();
			case RelANNISPackage.TA_OBJECT: return createTAObject();
			case RelANNISPackage.RA_CORPUS_ANNOTATION: return createRACorpusAnnotation();
			case RelANNISPackage.UNIQUE_VALUE_ENTRY: return (EObject)createUniqueValueEntry();
			case RelANNISPackage.UNIQUE_VALUE: return createUniqueValue();
			case RelANNISPackage.RA_TEXT: return createRAText();
			case RelANNISPackage.RA_NODE: return createRANode();
			case RelANNISPackage.RA_EDGE: return createRAEdge();
			case RelANNISPackage.RA_NODE_ANNOTATION: return createRANodeAnnotation();
			case RelANNISPackage.RA_COMPONENT: return createRAComponent();
			case RelANNISPackage.RA_EDGE_ANNOTATION: return createRAEdgeAnnotation();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case RelANNISPackage.RA_CORPUS_TYPE:
				return createRA_CORPUS_TYPEFromString(eDataType, initialValue);
			case RelANNISPackage.EXPORT_FILE:
				return createEXPORT_FILEFromString(eDataType, initialValue);
			case RelANNISPackage.DAOOBJECT:
				return createDAOOBJECTFromString(eDataType, initialValue);
			case RelANNISPackage.UNIQUE_VALUES:
				return createUNIQUE_VALUESFromString(eDataType, initialValue);
			case RelANNISPackage.SALT_PROJECT:
				return createSaltProjectFromString(eDataType, initialValue);
			case RelANNISPackage.FILE:
				return createFileFromString(eDataType, initialValue);
			case RelANNISPackage.TUPLE_WRITER:
				return createTupleWriterFromString(eDataType, initialValue);
			case RelANNISPackage.SELEMENT:
				return createSElementFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case RelANNISPackage.RA_CORPUS_TYPE:
				return convertRA_CORPUS_TYPEToString(eDataType, instanceValue);
			case RelANNISPackage.EXPORT_FILE:
				return convertEXPORT_FILEToString(eDataType, instanceValue);
			case RelANNISPackage.DAOOBJECT:
				return convertDAOOBJECTToString(eDataType, instanceValue);
			case RelANNISPackage.UNIQUE_VALUES:
				return convertUNIQUE_VALUESToString(eDataType, instanceValue);
			case RelANNISPackage.SALT_PROJECT:
				return convertSaltProjectToString(eDataType, instanceValue);
			case RelANNISPackage.FILE:
				return convertFileToString(eDataType, instanceValue);
			case RelANNISPackage.TUPLE_WRITER:
				return convertTupleWriterToString(eDataType, instanceValue);
			case RelANNISPackage.SELEMENT:
				return convertSElementToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RAExporter createRAExporter() {
		RAExporterImpl raExporter = new RAExporterImpl();
		return raExporter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RAMapper createRAMapper() {
		RAMapperImpl raMapper = new RAMapperImpl();
		return raMapper;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RADAO createRADAO() {
		RADAOImpl radao = new RADAOImpl();
		return radao;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RACorpus createRACorpus() {
		RACorpusImpl raCorpus = new RACorpusImpl();
		return raCorpus;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Map.Entry<DAOOBJECT, TupleWriterContainer> createTupleWriterEntry() {
		TupleWriterEntryImpl tupleWriterEntry = new TupleWriterEntryImpl();
		return tupleWriterEntry;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TupleWriterContainer createTupleWriterContainer() {
		TupleWriterContainerImpl tupleWriterContainer = new TupleWriterContainerImpl();
		return tupleWriterContainer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Map.Entry<Long, EList<TAObject>> createTaEntry() {
		TaEntryImpl taEntry = new TaEntryImpl();
		return taEntry;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TAObject createTAObject() {
		TAObjectImpl taObject = new TAObjectImpl();
		return taObject;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RACorpusAnnotation createRACorpusAnnotation() {
		RACorpusAnnotationImpl raCorpusAnnotation = new RACorpusAnnotationImpl();
		return raCorpusAnnotation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Map.Entry<UNIQUE_VALUES, UniqueValue> createUniqueValueEntry() {
		UniqueValueEntryImpl uniqueValueEntry = new UniqueValueEntryImpl();
		return uniqueValueEntry;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public UniqueValue createUniqueValue() {
		UniqueValueImpl uniqueValue = new UniqueValueImpl();
		return uniqueValue;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RAText createRAText() {
		RATextImpl raText = new RATextImpl();
		return raText;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RANode createRANode() {
		RANodeImpl raNode = new RANodeImpl();
		return raNode;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RAEdge createRAEdge() {
		RAEdgeImpl raEdge = new RAEdgeImpl();
		return raEdge;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RANodeAnnotation createRANodeAnnotation() {
		RANodeAnnotationImpl raNodeAnnotation = new RANodeAnnotationImpl();
		return raNodeAnnotation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RAComponent createRAComponent() {
		RAComponentImpl raComponent = new RAComponentImpl();
		return raComponent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RAEdgeAnnotation createRAEdgeAnnotation() {
		RAEdgeAnnotationImpl raEdgeAnnotation = new RAEdgeAnnotationImpl();
		return raEdgeAnnotation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RA_CORPUS_TYPE createRA_CORPUS_TYPEFromString(EDataType eDataType, String initialValue) {
		RA_CORPUS_TYPE result = RA_CORPUS_TYPE.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertRA_CORPUS_TYPEToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EXPORT_FILE createEXPORT_FILEFromString(EDataType eDataType, String initialValue) {
		EXPORT_FILE result = EXPORT_FILE.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertEXPORT_FILEToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DAOOBJECT createDAOOBJECTFromString(EDataType eDataType, String initialValue) {
		DAOOBJECT result = DAOOBJECT.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertDAOOBJECTToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public UNIQUE_VALUES createUNIQUE_VALUESFromString(EDataType eDataType, String initialValue) {
		UNIQUE_VALUES result = UNIQUE_VALUES.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertUNIQUE_VALUESToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SaltProject createSaltProjectFromString(EDataType eDataType, String initialValue) {
		return (SaltProject)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertSaltProjectToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public File createFileFromString(EDataType eDataType, String initialValue) {
		return (File)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertFileToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ITupleWriter createTupleWriterFromString(EDataType eDataType, String initialValue) {
		return (ITupleWriter)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertTupleWriterToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SElement createSElementFromString(EDataType eDataType, String initialValue) {
		return (SElement)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertSElementToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RelANNISPackage getRelANNISPackage() {
		return (RelANNISPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static RelANNISPackage getPackage() {
		return RelANNISPackage.eINSTANCE;
	}

} //RelANNISFactoryImpl
