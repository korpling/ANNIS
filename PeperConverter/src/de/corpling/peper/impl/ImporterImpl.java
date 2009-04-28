/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.impl;

import de.corpling.peper.Importer;
import de.corpling.peper.PeperPackage;

import de.corpling.salt.model.salt.SCorpus;
import de.corpling.salt.model.salt.SDocument;
import java.io.File;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Importer</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.impl.ImporterImpl#getInputDir <em>Input Dir</em>}</li>
 *   <li>{@link de.corpling.peper.impl.ImporterImpl#getSettingDir <em>Setting Dir</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ImporterImpl extends ImExporterImpl implements Importer {
	/**
	 * The default value of the '{@link #getInputDir() <em>Input Dir</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInputDir()
	 * @generated
	 * @ordered
	 */
	protected static final File INPUT_DIR_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getInputDir() <em>Input Dir</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInputDir()
	 * @generated
	 * @ordered
	 */
	protected File inputDir = INPUT_DIR_EDEFAULT;

	/**
	 * The default value of the '{@link #getSettingDir() <em>Setting Dir</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSettingDir()
	 * @generated
	 * @ordered
	 */
	protected static final File SETTING_DIR_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getSettingDir() <em>Setting Dir</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSettingDir()
	 * @generated
	 * @ordered
	 */
	protected File settingDir = SETTING_DIR_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ImporterImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PeperPackage.Literals.IMPORTER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public File getInputDir() {
		return inputDir;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setInputDir(File newInputDir) {
		File oldInputDir = inputDir;
		inputDir = newInputDir;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.IMPORTER__INPUT_DIR, oldInputDir, inputDir));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public File getSettingDir() {
		return settingDir;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSettingDir(File newSettingDir) {
		File oldSettingDir = settingDir;
		settingDir = newSettingDir;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.IMPORTER__SETTING_DIR, oldSettingDir, settingDir));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void importCorpusStructure() {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void importDocument(SDocument sDocument) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void close() {
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
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PeperPackage.IMPORTER__INPUT_DIR:
				return getInputDir();
			case PeperPackage.IMPORTER__SETTING_DIR:
				return getSettingDir();
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
			case PeperPackage.IMPORTER__INPUT_DIR:
				setInputDir((File)newValue);
				return;
			case PeperPackage.IMPORTER__SETTING_DIR:
				setSettingDir((File)newValue);
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
			case PeperPackage.IMPORTER__INPUT_DIR:
				setInputDir(INPUT_DIR_EDEFAULT);
				return;
			case PeperPackage.IMPORTER__SETTING_DIR:
				setSettingDir(SETTING_DIR_EDEFAULT);
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
			case PeperPackage.IMPORTER__INPUT_DIR:
				return INPUT_DIR_EDEFAULT == null ? inputDir != null : !INPUT_DIR_EDEFAULT.equals(inputDir);
			case PeperPackage.IMPORTER__SETTING_DIR:
				return SETTING_DIR_EDEFAULT == null ? settingDir != null : !SETTING_DIR_EDEFAULT.equals(settingDir);
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
		result.append(" (inputDir: ");
		result.append(inputDir);
		result.append(", settingDir: ");
		result.append(settingDir);
		result.append(')');
		return result.toString();
	}

} //ImporterImpl
