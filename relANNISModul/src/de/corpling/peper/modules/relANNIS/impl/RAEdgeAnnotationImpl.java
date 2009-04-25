/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.impl;

import de.corpling.peper.modules.relANNIS.RAEdgeAnnotation;
import de.corpling.peper.modules.relANNIS.RelANNISPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>RA Edge Annotation</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeAnnotationImpl#getEdge_ref <em>Edge ref</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeAnnotationImpl#getNamespace <em>Namespace</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeAnnotationImpl#getName <em>Name</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeAnnotationImpl#getValue <em>Value</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RAEdgeAnnotationImpl extends EObjectImpl implements RAEdgeAnnotation {
	/**
	 * The default value of the '{@link #getEdge_ref() <em>Edge ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEdge_ref()
	 * @generated
	 * @ordered
	 */
	protected static final Long EDGE_REF_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getEdge_ref() <em>Edge ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEdge_ref()
	 * @generated
	 * @ordered
	 */
	protected Long edge_ref = EDGE_REF_EDEFAULT;

	/**
	 * The default value of the '{@link #getNamespace() <em>Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamespace()
	 * @generated
	 * @ordered
	 */
	protected static final String NAMESPACE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNamespace() <em>Namespace</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNamespace()
	 * @generated
	 * @ordered
	 */
	protected String namespace = NAMESPACE_EDEFAULT;

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
	 * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected static final String VALUE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getValue()
	 * @generated
	 * @ordered
	 */
	protected String value = VALUE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RAEdgeAnnotationImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RelANNISPackage.Literals.RA_EDGE_ANNOTATION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getEdge_ref() {
		return edge_ref;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setEdge_ref(Long newEdge_ref) {
		Long oldEdge_ref = edge_ref;
		edge_ref = newEdge_ref;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EDGE_ANNOTATION__EDGE_REF, oldEdge_ref, edge_ref));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNamespace(String newNamespace) {
		String oldNamespace = namespace;
		namespace = newNamespace;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EDGE_ANNOTATION__NAMESPACE, oldNamespace, namespace));
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
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EDGE_ANNOTATION__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getValue() {
		return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setValue(String newValue) {
		String oldValue = value;
		value = newValue;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EDGE_ANNOTATION__VALUE, oldValue, value));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public EList<String> toStringList() 
	{
		EList<String> retList= new BasicEList<String>();
		//edge_ref
		if (this.getEdge_ref()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getEdge_ref()));
		//namespace	
		if (this.getNamespace()== null) retList.add("NULL");
		else retList.add(this.getNamespace());
		//name
		if (this.getName()== null) retList.add("NULL");
		else retList.add(this.getName());
		//value
		if (this.getValue()== null) retList.add("NULL");
		else retList.add(this.getValue());
		return(retList);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RelANNISPackage.RA_EDGE_ANNOTATION__EDGE_REF:
				return getEdge_ref();
			case RelANNISPackage.RA_EDGE_ANNOTATION__NAMESPACE:
				return getNamespace();
			case RelANNISPackage.RA_EDGE_ANNOTATION__NAME:
				return getName();
			case RelANNISPackage.RA_EDGE_ANNOTATION__VALUE:
				return getValue();
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
			case RelANNISPackage.RA_EDGE_ANNOTATION__EDGE_REF:
				setEdge_ref((Long)newValue);
				return;
			case RelANNISPackage.RA_EDGE_ANNOTATION__NAMESPACE:
				setNamespace((String)newValue);
				return;
			case RelANNISPackage.RA_EDGE_ANNOTATION__NAME:
				setName((String)newValue);
				return;
			case RelANNISPackage.RA_EDGE_ANNOTATION__VALUE:
				setValue((String)newValue);
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
			case RelANNISPackage.RA_EDGE_ANNOTATION__EDGE_REF:
				setEdge_ref(EDGE_REF_EDEFAULT);
				return;
			case RelANNISPackage.RA_EDGE_ANNOTATION__NAMESPACE:
				setNamespace(NAMESPACE_EDEFAULT);
				return;
			case RelANNISPackage.RA_EDGE_ANNOTATION__NAME:
				setName(NAME_EDEFAULT);
				return;
			case RelANNISPackage.RA_EDGE_ANNOTATION__VALUE:
				setValue(VALUE_EDEFAULT);
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
			case RelANNISPackage.RA_EDGE_ANNOTATION__EDGE_REF:
				return EDGE_REF_EDEFAULT == null ? edge_ref != null : !EDGE_REF_EDEFAULT.equals(edge_ref);
			case RelANNISPackage.RA_EDGE_ANNOTATION__NAMESPACE:
				return NAMESPACE_EDEFAULT == null ? namespace != null : !NAMESPACE_EDEFAULT.equals(namespace);
			case RelANNISPackage.RA_EDGE_ANNOTATION__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case RelANNISPackage.RA_EDGE_ANNOTATION__VALUE:
				return VALUE_EDEFAULT == null ? value != null : !VALUE_EDEFAULT.equals(value);
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
		result.append(" (edge_ref: ");
		result.append(edge_ref);
		result.append(", namespace: ");
		result.append(namespace);
		result.append(", name: ");
		result.append(name);
		result.append(", value: ");
		result.append(value);
		result.append(')');
		return result.toString();
	}

} //RAEdgeAnnotationImpl
