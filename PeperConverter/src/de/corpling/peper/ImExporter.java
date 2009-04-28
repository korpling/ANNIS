/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper;

import de.corpling.salt.saltFW.SaltProject;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Im Exporter</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.ImExporter#getSupportedFormats <em>Supported Formats</em>}</li>
 *   <li>{@link de.corpling.peper.ImExporter#getName <em>Name</em>}</li>
 *   <li>{@link de.corpling.peper.ImExporter#getSaltProject <em>Salt Project</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.PeperPackage#getImExporter()
 * @model
 * @generated
 */
public interface ImExporter extends EObject {
	/**
	 * Returns the value of the '<em><b>Supported Formats</b></em>' containment reference list.
	 * The list contents are of type {@link de.corpling.peper.FormatDefinition}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Supported Formats</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Supported Formats</em>' containment reference list.
	 * @see de.corpling.peper.PeperPackage#getImExporter_SupportedFormats()
	 * @model containment="true" required="true"
	 * @generated
	 */
	EList<FormatDefinition> getSupportedFormats();

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
	 * @see de.corpling.peper.PeperPackage#getImExporter_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ImExporter#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Salt Project</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Salt Project</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Salt Project</em>' attribute.
	 * @see #setSaltProject(SaltProject)
	 * @see de.corpling.peper.PeperPackage#getImExporter_SaltProject()
	 * @model dataType="de.corpling.peper.SaltProject"
	 * @generated
	 */
	SaltProject getSaltProject();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ImExporter#getSaltProject <em>Salt Project</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Salt Project</em>' attribute.
	 * @see #getSaltProject()
	 * @generated
	 */
	void setSaltProject(SaltProject value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	boolean isSupported(ImExportSet ImExportSet);

} // ImExporter
