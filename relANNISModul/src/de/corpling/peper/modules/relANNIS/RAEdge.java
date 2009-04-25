/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>RA Rank</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RAEdge#getPre <em>Pre</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RAEdge#getPost <em>Post</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RAEdge#getNode_ref <em>Node ref</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RAEdge#getComponent_ref <em>Component ref</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RAEdge#getParent <em>Parent</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAEdge()
 * @model
 * @generated
 */
public interface RAEdge extends EObject {
	/**
	 * Returns the value of the '<em><b>Pre</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Pre</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Pre</em>' attribute.
	 * @see #setPre(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAEdge_Pre()
	 * @model
	 * @generated
	 */
	Long getPre();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RAEdge#getPre <em>Pre</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pre</em>' attribute.
	 * @see #getPre()
	 * @generated
	 */
	void setPre(Long value);

	/**
	 * Returns the value of the '<em><b>Post</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Post</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Post</em>' attribute.
	 * @see #setPost(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAEdge_Post()
	 * @model
	 * @generated
	 */
	Long getPost();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RAEdge#getPost <em>Post</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Post</em>' attribute.
	 * @see #getPost()
	 * @generated
	 */
	void setPost(Long value);

	/**
	 * Returns the value of the '<em><b>Node ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Node ref</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Node ref</em>' attribute.
	 * @see #setNode_ref(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAEdge_Node_ref()
	 * @model
	 * @generated
	 */
	Long getNode_ref();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RAEdge#getNode_ref <em>Node ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Node ref</em>' attribute.
	 * @see #getNode_ref()
	 * @generated
	 */
	void setNode_ref(Long value);

	/**
	 * Returns the value of the '<em><b>Component ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Component ref</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Component ref</em>' attribute.
	 * @see #setComponent_ref(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAEdge_Component_ref()
	 * @model
	 * @generated
	 */
	Long getComponent_ref();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RAEdge#getComponent_ref <em>Component ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Component ref</em>' attribute.
	 * @see #getComponent_ref()
	 * @generated
	 */
	void setComponent_ref(Long value);

	/**
	 * Returns the value of the '<em><b>Parent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Parent</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' attribute.
	 * @see #setParent(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAEdge_Parent()
	 * @model
	 * @generated
	 */
	Long getParent();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RAEdge#getParent <em>Parent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' attribute.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(Long value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	EList<String> toStringList();

} // RARank
