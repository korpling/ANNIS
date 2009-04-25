/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.modules.relANNIS;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>DAOOBJECT</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getDAOOBJECT()
 * @model
 * @generated
 */
public enum DAOOBJECT implements Enumerator {
	/**
	 * The '<em><b>CORPUS</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CORPUS_VALUE
	 * @generated
	 * @ordered
	 */
	CORPUS(0, "CORPUS", "CORPUS"), /**
	 * The '<em><b>CORPUS ANNOTATION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CORPUS_ANNOTATION_VALUE
	 * @generated
	 * @ordered
	 */
	CORPUS_ANNOTATION(1, "CORPUS_ANNOTATION", "CORPUS_ANNOTATION"), /**
	 * The '<em><b>TEXT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TEXT_VALUE
	 * @generated
	 * @ordered
	 */
	TEXT(2, "TEXT", "TEXT"), /**
	 * The '<em><b>NODE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #NODE_VALUE
	 * @generated
	 * @ordered
	 */
	NODE(3, "NODE", "NODE"), /**
	 * The '<em><b>EDGE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #EDGE_VALUE
	 * @generated
	 * @ordered
	 */
	EDGE(4, "EDGE", "EDGE"), /**
	 * The '<em><b>COMPONENT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #COMPONENT_VALUE
	 * @generated
	 * @ordered
	 */
	COMPONENT(5, "COMPONENT", "COMPONENT"), /**
	 * The '<em><b>EDGE ANNOTATION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #EDGE_ANNOTATION_VALUE
	 * @generated
	 * @ordered
	 */
	EDGE_ANNOTATION(6, "EDGE_ANNOTATION", "EDGE_ANNOTATION"), /**
	 * The '<em><b>NODE ANNOTATION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #NODE_ANNOTATION_VALUE
	 * @generated
	 * @ordered
	 */
	NODE_ANNOTATION(7, "NODE_ANNOTATION", "NODE_ANNOTATION"), /**
	 * The '<em><b>ANNOTATION META ATTRIBUTE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ANNOTATION_META_ATTRIBUTE_VALUE
	 * @generated
	 * @ordered
	 */
	ANNOTATION_META_ATTRIBUTE(8, "ANNOTATION_META_ATTRIBUTE", "ANNOTATION_META_ATTRIBUTE");

	/**
	 * The '<em><b>CORPUS</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>CORPUS</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #CORPUS
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CORPUS_VALUE = 0;

	/**
	 * The '<em><b>CORPUS ANNOTATION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>CORPUS ANNOTATION</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #CORPUS_ANNOTATION
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CORPUS_ANNOTATION_VALUE = 1;

	/**
	 * The '<em><b>TEXT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>TEXT</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #TEXT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TEXT_VALUE = 2;

	/**
	 * The '<em><b>NODE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>NODE</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #NODE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NODE_VALUE = 3;

	/**
	 * The '<em><b>EDGE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>EDGE</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #EDGE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int EDGE_VALUE = 4;

	/**
	 * The '<em><b>COMPONENT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>COMPONENT</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #COMPONENT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int COMPONENT_VALUE = 5;

	/**
	 * The '<em><b>EDGE ANNOTATION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>EDGE ANNOTATION</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #EDGE_ANNOTATION
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int EDGE_ANNOTATION_VALUE = 6;

	/**
	 * The '<em><b>NODE ANNOTATION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>NODE ANNOTATION</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #NODE_ANNOTATION
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NODE_ANNOTATION_VALUE = 7;

	/**
	 * The '<em><b>ANNOTATION META ATTRIBUTE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>ANNOTATION META ATTRIBUTE</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #ANNOTATION_META_ATTRIBUTE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ANNOTATION_META_ATTRIBUTE_VALUE = 8;

	/**
	 * An array of all the '<em><b>DAOOBJECT</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final DAOOBJECT[] VALUES_ARRAY =
		new DAOOBJECT[] {
			CORPUS,
			CORPUS_ANNOTATION,
			TEXT,
			NODE,
			EDGE,
			COMPONENT,
			EDGE_ANNOTATION,
			NODE_ANNOTATION,
			ANNOTATION_META_ATTRIBUTE,
		};

	/**
	 * A public read-only list of all the '<em><b>DAOOBJECT</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<DAOOBJECT> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>DAOOBJECT</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DAOOBJECT get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			DAOOBJECT result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>DAOOBJECT</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DAOOBJECT getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			DAOOBJECT result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>DAOOBJECT</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DAOOBJECT get(int value) {
		switch (value) {
			case CORPUS_VALUE: return CORPUS;
			case CORPUS_ANNOTATION_VALUE: return CORPUS_ANNOTATION;
			case TEXT_VALUE: return TEXT;
			case NODE_VALUE: return NODE;
			case EDGE_VALUE: return EDGE;
			case COMPONENT_VALUE: return COMPONENT;
			case EDGE_ANNOTATION_VALUE: return EDGE_ANNOTATION;
			case NODE_ANNOTATION_VALUE: return NODE_ANNOTATION;
			case ANNOTATION_META_ATTRIBUTE_VALUE: return ANNOTATION_META_ATTRIBUTE;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private DAOOBJECT(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public int getValue() {
	  return value;
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
	public String getLiteral() {
	  return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //DAOOBJECT
