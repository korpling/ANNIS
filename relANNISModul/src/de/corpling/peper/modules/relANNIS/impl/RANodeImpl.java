/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.impl;

import de.corpling.peper.modules.relANNIS.RANode;
import de.corpling.peper.modules.relANNIS.RelANNISPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>RA Struct</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RANodeImpl#getId <em>Id</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RANodeImpl#getText_ref <em>Text ref</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RANodeImpl#getCorpus_ref <em>Corpus ref</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RANodeImpl#getNamespace <em>Namespace</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RANodeImpl#getName <em>Name</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RANodeImpl#getLeft <em>Left</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RANodeImpl#getRight <em>Right</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RANodeImpl#getToken_index <em>Token index</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RANodeImpl#isContinuous <em>Continuous</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RANodeImpl#getSpan <em>Span</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RANodeImpl extends EObjectImpl implements RANode 
{
	private static final String MSG_ERR= "Error("+RANode.class+")";
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final Long ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected Long id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getText_ref() <em>Text ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getText_ref()
	 * @generated
	 * @ordered
	 */
	protected static final Long TEXT_REF_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getText_ref() <em>Text ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getText_ref()
	 * @generated
	 * @ordered
	 */
	protected Long text_ref = TEXT_REF_EDEFAULT;

	/**
	 * The default value of the '{@link #getCorpus_ref() <em>Corpus ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCorpus_ref()
	 * @generated
	 * @ordered
	 */
	protected static final Long CORPUS_REF_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCorpus_ref() <em>Corpus ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCorpus_ref()
	 * @generated
	 * @ordered
	 */
	protected Long corpus_ref = CORPUS_REF_EDEFAULT;

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
	 * The default value of the '{@link #getLeft() <em>Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLeft()
	 * @generated
	 * @ordered
	 */
	protected static final Long LEFT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLeft() <em>Left</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLeft()
	 * @generated
	 * @ordered
	 */
	protected Long left = LEFT_EDEFAULT;

	/**
	 * The default value of the '{@link #getRight() <em>Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRight()
	 * @generated
	 * @ordered
	 */
	protected static final Long RIGHT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRight() <em>Right</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRight()
	 * @generated
	 * @ordered
	 */
	protected Long right = RIGHT_EDEFAULT;

	/**
	 * The default value of the '{@link #getToken_index() <em>Token index</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getToken_index()
	 * @generated
	 * @ordered
	 */
	protected static final Long TOKEN_INDEX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getToken_index() <em>Token index</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getToken_index()
	 * @generated
	 * @ordered
	 */
	protected Long token_index = TOKEN_INDEX_EDEFAULT;

	/**
	 * The default value of the '{@link #isContinuous() <em>Continuous</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isContinuous()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CONTINUOUS_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isContinuous() <em>Continuous</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isContinuous()
	 * @generated
	 * @ordered
	 */
	protected boolean continuous = CONTINUOUS_EDEFAULT;

	/**
	 * The default value of the '{@link #getSpan() <em>Span</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSpan()
	 * @generated
	 * @ordered
	 */
	protected static final String SPAN_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSpan() <em>Span</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSpan()
	 * @generated
	 * @ordered
	 */
	protected String span = SPAN_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RANodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RelANNISPackage.Literals.RA_NODE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setId(Long newId) {
		Long oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_NODE__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getText_ref() {
		return text_ref;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setText_ref(Long newText_ref) {
		Long oldText_ref = text_ref;
		text_ref = newText_ref;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_NODE__TEXT_REF, oldText_ref, text_ref));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getCorpus_ref() {
		return corpus_ref;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCorpus_ref(Long newCorpus_ref) {
		Long oldCorpus_ref = corpus_ref;
		corpus_ref = newCorpus_ref;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_NODE__CORPUS_REF, oldCorpus_ref, corpus_ref));
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
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_NODE__NAMESPACE, oldNamespace, namespace));
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
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_NODE__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getLeft() {
		return left;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void setLeft(Long newLeft) 
	{
		//checking that left isn´ t higher than right
		if ((this.getRight()!= null) && (this.getRight()< newLeft))
			throw new NullPointerException(MSG_ERR + "Cannot set left, because settet right is higher.");
		Long oldLeft = left;
		left = newLeft;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_NODE__LEFT, oldLeft, left));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getRight() {
		return right;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void setRight(Long newRight) 
	{
		//checking that left isn´ t higher than right
		if ((this.getLeft()!= null) && (this.getLeft()> newRight))
			throw new NullPointerException(MSG_ERR + "Cannot set right, because settet right is higher. Error in node: "+ this.getId());

		Long oldRight = right;
		right = newRight;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_NODE__RIGHT, oldRight, right));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getToken_index() {
		return token_index;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setToken_index(Long newToken_index) {
		Long oldToken_index = token_index;
		token_index = newToken_index;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_NODE__TOKEN_INDEX, oldToken_index, token_index));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean isContinuous() {
		return continuous;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setContinuous(boolean newContinuous) {
		boolean oldContinuous = continuous;
		continuous = newContinuous;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_NODE__CONTINUOUS, oldContinuous, continuous));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getSpan() {
		return span;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setSpan(String newSpan) {
		String oldSpan = span;
		span = newSpan;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_NODE__SPAN, oldSpan, span));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public EList<String> toStringList() 
	{
		EList<String> retList= new BasicEList<String>();
		//id 
		if (this.getId()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getId()));
		//text_ref 
		if (this.getText_ref()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getText_ref()));
		//corpus_ref
		if (this.getCorpus_ref()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getCorpus_ref()));
		//namespace	
		if (this.getNamespace()== null) retList.add("NULL");
		else retList.add(this.getNamespace());
		//name		
		if (this.getName()== null) retList.add("NULL");
		else retList.add(this.getName());
		//left 		
		if (this.getLeft()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getLeft()));
		//right
		if (this.getRight()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getRight()));
		//token_index
		if (this.getToken_index()== null)retList.add(String.valueOf("NULL"));	
		else retList.add(String.valueOf(this.getToken_index()));
		//continuous 
		retList.add(String.valueOf(this.isContinuous()));
		//span
		if (this.getToken_index()== null)retList.add(String.valueOf("NULL"));	
		else retList.add(this.getSpan());
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
			case RelANNISPackage.RA_NODE__ID:
				return getId();
			case RelANNISPackage.RA_NODE__TEXT_REF:
				return getText_ref();
			case RelANNISPackage.RA_NODE__CORPUS_REF:
				return getCorpus_ref();
			case RelANNISPackage.RA_NODE__NAMESPACE:
				return getNamespace();
			case RelANNISPackage.RA_NODE__NAME:
				return getName();
			case RelANNISPackage.RA_NODE__LEFT:
				return getLeft();
			case RelANNISPackage.RA_NODE__RIGHT:
				return getRight();
			case RelANNISPackage.RA_NODE__TOKEN_INDEX:
				return getToken_index();
			case RelANNISPackage.RA_NODE__CONTINUOUS:
				return isContinuous() ? Boolean.TRUE : Boolean.FALSE;
			case RelANNISPackage.RA_NODE__SPAN:
				return getSpan();
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
			case RelANNISPackage.RA_NODE__ID:
				setId((Long)newValue);
				return;
			case RelANNISPackage.RA_NODE__TEXT_REF:
				setText_ref((Long)newValue);
				return;
			case RelANNISPackage.RA_NODE__CORPUS_REF:
				setCorpus_ref((Long)newValue);
				return;
			case RelANNISPackage.RA_NODE__NAMESPACE:
				setNamespace((String)newValue);
				return;
			case RelANNISPackage.RA_NODE__NAME:
				setName((String)newValue);
				return;
			case RelANNISPackage.RA_NODE__LEFT:
				setLeft((Long)newValue);
				return;
			case RelANNISPackage.RA_NODE__RIGHT:
				setRight((Long)newValue);
				return;
			case RelANNISPackage.RA_NODE__TOKEN_INDEX:
				setToken_index((Long)newValue);
				return;
			case RelANNISPackage.RA_NODE__CONTINUOUS:
				setContinuous(((Boolean)newValue).booleanValue());
				return;
			case RelANNISPackage.RA_NODE__SPAN:
				setSpan((String)newValue);
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
			case RelANNISPackage.RA_NODE__ID:
				setId(ID_EDEFAULT);
				return;
			case RelANNISPackage.RA_NODE__TEXT_REF:
				setText_ref(TEXT_REF_EDEFAULT);
				return;
			case RelANNISPackage.RA_NODE__CORPUS_REF:
				setCorpus_ref(CORPUS_REF_EDEFAULT);
				return;
			case RelANNISPackage.RA_NODE__NAMESPACE:
				setNamespace(NAMESPACE_EDEFAULT);
				return;
			case RelANNISPackage.RA_NODE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case RelANNISPackage.RA_NODE__LEFT:
				setLeft(LEFT_EDEFAULT);
				return;
			case RelANNISPackage.RA_NODE__RIGHT:
				setRight(RIGHT_EDEFAULT);
				return;
			case RelANNISPackage.RA_NODE__TOKEN_INDEX:
				setToken_index(TOKEN_INDEX_EDEFAULT);
				return;
			case RelANNISPackage.RA_NODE__CONTINUOUS:
				setContinuous(CONTINUOUS_EDEFAULT);
				return;
			case RelANNISPackage.RA_NODE__SPAN:
				setSpan(SPAN_EDEFAULT);
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
			case RelANNISPackage.RA_NODE__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case RelANNISPackage.RA_NODE__TEXT_REF:
				return TEXT_REF_EDEFAULT == null ? text_ref != null : !TEXT_REF_EDEFAULT.equals(text_ref);
			case RelANNISPackage.RA_NODE__CORPUS_REF:
				return CORPUS_REF_EDEFAULT == null ? corpus_ref != null : !CORPUS_REF_EDEFAULT.equals(corpus_ref);
			case RelANNISPackage.RA_NODE__NAMESPACE:
				return NAMESPACE_EDEFAULT == null ? namespace != null : !NAMESPACE_EDEFAULT.equals(namespace);
			case RelANNISPackage.RA_NODE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case RelANNISPackage.RA_NODE__LEFT:
				return LEFT_EDEFAULT == null ? left != null : !LEFT_EDEFAULT.equals(left);
			case RelANNISPackage.RA_NODE__RIGHT:
				return RIGHT_EDEFAULT == null ? right != null : !RIGHT_EDEFAULT.equals(right);
			case RelANNISPackage.RA_NODE__TOKEN_INDEX:
				return TOKEN_INDEX_EDEFAULT == null ? token_index != null : !TOKEN_INDEX_EDEFAULT.equals(token_index);
			case RelANNISPackage.RA_NODE__CONTINUOUS:
				return continuous != CONTINUOUS_EDEFAULT;
			case RelANNISPackage.RA_NODE__SPAN:
				return SPAN_EDEFAULT == null ? span != null : !SPAN_EDEFAULT.equals(span);
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
		result.append(" (id: ");
		result.append(id);
		result.append(", text_ref: ");
		result.append(text_ref);
		result.append(", corpus_ref: ");
		result.append(corpus_ref);
		result.append(", namespace: ");
		result.append(namespace);
		result.append(", name: ");
		result.append(name);
		result.append(", left: ");
		result.append(left);
		result.append(", right: ");
		result.append(right);
		result.append(", token_index: ");
		result.append(token_index);
		result.append(", continuous: ");
		result.append(continuous);
		result.append(", span: ");
		result.append(span);
		result.append(')');
		return result.toString();
	}

} //RAStructImpl
