/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS;

import de.corpling.salt.model.saltCore.SElement;

import de.corpling.salt.model.salt.SDocument;

import de.corpling.salt.model.saltCore.STraversalObject;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>RA Mapper</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RAMapper#getRaDAO <em>Ra DAO</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RAMapper#getCurrTaId <em>Curr Ta Id</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RAMapper#getRaExporter <em>Ra Exporter</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RAMapper#getCoherentComponents <em>Coherent Components</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RAMapper#getSubConnectedComponents <em>Sub Connected Components</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAMapper()
 * @model
 * @generated
 */
public interface RAMapper extends STraversalObject {
	/**
	 * Returns the value of the '<em><b>Ra DAO</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ra DAO</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ra DAO</em>' reference.
	 * @see #setRaDAO(RADAO)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAMapper_RaDAO()
	 * @model
	 * @generated
	 */
	RADAO getRaDAO();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RAMapper#getRaDAO <em>Ra DAO</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ra DAO</em>' reference.
	 * @see #getRaDAO()
	 * @generated
	 */
	void setRaDAO(RADAO value);

	/**
	 * Returns the value of the '<em><b>Curr Ta Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Curr Ta Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Curr Ta Id</em>' attribute.
	 * @see #setCurrTaId(Long)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAMapper_CurrTaId()
	 * @model
	 * @generated
	 */
	Long getCurrTaId();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RAMapper#getCurrTaId <em>Curr Ta Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Curr Ta Id</em>' attribute.
	 * @see #getCurrTaId()
	 * @generated
	 */
	void setCurrTaId(Long value);

	/**
	 * Returns the value of the '<em><b>Ra Exporter</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link de.corpling.peper.modules.relANNIS.RAExporter#getRaMapper <em>Ra Mapper</em>}'.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ra Exporter</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ra Exporter</em>' reference.
	 * @see #setRaExporter(RAExporter)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAMapper_RaExporter()
	 * @see de.corpling.peper.modules.relANNIS.RAExporter#getRaMapper
	 * @model opposite="raMapper"
	 * @generated
	 */
	RAExporter getRaExporter();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RAMapper#getRaExporter <em>Ra Exporter</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ra Exporter</em>' reference.
	 * @see #getRaExporter()
	 * @generated
	 */
	void setRaExporter(RAExporter value);

	/**
	 * Returns the value of the '<em><b>Coherent Components</b></em>' attribute list.
	 * The list contents are of type {@link de.corpling.salt.model.saltCore.SElement}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Coherent Components</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Coherent Components</em>' attribute list.
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAMapper_CoherentComponents()
	 * @model dataType="de.corpling.peper.modules.relANNIS.SElement"
	 * @generated
	 */
	EList<SElement> getCoherentComponents();

	/**
	 * Returns the value of the '<em><b>Sub Connected Components</b></em>' attribute list.
	 * The list contents are of type {@link de.corpling.salt.model.saltCore.SElement}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Sub Connected Components</em>' attribute list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sub Connected Components</em>' attribute list.
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRAMapper_SubConnectedComponents()
	 * @model dataType="de.corpling.peper.modules.relANNIS.SElement"
	 * @generated
	 */
	EList<SElement> getSubConnectedComponents();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void init(SDocument document);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void close();

} // RAMapper
