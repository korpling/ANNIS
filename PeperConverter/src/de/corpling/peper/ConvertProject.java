/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Convert Project</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.ConvertProject#getImportObjects <em>Import Objects</em>}</li>
 *   <li>{@link de.corpling.peper.ConvertProject#getExportObjects <em>Export Objects</em>}</li>
 *   <li>{@link de.corpling.peper.ConvertProject#getSaltProject <em>Salt Project</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.PeperPackage#getConvertProject()
 * @model
 * @generated
 */
public interface ConvertProject extends EObject {
	/**
	 * Returns the value of the '<em><b>Import Objects</b></em>' containment reference list.
	 * The list contents are of type {@link de.corpling.peper.ImportObject}.
	 * It is bidirectional and its opposite is '{@link de.corpling.peper.ImportObject#getConvertProject <em>Convert Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Import Objects</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Import Objects</em>' containment reference list.
	 * @see de.corpling.peper.PeperPackage#getConvertProject_ImportObjects()
	 * @see de.corpling.peper.ImportObject#getConvertProject
	 * @model opposite="convertProject" containment="true"
	 * @generated
	 */
	EList<ImportObject> getImportObjects();

	/**
	 * Returns the value of the '<em><b>Export Objects</b></em>' containment reference list.
	 * The list contents are of type {@link de.corpling.peper.ExportObjects}.
	 * It is bidirectional and its opposite is '{@link de.corpling.peper.ExportObjects#getConvertProject <em>Convert Project</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Export Objects</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Export Objects</em>' containment reference list.
	 * @see de.corpling.peper.PeperPackage#getConvertProject_ExportObjects()
	 * @see de.corpling.peper.ExportObjects#getConvertProject
	 * @model opposite="convertProject" containment="true"
	 * @generated
	 */
	EList<ExportObjects> getExportObjects();

	/**
	 * Returns the value of the '<em><b>Salt Project</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Salt Project</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Salt Project</em>' attribute.
	 * @see #setSaltProject(String)
	 * @see de.corpling.peper.PeperPackage#getConvertProject_SaltProject()
	 * @model
	 * @generated
	 */
	String getSaltProject();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ConvertProject#getSaltProject <em>Salt Project</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Salt Project</em>' attribute.
	 * @see #getSaltProject()
	 * @generated
	 */
	void setSaltProject(String value);

} // ConvertProject
