/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper;

import de.corpling.salt.SaltConcrete.SCorpus;
import de.corpling.salt.SaltConcrete.SDocument;
import java.io.File;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Exporter</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.Exporter#getOutputDir <em>Output Dir</em>}</li>
 *   <li>{@link de.corpling.peper.Exporter#getSettingDir <em>Setting Dir</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.PeperPackage#getExporter()
 * @model
 * @generated
 */
public interface Exporter extends ImExporter {

	/**
	 * Returns the value of the '<em><b>Output Dir</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Output Dir</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Output Dir</em>' attribute.
	 * @see #setOutputDir(File)
	 * @see de.corpling.peper.PeperPackage#getExporter_OutputDir()
	 * @model dataType="de.corpling.peper.File"
	 * @generated
	 */
	File getOutputDir();

	/**
	 * Sets the value of the '{@link de.corpling.peper.Exporter#getOutputDir <em>Output Dir</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Output Dir</em>' attribute.
	 * @see #getOutputDir()
	 * @generated
	 */
	void setOutputDir(File value);

	/**
	 * Returns the value of the '<em><b>Setting Dir</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Setting Dir</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Setting Dir</em>' attribute.
	 * @see #setSettingDir(File)
	 * @see de.corpling.peper.PeperPackage#getExporter_SettingDir()
	 * @model dataType="de.corpling.peper.File"
	 * @generated
	 */
	File getSettingDir();

	/**
	 * Sets the value of the '{@link de.corpling.peper.Exporter#getSettingDir <em>Setting Dir</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Setting Dir</em>' attribute.
	 * @see #getSettingDir()
	 * @generated
	 */
	void setSettingDir(File value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void export(SCorpus sCorpus);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void export(SDocument sDocument);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void close();
} // Exporter
