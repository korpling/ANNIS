/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.impl;

import de.corpling.peper.ConvertJob;
import de.corpling.peper.ConvertProject;
import de.corpling.peper.ImportObject;
import de.corpling.peper.ImportSet;
import de.corpling.peper.Importer;
import de.corpling.peper.PeperPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Import Object</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.impl.ImportObjectImpl#getImporter <em>Importer</em>}</li>
 *   <li>{@link de.corpling.peper.impl.ImportObjectImpl#getImportSet <em>Import Set</em>}</li>
 *   <li>{@link de.corpling.peper.impl.ImportObjectImpl#getConvertJob <em>Convert Job</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ImportObjectImpl extends EObjectImpl implements ImportObject {
	/**
	 * The cached value of the '{@link #getImporter() <em>Importer</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImporter()
	 * @generated
	 * @ordered
	 */
	protected Importer importer;

	/**
	 * The cached value of the '{@link #getImportSet() <em>Import Set</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getImportSet()
	 * @generated
	 * @ordered
	 */
	protected ImportSet importSet;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ImportObjectImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PeperPackage.Literals.IMPORT_OBJECT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Importer getImporter() {
		return importer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetImporter(Importer newImporter, NotificationChain msgs) {
		Importer oldImporter = importer;
		importer = newImporter;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PeperPackage.IMPORT_OBJECT__IMPORTER, oldImporter, newImporter);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setImporter(Importer newImporter) {
		if (newImporter != importer) {
			NotificationChain msgs = null;
			if (importer != null)
				msgs = ((InternalEObject)importer).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PeperPackage.IMPORT_OBJECT__IMPORTER, null, msgs);
			if (newImporter != null)
				msgs = ((InternalEObject)newImporter).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PeperPackage.IMPORT_OBJECT__IMPORTER, null, msgs);
			msgs = basicSetImporter(newImporter, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.IMPORT_OBJECT__IMPORTER, newImporter, newImporter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ImportSet getImportSet() {
		return importSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetImportSet(ImportSet newImportSet, NotificationChain msgs) {
		ImportSet oldImportSet = importSet;
		importSet = newImportSet;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PeperPackage.IMPORT_OBJECT__IMPORT_SET, oldImportSet, newImportSet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setImportSet(ImportSet newImportSet) {
		if (newImportSet != importSet) {
			NotificationChain msgs = null;
			if (importSet != null)
				msgs = ((InternalEObject)importSet).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PeperPackage.IMPORT_OBJECT__IMPORT_SET, null, msgs);
			if (newImportSet != null)
				msgs = ((InternalEObject)newImportSet).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PeperPackage.IMPORT_OBJECT__IMPORT_SET, null, msgs);
			msgs = basicSetImportSet(newImportSet, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.IMPORT_OBJECT__IMPORT_SET, newImportSet, newImportSet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ConvertJob getConvertJob() {
		if (eContainerFeatureID != PeperPackage.IMPORT_OBJECT__CONVERT_JOB) return null;
		return (ConvertJob)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetConvertJob(ConvertJob newConvertJob, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newConvertJob, PeperPackage.IMPORT_OBJECT__CONVERT_JOB, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setConvertJob(ConvertJob newConvertJob) {
		if (newConvertJob != eInternalContainer() || (eContainerFeatureID != PeperPackage.IMPORT_OBJECT__CONVERT_JOB && newConvertJob != null)) {
			if (EcoreUtil.isAncestor(this, newConvertJob))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newConvertJob != null)
				msgs = ((InternalEObject)newConvertJob).eInverseAdd(this, PeperPackage.CONVERT_JOB__IMPORT_OBJECTS, ConvertJob.class, msgs);
			msgs = basicSetConvertJob(newConvertJob, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.IMPORT_OBJECT__CONVERT_JOB, newConvertJob, newConvertJob));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case PeperPackage.IMPORT_OBJECT__CONVERT_JOB:
				if (eInternalContainer() != null)
					msgs = eBasicRemoveFromContainer(msgs);
				return basicSetConvertJob((ConvertJob)otherEnd, msgs);
		}
		return super.eInverseAdd(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case PeperPackage.IMPORT_OBJECT__IMPORTER:
				return basicSetImporter(null, msgs);
			case PeperPackage.IMPORT_OBJECT__IMPORT_SET:
				return basicSetImportSet(null, msgs);
			case PeperPackage.IMPORT_OBJECT__CONVERT_JOB:
				return basicSetConvertJob(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eBasicRemoveFromContainerFeature(NotificationChain msgs) {
		switch (eContainerFeatureID) {
			case PeperPackage.IMPORT_OBJECT__CONVERT_JOB:
				return eInternalContainer().eInverseRemove(this, PeperPackage.CONVERT_JOB__IMPORT_OBJECTS, ConvertJob.class, msgs);
		}
		return super.eBasicRemoveFromContainerFeature(msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PeperPackage.IMPORT_OBJECT__IMPORTER:
				return getImporter();
			case PeperPackage.IMPORT_OBJECT__IMPORT_SET:
				return getImportSet();
			case PeperPackage.IMPORT_OBJECT__CONVERT_JOB:
				return getConvertJob();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case PeperPackage.IMPORT_OBJECT__IMPORTER:
				setImporter((Importer)newValue);
				return;
			case PeperPackage.IMPORT_OBJECT__IMPORT_SET:
				setImportSet((ImportSet)newValue);
				return;
			case PeperPackage.IMPORT_OBJECT__CONVERT_JOB:
				setConvertJob((ConvertJob)newValue);
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
			case PeperPackage.IMPORT_OBJECT__IMPORTER:
				setImporter((Importer)null);
				return;
			case PeperPackage.IMPORT_OBJECT__IMPORT_SET:
				setImportSet((ImportSet)null);
				return;
			case PeperPackage.IMPORT_OBJECT__CONVERT_JOB:
				setConvertJob((ConvertJob)null);
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
			case PeperPackage.IMPORT_OBJECT__IMPORTER:
				return importer != null;
			case PeperPackage.IMPORT_OBJECT__IMPORT_SET:
				return importSet != null;
			case PeperPackage.IMPORT_OBJECT__CONVERT_JOB:
				return getConvertJob() != null;
		}
		return super.eIsSet(featureID);
	}

} //ImportObjectImpl
