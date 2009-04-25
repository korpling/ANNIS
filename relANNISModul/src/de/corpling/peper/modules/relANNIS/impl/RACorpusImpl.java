/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.impl;

import de.corpling.peper.modules.relANNIS.RACorpus;
import de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE;
import de.corpling.peper.modules.relANNIS.RelANNISPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>RA Corpus</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RACorpusImpl#getId <em>Id</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RACorpusImpl#getName <em>Name</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RACorpusImpl#getType <em>Type</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RACorpusImpl#getVersion <em>Version</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RACorpusImpl#getPre <em>Pre</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RACorpusImpl#getPost <em>Post</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RACorpusImpl extends EObjectImpl implements RACorpus {
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
	 * The default value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected static final RA_CORPUS_TYPE TYPE_EDEFAULT = RA_CORPUS_TYPE.CORPUS;

	/**
	 * The cached value of the '{@link #getType() <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getType()
	 * @generated
	 * @ordered
	 */
	protected RA_CORPUS_TYPE type = TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVersion()
	 * @generated
	 * @ordered
	 */
	protected static final String VERSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVersion()
	 * @generated
	 * @ordered
	 */
	protected String version = VERSION_EDEFAULT;

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
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RACorpusImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RelANNISPackage.Literals.RA_CORPUS;
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
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_CORPUS__ID, oldId, id));
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
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_CORPUS__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RA_CORPUS_TYPE getType() {
		return type;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setType(RA_CORPUS_TYPE newType) {
		RA_CORPUS_TYPE oldType = type;
		type = newType == null ? TYPE_EDEFAULT : newType;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_CORPUS__TYPE, oldType, type));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setVersion(String newVersion) {
		String oldVersion = version;
		version = newVersion;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_CORPUS__VERSION, oldVersion, version));
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
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_CORPUS__PRE, oldPre, pre));
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
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_CORPUS__POST, oldPost, post));
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
		//name
		if (this.getName()== null) retList.add("NULL");
		else retList.add(this.getName());
		//type
		if (this.getType()== null) retList.add("NULL");
		else retList.add(this.getType().toString());
		//version
		if (this.getVersion()== null) retList.add("NULL");
		else retList.add(this.getVersion());
		//pre
		if (this.getPre()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getPre()));
		//post
		if (this.getPost()== null) retList.add("NULL");
		else retList.add(String.valueOf(this.getPost()));
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
			case RelANNISPackage.RA_CORPUS__ID:
				return getId();
			case RelANNISPackage.RA_CORPUS__NAME:
				return getName();
			case RelANNISPackage.RA_CORPUS__TYPE:
				return getType();
			case RelANNISPackage.RA_CORPUS__VERSION:
				return getVersion();
			case RelANNISPackage.RA_CORPUS__PRE:
				return getPre();
			case RelANNISPackage.RA_CORPUS__POST:
				return getPost();
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
			case RelANNISPackage.RA_CORPUS__ID:
				setId((Long)newValue);
				return;
			case RelANNISPackage.RA_CORPUS__NAME:
				setName((String)newValue);
				return;
			case RelANNISPackage.RA_CORPUS__TYPE:
				setType((RA_CORPUS_TYPE)newValue);
				return;
			case RelANNISPackage.RA_CORPUS__VERSION:
				setVersion((String)newValue);
				return;
			case RelANNISPackage.RA_CORPUS__PRE:
				setPre((Long)newValue);
				return;
			case RelANNISPackage.RA_CORPUS__POST:
				setPost((Long)newValue);
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
			case RelANNISPackage.RA_CORPUS__ID:
				setId(ID_EDEFAULT);
				return;
			case RelANNISPackage.RA_CORPUS__NAME:
				setName(NAME_EDEFAULT);
				return;
			case RelANNISPackage.RA_CORPUS__TYPE:
				setType(TYPE_EDEFAULT);
				return;
			case RelANNISPackage.RA_CORPUS__VERSION:
				setVersion(VERSION_EDEFAULT);
				return;
			case RelANNISPackage.RA_CORPUS__PRE:
				setPre(PRE_EDEFAULT);
				return;
			case RelANNISPackage.RA_CORPUS__POST:
				setPost(POST_EDEFAULT);
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
			case RelANNISPackage.RA_CORPUS__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case RelANNISPackage.RA_CORPUS__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case RelANNISPackage.RA_CORPUS__TYPE:
				return type != TYPE_EDEFAULT;
			case RelANNISPackage.RA_CORPUS__VERSION:
				return VERSION_EDEFAULT == null ? version != null : !VERSION_EDEFAULT.equals(version);
			case RelANNISPackage.RA_CORPUS__PRE:
				return PRE_EDEFAULT == null ? pre != null : !PRE_EDEFAULT.equals(pre);
			case RelANNISPackage.RA_CORPUS__POST:
				return POST_EDEFAULT == null ? post != null : !POST_EDEFAULT.equals(post);
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
		result.append(", name: ");
		result.append(name);
		result.append(", type: ");
		result.append(type);
		result.append(", version: ");
		result.append(version);
		result.append(", pre: ");
		result.append(pre);
		result.append(", post: ");
		result.append(post);
		result.append(')');
		return result.toString();
	}

} //RACorpusImpl
