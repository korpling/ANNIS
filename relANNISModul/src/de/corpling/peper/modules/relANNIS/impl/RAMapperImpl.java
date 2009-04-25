/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.impl;

import de.corpling.salt.saltFW.SaltFWFactory;
import de.corpling.salt.saltFW.SaltAccessorModule;
import de.corpling.salt.saltFW.SaltModule;
import de.corpling.salt.model.salt.SCorpus;
import de.corpling.salt.model.salt.SDocument;
import de.corpling.salt.model.salt.SSTEREOTYPES;
import de.corpling.salt.model.salt.STextualDataSource;
import de.corpling.salt.model.saltCore.SAnnotation;
import de.corpling.salt.model.saltCore.SElement;
import de.corpling.salt.model.saltCore.SProcessingAnnotation;
import de.corpling.salt.model.saltCore.SRelation;
import de.corpling.salt.model.saltCore.SStereotype;
import de.corpling.salt.model.saltCore.STRAVERSAL_MODE;

import de.corpling.peper.modules.relANNIS.RAEdgeAnnotation;
import de.corpling.peper.modules.relANNIS.RANodeAnnotation;
import de.corpling.peper.modules.relANNIS.RACorpus;
import de.corpling.peper.modules.relANNIS.RACorpusAnnotation;
import de.corpling.peper.modules.relANNIS.RADAO;
import de.corpling.peper.modules.relANNIS.RAExporter;
import de.corpling.peper.modules.relANNIS.RAMapper;
import de.corpling.peper.modules.relANNIS.RAEdge;
import de.corpling.peper.modules.relANNIS.RAComponent;
import de.corpling.peper.modules.relANNIS.RANode;
import de.corpling.peper.modules.relANNIS.RAText;
import de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE;
import de.corpling.peper.modules.relANNIS.RelANNISFactory;
import de.corpling.peper.modules.relANNIS.RelANNISPackage;
import java.util.Collection;

