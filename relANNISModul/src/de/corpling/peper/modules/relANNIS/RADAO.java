/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS;

import de.corpling.salt.model.saltCore.STraversalObject;
import de.dataconnector.tupleconnector.ITupleWriter;
import java.io.File;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>RADAO</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RADAO#getOutputDir <em>Output Dir</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RADAO#getTupleWriterEntries <em>Tuple Writer Entries</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RADAO#getTaEntries <em>Ta Entries</em>}</li>
 *   <li>{@link de.corpling.peper.modules.relANNIS.RADAO#getUniqueValues <em>Unique Values</em>}</li>
 * </ul>
 * </p>
 *
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRADAO()
 * @model
 * @generated
 */
public interface RADAO extends STraversalObject {

	/**
	 * Returns the value of the '<em><b>Output Dir</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Output Dir</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Output Dir</em>' attribute.
	 * @see #setOutputDir(File)
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRADAO_OutputDir()
	 * @model dataType="de.corpling.peper.modules.relANNIS.File"
	 * @generated
	 */
	File getOutputDir();

	/**
	 * Sets the value of the '{@link de.corpling.peper.modules.relANNIS.RADAO#getOutputDir <em>Output Dir</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Output Dir</em>' attribute.
	 * @see #getOutputDir()
	 * @generated
	 */
	void setOutputDir(File value);

	/**
	 * Returns the value of the '<em><b>Tuple Writer Entries</b></em>' map.
	 * The key is of type {@link de.corpling.peper.modules.relANNIS.DAOOBJECT},
	 * and the value is of type {@link de.corpling.peper.modules.relANNIS.TupleWriterContainer},
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Tuple Writer Entries</em>' map isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Tuple Writer Entries</em>' map.
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRADAO_TupleWriterEntries()
	 * @model mapType="de.corpling.peper.modules.relANNIS.TupleWriterEntry<de.corpling.peper.modules.relANNIS.DAOOBJECT, de.corpling.peper.modules.relANNIS.TupleWriterContainer>"
	 * @generated
	 */
	EMap<DAOOBJECT, TupleWriterContainer> getTupleWriterEntries();

	/**
	 * Returns the value of the '<em><b>Ta Entries</b></em>' map.
	 * The key is of type {@link java.lang.Long},
	 * and the value is of type list of {@link de.corpling.peper.modules.relANNIS.TAObject},
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Ta Entries</em>' map isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ta Entries</em>' map.
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRADAO_TaEntries()
	 * @model mapType="de.corpling.peper.modules.relANNIS.TaEntry<org.eclipse.emf.ecore.ELongObject, de.corpling.peper.modules.relANNIS.TAObject>"
	 * @generated
	 */
	EMap<Long, EList<TAObject>> getTaEntries();

	/**
	 * Returns the value of the '<em><b>Unique Values</b></em>' map.
	 * The key is of type {@link de.corpling.peper.modules.relANNIS.UNIQUE_VALUES},
	 * and the value is of type {@link de.corpling.peper.modules.relANNIS.UniqueValue},
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Unique Values</em>' map isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Unique Values</em>' map.
	 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getRADAO_UniqueValues()
	 * @model mapType="de.corpling.peper.modules.relANNIS.UniqueValueEntry<de.corpling.peper.modules.relANNIS.UNIQUE_VALUES, de.corpling.peper.modules.relANNIS.UniqueValue>"
	 * @generated
	 */
	EMap<UNIQUE_VALUES, UniqueValue> getUniqueValues();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void write(Long taId, RACorpus raCorpus);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	long beginTA();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void commitTA(long taId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model tupleWriterDataType="de.corpling.peper.modules.relANNIS.TupleWriter"
	 * @generated
	 */
	void addTupleWriter(DAOOBJECT daoObject, ITupleWriter tupleWriter);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model tupleWriterDataType="de.corpling.peper.modules.relANNIS.TupleWriter"
	 * @generated
	 */
	void reSetTupleWriter(DAOOBJECT daoObject, ITupleWriter tupleWriter);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model dataType="de.corpling.peper.modules.relANNIS.TupleWriter"
	 * @generated
	 */
	ITupleWriter getTupleWriter(DAOOBJECT daoObject);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void write(Long taId, RACorpusAnnotation raCorpusMetaAttribute);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	Long getUniqueValue(UNIQUE_VALUES uniqueValue);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void write(Long taId, RAText raText);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void write(Long taId, RANode raStruct);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void write(Long taId, RAEdge raRank);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void write(Long taId, RANodeAnnotation raAnno);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void write(Long taId, RAComponent raRankType);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @model
	 * @generated
	 */
	void write(Long taId, RAEdgeAnnotation raEdgeAnnotation);
} // RADAO
