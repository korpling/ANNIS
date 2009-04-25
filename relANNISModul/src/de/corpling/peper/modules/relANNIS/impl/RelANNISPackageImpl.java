/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS.impl;

import de.corpling.peper.PeperPackage;
import de.corpling.peper.impl.PeperPackageImpl;
import de.corpling.peper.modules.relANNIS.RAComponent;
import de.corpling.peper.modules.relANNIS.RACorpus;
import de.corpling.peper.modules.relANNIS.RACorpusAnnotation;
import de.corpling.peper.modules.relANNIS.RAEdge;
import de.corpling.peper.modules.relANNIS.RAEdgeAnnotation;
import de.corpling.peper.modules.relANNIS.RAExporter;
import de.corpling.peper.modules.relANNIS.RAMapper;
import de.corpling.peper.modules.relANNIS.RANode;
import de.corpling.peper.modules.relANNIS.RANodeAnnotation;
import de.corpling.peper.modules.relANNIS.RAText;
import de.corpling.peper.modules.relANNIS.RelANNISFactory;
import de.corpling.peper.modules.relANNIS.RelANNISPackage;
import de.corpling.peper.modules.relANNIS.TAObject;
import de.corpling.peper.modules.relANNIS.TupleWriterContainer;
import de.corpling.peper.modules.relANNIS.UniqueValue;

import de.corpling.salt.model.salt.SaltPackage;

import de.corpling.salt.model.salt.impl.SaltPackageImpl;

import de.corpling.salt.model.saltCore.SElement;
import de.corpling.salt.model.saltCore.SaltCorePackage;

import de.corpling.salt.model.saltCore.impl.SaltCorePackageImpl;

import de.corpling.salt.saltFW.SaltProject;

import de.dataconnector.tupleconnector.ITupleWriter;

import de.util.graph.GraphPackage;

import de.util.graph.impl.GraphPackageImpl;

import java.io.File;

