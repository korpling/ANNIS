/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS;

import de.dataconnector.tupleconnector.ITupleWriter;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Tuple Writer Container</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.TupleWriterContainer#getTupleWriter <em>Tuple Writer</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getTupleWriterContainer()
 * @model
 * @generated
 */
public interface TupleWriterContainer extends EObject {
	/**
	 * Returns the value of the '<em><b>Tuple Writer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tuple Writer</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tuple Writer</em>' attribute.
	 * @see #setTupleWriter(ITupleWriter)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getTupleWriterContainer_TupleWriter()
	 * @model dataType="de.corpling.peper.modules.relANNIS.TupleWriter"
	 * @generated
	 */
	ITupleWriter getTupleWriter();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.TupleWriterContainer#getTupleWriter <em>Tuple Writer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tuple Writer</em>' attribute.
	 * @see #getTupleWriter()
	 * @generated
	 */
	void setTupleWriter(ITupleWriter value);

} // TupleWriterContainer
