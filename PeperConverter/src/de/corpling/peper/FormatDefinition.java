/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Format Definition</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.FormatDefinition#getFormatName <em>Format Name</em>}</li>
 *   <li>{@link de.corpling.peper.FormatDefinition#getFormatVersion <em>Format Version</em>}</li>
 *   <li>{@link de.corpling.peper.FormatDefinition#getFormatReference <em>Format Reference</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.PeperPackage#getFormatDefinition()
 * @model
 * @generated
 */
public interface FormatDefinition extends EObject {
	/**
	 * Returns the value of the '<em><b>Format Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Format Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Format Name</em>' attribute.
	 * @see #setFormatName(String)
	 * @see de.corpling.peper.PeperPackage#getFormatDefinition_FormatName()
	 * @model
	 * @generated
	 */
	String getFormatName();

	/**
	 * Sets the value of the '{@link de.corpling.peper.FormatDefinition#getFormatName <em>Format Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Format Name</em>' attribute.
	 * @see #getFormatName()
	 * @generated
	 */
	void setFormatName(String value);

	/**
	 * Returns the value of the '<em><b>Format Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Format Version</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Format Version</em>' attribute.
	 * @see #setFormatVersion(String)
	 * @see de.corpling.peper.PeperPackage#getFormatDefinition_FormatVersion()
	 * @model
	 * @generated
	 */
	String getFormatVersion();

	/**
	 * Sets the value of the '{@link de.corpling.peper.FormatDefinition#getFormatVersion <em>Format Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Format Version</em>' attribute.
	 * @see #getFormatVersion()
	 * @generated
	 */
	void setFormatVersion(String value);

	/**
	 * Returns the value of the '<em><b>Format Reference</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Format Reference</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Format Reference</em>' attribute.
	 * @see #setFormatReference(String)
	 * @see de.corpling.peper.PeperPackage#getFormatDefinition_FormatReference()
	 * @model
	 * @generated
	 */
	String getFormatReference();

	/**
	 * Sets the value of the '{@link de.corpling.peper.FormatDefinition#getFormatReference <em>Format Reference</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Format Reference</em>' attribute.
	 * @see #getFormatReference()
	 * @generated
	 */
	void setFormatReference(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean equals(FormatDefinition otherObject);

} // FormatDefinition
