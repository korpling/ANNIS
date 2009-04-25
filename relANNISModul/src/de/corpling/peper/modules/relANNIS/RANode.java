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
 * A representation of the model object '<em><b>RA Struct</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RANode#getId <em>Id</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RANode#getText_ref <em>Text ref</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RANode#getCorpus_ref <em>Corpus ref</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RANode#getNamespace <em>Namespace</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RANode#getName <em>Name</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RANode#getLeft <em>Left</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RANode#getRight <em>Right</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RANode#getToken_index <em>Token index</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RANode#isContinuous <em>Continuous</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RANode#getSpan <em>Span</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRANode()
 * @model
 * @generated
 */
public interface RANode extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRANode_Id()
	 * @model
	 * @generated
	 */
	Long getId();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RANode#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(Long value);

	/**
	 * Returns the value of the '<em><b>Text ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Text ref</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Text ref</em>' attribute.
	 * @see #setText_ref(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRANode_Text_ref()
	 * @model
	 * @generated
	 */
	Long getText_ref();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RANode#getText_ref <em>Text ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Text ref</em>' attribute.
	 * @see #getText_ref()
	 * @generated
	 */
	void setText_ref(Long value);

	/**
	 * Returns the value of the '<em><b>Corpus ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Corpus ref</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Corpus ref</em>' attribute.
	 * @see #setCorpus_ref(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRANode_Corpus_ref()
	 * @model
	 * @generated
	 */
	Long getCorpus_ref();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RANode#getCorpus_ref <em>Corpus ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Corpus ref</em>' attribute.
	 * @see #getCorpus_ref()
	 * @generated
	 */
	void setCorpus_ref(Long value);

	/**
	 * Returns the value of the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Namespace</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Namespace</em>' attribute.
	 * @see #setNamespace(String)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRANode_Namespace()
	 * @model
	 * @generated
	 */
	String getNamespace();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RANode#getNamespace <em>Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Namespace</em>' attribute.
	 * @see #getNamespace()
	 * @generated
	 */
	void setNamespace(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRANode_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RANode#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Left</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Left</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Left</em>' attribute.
	 * @see #setLeft(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRANode_Left()
	 * @model
	 * @generated
	 */
	Long getLeft();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RANode#getLeft <em>Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Left</em>' attribute.
	 * @see #getLeft()
	 * @generated
	 */
	void setLeft(Long value);

	/**
	 * Returns the value of the '<em><b>Right</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Right</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Right</em>' attribute.
	 * @see #setRight(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRANode_Right()
	 * @model
	 * @generated
	 */
	Long getRight();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RANode#getRight <em>Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Right</em>' attribute.
	 * @see #getRight()
	 * @generated
	 */
	void setRight(Long value);

	/**
	 * Returns the value of the '<em><b>Token index</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Token index</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Token index</em>' attribute.
	 * @see #setToken_index(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRANode_Token_index()
	 * @model
	 * @generated
	 */
	Long getToken_index();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RANode#getToken_index <em>Token index</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Token index</em>' attribute.
	 * @see #getToken_index()
	 * @generated
	 */
	void setToken_index(Long value);

	/**
	 * Returns the value of the '<em><b>Continuous</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Continuous</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Continuous</em>' attribute.
	 * @see #setContinuous(boolean)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRANode_Continuous()
	 * @model
	 * @generated
	 */
	boolean isContinuous();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RANode#isContinuous <em>Continuous</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Continuous</em>' attribute.
	 * @see #isContinuous()
	 * @generated
	 */
	void setContinuous(boolean value);

	/**
	 * Returns the value of the '<em><b>Span</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Span</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Span</em>' attribute.
	 * @see #setSpan(String)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRANode_Span()
	 * @model
	 * @generated
	 */
	String getSpan();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RANode#getSpan <em>Span</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Span</em>' attribute.
	 * @see #getSpan()
	 * @generated
	 */
	void setSpan(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	EList<String> toStringList();

} // RAStruct
