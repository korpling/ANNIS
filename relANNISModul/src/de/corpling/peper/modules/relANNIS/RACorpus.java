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
 * A representation of the model object '<em><b>RA Corpus</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RACorpus#getId <em>Id</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RACorpus#getName <em>Name</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RACorpus#getType <em>Type</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RACorpus#getVersion <em>Version</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RACorpus#getPre <em>Pre</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RACorpus#getPost <em>Post</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRACorpus()
 * @model
 * @generated
 */
public interface RACorpus extends EObject {
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
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRACorpus_Id()
	 * @model
	 * @generated
	 */
	Long getId();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RACorpus#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(Long value);

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
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRACorpus_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RACorpus#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The literals are from the enumeration {@link de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE
	 * @see #setType(RA_CORPUS_TYPE)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRACorpus_Type()
	 * @model
	 * @generated
	 */
	RA_CORPUS_TYPE getType();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RACorpus#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE
	 * @see #getType()
	 * @generated
	 */
	void setType(RA_CORPUS_TYPE value);

	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Version</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #setVersion(String)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRACorpus_Version()
	 * @model
	 * @generated
	 */
	String getVersion();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RACorpus#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(String value);

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
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRACorpus_Pre()
	 * @model
	 * @generated
	 */
	Long getPre();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RACorpus#getPre <em>Pre</em>}' attribute.
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
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRACorpus_Post()
	 * @model
	 * @generated
	 */
	Long getPost();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RACorpus#getPost <em>Post</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Post</em>' attribute.
	 * @see #getPost()
	 * @generated
	 */
	void setPost(Long value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	EList<String> toStringList();

} // RACorpus
