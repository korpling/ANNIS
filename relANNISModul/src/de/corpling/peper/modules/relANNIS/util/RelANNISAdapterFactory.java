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

import java.util.Map;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage
 * @generated
 */
public class RelANNISAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static RelANNISPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RelANNISAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = RelANNISPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RelANNISSwitch<Adapter> modelSwitch =
		new RelANNISSwitch<Adapter>() {
			@Override
			public Adapter caseRAExporter(RAExporter object) {
				return createRAExporterAdapter();
			}
			@Override
			public Adapter caseRAMapper(RAMapper object) {
				return createRAMapperAdapter();
			}
			@Override
			public Adapter caseRADAO(RADAO object) {
				return createRADAOAdapter();
			}
			@Override
			public Adapter caseRACorpus(RACorpus object) {
				return createRACorpusAdapter();
			}
			@Override
			public Adapter caseTupleWriterEntry(Map.Entry<DAOOBJECT, TupleWriterContainer> object) {
				return createTupleWriterEntryAdapter();
			}
			@Override
			public Adapter caseTupleWriterContainer(TupleWriterContainer object) {
				return createTupleWriterContainerAdapter();
			}
			@Override
			public Adapter caseTaEntry(Map.Entry<Long, EList<TAObject>> object) {
				return createTaEntryAdapter();
			}
			@Override
			public Adapter caseTAObject(TAObject object) {
				return createTAObjectAdapter();
			}
			@Override
			public Adapter caseRACorpusAnnotation(RACorpusAnnotation object) {
				return createRACorpusAnnotationAdapter();
			}
			@Override
			public Adapter caseUniqueValueEntry(Map.Entry<UNIQUE_VALUES, UniqueValue> object) {
				return createUniqueValueEntryAdapter();
			}
			@Override
			public Adapter caseUniqueValue(UniqueValue object) {
				return createUniqueValueAdapter();
			}
			@Override
			public Adapter caseRAText(RAText object) {
				return createRATextAdapter();
			}
			@Override
			public Adapter caseRANode(RANode object) {
				return createRANodeAdapter();
			}
			@Override
			public Adapter caseRAEdge(RAEdge object) {
				return createRAEdgeAdapter();
			}
			@Override
			public Adapter caseRANodeAnnotation(RANodeAnnotation object) {
				return createRANodeAnnotationAdapter();
			}
			@Override
			public Adapter caseRAComponent(RAComponent object) {
				return createRAComponentAdapter();
			}
			@Override
			public Adapter caseRAEdgeAnnotation(RAEdgeAnnotation object) {
				return createRAEdgeAnnotationAdapter();
			}
			@Override
			public Adapter caseImExporter(ImExporter object) {
				return createImExporterAdapter();
			}
			@Override
			public Adapter caseExporter(Exporter object) {
				return createExporterAdapter();
			}
			@Override
			public Adapter caseSTraversalObject(STraversalObject object) {
				return createSTraversalObjectAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.RAExporter <em>RA Exporter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.RAExporter
	 * @generated
	 */
	public Adapter createRAExporterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.RAMapper <em>RA Mapper</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.RAMapper
	 * @generated
	 */
	public Adapter createRAMapperAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.RADAO <em>RADAO</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.RADAO
	 * @generated
	 */
	public Adapter createRADAOAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.RACorpus <em>RA Corpus</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.RACorpus
	 * @generated
	 */
	public Adapter createRACorpusAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link java.util.Map.Entry <em>Tuple Writer Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see java.util.Map.Entry
	 * @generated
	 */
	public Adapter createTupleWriterEntryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.TupleWriterContainer <em>Tuple Writer Container</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.TupleWriterContainer
	 * @generated
	 */
	public Adapter createTupleWriterContainerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link java.util.Map.Entry <em>Ta Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see java.util.Map.Entry
	 * @generated
	 */
	public Adapter createTaEntryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.TAObject <em>TA Object</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.TAObject
	 * @generated
	 */
	public Adapter createTAObjectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation <em>RA Corpus Annotation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.RACorpusAnnotation
	 * @generated
	 */
	public Adapter createRACorpusAnnotationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link java.util.Map.Entry <em>Unique Value Entry</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see java.util.Map.Entry
	 * @generated
	 */
	public Adapter createUniqueValueEntryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.UniqueValue <em>Unique Value</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.UniqueValue
	 * @generated
	 */
	public Adapter createUniqueValueAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.RAText <em>RA Text</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.RAText
	 * @generated
	 */
	public Adapter createRATextAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.RANode <em>RA Node</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.RANode
	 * @generated
	 */
	public Adapter createRANodeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.RAEdge <em>RA Edge</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.RAEdge
	 * @generated
	 */
	public Adapter createRAEdgeAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.RANodeAnnotation <em>RA Node Annotation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.RANodeAnnotation
	 * @generated
	 */
	public Adapter createRANodeAnnotationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.RAComponent <em>RA Component</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.RAComponent
	 * @generated
	 */
	public Adapter createRAComponentAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.modules.relANNIS.RAEdgeAnnotation <em>RA Edge Annotation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.modules.relANNIS.RAEdgeAnnotation
	 * @generated
	 */
	public Adapter createRAEdgeAnnotationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.ImExporter <em>Im Exporter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.ImExporter
	 * @generated
	 */
	public Adapter createImExporterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.Exporter <em>Exporter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.Exporter
	 * @generated
	 */
	public Adapter createExporterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.salt.model.saltCore.STraversalObject <em>STraversal Object</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.salt.model.saltCore.STraversalObject
	 * @generated
	 */
	public Adapter createSTraversalObjectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //RelANNISAdapterFactory
