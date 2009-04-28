/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.impl;

import de.corpling.peper.Exporter;
import de.corpling.peper.PeperPackage;

import de.corpling.salt.model.salt.SCorpus;
import de.corpling.salt.model.salt.SDocument;
import java.io.File;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Exporter</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.impl.ExporterImpl#getOutputDir <em>Output Dir</em>}</li>
 *   <li>{@link de.corpling.peper.impl.ExporterImpl#getSettingDir <em>Setting Dir</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ExporterImpl extends ImExporterImpl implements Exporter {
	/**
	 * The default value of the '{@link #getOutputDir() <em>Output Dir</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputDir()
	 * @generated
	 * @ordered
	 */
	protected static final File OUTPUT_DIR_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getOutputDir() <em>Output Dir</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutputDir()
	 * @generated
	 * @ordered
	 */
	protected File outputDir = OUTPUT_DIR_EDEFAULT;

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
	protected ExporterImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PeperPackage.Literals.EXPORTER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public File getOutputDir() {
		return outputDir;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setOutputDir(File newOutputDir) {
		File oldOutputDir = outputDir;
		outputDir = newOutputDir;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.EXPORTER__OUTPUT_DIR, oldOutputDir, outputDir));
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
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.EXPORTER__SETTING_DIR, oldSettingDir, settingDir));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void export(SCorpus sCorpus) 
	{
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void export(SDocument sDocument) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void close()
	{
		
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PeperPackage.EXPORTER__OUTPUT_DIR:
				return getOutputDir();
			case PeperPackage.EXPORTER__SETTING_DIR:
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
			case PeperPackage.EXPORTER__OUTPUT_DIR:
				setOutputDir((File)newValue);
				return;
			case PeperPackage.EXPORTER__SETTING_DIR:
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
			case PeperPackage.EXPORTER__OUTPUT_DIR:
				setOutputDir(OUTPUT_DIR_EDEFAULT);
				return;
			case PeperPackage.EXPORTER__SETTING_DIR:
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
			case PeperPackage.EXPORTER__OUTPUT_DIR:
				return OUTPUT_DIR_EDEFAULT == null ? outputDir != null : !OUTPUT_DIR_EDEFAULT.equals(outputDir);
			case PeperPackage.EXPORTER__SETTING_DIR:
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
		result.append(" (outputDir: ");
		result.append(outputDir);
		result.append(", settingDir: ");
		result.append(settingDir);
		result.append(')');
		return result.toString();
	}

} //ExporterImpl