import de.corpling.peper.modules.relANNIS.UNIQUE_VALUES;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>RA Mapper</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAMapperImpl#getRaDAO <em>Ra DAO</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAMapperImpl#getCurrTaId <em>Curr Ta Id</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAMapperImpl#getRaExporter <em>Ra Exporter</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAMapperImpl#getCoherentComponents <em>Coherent Components</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RAMapperImpl#getSubConnectedComponents <em>Sub Connected Components</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RAMapperImpl extends EObjectImpl implements RAMapper 
{
	private Logger logger= Logger.getLogger(RAMapperImpl.class);
	private static final String MSG_ERR=	"Error("+RAMapperImpl.class+"): ";
	/**
	 * The cached value of the '{@link #getRaDAO() <em>Ra DAO</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRaDAO()
	 * @generated
	 * @ordered
	 */
	protected RADAO raDAO;

	/**
	 * The default value of the '{@link #getCurrTaId() <em>Curr Ta Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCurrTaId()
	 * @generated
	 * @ordered
	 */
	protected static final Long CURR_TA_ID_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getCurrTaId() <em>Curr Ta Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCurrTaId()
	 * @generated
	 * @ordered
	 */
	protected Long currTaId = CURR_TA_ID_EDEFAULT;

	/**
	 * The cached value of the '{@link #getRaExporter() <em>Ra Exporter</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRaExporter()
	 * @generated
	 * @ordered
	 */
	protected RAExporter raExporter;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	protected RAMapperImpl() {
		super();
		init();
	}
	
	private void init()
	{
//		this.rankTypeList= new BasicEList<RAComponent>();
		this.subConnectedComponents = new EDataTypeUniqueEList<SElement>(SElement.class, this, RelANNISPackage.RA_MAPPER__COHERENT_COMPONENTS);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RelANNISPackage.Literals.RA_MAPPER;
	}

	protected EList<SElement> coherentComponents;
	
	/**
	 * The cached value of the '{@link #getSubConnectedComponents() <em>Sub Connected Components</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSubConnectedComponents()
	 * @generated
	 * @ordered
	 */
	protected EList<SElement> subConnectedComponents;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RADAO getRaDAO() {
		if (raDAO != null && raDAO.eIsProxy()) {
			InternalEObject oldRaDAO = (InternalEObject)raDAO;
			raDAO = (RADAO)eResolveProxy(oldRaDAO);
			if (raDAO != oldRaDAO) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, RelANNISPackage.RA_MAPPER__RA_DAO, oldRaDAO, raDAO));
			}
		}
		return raDAO;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RADAO basicGetRaDAO() {
		return raDAO;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRaDAO(RADAO newRaDAO) {
		RADAO oldRaDAO = raDAO;
		raDAO = newRaDAO;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_MAPPER__RA_DAO, oldRaDAO, raDAO));
	}

	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Long getCurrTaId() {
		return currTaId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCurrTaId(Long newCurrTaId) {
		Long oldCurrTaId = currTaId;
		currTaId = newCurrTaId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_MAPPER__CURR_TA_ID, oldCurrTaId, currTaId));
	}


	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RAExporter getRaExporter() {
		if (raExporter != null && raExporter.eIsProxy()) {
			InternalEObject oldRaExporter = (InternalEObject)raExporter;
			raExporter = (RAExporter)eResolveProxy(oldRaExporter);
			if (raExporter != oldRaExporter) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, RelANNISPackage.RA_MAPPER__RA_EXPORTER, oldRaExporter, raExporter));
			}
		}
		return raExporter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RAExporter basicGetRaExporter() {
		return raExporter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetRaExporter(RAExporter newRaExporter, NotificationChain msgs) {
		RAExporter oldRaExporter = raExporter;
		raExporter = newRaExporter;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_MAPPER__RA_EXPORTER, oldRaExporter, newRaExporter);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRaExporter(RAExporter newRaExporter) {
		if (newRaExporter != raExporter) {
			NotificationChain msgs = null;
			if (raExporter != null)
				msgs = ((InternalEObject)raExporter).eInverseRemove(this, RelANNISPackage.RA_EXPORTER__RA_MAPPER, RAExporter.class, msgs);
			if (newRaExporter != null)
				msgs = ((InternalEObject)newRaExporter).eInverseAdd(this, RelANNISPackage.RA_EXPORTER__RA_MAPPER, RAExporter.class, msgs);
			msgs = basicSetRaExporter(newRaExporter, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RA_MAPPER__RA_EXPORTER, newRaExporter, newRaExporter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<SElement> getCoherentComponents() {
		if (coherentComponents == null) {
			coherentComponents = new EDataTypeUniqueEList<SElement>(SElement.class, this, RelANNISPackage.RA_MAPPER__COHERENT_COMPONENTS);
		}
		return coherentComponents;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public EList<SElement> getSubConnectedComponents() {
		if (subConnectedComponents == null) {
			subConnectedComponents = new EDataTypeUniqueEList<SElement>(SElement.class, this, RelANNISPackage.RA_MAPPER__SUB_CONNECTED_COMPONENTS);
		}
		for (SElement subConnectedComponent: this.subConnectedComponents)
		{
			this.setElementStatus(subConnectedComponent, CC_STATUS.SUB_COHERENT_COMPONENT);
		}
		return subConnectedComponents;
	}

	/**
	 * accessor module
	 */
	private SaltAccessorModule accessor= null;
	/**
	 * root document, if export was export(sDocument)
	 */
	private SDocument sDocument= null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void init(SDocument document) 
	{
		for (SaltModule saltModule: this.getRaExporter().getSaltProject().getSModules())
			if (SaltAccessorModule.class.isInstance(saltModule))
				this.accessor= (SaltAccessorModule)saltModule;
		this.sDocument= document;
		if (sDocument.getSProcessingAnnotation(KW_PA_ID)== null)
		{
			//store relational id in SELEMENT
			SProcessingAnnotation spAnno= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
			spAnno.setFullName(KW_PA_ID);
			spAnno.setValue(this.raDAO.getUniqueValue(UNIQUE_VALUES.CORPUS_ID));
			sDocument.addSProcessingAnnotation(spAnno);
		}
	}


	/**
	 * Close this object, and does everything which has to be done 
	 * to finish correct mapping.
	 */
	public void close() 
	{
//		for (RAComponent raRankType : this.rankTypeList)
//		{
//			//storing rank_type entry
//			this.raDAO.write(this.getCurrTaId(), raRankType);
//		}
//		this.rankTypeList= null;
	}

	/**
	 * stores current relANNIS component object
	 */
	private RAComponent currRAComponent= null;
	
	private enum RA_TRAVERSAL_MODE {CORPUS, DOCUMENT_SUPER, DOCUMENT_SUB};
	/**
	 * stores current traversal mode
	 */
	private RA_TRAVERSAL_MODE currTraversalMode= null;
	
	/**
	 * @param sDocument
	 */
	public void export(SDocument sDocument) 
	{

		SStereotype sStereotype= null;
		
		//export all textual data sources
		this.logger.debug("Exporting all textual datasources...");
		sStereotype= sDocument.getSGraph().getSCoreProject().getSStereotypeByName(SSTEREOTYPES.STEXTUAL_DATASOURCE.toString());
		EList<SElement> textualDS= sDocument.getSDocumentGraph().getSElementsByStereotype(sStereotype);
		for (SElement element: textualDS)
		{
			this.mapSTextualDS(element);
		}
		
		//exporting all structures and token elements connected with SSPANNING_RELATION
		this.traverseSuperNSubComponents(sDocument.getSGraph().getSCoreProject().getSStereotypeByName(SSTEREOTYPES.SSPAN_RELATION.toString()));
		
		//Export all structures and token elements connected with SPOINTING_RELATION
		this.traverseSuperNSubComponents(sDocument.getSGraph().getSCoreProject().getSStereotypeByName(SSTEREOTYPES.SPOINTING_RELATION.toString()));
		
		//Export all structures and token elements connected with SDOMINANCE_RELATION
		this.traverseSuperNSubComponents(sDocument.getSGraph().getSCoreProject().getSStereotypeByName(SSTEREOTYPES.SDOMINANCE_RELATION.toString()));
		
		//Export all tokens who aren´t connected by (SSPANNING_RELATION, SPOINTING_RELATION or SDOMINANCE_RELATION)
		this.logger.debug("Export all tokens who aren´t connected by (SSPANNING_RELATION, SPOINTING_RELATION or SDOMINANCE_RELATION)");
		sStereotype= sDocument.getSGraph().getSCoreProject().getSStereotypeByName(SSTEREOTYPES.STOKEN.toString());
		EList<SElement> tokens= sDocument.getSDocumentGraph().getSElementsByStereotype(sStereotype);
		if (tokens!= null)
		{
			for (SElement token: tokens)
			{
				//if element does not already have been stored
				if (token.getSProcessingAnnotation(KW_PA_ID)== null)
				{
//					System.out.println("root of no relation: "+token.getId());
					//creating a new component
					this.currRAComponent= RelANNISFactory.eINSTANCE.createRAComponent();
					this.currRAComponent.setId(this.raDAO.getUniqueValue(UNIQUE_VALUES.RANK_TYPE_ID));
					sDocument.getSDocumentGraph().traverseSGraph(STRAVERSAL_MODE.DEPTH_FIRST, token, this, null);
					//storing component
					this.raDAO.write(this.getCurrTaId(), this.currRAComponent);	
				}
			}
		}
	}
	
	/**
	 * Starts traversing of graph for the given stereotype. Firstable the supertype will be
	 * traversed and secondly subtype will be traversed. 
	 * @param sStereotype
	 */
	private void traverseSuperNSubComponents(SStereotype sStereotype)
	{
		this.logger.debug("Export all structures and token elements connected with relation stereotype '"+sStereotype.getName()+"'");
		EList<SElement> prRoots= sDocument.getSDocumentGraph().getSRootsBySStereotypeRelation(sStereotype);
		if (prRoots!= null)
		{
			this.currTraversalMode= RA_TRAVERSAL_MODE.DOCUMENT_SUPER;
			this.rootsOfSubComponent= new BasicEList<ComponentElementPair>();
			//traversing all roots of components to compute super components
			for (SElement prRoot: prRoots)
			{
//				System.out.println("root of super component: "+prRoot.getId());
				//creating a new component
				this.currRAComponent= RelANNISFactory.eINSTANCE.createRAComponent();
				this.currRAComponent.setId(this.raDAO.getUniqueValue(UNIQUE_VALUES.RANK_TYPE_ID));
				sDocument.getSDocumentGraph().traverseSGraph(STRAVERSAL_MODE.DEPTH_FIRST, prRoot, this, sStereotype);
				//storing component
				this.raDAO.write(this.getCurrTaId(), this.currRAComponent);	
			}
			//traversing all roots of components to compute sub components 
			if (this.rootsOfSubComponent!= null)
			{
//				System.out.print("all sub roots: \n\t");
//				for (ComponentElementPair comElemPair: this.rootsOfSubComponent)
//				{	
//					System.out.print(comElemPair.element.getId()+"("+comElemPair.componentId+"), ");
//				}
//				System.out.println();
				this.currTraversalMode= RA_TRAVERSAL_MODE.DOCUMENT_SUB;
				for (ComponentElementPair comElemPair: this.rootsOfSubComponent)
				{
//					System.out.println("root of sub PR: "+comElemPair.element.getId());
					//creating a new component
					this.currRAComponent= RelANNISFactory.eINSTANCE.createRAComponent();
					this.currRAComponent.setId(this.raDAO.getUniqueValue(UNIQUE_VALUES.RANK_TYPE_ID));
					//setting current componentId
					this.currComponentId= comElemPair.componentId;
					this.sDocument.getSDocumentGraph().traverseSGraph(STRAVERSAL_MODE.DEPTH_FIRST, comElemPair.element, this, sStereotype);
					//storing component
					this.raDAO.write(this.getCurrTaId(), this.currRAComponent);	
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void mapCorpus(SElement element)
	{
		//CORPUS OR DOCUMENT(element is of type corpus)
		if ((SCorpus.class.isInstance(element)) || 
			(SDocument.class.isInstance(element)))
		{
			EList<Long> postList= null;
			if (postList== null)
			{
				postList= new BasicEList<Long>();
				SProcessingAnnotation sProcessingAnnotation= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
				sProcessingAnnotation.setFullName(KW_PA_POST);
				sProcessingAnnotation.setValue(postList);
				element.addSProcessingAnnotation(sProcessingAnnotation);
			}
			else 
			{
				postList= (EList<Long>)element.getSProcessingAnnotation(KW_PA_POST);
			}
			long post=this.getRaDAO().getUniqueValue(UNIQUE_VALUES.CORP_STRUCT_PPORDER);
			postList.add(post);
	
			EList<Long> preList= (EList<Long>)element.getSProcessingAnnotation(KW_PA_PRE).getValue();
			if (preList== null)
				throw new NullPointerException(MSG_ERR + "The pre value isn´set for this element. This might be an internal failure");
			
			long pre= preList.get(preList.size()-1);
			
			
			//store relational id in SELEMENT if non exists
			SProcessingAnnotation spAnno= null;
			spAnno= element.getSProcessingAnnotation(KW_PA_ID);
			if (spAnno== null)
			{	
				spAnno= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
				spAnno.setFullName(KW_PA_ID);
				spAnno.setValue(this.raDAO.getUniqueValue(UNIQUE_VALUES.CORPUS_ID));
				element.addSProcessingAnnotation(spAnno);
			}
			//create raCorpus Object and write to dao
			RACorpus raCorpus= RelANNISFactory.eINSTANCE.createRACorpus();
			raCorpus.setId((Long)spAnno.getValue());
			if (element.getName()!=  null)
				raCorpus.setName(element.getName());
			else 
				raCorpus.setName(element.getId().toString());
			if (SCorpus.class.isInstance(element))
				raCorpus.setType(RA_CORPUS_TYPE.CORPUS);
			else if (SDocument.class.isInstance(element))
				raCorpus.setType(RA_CORPUS_TYPE.DOCUMENT);
			raCorpus.setPre(pre);
			raCorpus.setPost(post);
			this.getRaDAO().write(this.getCurrTaId(), raCorpus);
			
			//write Annotations of Corpus or Document
			for (SAnnotation sAnno: element.getSAnnotations())
			{
				RACorpusAnnotation raMeta= RelANNISFactory.eINSTANCE.createRACorpusAnnotation();
				raMeta.setCorpus_ref(raCorpus.getId());
				raMeta.setNamespace(sAnno.getNamespace());
				raMeta.setName(sAnno.getName());
				raMeta.setValue(sAnno.getValue().toString());
				this.getRaDAO().write(this.getCurrTaId(), raMeta);
			}
		}
	}
	
	public void mapSTextualDS(SElement element)
	{
		//TEXTUAL_DATA_SOURCE
		if (element.getSStereotype().getName()== SSTEREOTYPES.STEXTUAL_DATASOURCE.toString())
		{
			//if element does not already have been stored
			if (element.getSProcessingAnnotation(KW_PA_STORED)== null)
			{
				RAText raText= RelANNISFactory.eINSTANCE.createRAText();
				raText.setId(this.getNewId(element));
				raText.setName(element.getName());
				raText.setText(((STextualDataSource)element).getSText());
				//write textual_DS
				this.getRaDAO().write(this.getCurrTaId(), raText);
				
				//marking element, that it has been storeds
				SProcessingAnnotation spAnno= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
				spAnno.setFullName(KW_PA_STORED);
				element.addSProcessingAnnotation(spAnno);
			}	
		}
	}
	
	public void mapSToken(SElement element)
	{
		if (element.getSStereotype().getName()== SSTEREOTYPES.STOKEN.toString())
		{
			//if element does not already have been vistited
			if (element.getSProcessingAnnotation(KW_PA_ID)== null)
			{
				String namespace= "UNKNOWN";
//				//TODO this is a dirty hack to get a namespace please fix me
//				if (element.getSProcessingAnnotation(KW_DIRTY_NS)!= null)
//				{
//					namespace= element.getSProcessingAnnotation(KW_DIRTY_NS).getValue().toString(); 
//				}
				
				//store relational id in SELEMENT
				SProcessingAnnotation spAnno= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
				spAnno.setFullName(KW_PA_ID);
				spAnno.setValue(this.raDAO.getUniqueValue(UNIQUE_VALUES.STRUCT_ID));
				element.addSProcessingAnnotation(spAnno);
				
				RANode raStruct= RelANNISFactory.eINSTANCE.createRANode();
				//id 
				raStruct.setId((Long)spAnno.getValue());
				//text_ref
				long text_ref= (Long)this.accessor.getSTextualDataSource(element).getSProcessingAnnotation(KW_PA_ID).getValue();
				raStruct.setText_ref(text_ref);
				//corpus_ref
				raStruct.setCorpus_ref((Long)this.sDocument.getSProcessingAnnotation(KW_PA_ID).getValue());
				//namespace	
				//TODO this is a dirty hack
				raStruct.setNamespace(namespace);
				//name
				//TODO gegen echten namen ändern
				raStruct.setName((String)element.getId());
				//left
				raStruct.setLeft(this.accessor.getSLeftPos(element));
				//right
				raStruct.setRight(this.accessor.getSRightPos(element));
				//token_index
				raStruct.setToken_index(new Long(this.accessor.getTokenByPos(sDocument).indexOf(element)));
				//continuous 
				raStruct.setContinuous(this.accessor.isContinuous(element));
				//span
				raStruct.setSpan(this.accessor.getOverlapedText(element));
				
				//write struct
				this.getRaDAO().write(this.getCurrTaId(), raStruct);
				
				//writing annotations
				RANodeAnnotation raAnno= null;
				for (SAnnotation sAnnotation: element.getSAnnotations())
				{
					raAnno= RelANNISFactory.eINSTANCE.createRANodeAnnotation();
					//id
					raAnno.setId(this.raDAO.getUniqueValue(UNIQUE_VALUES.ANNO_ID));
					//struct_ref
					raAnno.setNode_ref(raStruct.getId());
					//namespace
					raAnno.setNamespace(sAnnotation.getNamespace());
					//name
					raAnno.setName(sAnnotation.getName()); 
					//value
					raAnno.setValue(sAnnotation.getValue().toString());
					
					//write annotation for struct
					this.getRaDAO().write(this.getCurrTaId(), raAnno);
				}
			}
		}
	}
	
	public void mapSStructure(SElement element)
	{
		//SSTRUCTURE
		if (element.getSStereotype().getName()== SSTEREOTYPES.SSTRUCTURE.toString())
		{
			//writing struct
			//writing struct, but just one time. Therefore we have to check if relational id is already given. 
			//If one is given don´t store this object again.
			if (element.getSProcessingAnnotation(KW_PA_ID)== null)
			{
				//TODO this is a dirty hack to get a namespace please fix me
				String namespace= "UNKNOWN";
				if (element.getSProcessingAnnotation(KW_DIRTY_NS)!= null)
				{
					namespace= element.getSProcessingAnnotation(KW_DIRTY_NS).getValue().toString(); 
				}
				
				//store relational id in SELEMENT
				SProcessingAnnotation spAnno= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
				spAnno.setFullName(KW_PA_ID);
				spAnno.setValue(this.raDAO.getUniqueValue(UNIQUE_VALUES.STRUCT_ID));
				element.addSProcessingAnnotation(spAnno);
				RANode raStruct= RelANNISFactory.eINSTANCE.createRANode();
				
				//compute left and right value
				Long left= this.accessor.getSLeftPos(element);
				Long right= this.accessor.getSRightPos(element);
				if (left > right)
					throw new NullPointerException(MSG_ERR + "Cannot set left and right value, because left is higher than right (left: "+left+", right: "+right+").");
				//id 
				raStruct.setId((Long)spAnno.getValue());
				//text_ref
				long text_ref= this.getNewId(this.accessor.getSTextualDataSource(element));
				raStruct.setText_ref(text_ref);
				//corpus_ref
				raStruct.setCorpus_ref((Long)this.sDocument.getSProcessingAnnotation(KW_PA_ID).getValue());
				//namespace	
				//TODO
				raStruct.setNamespace(namespace);
				//name
				//TODO gegen echten namen tauschen
				raStruct.setName((String)element.getId());
				//left
				raStruct.setLeft(left);
				//right
				raStruct.setRight(right);
				//token_index
				raStruct.setToken_index(null);
				//continuous 
				raStruct.setContinuous(this.accessor.isContinuous(element));
				//span
				raStruct.setSpan(null);
				
				//write struct
				this.getRaDAO().write(this.getCurrTaId(), raStruct);
				
				//writing annotations
				RANodeAnnotation raAnno= null;
				for (SAnnotation sAnnotation: element.getSAnnotations())
				{
					//store relational id in SELEMENT
					raAnno= RelANNISFactory.eINSTANCE.createRANodeAnnotation();
					//id
					raAnno.setId(this.raDAO.getUniqueValue(UNIQUE_VALUES.ANNO_ID));
					//struct_ref
					raAnno.setNode_ref(raStruct.getId());
					//namespace
					raAnno.setNamespace(sAnnotation.getNamespace());
					//name
					raAnno.setName(sAnnotation.getName()); 
					//value
					raAnno.setValue(sAnnotation.getValue().toString());
					
					//write annotation for struct
					this.getRaDAO().write(this.getCurrTaId(), raAnno);
				}
			}
		}
	}
	
	/**
	 * Keywords for processingAnnotations
	 */
	private static final String KW_PA_PRE=	"ra:pre";
	private static final String KW_PA_POST=	"ra:post";
	private static final String KW_PA_ID=	"ra:id";
	private static final String KW_PA_STORED=	"ra:stored";
	private static final String KW_DIRTY_NS=	"dirty:ns";	
	private static final String KW_PA_COMPONENTS=	"ra::components";	
	
	/**
	 * Stores last Relation to notify relationswitch
	 */
	private SRelation lastRelation= null;
	
	/**
	 * Stores last Element to notify, that the last element is not parent of current element.
	 */
	private SElement lastElement= null;
	
	/**
	 * A pair of a component identifier and a node.
	 * @author Administrator
	 *
	 */
	private class ComponentElementPair
	{
		SElement element= null;
		String componentId= null;
	}
	
	/**
	 * list to store all roots of sub connected components
	 */
	private EList<ComponentElementPair> rootsOfSubComponent= null;
	
	@SuppressWarnings("unchecked")
	private boolean hasComponentId(SElement element, String componentId)
	{
		boolean retVal= false;
		if (element.getSProcessingAnnotation(KW_PA_COMPONENTS)!= null)
		{
			EList<String> componentIds= (EList<String>) element.getSProcessingAnnotation(KW_PA_COMPONENTS).getValue();
			for (String cId: componentIds)
			{
				// if componentID was found
				if (componentId.equalsIgnoreCase(cId))
				{
					retVal= true;
					break;
				}	
			}
		}
		return(retVal);
	}
	
	@SuppressWarnings("unchecked")
	private void markWithComponentId(SElement element, String componentId)
	{
		if (element== null)
			throw new NullPointerException(MSG_ERR + "Cannot mark an empty element with componentId.");
		SProcessingAnnotation spAnno= null;
		EList<String> componentIds= null;
		spAnno= element.getSProcessingAnnotation(KW_PA_COMPONENTS);
		if (spAnno!= null)
			componentIds= (EList<String>)element.getSProcessingAnnotation(KW_PA_COMPONENTS).getValue(); 
		if (componentIds== null)
		{	
			componentIds= new BasicEList<String>();
			spAnno= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
			spAnno.setFullName(KW_PA_COMPONENTS);
			spAnno.setValue(componentIds);
			element.addSProcessingAnnotation(spAnno);
		}
		//mark element if it isn´t already marked
		if (!componentIds.contains(componentId))
			componentIds.add(componentId);
	}
	
	/**
	 * stores the current component id for traversing of sub components
	 */
	private String currComponentId= null;
	/**
	 * 
	 */
	@Override
	public boolean checkConstraint(STRAVERSAL_MODE traversalMode,
			SRelation edge, SElement currNode, long order) 
	{
		SRelation relation= (SRelation) edge;
		SElement currElement= (SElement) currNode;
		boolean retVal= true;
		
		if (this.currTraversalMode== RA_TRAVERSAL_MODE.DOCUMENT_SUPER)
		{
			//only check for subcoherent components if relation has one
			if (	(relation!= null) &&
					(relation.getSType()!= null) &&
					(!relation.getSType().equalsIgnoreCase("")))
			{
				//create component identifier
				String componentId= relation.getSStereotype().getName() + "::"+relation.getSType();
//				System.out.println("componentId:  "+componentId);
				
				{//start marking as sub component root 
					ComponentElementPair comElemPair= new ComponentElementPair();
					comElemPair.element= relation.getSSourceElement();
					comElemPair.componentId= componentId;
					
					//Case1: last relation is empty, means root of subtype component= root of supercomponent
					if (lastRelation== null)
					{
						if (!this.hasComponentId(relation.getSSourceElement(), componentId))
						{
							this.rootsOfSubComponent.add(comElemPair);
//							System.out.println("insert root1: "+comElemPair.element.getId()+  ", component: "+ comElemPair.componentId);
							retVal= true;
						}
					}
					//Case2: lastRelation is not null, but it doesn´t have a subtype
					else if (	(lastRelation != null) &&
								(	(lastRelation.getSType()== null) ||
									(lastRelation.getSType().equalsIgnoreCase(""))))
					{	
						if (!this.hasComponentId(relation.getSSourceElement(), componentId))
						{
							this.rootsOfSubComponent.add(comElemPair);
//							System.out.println("insert root2: "+comElemPair.element.getId()+  ", component: "+ comElemPair.componentId);
						}
						retVal= true;
					}
					//Case3: lastRelation has a different subtype as current relation
					else if (	(lastRelation != null) &&
								(!lastRelation.getSType().equalsIgnoreCase(relation.getSType())))
					{
						if (!this.hasComponentId(relation.getSSourceElement(), componentId))
						{
							this.rootsOfSubComponent.add(comElemPair);
//							System.out.println("insert root3: "+comElemPair.element.getId()+  ", component: "+ comElemPair.componentId);
						}
						retVal= true;
					}
				}//end marking as sub component root
				
				{//start marking with componentId
					//mark source and destination with componentId
					if (!this.hasComponentId(relation.getSSourceElement(), componentId))
						this.markWithComponentId(relation.getSSourceElement(), componentId);
					
//					System.out.println("element '"+relation.getSSourceElement().getId()+"' is marked: "+this.hasComponentId(relation.getSSourceElement(), componentId));
					//if destination is not marked, mark it
					if (!this.hasComponentId(relation.getSDestinationElement(), componentId))
						this.markWithComponentId(relation.getSDestinationElement(), componentId);
					//if it is already marked, look if it is marked as root(with component id) and delete the mark as root
					else
					{
						//locate if a componentElementPair is to delete
						ComponentElementPair delComElemPair= null;
						for(ComponentElementPair comElemPair: this.rootsOfSubComponent)
						{
							// if this element is marked as root with componentId
							if (	(comElemPair.element== relation.getSDestinationElement()) &&
									(comElemPair.componentId.equalsIgnoreCase(componentId)))
							{
								delComElemPair= comElemPair;
							}
						}
						//delete the pair if neccessary
						if (delComElemPair!= null)
						{	
							this.rootsOfSubComponent.remove(delComElemPair);
//							System.out.println("delete root: "+delComElemPair.element.getId()+  ", component: "+ delComElemPair.componentId);
						}
					}
//					System.out.println("element '"+relation.getDestination().getId()+"' is marked: "+this.hasComponentId(relation.getSDestinationElement(), componentId));
				}//end marking with componentId
			}
		}
		else if (this.currTraversalMode== RA_TRAVERSAL_MODE.DOCUMENT_SUB)
		{
			//only check if relation has a subtype, else it can´t be a sub component
			if (	(relation!= null) &&
					(relation.getSType()!= null) &&
					(!relation.getSType().equalsIgnoreCase("")))
			{	
				String componentId= relation.getSStereotype().getName() + "::"+relation.getSType();
				
				//if componentId equals current component id, traversing has to go on
				if (componentId.equalsIgnoreCase(this.currComponentId))
				{
					retVal= true;
				}
				else
				{
//					System.out.println("false1");
					retVal= false;
				}
			}
			else if (	(relation!= null) &&
						((relation.getSType()== null) ||
						(relation.getSType().equalsIgnoreCase(""))))
			{
				retVal= false;
//				System.out.println("false2");
			}
			else retVal= true;
		}
//		lastElement= currElement;
		lastRelation= relation;
		return(retVal);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sElementLeft(STRAVERSAL_MODE traversalMode,
			SElement currNode, SRelation edge, SElement fromNode,
			long order) 
	{
		SElement fromElement= (SElement) fromNode;
		SRelation relation= (SRelation) edge;
		SElement currElement= (SElement) currNode;
		
		//detect current run as leaf and 
		//set lastRelation to null
		if (this.currElement== currElement)
		{
//			System.out.println("detect a leaf");
			this.lastRelation= null;
		}
		//System.out.println(this.getClass()+": node left : "+currElement.getId());
		//CORPUS OR DOCUMENT(element is of type corpus)
		if ((SCorpus.class.isInstance(currElement)) || 
			(SDocument.class.isInstance(currElement)))
		{
			this.mapCorpus(currElement);
		}

		// STOKEN and SSTRUCTURE
		if ((currElement.getSStereotype().getName()== SSTEREOTYPES.STOKEN.toString()) ||
				(currElement.getSStereotype().getName()== SSTEREOTYPES.SSTRUCTURE.toString()))
		{
			//TOKEN
			if (currElement.getSStereotype().getName()== SSTEREOTYPES.STOKEN.toString())
			{
				this.mapSToken(currElement);
			}
			
			//SSTRUCTURE
			else if (currElement.getSStereotype().getName()== SSTEREOTYPES.SSTRUCTURE.toString())
			{
				//writing struct
				//writing struct, but just one time. Therefore we have to check if relational id is already given. 
				//If one is given don´t store this object again.
				if (currElement.getSProcessingAnnotation(KW_PA_ID)== null)
				{
					this.mapSStructure(currElement);
				}
			}
			//creating rank entry for STOKEN or SSTRUCTURE
			//rank entry for whole graph as coherence component
			
			//post value
			EList<Long> postList= null;
			if (currElement.getSProcessingAnnotation(KW_PA_POST)== null)
			{
				postList= new BasicEList<Long>();
				SProcessingAnnotation sProcessingAnnotation= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
				sProcessingAnnotation.setFullName(KW_PA_POST);
				sProcessingAnnotation.setValue(postList);
				currElement.addSProcessingAnnotation(sProcessingAnnotation);
			}
			else 
			{	
				postList= (EList<Long>) currElement.getSProcessingAnnotation(KW_PA_POST).getValue();
			}
			long post=this.getRaDAO().getUniqueValue(UNIQUE_VALUES.RANK_PPORDER);
			postList.add(post);
	
			//pre value
			EList<Long> preList= (EList<Long>)currElement.getSProcessingAnnotation(KW_PA_PRE).getValue();
			if (preList== null)
				throw new NullPointerException(MSG_ERR + "The pre value isn´set for this element. This might be an internal failure");
			
			long pre= preList.get(preList.size()-1);
			
			//parent value
			Long parent= null;
			if (fromElement!= null)
			{
				EList<Long> parentList= (EList<Long>)fromElement.getSProcessingAnnotation(KW_PA_PRE).getValue();
				if (preList== null)
					throw new NullPointerException(MSG_ERR + "The pre value isn´set for this element. This might be an internal failure");
				parent= parentList.get(parentList.size()-1);
			}
			
			RAEdge raRank= RelANNISFactory.eINSTANCE.createRAEdge();
//			if (this.currRankType!= null)
//				raRank.setComponent_ref(this.currRankType.getId());
			raRank.setComponent_ref(this.currRAComponent.getId());
			raRank.setPre(pre);
			raRank.setPost(post);
			raRank.setParent(parent);
			raRank.setNode_ref((Long)currElement.getSProcessingAnnotation(KW_PA_ID).getValue());
			//write rank
			this.getRaDAO().write(this.getCurrTaId(), raRank);
			
			RAEdgeAnnotation raEdgeAnno= null;
			//edge_annotation
			if (relation!= null)
			{
				for (SAnnotation sAnno: relation.getSAnnotations())
				{
					raEdgeAnno= RelANNISFactory.eINSTANCE.createRAEdgeAnnotation();
					raEdgeAnno.setEdge_ref(raRank.getPre());
					raEdgeAnno.setNamespace(sAnno.getNamespace());
					raEdgeAnno.setName(sAnno.getName());
					raEdgeAnno.setValue(sAnno.getValue().toString());
					//write edge annotation
					this.getRaDAO().write(this.getCurrTaId(), raEdgeAnno);
				}
			}
			
			//filling raComponent object with content
			//if no relation exists and component wasn´t already filled
			if ((relation!= null) && (this.currRAComponent.getType()== null))
			{
				//type of rank type (values= {"c", "p", "d"})
				String type= null;
				if (SSTEREOTYPES.SDOMINANCE_RELATION.toString().equals(relation.getSStereotype().getName()))
					type= "d";
				if (SSTEREOTYPES.SSPAN_RELATION.toString().equals(relation.getSStereotype().getName()))
					type= "c";
				if (SSTEREOTYPES.SPOINTING_RELATION.toString().equals(relation.getSStereotype().getName()))
					type= "p";
				this.currRAComponent.setType(type);
				//namespace of component
				this.currRAComponent.setNamespace(null);
				if (this.currTraversalMode== RA_TRAVERSAL_MODE.DOCUMENT_SUB)
					this.currRAComponent.setName(relation.getSType());
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sElementReached(STRAVERSAL_MODE traversalMode,
			SElement currNode, SRelation relation, SElement fromSElement,
			long order) 
	{

//		SElement fromElement= (SElement) fromNode;
//		SRelation relation= (SRelation) edge;
		SElement currElement= (SElement) currNode;
		this.currElement= currElement;
//		System.out.println("reached");
//		System.out.println(this.getClass()+": node reached : "+currElement.getId());
		//CORPUS OR DOCUMENT(element is of type corpus)
		if ((SCorpus.class.isInstance(currElement)) || 
			(SDocument.class.isInstance(currElement)))
		{
			EList<Long> preList= null;
			if (currElement.getSProcessingAnnotation(KW_PA_PRE)== null)
			{
				preList= new BasicEList<Long>();
				SProcessingAnnotation sProcessingAnnotation= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
				sProcessingAnnotation.setFullName(KW_PA_PRE);
				sProcessingAnnotation.setValue(preList);
				currElement.addSProcessingAnnotation(sProcessingAnnotation);
			}
			else 
			{	
				preList= (EList<Long>) currElement.getSProcessingAnnotation(KW_PA_PRE).getValue();
			}
			preList.add(this.raDAO.getUniqueValue(UNIQUE_VALUES.CORP_STRUCT_PPORDER));
		}
		
		//STOKEN and SStructure
		else if ((currElement.getSStereotype().getName()== SSTEREOTYPES.STOKEN.toString()) || 
				(currElement.getSStereotype().getName()== SSTEREOTYPES.SSTRUCTURE.toString()))
		{
			//handling ppOrder
			EList<Long> preList= null;
			if (currElement.getSProcessingAnnotation(KW_PA_PRE)== null)
			{
				preList= new BasicEList<Long>();
				SProcessingAnnotation sProcessingAnnotation= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
				sProcessingAnnotation.setFullName(KW_PA_PRE);
				sProcessingAnnotation.setValue(preList);
				currElement.addSProcessingAnnotation(sProcessingAnnotation);
			}
			else 
			{	
				preList= (EList<Long>) currElement.getSProcessingAnnotation(KW_PA_PRE).getValue();
			}
			preList.add(this.raDAO.getUniqueValue(UNIQUE_VALUES.RANK_PPORDER));
		}
		
	}
	
	/**
	 * Stores the current element. This is neccessary to locate a leaf. An element is
	 * a leaf, if the same element is treated by nodeReached and nodeLeft. 
	 */
	private SElement currElement= null;
	
	/**
	 * Possible types of traversal in coherent components
	 * SUPER_COHERENT_COMPONENT means the highest kind, with fewest restrictions
	 * SUB_COHERENT_COMPONENT means finest granular traversal with most restrictions
	 */
	private enum CC_STATUS {SUPER_COHERENT_COMPONENT, SUB_COHERENT_COMPONENT};
	/**
	 * keyword to store if the root element of coherent component is already vistited 
	 */
	private static final String KW_COHERENT_STATUS="ra::coherent_stat";
	private CC_STATUS getElementStatus(SElement element)
	{
		CC_STATUS retVal= null;
		if ((element!= null) &&
			(element.getSProcessingAnnotation(KW_COHERENT_STATUS)!= null))
		{	
			retVal= (CC_STATUS) element.getSProcessingAnnotation(KW_COHERENT_STATUS).getValue();
		}
		return(retVal);
	}
	private void setElementStatus(SElement element, CC_STATUS status)
	{
		if ((element!= null) &&
			(element.getSProcessingAnnotation(KW_COHERENT_STATUS)== null))
		{	
			SProcessingAnnotation pAnno= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
			pAnno.setFullName(KW_COHERENT_STATUS);
			pAnno.setValue(status);
			element.addSProcessingAnnotation(pAnno);
		}
		else if ((element!= null) &&
				(element.getSProcessingAnnotation(KW_COHERENT_STATUS)!= null))
		{
			element.getSProcessingAnnotation(KW_COHERENT_STATUS).setValue(status);
		}
		else throw new NullPointerException(MSG_ERR + "Cannot set coherent component status to an empty element.");
	}
	private CC_STATUS getRelationStatus(SRelation relation)
	{
		CC_STATUS retVal= null;
		if ((relation!= null) &&
			(relation.getSProcessingAnnotation(KW_COHERENT_STATUS)!= null))
		{	
			retVal= (CC_STATUS) relation.getSProcessingAnnotation(KW_COHERENT_STATUS).getValue();
		}
		return(retVal);
	}
	
	private void setRelationStatus(SRelation relation, CC_STATUS status)
	{
		if ((relation!= null) &&
			(relation.getSProcessingAnnotation(KW_COHERENT_STATUS)== null))
		{	
			SProcessingAnnotation pAnno= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
			pAnno.setFullName(KW_COHERENT_STATUS);
			pAnno.setValue(status);
			relation.addSProcessingAnnotation(pAnno);
		}
		else if ((relation!= null) &&
				(relation.getSProcessingAnnotation(KW_COHERENT_STATUS)!= null))
		{
			relation.getSProcessingAnnotation(KW_COHERENT_STATUS).setValue(status);
		}
		else throw new NullPointerException(MSG_ERR + "Cannot set coherent component status to an empty relation.");
	}
	
	private Long getNewId(SElement element)
	{
		Long retVal= null; 
		UNIQUE_VALUES uniqueVal= null;
		//SSTEXTUAL_DATASOURCE
		if (SSTEREOTYPES.STEXTUAL_DATASOURCE.toString().equalsIgnoreCase(element.getSStereotype().getName()))
			uniqueVal= UNIQUE_VALUES.TEXT_ID;
		//STOKEN
		else if (SSTEREOTYPES.STOKEN.toString().equalsIgnoreCase(element.getSStereotype().getName()))
			uniqueVal= UNIQUE_VALUES.STRUCT_ID;
		//SSTRUCTURE
		else if (SSTEREOTYPES.SSTRUCTURE.toString().equalsIgnoreCase(element.getSStereotype().getName()))
			uniqueVal= UNIQUE_VALUES.STRUCT_ID;
		if (element.getSProcessingAnnotation(KW_PA_ID)== null)
		{
			retVal= this.raDAO.getUniqueValue(uniqueVal);
			//store relational id in SELEMENT
			SProcessingAnnotation spAnno= SaltFWFactory.eINSTANCE.createSProcessingAnnotation();
			spAnno.setFullName(KW_PA_ID);
			spAnno.setValue(retVal);
			this.accessor.getSTextualDataSource(element).addSProcessingAnnotation(spAnno);
		}
		else retVal= (Long)element.getSProcessingAnnotation(KW_PA_ID).getValue();
		return(retVal);
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RelANNISPackage.RA_MAPPER__RA_EXPORTER:
				if (raExporter != null)
					msgs = ((InternalEObject)raExporter).eInverseRemove(this, RelANNISPackage.RA_EXPORTER__RA_MAPPER, RAExporter.class, msgs);
				return basicSetRaExporter((RAExporter)otherEnd, msgs);
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
			case RelANNISPackage.RA_MAPPER__RA_EXPORTER:
				return basicSetRaExporter(null, msgs);
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
			case RelANNISPackage.RA_MAPPER__RA_DAO:
				if (resolve) return getRaDAO();
				return basicGetRaDAO();
			case RelANNISPackage.RA_MAPPER__CURR_TA_ID:
				return getCurrTaId();
			case RelANNISPackage.RA_MAPPER__RA_EXPORTER:
				if (resolve) return getRaExporter();
				return basicGetRaExporter();
			case RelANNISPackage.RA_MAPPER__COHERENT_COMPONENTS:
				return getCoherentComponents();
			case RelANNISPackage.RA_MAPPER__SUB_CONNECTED_COMPONENTS:
				return getSubConnectedComponents();
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
			case RelANNISPackage.RA_MAPPER__RA_DAO:
				setRaDAO((RADAO)newValue);
				return;
			case RelANNISPackage.RA_MAPPER__CURR_TA_ID:
				setCurrTaId((Long)newValue);
				return;
			case RelANNISPackage.RA_MAPPER__RA_EXPORTER:
				setRaExporter((RAExporter)newValue);
				return;
			case RelANNISPackage.RA_MAPPER__COHERENT_COMPONENTS:
				getCoherentComponents().clear();
				getCoherentComponents().addAll((Collection<? extends SElement>)newValue);
				return;
			case RelANNISPackage.RA_MAPPER__SUB_CONNECTED_COMPONENTS:
				getSubConnectedComponents().clear();
				getSubConnectedComponents().addAll((Collection<? extends SElement>)newValue);
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
			case RelANNISPackage.RA_MAPPER__RA_DAO:
				setRaDAO((RADAO)null);
				return;
			case RelANNISPackage.RA_MAPPER__CURR_TA_ID:
				setCurrTaId(CURR_TA_ID_EDEFAULT);
				return;
			case RelANNISPackage.RA_MAPPER__RA_EXPORTER:
				setRaExporter((RAExporter)null);
				return;
			case RelANNISPackage.RA_MAPPER__COHERENT_COMPONENTS:
				getCoherentComponents().clear();
				return;
			case RelANNISPackage.RA_MAPPER__SUB_CONNECTED_COMPONENTS:
				getSubConnectedComponents().clear();
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
			case RelANNISPackage.RA_MAPPER__RA_DAO:
				return raDAO != null;
			case RelANNISPackage.RA_MAPPER__CURR_TA_ID:
				return CURR_TA_ID_EDEFAULT == null ? currTaId != null : !CURR_TA_ID_EDEFAULT.equals(currTaId);
			case RelANNISPackage.RA_MAPPER__RA_EXPORTER:
				return raExporter != null;
			case RelANNISPackage.RA_MAPPER__COHERENT_COMPONENTS:
				return coherentComponents != null && !coherentComponents.isEmpty();
			case RelANNISPackage.RA_MAPPER__SUB_CONNECTED_COMPONENTS:
				return subConnectedComponents != null && !subConnectedComponents.isEmpty();
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
		result.append(" (currTaId: ");
		result.append(currTaId);
		result.append(", coherentComponents: ");
		result.append(coherentComponents);
		result.append(", subConnectedComponents: ");
		result.append(subConnectedComponents);
		result.append(')');
		return result.toString();
	}
} //RAMapperImpl