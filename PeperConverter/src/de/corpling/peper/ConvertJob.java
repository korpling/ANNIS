/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper;

import de.corpling.salt.SaltProject;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Convert Job</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.ConvertJob#getImportObjects <em>Import Objects</em>}</li>
 *   <li>{@link de.corpling.peper.ConvertJob#getExportObjects <em>Export Objects</em>}</li>
 *   <li>{@link de.corpling.peper.ConvertJob#getSaltProject <em>Salt Project</em>}</li>
 *   <li>{@link de.corpling.peper.ConvertJob#getPorterEmitter <em>Porter Emitter</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.PeperPackage#getConvertJob()
 * @model
 * @generated
 */
public interface ConvertJob extends EObject {
	/**
	 * Returns the value of the '<em><b>Import Objects</b></em>' containment reference list.
	 * The list contents are of type {@link de.corpling.peper.ImportObject}.
	 * It is bidirectional and its opposite is '{@link de.corpling.peper.ImportObject#getConvertJob <em>Convert Job</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Import Objects</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Import Objects</em>' containment reference list.
	 * @see de.corpling.peper.PeperPackage#getConvertJob_ImportObjects()
	 * @see de.corpling.peper.ImportObject#getConvertJob
	 * @model opposite="convertJob" containment="true"
	 * @generated
	 */
	EList<ImportObject> getImportObjects();

	/**
	 * Returns the value of the '<em><b>Export Objects</b></em>' containment reference list.
	 * The list contents are of type {@link de.corpling.peper.ExportObject}.
	 * It is bidirectional and its opposite is '{@link de.corpling.peper.ExportObject#getConvertJob <em>Convert Job</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Export Objects</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Export Objects</em>' containment reference list.
	 * @see de.corpling.peper.PeperPackage#getConvertJob_ExportObjects()
	 * @see de.corpling.peper.ExportObject#getConvertJob
	 * @model opposite="convertJob" containment="true"
	 * @generated
	 */
	EList<ExportObject> getExportObjects();

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
	 * @see de.corpling.peper.PeperPackage#getConvertJob_SaltProject()
	 * @model dataType="de.corpling.peper.SaltProject"
	 * @generated
	 */
	SaltProject getSaltProject();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ConvertJob#getSaltProject <em>Salt Project</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Salt Project</em>' attribute.
	 * @see #getSaltProject()
	 * @generated
	 */
	void setSaltProject(SaltProject value);

	/**
	 * Returns the value of the '<em><b>Porter Emitter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Porter Emitter</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Porter Emitter</em>' reference.
	 * @see #setPorterEmitter(PorterEmitter)
	 * @see de.corpling.peper.PeperPackage#getConvertJob_PorterEmitter()
	 * @model
	 * @generated
	 */
	PorterEmitter getPorterEmitter();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ConvertJob#getPorterEmitter <em>Porter Emitter</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Porter Emitter</em>' reference.
	 * @see #getPorterEmitter()
	 * @generated
	 */
	void setPorterEmitter(PorterEmitter value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void start();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void addImportSet(ImportSet importSet);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void addExportSet(ExportSet exportSet);

} // ConvertJob
