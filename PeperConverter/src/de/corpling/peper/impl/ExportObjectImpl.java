/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.impl;

import de.corpling.peper.ConvertJob;
import de.corpling.peper.ExportObject;
import de.corpling.peper.ExportSet;
import de.corpling.peper.Exporter;
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
 * An implementation of the model object '<em><b>Export Object</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.impl.ExportObjectImpl#getExportSet <em>Export Set</em>}</li>
 *   <li>{@link de.corpling.peper.impl.ExportObjectImpl#getExporter <em>Exporter</em>}</li>
 *   <li>{@link de.corpling.peper.impl.ExportObjectImpl#getConvertJob <em>Convert Job</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ExportObjectImpl extends EObjectImpl implements ExportObject {
	/**
	 * The cached value of the '{@link #getExportSet() <em>Export Set</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExportSet()
	 * @generated
	 * @ordered
	 */
	protected ExportSet exportSet;

	/**
	 * The cached value of the '{@link #getExporter() <em>Exporter</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExporter()
	 * @generated
	 * @ordered
	 */
	protected Exporter exporter;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ExportObjectImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PeperPackage.Literals.EXPORT_OBJECT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ExportSet getExportSet() {
		return exportSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetExportSet(ExportSet newExportSet, NotificationChain msgs) {
		ExportSet oldExportSet = exportSet;
		exportSet = newExportSet;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PeperPackage.EXPORT_OBJECT__EXPORT_SET, oldExportSet, newExportSet);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setExportSet(ExportSet newExportSet) {
		if (newExportSet != exportSet) {
			NotificationChain msgs = null;
			if (exportSet != null)
				msgs = ((InternalEObject)exportSet).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PeperPackage.EXPORT_OBJECT__EXPORT_SET, null, msgs);
			if (newExportSet != null)
				msgs = ((InternalEObject)newExportSet).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PeperPackage.EXPORT_OBJECT__EXPORT_SET, null, msgs);
			msgs = basicSetExportSet(newExportSet, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.EXPORT_OBJECT__EXPORT_SET, newExportSet, newExportSet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Exporter getExporter() {
		return exporter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetExporter(Exporter newExporter, NotificationChain msgs) {
		Exporter oldExporter = exporter;
		exporter = newExporter;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, PeperPackage.EXPORT_OBJECT__EXPORTER, oldExporter, newExporter);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setExporter(Exporter newExporter) {
		if (newExporter != exporter) {
			NotificationChain msgs = null;
			if (exporter != null)
				msgs = ((InternalEObject)exporter).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - PeperPackage.EXPORT_OBJECT__EXPORTER, null, msgs);
			if (newExporter != null)
				msgs = ((InternalEObject)newExporter).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - PeperPackage.EXPORT_OBJECT__EXPORTER, null, msgs);
			msgs = basicSetExporter(newExporter, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.EXPORT_OBJECT__EXPORTER, newExporter, newExporter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ConvertJob getConvertJob() {
		if (eContainerFeatureID != PeperPackage.EXPORT_OBJECT__CONVERT_JOB) return null;
		return (ConvertJob)eContainer();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetConvertJob(ConvertJob newConvertJob, NotificationChain msgs) {
		msgs = eBasicSetContainer((InternalEObject)newConvertJob, PeperPackage.EXPORT_OBJECT__CONVERT_JOB, msgs);
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setConvertJob(ConvertJob newConvertJob) {
		if (newConvertJob != eInternalContainer() || (eContainerFeatureID != PeperPackage.EXPORT_OBJECT__CONVERT_JOB && newConvertJob != null)) {
			if (EcoreUtil.isAncestor(this, newConvertJob))
				throw new IllegalArgumentException("Recursive containment not allowed for " + toString());
			NotificationChain msgs = null;
			if (eInternalContainer() != null)
				msgs = eBasicRemoveFromContainer(msgs);
			if (newConvertJob != null)
				msgs = ((InternalEObject)newConvertJob).eInverseAdd(this, PeperPackage.CONVERT_JOB__EXPORT_OBJECTS, ConvertJob.class, msgs);
			msgs = basicSetConvertJob(newConvertJob, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.EXPORT_OBJECT__CONVERT_JOB, newConvertJob, newConvertJob));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case PeperPackage.EXPORT_OBJECT__CONVERT_JOB:
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
			case PeperPackage.EXPORT_OBJECT__EXPORT_SET:
				return basicSetExportSet(null, msgs);
			case PeperPackage.EXPORT_OBJECT__EXPORTER:
				return basicSetExporter(null, msgs);
			case PeperPackage.EXPORT_OBJECT__CONVERT_JOB:
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
			case PeperPackage.EXPORT_OBJECT__CONVERT_JOB:
				return eInternalContainer().eInverseRemove(this, PeperPackage.CONVERT_JOB__EXPORT_OBJECTS, ConvertJob.class, msgs);
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
			case PeperPackage.EXPORT_OBJECT__EXPORT_SET:
				return getExportSet();
			case PeperPackage.EXPORT_OBJECT__EXPORTER:
				return getExporter();
			case PeperPackage.EXPORT_OBJECT__CONVERT_JOB:
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
			case PeperPackage.EXPORT_OBJECT__EXPORT_SET:
				setExportSet((ExportSet)newValue);
				return;
			case PeperPackage.EXPORT_OBJECT__EXPORTER:
				setExporter((Exporter)newValue);
				return;
			case PeperPackage.EXPORT_OBJECT__CONVERT_JOB:
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
			case PeperPackage.EXPORT_OBJECT__EXPORT_SET:
				setExportSet((ExportSet)null);
				return;
			case PeperPackage.EXPORT_OBJECT__EXPORTER:
				setExporter((Exporter)null);
				return;
			case PeperPackage.EXPORT_OBJECT__CONVERT_JOB:
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
			case PeperPackage.EXPORT_OBJECT__EXPORT_SET:
				return exportSet != null;
			case PeperPackage.EXPORT_OBJECT__EXPORTER:
				return exporter != null;
			case PeperPackage.EXPORT_OBJECT__CONVERT_JOB:
				return getConvertJob() != null;
		}
		return super.eIsSet(featureID);
	}

} //ExportObjectImpl
