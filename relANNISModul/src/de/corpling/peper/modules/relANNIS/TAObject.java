/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>TA Object</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.TAObject#getTaId <em>Ta Id</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.TAObject#getTwWriterKey <em>Tw Writer Key</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getTAObject()
 * @model
 * @generated
 */
public interface TAObject extends EObject {
	/**
	 * Returns the value of the '<em><b>Ta Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ta Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ta Id</em>' attribute.
	 * @see #setTaId(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getTAObject_TaId()
	 * @model
	 * @generated
	 */
	Long getTaId();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.TAObject#getTaId <em>Ta Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ta Id</em>' attribute.
	 * @see #getTaId()
	 * @generated
	 */
	void setTaId(Long value);

	/**
	 * Returns the value of the '<em><b>Tw Writer Key</b></em>' attribute.
	 * The literals are from the enumeration {@link de.corpling.peper.modules.relANNIS.DAOOBJECT}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tw Writer Key</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tw Writer Key</em>' attribute.
	 * @see de.corpling.peper.modules.relANNIS.DAOOBJECT
	 * @see #setTwWriterKey(DAOOBJECT)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getTAObject_TwWriterKey()
	 * @model
	 * @generated
	 */
	DAOOBJECT getTwWriterKey();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.TAObject#getTwWriterKey <em>Tw Writer Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Tw Writer Key</em>' attribute.
	 * @see de.corpling.peper.modules.relANNIS.DAOOBJECT
	 * @see #getTwWriterKey()
	 * @generated
	 */
	void setTwWriterKey(DAOOBJECT value);

} // TAObject
