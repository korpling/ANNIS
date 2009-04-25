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
 * A representation of the literals of the enumeration '<em><b>UNIQUE VALUES</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see de.corpling.peper.modules.relANNIS.RelANNISPackage#getUNIQUE_VALUES()
 * @model
 * @generated
 */
public enum UNIQUE_VALUES implements Enumerator {
	/**
	 * The '<em><b>CORP STRUCT PPORDER</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CORP_STRUCT_PPORDER_VALUE
	 * @generated
	 * @ordered
	 */
	CORP_STRUCT_PPORDER(0, "CORP_STRUCT_PPORDER", "CORP_STRUCT_PPORDER"),

	/**
	 * The '<em><b>CORPUS ID</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CORPUS_ID_VALUE
	 * @generated
	 * @ordered
	 */
	CORPUS_ID(1, "CORPUS_ID", "CORPUS_ID"), /**
	 * The '<em><b>TEXT ID</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #TEXT_ID_VALUE
	 * @generated
	 * @ordered
	 */
	TEXT_ID(2, "TEXT_ID", "TEXT_ID"), /**
	 * The '<em><b>STRUCT ID</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #STRUCT_ID_VALUE
	 * @generated
	 * @ordered
	 */
	STRUCT_ID(3, "STRUCT_ID", "STRUCT_ID"), /**
	 * The '<em><b>RANK PPORDER</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #RANK_PPORDER_VALUE
	 * @generated
	 * @ordered
	 */
	RANK_PPORDER(4, "RANK_PPORDER", "RANK_PPORDER"), /**
	 * The '<em><b>ANNO ID</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ANNO_ID_VALUE
	 * @generated
	 * @ordered
	 */
	ANNO_ID(5, "ANNO_ID", "ANNO_ID"), /**
	 * The '<em><b>RANK TYPE ID</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #RANK_TYPE_ID_VALUE
	 * @generated
	 * @ordered
	 */
	RANK_TYPE_ID(6, "RANK_TYPE_ID", "RANK_TYPE_ID");

	/**
	 * The '<em><b>CORP STRUCT PPORDER</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>CORP STRUCT PPORDER</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #CORP_STRUCT_PPORDER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CORP_STRUCT_PPORDER_VALUE = 0;

	/**
	 * The '<em><b>CORPUS ID</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>CORPUS ID</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #CORPUS_ID
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CORPUS_ID_VALUE = 1;

	/**
	 * The '<em><b>TEXT ID</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>TEXT ID</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #TEXT_ID
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TEXT_ID_VALUE = 2;

	/**
	 * The '<em><b>STRUCT ID</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>STRUCT ID</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #STRUCT_ID
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int STRUCT_ID_VALUE = 3;

	/**
	 * The '<em><b>RANK PPORDER</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>RANK PPORDER</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #RANK_PPORDER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int RANK_PPORDER_VALUE = 4;

	/**
	 * The '<em><b>ANNO ID</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>ANNO ID</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #ANNO_ID
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ANNO_ID_VALUE = 5;

	/**
	 * The '<em><b>RANK TYPE ID</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of '<em><b>RANK TYPE ID</b></em>' literal object isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @see #RANK_TYPE_ID
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int RANK_TYPE_ID_VALUE = 6;

	/**
	 * An array of all the '<em><b>UNIQUE VALUES</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final UNIQUE_VALUES[] VALUES_ARRAY =
		new UNIQUE_VALUES[] {
			CORP_STRUCT_PPORDER,
			CORPUS_ID,
			TEXT_ID,
			STRUCT_ID,
			RANK_PPORDER,
			ANNO_ID,
			RANK_TYPE_ID,
		};

	/**
	 * A public read-only list of all the '<em><b>UNIQUE VALUES</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<UNIQUE_VALUES> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>UNIQUE VALUES</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static UNIQUE_VALUES get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			UNIQUE_VALUES result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>UNIQUE VALUES</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static UNIQUE_VALUES getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			UNIQUE_VALUES result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>UNIQUE VALUES</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static UNIQUE_VALUES get(int value) {
		switch (value) {
			case CORP_STRUCT_PPORDER_VALUE: return CORP_STRUCT_PPORDER;
			case CORPUS_ID_VALUE: return CORPUS_ID;
			case TEXT_ID_VALUE: return TEXT_ID;
			case STRUCT_ID_VALUE: return STRUCT_ID;
			case RANK_PPORDER_VALUE: return RANK_PPORDER;
			case ANNO_ID_VALUE: return ANNO_ID;
			case RANK_TYPE_ID_VALUE: return RANK_TYPE_ID;
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
	private UNIQUE_VALUES(int value, String name, String literal) {
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
	
} //UNIQUE_VALUES
