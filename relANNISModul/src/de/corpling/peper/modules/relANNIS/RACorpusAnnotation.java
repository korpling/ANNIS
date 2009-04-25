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
 * A representation of the model object '<em><b>RA Corpus Meta Attribute</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getCorpus_ref <em>Corpus ref</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getNamespace <em>Namespace</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getName <em>Name</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getValue <em>Value</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRACorpusAnnotation()
 * @model
 * @generated
 */
public interface RACorpusAnnotation extends EObject {
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
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRACorpusAnnotation_Corpus_ref()
	 * @model
	 * @generated
	 */
	Long getCorpus_ref();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getCorpus_ref <em>Corpus ref</em>}' attribute.
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
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRACorpusAnnotation_Namespace()
	 * @model
	 * @generated
	 */
	String getNamespace();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getNamespace <em>Namespace</em>}' attribute.
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
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRACorpusAnnotation_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(String)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRACorpusAnnotation_Value()
	 * @model
	 * @generated
	 */
	String getValue();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RACorpusAnnotation#getValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #getValue()
	 * @generated
	 */
	void setValue(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	EList<String> toStringList();

} // RACorpusMetaAttribute
