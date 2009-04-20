/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.impl;

import de.corpling.peper.FormatDefinition;
import de.corpling.peper.ImExportSet;
import de.corpling.peper.ImExporter;
import de.corpling.peper.PeperPackage;

import de.corpling.salt.SaltProject;
import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Im Exporter</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.impl.ImExporterImpl#getSupportedFormats <em>Supported Formats</em>}</li>
 *   <li>{@link de.corpling.peper.impl.ImExporterImpl#getName <em>Name</em>}</li>
 *   <li>{@link de.corpling.peper.impl.ImExporterImpl#getSaltProject <em>Salt Project</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ImExporterImpl extends EObjectImpl implements ImExporter {
	/**
	 * The cached value of the '{@link #getSupportedFormats() <em>Supported Formats</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSupportedFormats()
	 * @generated
	 * @ordered
	 */
	protected EList<FormatDefinition> supportedFormats;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getSaltProject() <em>Salt Project</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSaltProject()
	 * @generated
	 * @ordered
	 */
	protected static final SaltProject SALT_PROJECT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSaltProject() <em>Salt Project</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSaltProject()
	 * @generated
	 * @ordered
	 */
	protected SaltProject saltProject = SALT_PROJECT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ImExporterImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PeperPackage.Literals.IM_EXPORTER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<FormatDefinition> getSupportedFormats() {
		if (supportedFormats == null) {
			supportedFormats = new EObjectContainmentEList<FormatDefinition>(FormatDefinition.class, this, PeperPackage.IM_EXPORTER__SUPPORTED_FORMATS);
		}
		return supportedFormats;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.IM_EXPORTER__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SaltProject getSaltProject() {
		return saltProject;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSaltProject(SaltProject newSaltProject) {
		SaltProject oldSaltProject = saltProject;
		saltProject = newSaltProject;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.IM_EXPORTER__SALT_PROJECT, oldSaltProject, saltProject));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isSupported(ImExportSet ImExportSet) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case PeperPackage.IM_EXPORTER__SUPPORTED_FORMATS:
				return ((InternalEList<?>)getSupportedFormats()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PeperPackage.IM_EXPORTER__SUPPORTED_FORMATS:
				return getSupportedFormats();
			case PeperPackage.IM_EXPORTER__NAME:
				return getName();
			case PeperPackage.IM_EXPORTER__SALT_PROJECT:
				return getSaltProject();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case PeperPackage.IM_EXPORTER__SUPPORTED_FORMATS:
				getSupportedFormats().clear();
				getSupportedFormats().addAll((Collection<? extends FormatDefinition>)newValue);
				return;
			case PeperPackage.IM_EXPORTER__NAME:
				setName((String)newValue);
				return;
			case PeperPackage.IM_EXPORTER__SALT_PROJECT:
				setSaltProject((SaltProject)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case PeperPackage.IM_EXPORTER__SUPPORTED_FORMATS:
				getSupportedFormats().clear();
				return;
			case PeperPackage.IM_EXPORTER__NAME:
				setName(NAME_EDEFAULT);
				return;
			case PeperPackage.IM_EXPORTER__SALT_PROJECT:
				setSaltProject(SALT_PROJECT_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case PeperPackage.IM_EXPORTER__SUPPORTED_FORMATS:
				return supportedFormats != null && !supportedFormats.isEmpty();
			case PeperPackage.IM_EXPORTER__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case PeperPackage.IM_EXPORTER__SALT_PROJECT:
				return SALT_PROJECT_EDEFAULT == null ? saltProject != null : !SALT_PROJECT_EDEFAULT.equals(saltProject);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(", saltProject: ");
		result.append(saltProject);
		result.append(')');
		return result.toString();
	}

} //ImExporterImpl
