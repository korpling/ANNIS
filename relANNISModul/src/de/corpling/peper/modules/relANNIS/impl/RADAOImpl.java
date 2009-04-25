/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.impl;

import de.corpling.peper.modules.relANNIS.DAOOBJECT;
import de.corpling.peper.modules.relANNIS.EXPORT_FILE;
import de.corpling.peper.modules.relANNIS.RAComponent;
import de.corpling.peper.modules.relANNIS.RACorpus;
import de.corpling.peper.modules.relANNIS.RACorpusAnnotation;
import de.corpling.peper.modules.relANNIS.RADAO;
import de.corpling.peper.modules.relANNIS.RAEdge;
import de.corpling.peper.modules.relANNIS.RAEdgeAnnotation;
import de.corpling.peper.modules.relANNIS.RANode;
import de.corpling.peper.modules.relANNIS.RANodeAnnotation;
import de.corpling.peper.modules.relANNIS.RAText;
import de.corpling.peper.modules.relANNIS.RelANNISFactory;
import de.corpling.peper.modules.relANNIS.RelANNISPackage;
import de.corpling.peper.modules.relANNIS.TAObject;
import de.corpling.peper.modules.relANNIS.TupleWriterContainer;
import de.corpling.peper.modules.relANNIS.UNIQUE_VALUES;
import de.corpling.peper.modules.relANNIS.UniqueValue;
import de.corpling.salt.model.saltCore.SElement;
import de.corpling.salt.model.saltCore.SRelation;
import de.corpling.salt.model.saltCore.STRAVERSAL_MODE;
import de.dataconnector.tupleconnector.ITupleWriter;
import de.dataconnector.tupleconnector.TupleWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.emf.ecore.util.BasicFeatureMap;
import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>RADAO</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RADAOImpl#getOutputDir <em>Output Dir</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RADAOImpl#getTupleWriterEntries <em>Tuple Writer Entries</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RADAOImpl#getTaEntries <em>Ta Entries</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.impl.RADAOImpl#getUniqueValues <em>Unique Values</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RADAOImpl extends EObjectImpl implements RADAO 
{
	private static final String MSG_ERR=	"Error("+RADAOImpl.class+"): ";
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
	 * The cached value of the '{@link #getTupleWriterEntries() <em>Tuple Writer Entries</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTupleWriterEntries()
	 * @generated
	 * @ordered
	 */
	protected EMap<DAOOBJECT, TupleWriterContainer> tupleWriterEntries;

	/**
	 * The cached value of the '{@link #getTaEntries() <em>Ta Entries</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTaEntries()
	 * @generated
	 * @ordered
	 */
	protected EMap<Long, EList<TAObject>> taEntries;

	/**
	 * The cached value of the '{@link #getUniqueValues() <em>Unique Values</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUniqueValues()
	 * @generated
	 * @ordered
	 */
	protected EMap<UNIQUE_VALUES, UniqueValue> uniqueValues;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	protected RADAOImpl() {
		super();
		this.init();
	}
	
	private void init()
	{
		this.tupleWriterEntries= new BasicEMap<DAOOBJECT, TupleWriterContainer>();
		//init unique value map
		this.uniqueValues= new BasicEMap<UNIQUE_VALUES, UniqueValue>();
	}
	
	/**
	 * Initializes all tuple writers. called by setOutpuDir
	 */
	//TODO reengineer for setting with spring
	private void initTupleWriter()
	{
		TupleWriter tupleWriter= null;
		File outputFile= null;
		EList<String> attNames= null;
		
		//Corpus tupleWriter
		tupleWriter= new TupleWriter();
		outputFile= new File(this.getOutputDir()+ "/"+ "corpus.tab");
		tupleWriter.setFile(outputFile);
		attNames= new BasicEList<String>();
		attNames.add("id");
		attNames.add("name");
		attNames.add("type");
		attNames.add("version");
		attNames.add("pre");
		attNames.add("post");
		tupleWriter.setAttNames(attNames);
		tupleWriter.setSeperator("\t");
		this.addTupleWriter(DAOOBJECT.CORPUS, tupleWriter);
		
		//Corpus_mate_attribute tupleWriter
		tupleWriter= new TupleWriter();
		outputFile= new File(this.getOutputDir()+ "/"+ "corpus_annotation.tab");
		tupleWriter.setFile(outputFile);
		attNames= new BasicEList<String>();
		attNames.add("corpus_ref");
		attNames.add("namespace");
		attNames.add("name");
		attNames.add("value");
		tupleWriter.setAttNames(attNames);
		tupleWriter.setSeperator("\t");
		this.addTupleWriter(DAOOBJECT.CORPUS_ANNOTATION, tupleWriter);
		
		//text tupleWriter
		tupleWriter= new TupleWriter();
		outputFile= new File(this.getOutputDir()+ "/"+ "text.tab");
		tupleWriter.setFile(outputFile);
		attNames= new BasicEList<String>();
		attNames.add("id");
		attNames.add("name");
		attNames.add("text");
		tupleWriter.setAttNames(attNames);
		tupleWriter.setSeperator("\t");
		this.addTupleWriter(DAOOBJECT.TEXT, tupleWriter);
		
		//struct tupleWriter
		tupleWriter= new TupleWriter();
		outputFile= new File(this.getOutputDir()+ "/"+ "node.tab");
		tupleWriter.setFile(outputFile);
		attNames= new BasicEList<String>();
		attNames.add("id");
		attNames.add("text_ref");
		attNames.add("corpus_ref");
		attNames.add("namespace");
		attNames.add("name");
		attNames.add("left");
		attNames.add("right");
		attNames.add("token_index");
		attNames.add("continuous");
		attNames.add("span");
		tupleWriter.setAttNames(attNames);
		tupleWriter.setSeperator("\t");
		this.addTupleWriter(DAOOBJECT.NODE, tupleWriter);
		
		//rank tupleWriter
		tupleWriter= new TupleWriter();
		outputFile= new File(this.getOutputDir()+ "/"+ "rank.tab");
		tupleWriter.setFile(outputFile);
		attNames= new BasicEList<String>();
		attNames.add("pre");
		attNames.add("post");
		attNames.add("struct_ref");
		attNames.add("rank_type_ref");
		attNames.add("parent");
		tupleWriter.setAttNames(attNames);
		tupleWriter.setSeperator("\t");
		this.addTupleWriter(DAOOBJECT.EDGE, tupleWriter);
		
		//rank_type tupleWriter
		tupleWriter= new TupleWriter();
		outputFile= new File(this.getOutputDir()+ "/"+ "component.tab");
		tupleWriter.setFile(outputFile);
		attNames= new BasicEList<String>();
		attNames.add("id");
		attNames.add("type");
		attNames.add("namespace");
		attNames.add("name");
		attNames.add("value");
		tupleWriter.setAttNames(attNames);
		tupleWriter.setSeperator("\t");
		this.addTupleWriter(DAOOBJECT.COMPONENT, tupleWriter);
		
		//edge_annotation tupleWriter
		tupleWriter= new TupleWriter();
		outputFile= new File(this.getOutputDir()+ "/"+ "rank_annotation.tab");
		tupleWriter.setFile(outputFile);
		attNames= new BasicEList<String>();
		attNames.add("rank_ref");
		attNames.add("namespace");
		attNames.add("name");
		attNames.add("value");
		tupleWriter.setAttNames(attNames);
		tupleWriter.setSeperator("\t");
		this.addTupleWriter(DAOOBJECT.EDGE_ANNOTATION, tupleWriter);
		
		//annotation tupleWriter
		tupleWriter= new TupleWriter();
		outputFile= new File(this.getOutputDir()+ "/"+ "node_annotation.tab");
		tupleWriter.setFile(outputFile);
		attNames= new BasicEList<String>();
		attNames.add("id");
		attNames.add("struct_ref");
		attNames.add("namespace");
		attNames.add("name");
		attNames.add("value");
		tupleWriter.setAttNames(attNames);
		tupleWriter.setSeperator("\t");
		this.addTupleWriter(DAOOBJECT.NODE_ANNOTATION, tupleWriter);
		
//		//annotation_meta_attribute tupleWriter
//		tupleWriter= new TupleWriter();
//		outputFile= new File(this.getOutputDir()+ "/"+ "annotation_meta_attribute.tab");
//		tupleWriter.setFile(outputFile);
//		attNames= new BasicEList<String>();
//		attNames.add("annotation_ref");
//		attNames.add("namespace");
//		attNames.add("name");
//		attNames.add("value");
//		tupleWriter.setAttNames(attNames);
//		tupleWriter.setSeperator("\t");
//		this.addTupleWriter(DAOOBJECT.ANNOTATION_META_ATTRIBUTE, tupleWriter);
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RelANNISPackage.Literals.RADAO;
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
	 */
	public void setOutputDir(File newOutputDir) 
	{
		if (newOutputDir== null)
			throw new NullPointerException(MSG_ERR + "Cannot set an empty output directory");
		File oldOutputDir = outputDir;
		outputDir = newOutputDir;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RelANNISPackage.RADAO__OUTPUT_DIR, oldOutputDir, outputDir));
		this.initTupleWriter();
	}

	/**
	 * Returns if object is full initialized or not
	 */
	private boolean isInitialized()
	{
		boolean retVal= true;
		if (this.getOutputDir()== null)
			return(false);
		
		return(retVal);
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<DAOOBJECT, TupleWriterContainer> getTupleWriterEntries() {
		if (tupleWriterEntries == null) {
			tupleWriterEntries = new EcoreEMap<DAOOBJECT,TupleWriterContainer>(RelANNISPackage.Literals.TUPLE_WRITER_ENTRY, TupleWriterEntryImpl.class, this, RelANNISPackage.RADAO__TUPLE_WRITER_ENTRIES);
		}
		return tupleWriterEntries;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<Long, EList<TAObject>> getTaEntries() {
		if (taEntries == null) {
			taEntries = new EcoreEMap<Long,EList<TAObject>>(RelANNISPackage.Literals.TA_ENTRY, TaEntryImpl.class, this, RelANNISPackage.RADAO__TA_ENTRIES);
		}
		return taEntries;
	}

// ======================= start: unique-value-handling
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EMap<UNIQUE_VALUES, UniqueValue> getUniqueValues() {
		if (uniqueValues == null) {
			uniqueValues = new EcoreEMap<UNIQUE_VALUES,UniqueValue>(RelANNISPackage.Literals.UNIQUE_VALUE_ENTRY, UniqueValueEntryImpl.class, this, RelANNISPackage.RADAO__UNIQUE_VALUES);
		}
		return uniqueValues;
	}

	/**
	 * Returns a new unique value for given identifier.
	 */
	public Long getUniqueValue(UNIQUE_VALUES uniqueValue) 
	{
		if (uniqueValue== null)
			throw new NullPointerException(MSG_ERR + "Cannot return a new unique value, because identifier is empty.");
		
		UniqueValue uniqueValueObj= this.uniqueValues.get(uniqueValue);
		if (uniqueValueObj== null)
		{	
			uniqueValueObj= RelANNISFactory.eINSTANCE.createUniqueValue();
			uniqueValueObj.setValue(0l);
			this.uniqueValues.put(uniqueValue, uniqueValueObj);
		}
		
		long retVal= uniqueValueObj.getValue();
		uniqueValueObj.setValue(retVal+1);
		return(retVal);
	}
	// ======================= end: unique-value-handling
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void write(Long taId, RACorpus raCorpus) 
	{
		if (!this.isInitialized())
			throw new NullPointerException(MSG_ERR + "Cannot write object, because this dao isn´t initialized.");
		
		if (this.getTaEntries().get(taId)== null)
			throw new NullPointerException(MSG_ERR + "Cannot write object, because given taId doesn´t exists. Maybe its out of date (taId: "+taId+").");
		if (raCorpus== null)
			throw new NullPointerException(MSG_ERR + "Cannot write an empty raCorpus object.");
			
		ITupleWriter tupleWriter= this.getTupleWriter(DAOOBJECT.CORPUS);
		try {
			tupleWriter.addTuple(taId, raCorpus.toStringList());
		} catch (FileNotFoundException e) 
		{
			throw new NullPointerException(MSG_ERR + e.getLocalizedMessage());
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void write(Long taId, RACorpusAnnotation raCorpusMetaAttribute) 
	{
		if (!this.isInitialized())
			throw new NullPointerException(MSG_ERR + "Cannot write object, because this dao isn´t initialized.");
		
		if (this.getTaEntries().get(taId)== null)
			throw new NullPointerException(MSG_ERR + "Cannot write object, because given taId doesn´t exists. Maybe its out of date (taId: "+taId+").");
		if (raCorpusMetaAttribute== null)
			throw new NullPointerException(MSG_ERR + "Cannot write an empty raCorpus object.");
			
		ITupleWriter tupleWriter= this.getTupleWriter(DAOOBJECT.CORPUS_ANNOTATION);
		try {
			tupleWriter.addTuple(taId, raCorpusMetaAttribute.toStringList());
		} catch (FileNotFoundException e) 
		{ throw new NullPointerException(MSG_ERR + e.getLocalizedMessage()); }
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void write(Long taId, RAText raText) 
	{
		if (!this.isInitialized())
			throw new NullPointerException(MSG_ERR + "Cannot write object, because this dao isn´t initialized.");
		
		if (this.getTaEntries().get(taId)== null)
			throw new NullPointerException(MSG_ERR + "Cannot write object, because given taId doesn´t exists. Maybe its out of date (taId: "+taId+").");
		if (raText== null)
			throw new NullPointerException(MSG_ERR + "Cannot write an empty raCorpus object.");
			
		ITupleWriter tupleWriter= this.getTupleWriter(DAOOBJECT.TEXT);
		if (tupleWriter== null)
			System.out.println("tupleWriter is null");
		try {
			tupleWriter.addTuple(taId, raText.toStringList());
		} catch (FileNotFoundException e) 
		{ throw new NullPointerException(MSG_ERR + e.getLocalizedMessage()); }
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void write(Long taId, RANode raStruct) 
	{
		if (!this.isInitialized())
			throw new NullPointerException(MSG_ERR + "Cannot write object, because this dao isn´t initialized.");
		
		if (this.getTaEntries().get(taId)== null)
			throw new NullPointerException(MSG_ERR + "Cannot write object, because given taId doesn´t exists. Maybe its out of date (taId: "+taId+").");
		if (raStruct== null)
			throw new NullPointerException(MSG_ERR + "Cannot write an empty raCorpus object.");
			
		ITupleWriter tupleWriter= this.getTupleWriter(DAOOBJECT.NODE);
		if (tupleWriter== null)
			System.out.println("tupleWriter is null");
		try {
			tupleWriter.addTuple(taId, raStruct.toStringList());
		} catch (FileNotFoundException e) 
		{ throw new NullPointerException(MSG_ERR + e.getLocalizedMessage()); }
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void write(Long taId, RAEdge raRank) 
	{
		if (!this.isInitialized())
			throw new NullPointerException(MSG_ERR + "Cannot write object, because this dao isn´t initialized.");
		
		if (this.getTaEntries().get(taId)== null)
			throw new NullPointerException(MSG_ERR + "Cannot write object, because given taId doesn´t exists. Maybe its out of date (taId: "+taId+").");
		if (raRank== null)
			throw new NullPointerException(MSG_ERR + "Cannot write an empty raCorpus object.");
			
		ITupleWriter tupleWriter= this.getTupleWriter(DAOOBJECT.EDGE);
		if (tupleWriter== null)
			System.out.println("tupleWriter is null");
		try {
			tupleWriter.addTuple(taId, raRank.toStringList());
		} catch (FileNotFoundException e) 
		{ throw new NullPointerException(MSG_ERR + e.getLocalizedMessage()); }
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void write(Long taId, RANodeAnnotation raAnno) 
	{
		if (!this.isInitialized())
			throw new NullPointerException(MSG_ERR + "Cannot write object, because this dao isn´t initialized.");
		
		if (this.getTaEntries().get(taId)== null)
			throw new NullPointerException(MSG_ERR + "Cannot write object, because given taId doesn´t exists. Maybe its out of date (taId: "+taId+").");
		if (raAnno== null)
			throw new NullPointerException(MSG_ERR + "Cannot write an empty raCorpus object.");
			
		ITupleWriter tupleWriter= this.getTupleWriter(DAOOBJECT.NODE_ANNOTATION);
		if (tupleWriter== null)
			System.out.println("tupleWriter is null");
		try {
			tupleWriter.addTuple(taId, raAnno.toStringList());
		} catch (FileNotFoundException e) 
		{ throw new NullPointerException(MSG_ERR + e.getLocalizedMessage()); }
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void write(Long taId, RAComponent raRankType) 
	{
		if (!this.isInitialized())
			throw new NullPointerException(MSG_ERR + "Cannot write object, because this dao isn´t initialized.");
		
		if (this.getTaEntries().get(taId)== null)
			throw new NullPointerException(MSG_ERR + "Cannot write object, because given taId doesn´t exists. Maybe its out of date (taId: "+taId+").");
		if (raRankType== null)
			throw new NullPointerException(MSG_ERR + "Cannot write an empty raCorpus object.");
			
		ITupleWriter tupleWriter= this.getTupleWriter(DAOOBJECT.COMPONENT);
		if (tupleWriter== null)
			System.out.println("tupleWriter is null");
		try {
			tupleWriter.addTuple(taId, raRankType.toStringList());
		} catch (FileNotFoundException e) 
		{ throw new NullPointerException(MSG_ERR + e.getLocalizedMessage()); }
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void write(Long taId, RAEdgeAnnotation raEdgeAnnotation) 
	{
		if (!this.isInitialized())
			throw new NullPointerException(MSG_ERR + "Cannot write object, because this dao isn´t initialized.");
		
		if (this.getTaEntries().get(taId)== null)
			throw new NullPointerException(MSG_ERR + "Cannot write object, because given taId doesn´t exists. Maybe its out of date (taId: "+taId+").");
		if (raEdgeAnnotation== null)
			throw new NullPointerException(MSG_ERR + "Cannot write an empty raCorpus object.");
			
		ITupleWriter tupleWriter= this.getTupleWriter(DAOOBJECT.EDGE_ANNOTATION);
		if (tupleWriter== null)
			System.out.println("tupleWriter is null");
		try {
			tupleWriter.addTuple(taId, raEdgeAnnotation.toStringList());
		} catch (FileNotFoundException e) 
		{ throw new NullPointerException(MSG_ERR + e.getLocalizedMessage()); }
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void sElementReached(STRAVERSAL_MODE traversalMode, SElement currSElement, SRelation sRelation, SElement fromSElement, long order) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void sElementLeft(STRAVERSAL_MODE traversalMode, SElement currSElement, SRelation sRelation, SElement fromSElement, long order) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean checkConstraint(STRAVERSAL_MODE traversalMode, SRelation sRelation, SElement currSElement, long order) {
		// TODO: implement this method
		// Ensure that you remove @generated or mark it @generated NOT
		throw new UnsupportedOperationException();
	}

	/*
	private static Long corpId= 0l; 
	/**
	 * Returns a new unforgiven id for corpus objects
	 * @return
	 */
	/*
	private static Long getCorpId()
	{
		corpId++;
		return(corpId);
	}
*/

// ================================== start: transaction handling	
	/**
	 * stores number of current transaction
	 */
	private Long taCounter= 0l;
	private Long getNewTaId()
	{
		Long retVal= this.taCounter;
		this.taCounter++;
		return(retVal);
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public long beginTA() 
	{
		//set all tuplewriter to begin ta
		//generate new taId
		Long currTaId= this.getNewTaId();
		
		//for all tupleWriters
		EList<TAObject> taList= new BasicEList<TAObject>();
		for (DAOOBJECT daoObject: this.getTupleWriterEntries().keySet())
		{
			TAObject taObject= RelANNISFactory.eINSTANCE.createTAObject();
			taObject.setTwWriterKey(daoObject);
			taObject.setTaId(this.getTupleWriter(daoObject).beginTA());
			taList.add(taObject);
		}
		this.getTaEntries().put(currTaId, taList);
		return(currTaId);
	}



	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void commitTA(long taId) 
	{
		EList<TAObject> taList= this.getTaEntries().get(taId);
		if (taList== null)
			throw new NullPointerException(MSG_ERR + "Cannot commit a non existing transaction (taId: "+taId+").");
		
		for (TAObject taObject: taList)
		{
			ITupleWriter tupleWriter= this.getTupleWriter(taObject.getTwWriterKey());
			
			try {
				tupleWriter.commitTA(taObject.getTaId());
			} catch (FileNotFoundException e) {
				throw new NullPointerException(e.getLocalizedMessage());
			}
		}
		//delete ta from ta list
		this.getTaEntries().remove(taId);
	}
// ================================== end: transaction handling

// ================================== start: tupleWriter handling
	/**
	 * This method adds the given TupleWriter Object to this object. The tuple writer 
	 * relates to given daoObject and can be reached with it. 
	 */
	public void addTupleWriter(DAOOBJECT daoObject, ITupleWriter tupleWriter) 
	{
		//checking parameters
		if (tupleWriter== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an empty TupleWriter-Object.");
		if (daoObject== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an empty daoObject.");
		
		if (this.tupleWriterEntries== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an empty, please call setOutputDir() first.");
		
		//checking if tupleWriter for daoObject already exists
		if (this.tupleWriterEntries.get(daoObject)!= null)
			throw new NullPointerException(MSG_ERR +"Cannot add given TupleWriterObject, because an entry for given daoObject already exists.");
		
		TupleWriterContainer twContainer= RelANNISFactory.eINSTANCE.createTupleWriterContainer();
		twContainer.setTupleWriter(tupleWriter);
		this.tupleWriterEntries.put(daoObject, twContainer);
	}



	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void reSetTupleWriter(DAOOBJECT daoObject, ITupleWriter tupleWriter) 
	{
		if (this.tupleWriterEntries== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an empty, please call setOutputDir() first.");
		if (this.tupleWriterEntries== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an empty, please call setOutputDir() first.");
		if (tupleWriter== null)
			throw new NullPointerException(MSG_ERR + "Cannot add an empty TupleWriter-Object");
		
		// if tupleWriter doesn´t exist add it
		if (this.tupleWriterEntries.get(daoObject)== null)
			this.addTupleWriter(daoObject, tupleWriter);
		
		TupleWriterContainer twContainer= this.tupleWriterEntries.get(daoObject);
		twContainer.setTupleWriter(tupleWriter);
	}



	/**
	 * Returns a TupleWriter-Object matching to the given identifier daoObject.
	 */
	public ITupleWriter getTupleWriter(DAOOBJECT daoObject) 
	{
		ITupleWriter retVal= null;
		if (this.tupleWriterEntries.get(daoObject)!= null)
			return(this.tupleWriterEntries.get(daoObject).getTupleWriter());
		return(retVal);
	}

	// ================================== end: tupleWriter handling


	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case RelANNISPackage.RADAO__TUPLE_WRITER_ENTRIES:
				return ((InternalEList<?>)getTupleWriterEntries()).basicRemove(otherEnd, msgs);
			case RelANNISPackage.RADAO__TA_ENTRIES:
				return ((InternalEList<?>)getTaEntries()).basicRemove(otherEnd, msgs);
			case RelANNISPackage.RADAO__UNIQUE_VALUES:
				return ((InternalEList<?>)getUniqueValues()).basicRemove(otherEnd, msgs);
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
			case RelANNISPackage.RADAO__OUTPUT_DIR:
				return getOutputDir();
			case RelANNISPackage.RADAO__TUPLE_WRITER_ENTRIES:
				if (coreType) return getTupleWriterEntries();
				else return getTupleWriterEntries().map();
			case RelANNISPackage.RADAO__TA_ENTRIES:
				if (coreType) return getTaEntries();
				else return getTaEntries().map();
			case RelANNISPackage.RADAO__UNIQUE_VALUES:
				if (coreType) return getUniqueValues();
				else return getUniqueValues().map();
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
			case RelANNISPackage.RADAO__OUTPUT_DIR:
				setOutputDir((File)newValue);
				return;
			case RelANNISPackage.RADAO__TUPLE_WRITER_ENTRIES:
				((EStructuralFeature.Setting)getTupleWriterEntries()).set(newValue);
				return;
			case RelANNISPackage.RADAO__TA_ENTRIES:
				((EStructuralFeature.Setting)getTaEntries()).set(newValue);
				return;
			case RelANNISPackage.RADAO__UNIQUE_VALUES:
				((EStructuralFeature.Setting)getUniqueValues()).set(newValue);
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
			case RelANNISPackage.RADAO__OUTPUT_DIR:
				setOutputDir(OUTPUT_DIR_EDEFAULT);
				return;
			case RelANNISPackage.RADAO__TUPLE_WRITER_ENTRIES:
				getTupleWriterEntries().clear();
				return;
			case RelANNISPackage.RADAO__TA_ENTRIES:
				getTaEntries().clear();
				return;
			case RelANNISPackage.RADAO__UNIQUE_VALUES:
				getUniqueValues().clear();
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
			case RelANNISPackage.RADAO__OUTPUT_DIR:
				return OUTPUT_DIR_EDEFAULT == null ? outputDir != null : !OUTPUT_DIR_EDEFAULT.equals(outputDir);
			case RelANNISPackage.RADAO__TUPLE_WRITER_ENTRIES:
				return tupleWriterEntries != null && !tupleWriterEntries.isEmpty();
			case RelANNISPackage.RADAO__TA_ENTRIES:
				return taEntries != null && !taEntries.isEmpty();
			case RelANNISPackage.RADAO__UNIQUE_VALUES:
				return uniqueValues != null && !uniqueValues.isEmpty();
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
		result.append(')');
		return result.toString();
	}

} //RADAOImpl
