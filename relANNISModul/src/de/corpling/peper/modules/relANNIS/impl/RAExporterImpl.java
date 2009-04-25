/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.impl;

import de.corpling.peper.impl.ExporterImpl;
import de.corpling.peper.modules.relANNIS.RADAO;
import de.corpling.peper.modules.relANNIS.RAExporter;
import de.corpling.peper.modules.relANNIS.RAMapper;
import de.corpling.peper.modules.relANNIS.RelANNISFactory;
import de.corpling.peper.modules.relANNIS.RelANNISPackage;
import de.corpling.salt.model.salt.SCorpus;
import de.corpling.salt.model.salt.SDocument;
import de.corpling.salt.model.saltCore.SGraph;
import de.corpling.salt.model.saltCore.STRAVERSAL_MODE;

import java.io.File;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>RA Exporter</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAExporterImpl#getRaMapper <em>Ra Mapper</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAExporterImpl#getRaDAO <em>Ra DAO</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RAExporterImpl extends ExporterImpl implements RAExporter 
{
	private static String MSG_ERR=	"Error("+RAExporter.class+"): ";
	/**
	 * The cached value of the '{@link #getRaMapper() <em>Ra Mapper</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRaMapper()
	 * @generated
	 * @ordered
	 */
	protected RAMapper raMapper;

	/**
	 * The cached value of the '{@link #getRaDAO() <em>Ra DAO</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRaDAO()
	 * @generated
	 * @ordered
	 */
	protected RADAO raDAO;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public RAExporterImpl() {
		super();
		this.init();
	}

	/**
	 * Initializes this object
	 */
	private void init()
	{
		//create dao
		raDAO= RelANNISFactory.eINSTANCE.createRADAO();
		
//		//create new mapper
//		this.setRaMapper(RelANNISFactory.eINSTANCE.createRAMapper());
//		this.getRaMapper().setRaDAO(raDAO);
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RelANNISPackage.Literals.RA_EXPORTER;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void setOutputDir(File newOutputDir) {
		File oldOutputDir = outputDir;
		outputDir = newOutputDir;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EXPORTER__OUTPUT_DIR, oldOutputDir, outputDir));
		raDAO.setOutputDir(this.getOutputDir());
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RAMapper getRaMapper() {
		if (raMapper != null && raMapper.eIsProxy()) {
			InternalEObject oldRaMapper = (InternalEObject)raMapper;
			raMapper = (RAMapper)eResolveProxy(oldRaMapper);
			if (raMapper != oldRaMapper) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, RelANNISPackage.RA_EXPORTER__RA_MAPPER, oldRaMapper, raMapper));
			}
		}
		return raMapper;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RAMapper basicGetRaMapper() {
		return raMapper;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRaMapper(RAMapper newRaMapper, NotificationChain msgs) {
		RAMapper oldRaMapper = raMapper;
		raMapper = newRaMapper;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EXPORTER__RA_MAPPER, oldRaMapper, newRaMapper);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRaMapper(RAMapper newRaMapper) 
	{
		if (newRaMapper != raMapper) {
			NotificationChain msgs = null;
			if (raMapper != null)
				msgs = ((InternalEObject)raMapper).eInverseRemove(this, RelANNISPackage.RA_MAPPER__RA_EXPORTER, RAMapper.class, msgs);
			if (newRaMapper != null)
				msgs = ((InternalEObject)newRaMapper).eInverseAdd(this, RelANNISPackage.RA_MAPPER__RA_EXPORTER, RAMapper.class, msgs);
			msgs = basicSetRaMapper(newRaMapper, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EXPORTER__RA_MAPPER, newRaMapper, newRaMapper));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RADAO getRaDAO() {
		return raDAO;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRaDAO(RADAO newRaDAO, NotificationChain msgs) {
		RADAO oldRaDAO = raDAO;
		raDAO = newRaDAO;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EXPORTER__RA_DAO, oldRaDAO, newRaDAO);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRaDAO(RADAO newRaDAO) {
		if (newRaDAO != raDAO) {
			NotificationChain msgs = null;
			if (raDAO != null)
				msgs = ((InternalEObject)raDAO).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - RelANNISPackage.RA_EXPORTER__RA_DAO, null, msgs);
			if (newRaDAO != null)
				msgs = ((InternalEObject)newRaDAO).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - RelANNISPackage.RA_EXPORTER__RA_DAO, null, msgs);
			msgs = basicSetRaDAO(newRaDAO, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_EXPORTER__RA_DAO, newRaDAO, newRaDAO));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void export(SCorpus sCorpus) 
	{
		if (this.getSaltProject()== null)
			throw new NullPointerException(MSG_ERR + "Cannot export project, because project isn´t set.");
		if (this.getOutputDir()== null)
			throw new NullPointerException(MSG_ERR + "Cannot export project, because output directory.");
		if (sCorpus== null)
			throw new NullPointerException(MSG_ERR + "Cannot export project, because given corpus object is empty.");
		
		//create new mapper
		this.setRaMapper(RelANNISFactory.eINSTANCE.createRAMapper());
		this.getRaMapper().setRaDAO(raDAO);

		
		//searching for corpus through pathes
		for (SGraph sGraph: this.getSaltProject().getSaltGraphs())
		{
			//if graph doesn´t contain given corpus element
			if (sGraph.getNode(sCorpus.getId())== null)
				break;
			
			long taId= raDAO.beginTA();
			this.getRaMapper().setCurrTaId(taId);
			sGraph.traverseSGraph(STRAVERSAL_MODE.DEPTH_FIRST, sCorpus, this.getRaMapper(), null);
			raDAO.commitTA(taId);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void export(SDocument sDocument) 
	{
		if (this.getSaltProject()== null)
			throw new NullPointerException(MSG_ERR + "Cannot export project, because project isn´t set.");
		if (this.getOutputDir()== null)
			throw new NullPointerException(MSG_ERR + "Cannot export project, because output directory.");
		if (sDocument== null)
			throw new NullPointerException(MSG_ERR + "Cannot export project, because given document object is empty.");
		if (sDocument.getSDocumentGraph().getNumOfNodes()== 0)
			throw new NullPointerException(MSG_ERR + "Cannot export document graph, because there are no elements in graph.");
		//create new mapper
		long taId= raDAO.beginTA();
		this.setRaMapper(RelANNISFactory.eINSTANCE.createRAMapper());
		this.getRaMapper().setRaDAO(raDAO);
		this.getRaMapper().setCurrTaId(taId);
		this.getRaMapper().init(sDocument);
		((RAMapperImpl)this.getRaMapper()).export(sDocument);
		
		this.raMapper.close();
		this.raDAO.commitTA(taId);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RelANNISPackage.RA_EXPORTER__RA_MAPPER:
				if (raMapper != null)
					msgs = ((InternalEObject)raMapper).eInverseRemove(this, RelANNISPackage.RA_MAPPER__RA_EXPORTER, RAMapper.class, msgs);
				return basicSetRaMapper((RAMapper)otherEnd, msgs);
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
			case RelANNISPackage.RA_EXPORTER__RA_MAPPER:
				return basicSetRaMapper(null, msgs);
			case RelANNISPackage.RA_EXPORTER__RA_DAO:
				return basicSetRaDAO(null, msgs);
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
			case RelANNISPackage.RA_EXPORTER__RA_MAPPER:
				if (resolve) return getRaMapper();
				return basicGetRaMapper();
			case RelANNISPackage.RA_EXPORTER__RA_DAO:
				return getRaDAO();
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
			case RelANNISPackage.RA_EXPORTER__RA_MAPPER:
				setRaMapper((RAMapper)newValue);
				return;
			case RelANNISPackage.RA_EXPORTER__RA_DAO:
				setRaDAO((RADAO)newValue);
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
			case RelANNISPackage.RA_EXPORTER__RA_MAPPER:
				setRaMapper((RAMapper)null);
				return;
			case RelANNISPackage.RA_EXPORTER__RA_DAO:
				setRaDAO((RADAO)null);
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
			case RelANNISPackage.RA_EXPORTER__RA_MAPPER:
				return raMapper != null;
			case RelANNISPackage.RA_EXPORTER__RA_DAO:
				return raDAO != null;
		}
		return super.eIsSet(featureID);
	}

} //RAExporterImpl
