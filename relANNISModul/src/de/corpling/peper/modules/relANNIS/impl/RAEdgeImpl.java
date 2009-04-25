/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.impl;

import de.corpling.peper.modules.relANNIS.RAEdge;
import de.corpling.peper.modules.relANNIS.RelANNISPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>RA Rank</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeImpl#getPre <em>Pre</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeImpl#getPost <em>Post</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeImpl#getNode_ref <em>Node ref</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeImpl#getComponent_ref <em>Component ref</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAEdgeImpl#getParent <em>Parent</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RAEdgeImpl extends EObjectImpl implements RAEdge {
	/**
	 * The default value of the '{@link #getPre() <em>Pre</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPre()
	 * @generated
	 * @ordered
	 */
	protected static final Long PRE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPre() <em>Pre</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPre()
	 * @generated
	 * @ordered
	 */
	protected Long pre = PRE_EDEFAULT;

	/**
	 * The default value of the '{@link #getPost() <em>Post</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPost()
	 * @generated
	 * @ordered
	 */
	protected static final Long POST_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPost() <em>Post</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPost()
	 * @generated
	 * @ordered
	 */
	protected Long post = POST_EDEFAULT;

	/**
	 * The default value of the '{@link #getNode_ref() <em>Node ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNode_ref()
	 * @generated
	 * @ordered
	 */
	protected static final Long NODE_REF_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNode_ref() <em>Node ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNode_ref()
	 * @generated
	 * @ordered
	 */
	protected Long node_ref = NODE_REF_EDEFAULT;

	/**
	 * The default value of the '{@link #getComponent_ref() <em>Component ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComponent_ref()
	 * @generated
	 * @ordered
	 */
	protected static final Long COMPONENT_REF_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getComponent_ref() <em>Component ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComponent_ref()
	 * @generated
	 * @ordered
	 */
	protected Long component_ref = COMPONENT_REF_EDEFAULT;

	/**
	 * The default value of the '{@link #getParent() <em>Parent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParent()
	 * @generated
	 * @ordered
	 */
	protected static final Long PARENT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getParent() <em>Parent</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParent()
	 * @generated
	 * @ordered
	 */
	protected Long parent = PARENT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RAEdgeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RelANNISPackage.Literals.RA_EDGE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getPre() {
		return pre;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPre(Long newPre) {
		Long oldPre = pre;
		pre = newPre;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EDGE__PRE, oldPre, pre));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getPost() {
		return post;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setPost(Long newPost) {
		Long oldPost = post;
		post = newPost;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EDGE__POST, oldPost, post));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getNode_ref() {
		return node_ref;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setNode_ref(Long newNode_ref) {
		Long oldNode_ref = node_ref;
		node_ref = newNode_ref;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EDGE__NODE_REF, oldNode_ref, node_ref));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getComponent_ref() {
		return component_ref;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setComponent_ref(Long newComponent_ref) {
		Long oldComponent_ref = component_ref;
		component_ref = newComponent_ref;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EDGE__COMPONENT_REF, oldComponent_ref, component_ref));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getParent() {
		return parent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParent(Long newParent) {
		Long oldParent = parent;
		parent = newParent;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EDGE__PARENT, oldParent, parent));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public EList<String> toStringList() 
	{
		EList<String> retList= new BasicEList<String>();
		//pre value
		if (this.getPre()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getPre()));
		//post value
		if (this.getPost()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getPost()));
		//struct_ref
		if (this.getNode_ref()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getNode_ref()));
		//coherence component	
		if (this.getComponent_ref()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getComponent_ref()));
		//parent pre of parent element
		if (this.getParent()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getParent()));
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
			case RelANNISPackage.RA_EDGE__PRE:
				return getPre();
			case RelANNISPackage.RA_EDGE__POST:
				return getPost();
			case RelANNISPackage.RA_EDGE__NODE_REF:
				return getNode_ref();
			case RelANNISPackage.RA_EDGE__COMPONENT_REF:
				return getComponent_ref();
			case RelANNISPackage.RA_EDGE__PARENT:
				return getParent();
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
			case RelANNISPackage.RA_EDGE__PRE:
				setPre((Long)newValue);
				return;
			case RelANNISPackage.RA_EDGE__POST:
				setPost((Long)newValue);
				return;
			case RelANNISPackage.RA_EDGE__NODE_REF:
				setNode_ref((Long)newValue);
				return;
			case RelANNISPackage.RA_EDGE__COMPONENT_REF:
				setComponent_ref((Long)newValue);
				return;
			case RelANNISPackage.RA_EDGE__PARENT:
				setParent((Long)newValue);
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
			case RelANNISPackage.RA_EDGE__PRE:
				setPre(PRE_EDEFAULT);
				return;
			case RelANNISPackage.RA_EDGE__POST:
				setPost(POST_EDEFAULT);
				return;
			case RelANNISPackage.RA_EDGE__NODE_REF:
				setNode_ref(NODE_REF_EDEFAULT);
				return;
			case RelANNISPackage.RA_EDGE__COMPONENT_REF:
				setComponent_ref(COMPONENT_REF_EDEFAULT);
				return;
			case RelANNISPackage.RA_EDGE__PARENT:
				setParent(PARENT_EDEFAULT);
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
			case RelANNISPackage.RA_EDGE__PRE:
				return PRE_EDEFAULT == null ? pre != null : !PRE_EDEFAULT.equals(pre);
			case RelANNISPackage.RA_EDGE__POST:
				return POST_EDEFAULT == null ? post != null : !POST_EDEFAULT.equals(post);
			case RelANNISPackage.RA_EDGE__NODE_REF:
				return NODE_REF_EDEFAULT == null ? node_ref != null : !NODE_REF_EDEFAULT.equals(node_ref);
			case RelANNISPackage.RA_EDGE__COMPONENT_REF:
				return COMPONENT_REF_EDEFAULT == null ? component_ref != null : !COMPONENT_REF_EDEFAULT.equals(component_ref);
			case RelANNISPackage.RA_EDGE__PARENT:
				return PARENT_EDEFAULT == null ? parent != null : !PARENT_EDEFAULT.equals(parent);
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
		result.append(" (pre: ");
		result.append(pre);
		result.append(", post: ");
		result.append(post);
		result.append(", node_ref: ");
		result.append(node_ref);
		result.append(", component_ref: ");
		result.append(component_ref);
		result.append(", parent: ");
		result.append(parent);
		result.append(')');
		return result.toString();
	}

} //RARankImpl
