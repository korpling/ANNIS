/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.util;

import de.corpling.peper.Exporter;
import de.corpling.peper.ImExporter;
import de.corpling.peper.modules.relANNIS.*;

import de.corpling.salt.model.saltCore.STraversalObject;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage
 * @generated
 */
public class RelANNISSwitch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static RelANNISPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RelANNISSwitch() {
		if (modelPackage == null) {
			modelPackage = RelANNISPackage.eINSTANCE;
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	public T doSwitch(EObject theEObject) {
		return doSwitch(theEObject.eClass(), theEObject);
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T doSwitch(EClass theEClass, EObject theEObject) {
		if (theEClass.eContainer() == modelPackage) {
			return doSwitch(theEClass.getClassifierID(), theEObject);
		}
		else {
			List<EClass> eSuperTypes = theEClass.getESuperTypes();
			return
				eSuperTypes.isEmpty() ?
					defaultCase(theEObject) :
					doSwitch(eSuperTypes.get(0), theEObject);
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case RelANNISPackage.RA_EXPORTER: {
				RAExporter raExporter = (RAExporter)theEObject;
				T result = caseRAExporter(raExporter);
				if (result == null) result = caseExporter(raExporter);
				if (result == null) result = caseImExporter(raExporter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.RA_MAPPER: {
				RAMapper raMapper = (RAMapper)theEObject;
				T result = caseRAMapper(raMapper);
				if (result == null) result = caseSTraversalObject(raMapper);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.RADAO: {
				RADAO radao = (RADAO)theEObject;
				T result = caseRADAO(radao);
				if (result == null) result = caseSTraversalObject(radao);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.RA_CORPUS: {
				RACorpus raCorpus = (RACorpus)theEObject;
				T result = caseRACorpus(raCorpus);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.TUPLE_WRITER_ENTRY: {
				@SuppressWarnings("unchecked") Map.Entry<DAOOBJECT, TupleWriterContainer> tupleWriterEntry = (Map.Entry<DAOOBJECT, TupleWriterContainer>)theEObject;
				T result = caseTupleWriterEntry(tupleWriterEntry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.TUPLE_WRITER_CONTAINER: {
				TupleWriterContainer tupleWriterContainer = (TupleWriterContainer)theEObject;
				T result = caseTupleWriterContainer(tupleWriterContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.TA_ENTRY: {
				@SuppressWarnings("unchecked") Map.Entry<Long, EList<TAObject>> taEntry = (Map.Entry<Long, EList<TAObject>>)theEObject;
				T result = caseTaEntry(taEntry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.TA_OBJECT: {
				TAObject taObject = (TAObject)theEObject;
				T result = caseTAObject(taObject);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.RA_CORPUS_ANNOTATION: {
				RACorpusAnnotation raCorpusAnnotation = (RACorpusAnnotation)theEObject;
				T result = caseRACorpusAnnotation(raCorpusAnnotation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.UNIQUE_VALUE_ENTRY: {
				@SuppressWarnings("unchecked") Map.Entry<UNIQUE_VALUES, UniqueValue> uniqueValueEntry = (Map.Entry<UNIQUE_VALUES, UniqueValue>)theEObject;
				T result = caseUniqueValueEntry(uniqueValueEntry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.UNIQUE_VALUE: {
				UniqueValue uniqueValue = (UniqueValue)theEObject;
				T result = caseUniqueValue(uniqueValue);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.RA_TEXT: {
				RAText raText = (RAText)theEObject;
				T result = caseRAText(raText);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.RA_NODE: {
				RANode raNode = (RANode)theEObject;
				T result = caseRANode(raNode);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.RA_EDGE: {
				RAEdge raEdge = (RAEdge)theEObject;
				T result = caseRAEdge(raEdge);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.RA_NODE_ANNOTATION: {
				RANodeAnnotation raNodeAnnotation = (RANodeAnnotation)theEObject;
				T result = caseRANodeAnnotation(raNodeAnnotation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.RA_COMPONENT: {
				RAComponent raComponent = (RAComponent)theEObject;
				T result = caseRAComponent(raComponent);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case RelANNISPackage.RA_EDGE_ANNOTATION: {
				RAEdgeAnnotation raEdgeAnnotation = (RAEdgeAnnotation)theEObject;
				T result = caseRAEdgeAnnotation(raEdgeAnnotation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>RA Exporter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>RA Exporter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRAExporter(RAExporter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>RA Mapper</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>RA Mapper</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRAMapper(RAMapper object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>RADAO</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>RADAO</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRADAO(RADAO object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>RA Corpus</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>RA Corpus</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRACorpus(RACorpus object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tuple Writer Entry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tuple Writer Entry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTupleWriterEntry(Map.Entry<DAOOBJECT, TupleWriterContainer> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Tuple Writer Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Tuple Writer Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTupleWriterContainer(TupleWriterContainer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Ta Entry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Ta Entry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTaEntry(Map.Entry<Long, EList<TAObject>> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>TA Object</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>TA Object</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseTAObject(TAObject object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>RA Corpus Annotation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>RA Corpus Annotation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRACorpusAnnotation(RACorpusAnnotation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Unique Value Entry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Unique Value Entry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseUniqueValueEntry(Map.Entry<UNIQUE_VALUES, UniqueValue> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Unique Value</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Unique Value</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseUniqueValue(UniqueValue object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>RA Text</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>RA Text</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRAText(RAText object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>RA Node</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>RA Node</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRANode(RANode object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>RA Edge</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>RA Edge</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRAEdge(RAEdge object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>RA Node Annotation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>RA Node Annotation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRANodeAnnotation(RANodeAnnotation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>RA Component</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>RA Component</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRAComponent(RAComponent object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>RA Edge Annotation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>RA Edge Annotation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRAEdgeAnnotation(RAEdgeAnnotation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Im Exporter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Im Exporter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseImExporter(ImExporter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Exporter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Exporter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseExporter(Exporter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>STraversal Object</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>STraversal Object</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSTraversalObject(STraversalObject object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	public T defaultCase(EObject object) {
		return null;
	}

} //RelANNISSwitch
