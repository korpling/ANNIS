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
 * A representation of the model object '<em><b>Import Object</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.ImportObject#getImporter <em>Importer</em>}</li>
 *   <li>{@link de.corpling.peper.ImportObject#getImportSet <em>Import Set</em>}</li>
 *   <li>{@link de.corpling.peper.ImportObject#getConvertJob <em>Convert Job</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.PeperPackage#getImportObject()
 * @model
 * @generated
 */
public interface ImportObject extends EObject {
	/**
	 * Returns the value of the '<em><b>Importer</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Importer</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Importer</em>' containment reference.
	 * @see #setImporter(Importer)
	 * @see de.corpling.peper.PeperPackage#getImportObject_Importer()
	 * @model containment="true" required="true"
	 * @generated
	 */
	Importer getImporter();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ImportObject#getImporter <em>Importer</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Importer</em>' containment reference.
	 * @see #getImporter()
	 * @generated
	 */
	void setImporter(Importer value);

	/**
	 * Returns the value of the '<em><b>Import Set</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Import Set</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Import Set</em>' containment reference.
	 * @see #setImportSet(ImportSet)
	 * @see de.corpling.peper.PeperPackage#getImportObject_ImportSet()
	 * @model containment="true" required="true"
	 * @generated
	 */
	ImportSet getImportSet();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ImportObject#getImportSet <em>Import Set</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Import Set</em>' containment reference.
	 * @see #getImportSet()
	 * @generated
	 */
	void setImportSet(ImportSet value);

	/**
	 * Returns the value of the '<em><b>Convert Job</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link de.corpling.peper.ConvertJob#getImportObjects <em>Import Objects</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Convert Job</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Convert Job</em>' container reference.
	 * @see #setConvertJob(ConvertJob)
	 * @see de.corpling.peper.PeperPackage#getImportObject_ConvertJob()
	 * @see de.corpling.peper.ConvertJob#getImportObjects
	 * @model opposite="importObjects" required="true" transient="false"
	 * @generated
	 */
	ConvertJob getConvertJob();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ImportObject#getConvertJob <em>Convert Job</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Convert Job</em>' container reference.
	 * @see #getConvertJob()
	 * @generated
	 */
	void setConvertJob(ConvertJob value);

} // ImportObject
