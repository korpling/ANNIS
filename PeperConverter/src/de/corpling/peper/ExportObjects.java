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
 * A representation of the model object '<em><b>Export Objects</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.ExportObjects#getExportSet <em>Export Set</em>}</li>
 *   <li>{@link de.corpling.peper.ExportObjects#getExporter <em>Exporter</em>}</li>
 *   <li>{@link de.corpling.peper.ExportObjects#getConvertJob <em>Convert Job</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.PeperPackage#getExportObjects()
 * @model
 * @generated
 */
public interface ExportObjects extends EObject {
	/**
	 * Returns the value of the '<em><b>Export Set</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Export Set</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Export Set</em>' containment reference.
	 * @see #setExportSet(ExportSet)
	 * @see de.corpling.peper.PeperPackage#getExportObjects_ExportSet()
	 * @model containment="true" required="true"
	 * @generated
	 */
	ExportSet getExportSet();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ExportObjects#getExportSet <em>Export Set</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Export Set</em>' containment reference.
	 * @see #getExportSet()
	 * @generated
	 */
	void setExportSet(ExportSet value);

	/**
	 * Returns the value of the '<em><b>Exporter</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Exporter</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Exporter</em>' containment reference.
	 * @see #setExporter(Exporter)
	 * @see de.corpling.peper.PeperPackage#getExportObjects_Exporter()
	 * @model containment="true" required="true"
	 * @generated
	 */
	Exporter getExporter();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ExportObjects#getExporter <em>Exporter</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Exporter</em>' containment reference.
	 * @see #getExporter()
	 * @generated
	 */
	void setExporter(Exporter value);

	/**
	 * Returns the value of the '<em><b>Convert Job</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link de.corpling.peper.ConvertJob#getExportObjects <em>Export Objects</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Convert Job</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Convert Job</em>' container reference.
	 * @see #setConvertJob(ConvertJob)
	 * @see de.corpling.peper.PeperPackage#getExportObjects_ConvertJob()
	 * @see de.corpling.peper.ConvertJob#getExportObjects
	 * @model opposite="exportObjects" required="true" transient="false"
	 * @generated
	 */
	ConvertJob getConvertJob();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ExportObjects#getConvertJob <em>Convert Job</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Convert Job</em>' container reference.
	 * @see #getConvertJob()
	 * @generated
	 */
	void setConvertJob(ConvertJob value);

} // ExportObjects
