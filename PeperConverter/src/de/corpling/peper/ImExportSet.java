/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper;

import java.io.File;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Im Export Set</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.ImExportSet#getDataSourcePath <em>Data Source Path</em>}</li>
 *   <li>{@link de.corpling.peper.ImExportSet#getFormatDefinition <em>Format Definition</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.PeperPackage#getImExportSet()
 * @model
 * @generated
 */
public interface ImExportSet extends EObject {
	/**
	 * Returns the value of the '<em><b>Data Source Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Data Source Path</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data Source Path</em>' attribute.
	 * @see #setDataSourcePath(File)
	 * @see de.corpling.peper.PeperPackage#getImExportSet_DataSourcePath()
	 * @model dataType="de.corpling.peper.File"
	 * @generated
	 */
	File getDataSourcePath();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ImExportSet#getDataSourcePath <em>Data Source Path</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data Source Path</em>' attribute.
	 * @see #getDataSourcePath()
	 * @generated
	 */
	void setDataSourcePath(File value);

	/**
	 * Returns the value of the '<em><b>Format Definition</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Format Definition</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Format Definition</em>' containment reference.
	 * @see #setFormatDefinition(FormatDefinition)
	 * @see de.corpling.peper.PeperPackage#getImExportSet_FormatDefinition()
	 * @model containment="true" required="true"
	 * @generated
	 */
	FormatDefinition getFormatDefinition();

	/**
	 * Sets the value of the '{@link de.corpling.peper.ImExportSet#getFormatDefinition <em>Format Definition</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Format Definition</em>' containment reference.
	 * @see #getFormatDefinition()
	 * @generated
	 */
	void setFormatDefinition(FormatDefinition value);

} // ImExportSet
