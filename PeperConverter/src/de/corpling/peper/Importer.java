/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper;

import de.corpling.salt.model.salt.SDocument;
import de.corpling.salt.model.salt.SCorpus;
import java.io.File;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Importer</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.Importer#getInputDir <em>Input Dir</em>}</li>
 *   <li>{@link de.corpling.peper.Importer#getSettingDir <em>Setting Dir</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.PeperPackage#getImporter()
 * @model
 * @generated
 */
public interface Importer extends ImExporter {

	/**
	 * Returns the value of the '<em><b>Input Dir</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Input Dir</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Input Dir</em>' attribute.
	 * @see #setInputDir(File)
	 * @see de.corpling.peper.PeperPackage#getImporter_InputDir()
	 * @model dataType="de.corpling.peper.File"
	 * @generated
	 */
	File getInputDir();

	/**
	 * Sets the value of the '{@link de.corpling.peper.Importer#getInputDir <em>Input Dir</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Input Dir</em>' attribute.
	 * @see #getInputDir()
	 * @generated
	 */
	void setInputDir(File value);

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
	 * @see de.corpling.peper.PeperPackage#getImporter_SettingDir()
	 * @model dataType="de.corpling.peper.File"
	 * @generated
	 */
	File getSettingDir();

	/**
	 * Sets the value of the '{@link de.corpling.peper.Importer#getSettingDir <em>Setting Dir</em>}' attribute.
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
	void importCorpusStructure();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model sDocumentDataType="de.corpling.peper.SDocument"
	 * @generated
	 */
	void importDocument(SDocument sDocument);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void close();
} // Importer
