/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage
 * @generated
 */
public interface RelANNISFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	RelANNISFactory eINSTANCE = de.corpling.peper.modules.relANNIS.impl.RelANNISFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>RA Exporter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>RA Exporter</em>'.
	 * @generated
	 */
	RAExporter createRAExporter();

	/**
	 * Returns a new object of class '<em>RA Mapper</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>RA Mapper</em>'.
	 * @generated
	 */
	RAMapper createRAMapper();

	/**
	 * Returns a new object of class '<em>RADAO</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>RADAO</em>'.
	 * @generated
	 */
	RADAO createRADAO();

	/**
	 * Returns a new object of class '<em>RA Corpus</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>RA Corpus</em>'.
	 * @generated
	 */
	RACorpus createRACorpus();

	/**
	 * Returns a new object of class '<em>Tuple Writer Container</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Tuple Writer Container</em>'.
	 * @generated
	 */
	TupleWriterContainer createTupleWriterContainer();

	/**
	 * Returns a new object of class '<em>TA Object</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>TA Object</em>'.
	 * @generated
	 */
	TAObject createTAObject();

	/**
	 * Returns a new object of class '<em>RA Corpus Annotation</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>RA Corpus Annotation</em>'.
	 * @generated
	 */
	RACorpusAnnotation createRACorpusAnnotation();

	/**
	 * Returns a new object of class '<em>Unique Value</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Unique Value</em>'.
	 * @generated
	 */
	UniqueValue createUniqueValue();

	/**
	 * Returns a new object of class '<em>RA Text</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>RA Text</em>'.
	 * @generated
	 */
	RAText createRAText();

	/**
	 * Returns a new object of class '<em>RA Node</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>RA Node</em>'.
	 * @generated
	 */
	RANode createRANode();

	/**
	 * Returns a new object of class '<em>RA Edge</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>RA Edge</em>'.
	 * @generated
	 */
	RAEdge createRAEdge();

	/**
	 * Returns a new object of class '<em>RA Node Annotation</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>RA Node Annotation</em>'.
	 * @generated
	 */
	RANodeAnnotation createRANodeAnnotation();

	/**
	 * Returns a new object of class '<em>RA Component</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>RA Component</em>'.
	 * @generated
	 */
	RAComponent createRAComponent();

	/**
	 * Returns a new object of class '<em>RA Edge Annotation</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>RA Edge Annotation</em>'.
	 * @generated
	 */
	RAEdgeAnnotation createRAEdgeAnnotation();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	RelANNISPackage getRelANNISPackage();

} //RelANNISFactory
