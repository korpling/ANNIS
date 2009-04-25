/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS;

import de.corpling.peper.Exporter;
import de.corpling.salt.model.salt.SCorpus;
import de.corpling.salt.model.salt.SDocument;
import de.corpling.salt.saltFW.SaltProject;

import java.io.File;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>RA Exporter</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RAExporter#getRaMapper <em>Ra Mapper</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RAExporter#getRaDAO <em>Ra DAO</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAExporter()
 * @model
 * @generated
 */
public interface RAExporter extends Exporter {
	/**
	 * Returns the value of the '<em><b>Ra Mapper</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link de.corpling.peper.modules.relANNIS.RAMapper#getRaExporter <em>Ra Exporter</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ra Mapper</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ra Mapper</em>' reference.
	 * @see #setRaMapper(RAMapper)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAExporter_RaMapper()
	 * @see de.corpling.peper.modules.relANNIS.RAMapper#getRaExporter
	 * @model opposite="raExporter"
	 * @generated
	 */
	RAMapper getRaMapper();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RAExporter#getRaMapper <em>Ra Mapper</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ra Mapper</em>' reference.
	 * @see #getRaMapper()
	 * @generated
	 */
	void setRaMapper(RAMapper value);

	/**
	 * Returns the value of the '<em><b>Ra DAO</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ra DAO</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ra DAO</em>' containment reference.
	 * @see #setRaDAO(RADAO)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAExporter_RaDAO()
	 * @model containment="true"
	 * @generated
	 */
	RADAO getRaDAO();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RAExporter#getRaDAO <em>Ra DAO</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ra DAO</em>' containment reference.
	 * @see #getRaDAO()
	 * @generated
	 */
	void setRaDAO(RADAO value);

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

} // RAExporter
