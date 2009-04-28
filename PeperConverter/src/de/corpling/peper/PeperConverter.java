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
 * A representation of the model object '<em><b>Converter</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.PeperConverter#getConvertJobs <em>Convert Jobs</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.PeperPackage#getPeperConverter()
 * @model
 * @generated
 */
public interface PeperConverter extends EObject {
	/**
	 * Returns the value of the '<em><b>Convert Jobs</b></em>' containment reference list.
	 * The list contents are of type {@link de.corpling.peper.ConvertJob}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Convert Jobs</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Convert Jobs</em>' containment reference list.
	 * @see de.corpling.peper.PeperPackage#getPeperConverter_ConvertJobs()
	 * @model containment="true"
	 * @generated
	 */
	EList<ConvertJob> getConvertJobs();

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
	void addJob(String importDescription, String exportDescription);

} // PeperConverter