import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class RelANNISPackageImpl extends EPackageImpl implements RelANNISPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass raExporterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass raMapperEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass radaoEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass raCorpusEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass tupleWriterEntryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass tupleWriterContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass taEntryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass taObjectEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass raCorpusAnnotationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass uniqueValueEntryEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass uniqueValueEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass raTextEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass raNodeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass raEdgeEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass raNodeAnnotationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass raComponentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass raEdgeAnnotationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum rA_CORPUS_TYPEEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum exporT_FILEEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum daoobjectEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum uniquE_VALUESEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType saltProjectEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType fileEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType tupleWriterEDataType = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType sElementEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private RelANNISPackageImpl() {
		super(eNS_URI, RelANNISFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this
	 * model, and for any others upon which it depends.  Simple
	 * dependencies are satisfied by calling this method on all
	 * dependent packages before doing anything else.  This method drives
	 * initialization for interdependent packages directly, in parallel
	 * with this package, itself.
	 * <p>Of this package and its interdependencies, all packages which
	 * have not yet been registered by their URI values are first created
	 * and registered.  The packages are then initialized in two steps:
	 * meta-model objects for all of the packages are created before any
	 * are initialized, since one package's meta-model objects may refer to
	 * those of another.
	 * <p>Invocation of this method will not affect any packages that have
	 * already been initialized.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static RelANNISPackage init() {
		if (isInited) return (RelANNISPackage)EPackage.Registry.INSTANCE.getEPackage(RelANNISPackage.eNS_URI);

		// Obtain or create and register package
		RelANNISPackageImpl theRelANNISPackage = (RelANNISPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof RelANNISPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new RelANNISPackageImpl());

		isInited = true;

		// Obtain or create and register interdependencies
		SaltPackageImpl theSaltPackage = (SaltPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(SaltPackage.eNS_URI) instanceof SaltPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(SaltPackage.eNS_URI) : SaltPackage.eINSTANCE);
		SaltCorePackageImpl theSaltCorePackage = (SaltCorePackageImpl)(EPackage.Registry.INSTANCE.getEPackage(SaltCorePackage.eNS_URI) instanceof SaltCorePackageImpl ? EPackage.Registry.INSTANCE.getEPackage(SaltCorePackage.eNS_URI) : SaltCorePackage.eINSTANCE);
		GraphPackageImpl theGraphPackage = (GraphPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(GraphPackage.eNS_URI) instanceof GraphPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(GraphPackage.eNS_URI) : GraphPackage.eINSTANCE);
		PeperPackageImpl thePeperPackage = (PeperPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(PeperPackage.eNS_URI) instanceof PeperPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(PeperPackage.eNS_URI) : PeperPackage.eINSTANCE);

		// Create package meta-data objects
		theRelANNISPackage.createPackageContents();
		theSaltPackage.createPackageContents();
		theSaltCorePackage.createPackageContents();
		theGraphPackage.createPackageContents();
		thePeperPackage.createPackageContents();

		// Initialize created meta-data
		theRelANNISPackage.initializePackageContents();
		theSaltPackage.initializePackageContents();
		theSaltCorePackage.initializePackageContents();
		theGraphPackage.initializePackageContents();
		thePeperPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theRelANNISPackage.freeze();

		return theRelANNISPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRAExporter() {
		return raExporterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRAExporter_RaMapper() {
		return (EReference)raExporterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRAExporter_RaDAO() {
		return (EReference)raExporterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRAMapper() {
		return raMapperEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRAMapper_RaDAO() {
		return (EReference)raMapperEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAMapper_CurrTaId() {
		return (EAttribute)raMapperEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRAMapper_RaExporter() {
		return (EReference)raMapperEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAMapper_CoherentComponents() {
		return (EAttribute)raMapperEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAMapper_SubConnectedComponents() {
		return (EAttribute)raMapperEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRADAO() {
		return radaoEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRADAO_OutputDir() {
		return (EAttribute)radaoEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRADAO_TupleWriterEntries() {
		return (EReference)radaoEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRADAO_TaEntries() {
		return (EReference)radaoEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getRADAO_UniqueValues() {
		return (EReference)radaoEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRACorpus() {
		return raCorpusEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRACorpus_Id() {
		return (EAttribute)raCorpusEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRACorpus_Name() {
		return (EAttribute)raCorpusEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRACorpus_Type() {
		return (EAttribute)raCorpusEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRACorpus_Version() {
		return (EAttribute)raCorpusEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRACorpus_Pre() {
		return (EAttribute)raCorpusEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRACorpus_Post() {
		return (EAttribute)raCorpusEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTupleWriterEntry() {
		return tupleWriterEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTupleWriterEntry_Key() {
		return (EAttribute)tupleWriterEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTupleWriterEntry_Value() {
		return (EReference)tupleWriterEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTupleWriterContainer() {
		return tupleWriterContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTupleWriterContainer_TupleWriter() {
		return (EAttribute)tupleWriterContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTaEntry() {
		return taEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTaEntry_Key() {
		return (EAttribute)taEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getTaEntry_Value() {
		return (EReference)taEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getTAObject() {
		return taObjectEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTAObject_TaId() {
		return (EAttribute)taObjectEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getTAObject_TwWriterKey() {
		return (EAttribute)taObjectEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRACorpusAnnotation() {
		return raCorpusAnnotationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRACorpusAnnotation_Corpus_ref() {
		return (EAttribute)raCorpusAnnotationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRACorpusAnnotation_Namespace() {
		return (EAttribute)raCorpusAnnotationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRACorpusAnnotation_Name() {
		return (EAttribute)raCorpusAnnotationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRACorpusAnnotation_Value() {
		return (EAttribute)raCorpusAnnotationEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getUniqueValueEntry() {
		return uniqueValueEntryEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EReference getUniqueValueEntry_Value() {
		return (EReference)uniqueValueEntryEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getUniqueValueEntry_Key() {
		return (EAttribute)uniqueValueEntryEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getUniqueValue() {
		return uniqueValueEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getUniqueValue_Value() {
		return (EAttribute)uniqueValueEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRAText() {
		return raTextEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAText_Id() {
		return (EAttribute)raTextEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAText_Name() {
		return (EAttribute)raTextEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAText_Text() {
		return (EAttribute)raTextEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRANode() {
		return raNodeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANode_Id() {
		return (EAttribute)raNodeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANode_Text_ref() {
		return (EAttribute)raNodeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANode_Corpus_ref() {
		return (EAttribute)raNodeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANode_Namespace() {
		return (EAttribute)raNodeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANode_Name() {
		return (EAttribute)raNodeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANode_Left() {
		return (EAttribute)raNodeEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANode_Right() {
		return (EAttribute)raNodeEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANode_Token_index() {
		return (EAttribute)raNodeEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANode_Continuous() {
		return (EAttribute)raNodeEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANode_Span() {
		return (EAttribute)raNodeEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRAEdge() {
		return raEdgeEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAEdge_Pre() {
		return (EAttribute)raEdgeEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAEdge_Post() {
		return (EAttribute)raEdgeEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAEdge_Node_ref() {
		return (EAttribute)raEdgeEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAEdge_Component_ref() {
		return (EAttribute)raEdgeEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAEdge_Parent() {
		return (EAttribute)raEdgeEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRANodeAnnotation() {
		return raNodeAnnotationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANodeAnnotation_Id() {
		return (EAttribute)raNodeAnnotationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANodeAnnotation_Node_ref() {
		return (EAttribute)raNodeAnnotationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANodeAnnotation_Namespace() {
		return (EAttribute)raNodeAnnotationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANodeAnnotation_Name() {
		return (EAttribute)raNodeAnnotationEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRANodeAnnotation_Value() {
		return (EAttribute)raNodeAnnotationEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRAComponent() {
		return raComponentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAComponent_Id() {
		return (EAttribute)raComponentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAComponent_Type() {
		return (EAttribute)raComponentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAComponent_Namespace() {
		return (EAttribute)raComponentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAComponent_Name() {
		return (EAttribute)raComponentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRAEdgeAnnotation() {
		return raEdgeAnnotationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAEdgeAnnotation_Edge_ref() {
		return (EAttribute)raEdgeAnnotationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAEdgeAnnotation_Namespace() {
		return (EAttribute)raEdgeAnnotationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAEdgeAnnotation_Name() {
		return (EAttribute)raEdgeAnnotationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRAEdgeAnnotation_Value() {
		return (EAttribute)raEdgeAnnotationEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getRA_CORPUS_TYPE() {
		return rA_CORPUS_TYPEEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getEXPORT_FILE() {
		return exporT_FILEEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getDAOOBJECT() {
		return daoobjectEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getUNIQUE_VALUES() {
		return uniquE_VALUESEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getSaltProject() {
		return saltProjectEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getFile() {
		return fileEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getTupleWriter() {
		return tupleWriterEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EDataType getSElement() {
		return sElementEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RelANNISFactory getRelANNISFactory() {
		return (RelANNISFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		raExporterEClass = createEClass(RA_EXPORTER);
		createEReference(raExporterEClass, RA_EXPORTER__RA_MAPPER);
		createEReference(raExporterEClass, RA_EXPORTER__RA_DAO);

		raMapperEClass = createEClass(RA_MAPPER);
		createEReference(raMapperEClass, RA_MAPPER__RA_DAO);
		createEAttribute(raMapperEClass, RA_MAPPER__CURR_TA_ID);
		createEReference(raMapperEClass, RA_MAPPER__RA_EXPORTER);
		createEAttribute(raMapperEClass, RA_MAPPER__COHERENT_COMPONENTS);
		createEAttribute(raMapperEClass, RA_MAPPER__SUB_CONNECTED_COMPONENTS);

		radaoEClass = createEClass(RADAO);
		createEAttribute(radaoEClass, RADAO__OUTPUT_DIR);
		createEReference(radaoEClass, RADAO__TUPLE_WRITER_ENTRIES);
		createEReference(radaoEClass, RADAO__TA_ENTRIES);
		createEReference(radaoEClass, RADAO__UNIQUE_VALUES);

		raCorpusEClass = createEClass(RA_CORPUS);
		createEAttribute(raCorpusEClass, RA_CORPUS__ID);
		createEAttribute(raCorpusEClass, RA_CORPUS__NAME);
		createEAttribute(raCorpusEClass, RA_CORPUS__TYPE);
		createEAttribute(raCorpusEClass, RA_CORPUS__VERSION);
		createEAttribute(raCorpusEClass, RA_CORPUS__PRE);
		createEAttribute(raCorpusEClass, RA_CORPUS__POST);

		tupleWriterEntryEClass = createEClass(TUPLE_WRITER_ENTRY);
		createEAttribute(tupleWriterEntryEClass, TUPLE_WRITER_ENTRY__KEY);
		createEReference(tupleWriterEntryEClass, TUPLE_WRITER_ENTRY__VALUE);

		tupleWriterContainerEClass = createEClass(TUPLE_WRITER_CONTAINER);
		createEAttribute(tupleWriterContainerEClass, TUPLE_WRITER_CONTAINER__TUPLE_WRITER);

		taEntryEClass = createEClass(TA_ENTRY);
		createEAttribute(taEntryEClass, TA_ENTRY__KEY);
		createEReference(taEntryEClass, TA_ENTRY__VALUE);

		taObjectEClass = createEClass(TA_OBJECT);
		createEAttribute(taObjectEClass, TA_OBJECT__TA_ID);
		createEAttribute(taObjectEClass, TA_OBJECT__TW_WRITER_KEY);

		raCorpusAnnotationEClass = createEClass(RA_CORPUS_ANNOTATION);
		createEAttribute(raCorpusAnnotationEClass, RA_CORPUS_ANNOTATION__CORPUS_REF);
		createEAttribute(raCorpusAnnotationEClass, RA_CORPUS_ANNOTATION__NAMESPACE);
		createEAttribute(raCorpusAnnotationEClass, RA_CORPUS_ANNOTATION__NAME);
		createEAttribute(raCorpusAnnotationEClass, RA_CORPUS_ANNOTATION__VALUE);

		uniqueValueEntryEClass = createEClass(UNIQUE_VALUE_ENTRY);
		createEReference(uniqueValueEntryEClass, UNIQUE_VALUE_ENTRY__VALUE);
		createEAttribute(uniqueValueEntryEClass, UNIQUE_VALUE_ENTRY__KEY);

		uniqueValueEClass = createEClass(UNIQUE_VALUE);
		createEAttribute(uniqueValueEClass, UNIQUE_VALUE__VALUE);

		raTextEClass = createEClass(RA_TEXT);
		createEAttribute(raTextEClass, RA_TEXT__ID);
		createEAttribute(raTextEClass, RA_TEXT__NAME);
		createEAttribute(raTextEClass, RA_TEXT__TEXT);

		raNodeEClass = createEClass(RA_NODE);
		createEAttribute(raNodeEClass, RA_NODE__ID);
		createEAttribute(raNodeEClass, RA_NODE__TEXT_REF);
		createEAttribute(raNodeEClass, RA_NODE__CORPUS_REF);
		createEAttribute(raNodeEClass, RA_NODE__NAMESPACE);
		createEAttribute(raNodeEClass, RA_NODE__NAME);
		createEAttribute(raNodeEClass, RA_NODE__LEFT);
		createEAttribute(raNodeEClass, RA_NODE__RIGHT);
		createEAttribute(raNodeEClass, RA_NODE__TOKEN_INDEX);
		createEAttribute(raNodeEClass, RA_NODE__CONTINUOUS);
		createEAttribute(raNodeEClass, RA_NODE__SPAN);

		raEdgeEClass = createEClass(RA_EDGE);
		createEAttribute(raEdgeEClass, RA_EDGE__PRE);
		createEAttribute(raEdgeEClass, RA_EDGE__POST);
		createEAttribute(raEdgeEClass, RA_EDGE__NODE_REF);
		createEAttribute(raEdgeEClass, RA_EDGE__COMPONENT_REF);
		createEAttribute(raEdgeEClass, RA_EDGE__PARENT);

		raNodeAnnotationEClass = createEClass(RA_NODE_ANNOTATION);
		createEAttribute(raNodeAnnotationEClass, RA_NODE_ANNOTATION__ID);
		createEAttribute(raNodeAnnotationEClass, RA_NODE_ANNOTATION__NODE_REF);
		createEAttribute(raNodeAnnotationEClass, RA_NODE_ANNOTATION__NAMESPACE);
		createEAttribute(raNodeAnnotationEClass, RA_NODE_ANNOTATION__NAME);
		createEAttribute(raNodeAnnotationEClass, RA_NODE_ANNOTATION__VALUE);

		raComponentEClass = createEClass(RA_COMPONENT);
		createEAttribute(raComponentEClass, RA_COMPONENT__ID);
		createEAttribute(raComponentEClass, RA_COMPONENT__TYPE);
		createEAttribute(raComponentEClass, RA_COMPONENT__NAMESPACE);
		createEAttribute(raComponentEClass, RA_COMPONENT__NAME);

		raEdgeAnnotationEClass = createEClass(RA_EDGE_ANNOTATION);
		createEAttribute(raEdgeAnnotationEClass, RA_EDGE_ANNOTATION__EDGE_REF);
		createEAttribute(raEdgeAnnotationEClass, RA_EDGE_ANNOTATION__NAMESPACE);
		createEAttribute(raEdgeAnnotationEClass, RA_EDGE_ANNOTATION__NAME);
		createEAttribute(raEdgeAnnotationEClass, RA_EDGE_ANNOTATION__VALUE);

		// Create enums
		rA_CORPUS_TYPEEEnum = createEEnum(RA_CORPUS_TYPE);
		exporT_FILEEEnum = createEEnum(EXPORT_FILE);
		daoobjectEEnum = createEEnum(DAOOBJECT);
		uniquE_VALUESEEnum = createEEnum(UNIQUE_VALUES);

		// Create data types
		saltProjectEDataType = createEDataType(SALT_PROJECT);
		fileEDataType = createEDataType(FILE);
		tupleWriterEDataType = createEDataType(TUPLE_WRITER);
		sElementEDataType = createEDataType(SELEMENT);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Obtain other dependent packages
		PeperPackage thePeperPackage = (PeperPackage)EPackage.Registry.INSTANCE.getEPackage(PeperPackage.eNS_URI);
		SaltPackage theSaltPackage = (SaltPackage)EPackage.Registry.INSTANCE.getEPackage(SaltPackage.eNS_URI);
		SaltCorePackage theSaltCorePackage = (SaltCorePackage)EPackage.Registry.INSTANCE.getEPackage(SaltCorePackage.eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		raExporterEClass.getESuperTypes().add(thePeperPackage.getExporter());
		raMapperEClass.getESuperTypes().add(theSaltCorePackage.getSTraversalObject());
		radaoEClass.getESuperTypes().add(theSaltCorePackage.getSTraversalObject());

		// Initialize classes and features; add operations and parameters
		initEClass(raExporterEClass, RAExporter.class, "RAExporter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRAExporter_RaMapper(), this.getRAMapper(), this.getRAMapper_RaExporter(), "raMapper", null, 0, 1, RAExporter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRAExporter_RaDAO(), this.getRADAO(), null, "raDAO", null, 0, 1, RAExporter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		EOperation op = addEOperation(raExporterEClass, null, "export", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theSaltPackage.getSCorpus(), "sCorpus", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(raExporterEClass, null, "export", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theSaltPackage.getSDocument(), "sDocument", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(raMapperEClass, RAMapper.class, "RAMapper", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRAMapper_RaDAO(), this.getRADAO(), null, "raDAO", null, 0, 1, RAMapper.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAMapper_CurrTaId(), ecorePackage.getELongObject(), "currTaId", null, 0, 1, RAMapper.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRAMapper_RaExporter(), this.getRAExporter(), this.getRAExporter_RaMapper(), "raExporter", null, 0, 1, RAMapper.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAMapper_CoherentComponents(), this.getSElement(), "coherentComponents", null, 0, -1, RAMapper.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAMapper_SubConnectedComponents(), this.getSElement(), "subConnectedComponents", null, 0, -1, RAMapper.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(raMapperEClass, null, "init", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, theSaltPackage.getSDocument(), "document", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(raMapperEClass, null, "close", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(radaoEClass, de.corpling.peper.modules.relANNIS.RADAO.class, "RADAO", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRADAO_OutputDir(), this.getFile(), "outputDir", null, 0, 1, de.corpling.peper.modules.relANNIS.RADAO.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRADAO_TupleWriterEntries(), this.getTupleWriterEntry(), null, "tupleWriterEntries", null, 0, -1, de.corpling.peper.modules.relANNIS.RADAO.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRADAO_TaEntries(), this.getTaEntry(), null, "taEntries", null, 0, -1, de.corpling.peper.modules.relANNIS.RADAO.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRADAO_UniqueValues(), this.getUniqueValueEntry(), null, "uniqueValues", null, 0, -1, de.corpling.peper.modules.relANNIS.RADAO.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		op = addEOperation(radaoEClass, null, "write", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getELongObject(), "taId", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getRACorpus(), "raCorpus", 0, 1, IS_UNIQUE, IS_ORDERED);

		addEOperation(radaoEClass, ecorePackage.getELong(), "beginTA", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(radaoEClass, null, "commitTA", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getELong(), "taId", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(radaoEClass, null, "addTupleWriter", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getDAOOBJECT(), "daoObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getTupleWriter(), "tupleWriter", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(radaoEClass, null, "reSetTupleWriter", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getDAOOBJECT(), "daoObject", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getTupleWriter(), "tupleWriter", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(radaoEClass, this.getTupleWriter(), "getTupleWriter", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getDAOOBJECT(), "daoObject", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(radaoEClass, null, "write", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getELongObject(), "taId", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getRACorpusAnnotation(), "raCorpusMetaAttribute", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(radaoEClass, ecorePackage.getELongObject(), "getUniqueValue", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getUNIQUE_VALUES(), "uniqueValue", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(radaoEClass, null, "write", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getELongObject(), "taId", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getRAText(), "raText", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(radaoEClass, null, "write", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getELongObject(), "taId", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getRANode(), "raStruct", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(radaoEClass, null, "write", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getELongObject(), "taId", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getRAEdge(), "raRank", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(radaoEClass, null, "write", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getELongObject(), "taId", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getRANodeAnnotation(), "raAnno", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(radaoEClass, null, "write", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getELongObject(), "taId", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getRAComponent(), "raRankType", 0, 1, IS_UNIQUE, IS_ORDERED);

		op = addEOperation(radaoEClass, null, "write", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, ecorePackage.getELongObject(), "taId", 0, 1, IS_UNIQUE, IS_ORDERED);
		addEParameter(op, this.getRAEdgeAnnotation(), "raEdgeAnnotation", 0, 1, IS_UNIQUE, IS_ORDERED);

		initEClass(raCorpusEClass, RACorpus.class, "RACorpus", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRACorpus_Id(), ecorePackage.getELongObject(), "id", null, 0, 1, RACorpus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRACorpus_Name(), ecorePackage.getEString(), "name", null, 0, 1, RACorpus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRACorpus_Type(), this.getRA_CORPUS_TYPE(), "type", null, 0, 1, RACorpus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRACorpus_Version(), ecorePackage.getEString(), "version", null, 0, 1, RACorpus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRACorpus_Pre(), ecorePackage.getELongObject(), "pre", null, 0, 1, RACorpus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRACorpus_Post(), ecorePackage.getELongObject(), "post", null, 0, 1, RACorpus.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(raCorpusEClass, ecorePackage.getEString(), "toStringList", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEClass(tupleWriterEntryEClass, Map.Entry.class, "TupleWriterEntry", !IS_ABSTRACT, !IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTupleWriterEntry_Key(), this.getDAOOBJECT(), "key", null, 0, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTupleWriterEntry_Value(), this.getTupleWriterContainer(), null, "value", null, 0, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(tupleWriterContainerEClass, TupleWriterContainer.class, "TupleWriterContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTupleWriterContainer_TupleWriter(), this.getTupleWriter(), "tupleWriter", null, 0, 1, TupleWriterContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(taEntryEClass, Map.Entry.class, "TaEntry", !IS_ABSTRACT, !IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTaEntry_Key(), ecorePackage.getELongObject(), "key", null, 0, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTaEntry_Value(), this.getTAObject(), null, "value", null, 0, -1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(taObjectEClass, TAObject.class, "TAObject", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTAObject_TaId(), ecorePackage.getELongObject(), "taId", null, 0, 1, TAObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTAObject_TwWriterKey(), this.getDAOOBJECT(), "twWriterKey", null, 0, 1, TAObject.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(raCorpusAnnotationEClass, RACorpusAnnotation.class, "RACorpusAnnotation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRACorpusAnnotation_Corpus_ref(), ecorePackage.getELongObject(), "corpus_ref", null, 0, 1, RACorpusAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRACorpusAnnotation_Namespace(), ecorePackage.getEString(), "namespace", null, 0, 1, RACorpusAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRACorpusAnnotation_Name(), ecorePackage.getEString(), "name", null, 0, 1, RACorpusAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRACorpusAnnotation_Value(), ecorePackage.getEString(), "value", null, 0, 1, RACorpusAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(raCorpusAnnotationEClass, ecorePackage.getEString(), "toStringList", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEClass(uniqueValueEntryEClass, Map.Entry.class, "UniqueValueEntry", !IS_ABSTRACT, !IS_INTERFACE, !IS_GENERATED_INSTANCE_CLASS);
		initEReference(getUniqueValueEntry_Value(), this.getUniqueValue(), null, "value", null, 0, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getUniqueValueEntry_Key(), this.getUNIQUE_VALUES(), "key", null, 0, 1, Map.Entry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(uniqueValueEClass, UniqueValue.class, "UniqueValue", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getUniqueValue_Value(), ecorePackage.getELongObject(), "value", null, 0, 1, UniqueValue.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(raTextEClass, RAText.class, "RAText", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRAText_Id(), ecorePackage.getELongObject(), "id", null, 0, 1, RAText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAText_Name(), ecorePackage.getEString(), "name", null, 0, 1, RAText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAText_Text(), ecorePackage.getEString(), "text", null, 0, 1, RAText.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(raTextEClass, ecorePackage.getEString(), "toStringList", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEClass(raNodeEClass, RANode.class, "RANode", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRANode_Id(), ecorePackage.getELongObject(), "id", null, 0, 1, RANode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANode_Text_ref(), ecorePackage.getELongObject(), "text_ref", null, 0, 1, RANode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANode_Corpus_ref(), ecorePackage.getELongObject(), "corpus_ref", null, 0, 1, RANode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANode_Namespace(), ecorePackage.getEString(), "namespace", null, 0, 1, RANode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANode_Name(), ecorePackage.getEString(), "name", null, 0, 1, RANode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANode_Left(), ecorePackage.getELongObject(), "left", null, 0, 1, RANode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANode_Right(), ecorePackage.getELongObject(), "right", null, 0, 1, RANode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANode_Token_index(), ecorePackage.getELongObject(), "token_index", null, 0, 1, RANode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANode_Continuous(), ecorePackage.getEBoolean(), "continuous", null, 0, 1, RANode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANode_Span(), ecorePackage.getEString(), "span", null, 0, 1, RANode.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(raNodeEClass, ecorePackage.getEString(), "toStringList", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEClass(raEdgeEClass, RAEdge.class, "RAEdge", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRAEdge_Pre(), ecorePackage.getELongObject(), "pre", null, 0, 1, RAEdge.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAEdge_Post(), ecorePackage.getELongObject(), "post", null, 0, 1, RAEdge.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAEdge_Node_ref(), ecorePackage.getELongObject(), "node_ref", null, 0, 1, RAEdge.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAEdge_Component_ref(), ecorePackage.getELongObject(), "component_ref", null, 0, 1, RAEdge.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAEdge_Parent(), ecorePackage.getELongObject(), "parent", null, 0, 1, RAEdge.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(raEdgeEClass, ecorePackage.getEString(), "toStringList", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEClass(raNodeAnnotationEClass, RANodeAnnotation.class, "RANodeAnnotation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRANodeAnnotation_Id(), ecorePackage.getELongObject(), "id", null, 0, 1, RANodeAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANodeAnnotation_Node_ref(), ecorePackage.getELongObject(), "node_ref", null, 0, 1, RANodeAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANodeAnnotation_Namespace(), ecorePackage.getEString(), "namespace", null, 0, 1, RANodeAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANodeAnnotation_Name(), ecorePackage.getEString(), "name", null, 0, 1, RANodeAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRANodeAnnotation_Value(), ecorePackage.getEString(), "value", null, 0, 1, RANodeAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(raNodeAnnotationEClass, ecorePackage.getEString(), "toStringList", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEClass(raComponentEClass, RAComponent.class, "RAComponent", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRAComponent_Id(), ecorePackage.getELongObject(), "id", null, 0, 1, RAComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAComponent_Type(), ecorePackage.getEString(), "type", null, 0, 1, RAComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAComponent_Namespace(), ecorePackage.getEString(), "namespace", null, 0, 1, RAComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAComponent_Name(), ecorePackage.getEString(), "name", null, 0, 1, RAComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(raComponentEClass, ecorePackage.getEString(), "toStringList", 0, -1, IS_UNIQUE, IS_ORDERED);

		initEClass(raEdgeAnnotationEClass, RAEdgeAnnotation.class, "RAEdgeAnnotation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRAEdgeAnnotation_Edge_ref(), ecorePackage.getELongObject(), "edge_ref", null, 0, 1, RAEdgeAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAEdgeAnnotation_Namespace(), ecorePackage.getEString(), "namespace", null, 0, 1, RAEdgeAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAEdgeAnnotation_Name(), ecorePackage.getEString(), "name", null, 0, 1, RAEdgeAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRAEdgeAnnotation_Value(), ecorePackage.getEString(), "value", null, 0, 1, RAEdgeAnnotation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		addEOperation(raEdgeAnnotationEClass, ecorePackage.getEString(), "toStringList", 0, -1, IS_UNIQUE, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(rA_CORPUS_TYPEEEnum, de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE.class, "RA_CORPUS_TYPE");
		addEEnumLiteral(rA_CORPUS_TYPEEEnum, de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE.CORPUS);
		addEEnumLiteral(rA_CORPUS_TYPEEEnum, de.corpling.peper.modules.relANNIS.RA_CORPUS_TYPE.DOCUMENT);

		initEEnum(exporT_FILEEEnum, de.corpling.peper.modules.relANNIS.EXPORT_FILE.class, "EXPORT_FILE");
		addEEnumLiteral(exporT_FILEEEnum, de.corpling.peper.modules.relANNIS.EXPORT_FILE.CORPUS);

		initEEnum(daoobjectEEnum, de.corpling.peper.modules.relANNIS.DAOOBJECT.class, "DAOOBJECT");
		addEEnumLiteral(daoobjectEEnum, de.corpling.peper.modules.relANNIS.DAOOBJECT.CORPUS);
		addEEnumLiteral(daoobjectEEnum, de.corpling.peper.modules.relANNIS.DAOOBJECT.CORPUS_ANNOTATION);
		addEEnumLiteral(daoobjectEEnum, de.corpling.peper.modules.relANNIS.DAOOBJECT.TEXT);
		addEEnumLiteral(daoobjectEEnum, de.corpling.peper.modules.relANNIS.DAOOBJECT.NODE);
		addEEnumLiteral(daoobjectEEnum, de.corpling.peper.modules.relANNIS.DAOOBJECT.EDGE);
		addEEnumLiteral(daoobjectEEnum, de.corpling.peper.modules.relANNIS.DAOOBJECT.COMPONENT);
		addEEnumLiteral(daoobjectEEnum, de.corpling.peper.modules.relANNIS.DAOOBJECT.EDGE_ANNOTATION);
		addEEnumLiteral(daoobjectEEnum, de.corpling.peper.modules.relANNIS.DAOOBJECT.NODE_ANNOTATION);
		addEEnumLiteral(daoobjectEEnum, de.corpling.peper.modules.relANNIS.DAOOBJECT.ANNOTATION_META_ATTRIBUTE);

		initEEnum(uniquE_VALUESEEnum, de.corpling.peper.modules.relANNIS.UNIQUE_VALUES.class, "UNIQUE_VALUES");
		addEEnumLiteral(uniquE_VALUESEEnum, de.corpling.peper.modules.relANNIS.UNIQUE_VALUES.CORP_STRUCT_PPORDER);
		addEEnumLiteral(uniquE_VALUESEEnum, de.corpling.peper.modules.relANNIS.UNIQUE_VALUES.CORPUS_ID);
		addEEnumLiteral(uniquE_VALUESEEnum, de.corpling.peper.modules.relANNIS.UNIQUE_VALUES.TEXT_ID);
		addEEnumLiteral(uniquE_VALUESEEnum, de.corpling.peper.modules.relANNIS.UNIQUE_VALUES.STRUCT_ID);
		addEEnumLiteral(uniquE_VALUESEEnum, de.corpling.peper.modules.relANNIS.UNIQUE_VALUES.RANK_PPORDER);
		addEEnumLiteral(uniquE_VALUESEEnum, de.corpling.peper.modules.relANNIS.UNIQUE_VALUES.ANNO_ID);
		addEEnumLiteral(uniquE_VALUESEEnum, de.corpling.peper.modules.relANNIS.UNIQUE_VALUES.RANK_TYPE_ID);

		// Initialize data types
		initEDataType(saltProjectEDataType, SaltProject.class, "SaltProject", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(fileEDataType, File.class, "File", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(tupleWriterEDataType, ITupleWriter.class, "TupleWriter", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);
		initEDataType(sElementEDataType, SElement.class, "SElement", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //RelANNISPackageImpl
