/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.util;

import de.corpling.peper.*;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see de.corpling.peper.PeperPackage
 * @generated
 */
public class PeperSwitch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static PeperPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PeperSwitch() {
		if (modelPackage == null) {
			modelPackage = PeperPackage.eINSTANCE;
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	public T doSwitch(EObject theEObject) {
		return doSwitch(theEObject.eClass(), theEObject);
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T doSwitch(EClass theEClass, EObject theEObject) {
		if (theEClass.eContainer() == modelPackage) {
			return doSwitch(theEClass.getClassifierID(), theEObject);
		}
		else {
			List<EClass> eSuperTypes = theEClass.getESuperTypes();
			return
				eSuperTypes.isEmpty() ?
					defaultCase(theEObject) :
					doSwitch(eSuperTypes.get(0), theEObject);
		}
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case PeperPackage.IM_EXPORT_SET: {
				ImExportSet imExportSet = (ImExportSet)theEObject;
				T result = caseImExportSet(imExportSet);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PeperPackage.EXPORT_SET: {
				ExportSet exportSet = (ExportSet)theEObject;
				T result = caseExportSet(exportSet);
				if (result == null) result = caseImExportSet(exportSet);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PeperPackage.IMPORT_SET: {
				ImportSet importSet = (ImportSet)theEObject;
				T result = caseImportSet(importSet);
				if (result == null) result = caseImExportSet(importSet);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PeperPackage.IMPORTER: {
				Importer importer = (Importer)theEObject;
				T result = caseImporter(importer);
				if (result == null) result = caseImExporter(importer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PeperPackage.EXPORTER: {
				Exporter exporter = (Exporter)theEObject;
				T result = caseExporter(exporter);
				if (result == null) result = caseImExporter(exporter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PeperPackage.IM_EXPORTER: {
				ImExporter imExporter = (ImExporter)theEObject;
				T result = caseImExporter(imExporter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PeperPackage.FORMAT_DEFINITION: {
				FormatDefinition formatDefinition = (FormatDefinition)theEObject;
				T result = caseFormatDefinition(formatDefinition);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PeperPackage.CONVERT_JOB: {
				ConvertJob convertJob = (ConvertJob)theEObject;
				T result = caseConvertJob(convertJob);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PeperPackage.IMPORT_OBJECT: {
				ImportObject importObject = (ImportObject)theEObject;
				T result = caseImportObject(importObject);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PeperPackage.EXPORT_OBJECT: {
				ExportObject exportObject = (ExportObject)theEObject;
				T result = caseExportObject(exportObject);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PeperPackage.PEPER_CONVERTER: {
				PeperConverter peperConverter = (PeperConverter)theEObject;
				T result = casePeperConverter(peperConverter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case PeperPackage.PORTER_EMITTER: {
				PorterEmitter porterEmitter = (PorterEmitter)theEObject;
				T result = casePorterEmitter(porterEmitter);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Im Export Set</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Im Export Set</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseImExportSet(ImExportSet object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Export Set</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Export Set</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseExportSet(ExportSet object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Import Set</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Import Set</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseImportSet(ImportSet object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Importer</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Importer</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseImporter(Importer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Exporter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Exporter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseExporter(Exporter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Im Exporter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Im Exporter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseImExporter(ImExporter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Format Definition</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Format Definition</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFormatDefinition(FormatDefinition object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Convert Job</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Convert Job</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseConvertJob(ConvertJob object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Import Object</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Import Object</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseImportObject(ImportObject object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Export Object</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Export Object</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseExportObject(ExportObject object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Converter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Converter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePeperConverter(PeperConverter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Porter Emitter</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Porter Emitter</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePorterEmitter(PorterEmitter object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	public T defaultCase(EObject object) {
		return null;
	}

} //PeperSwitch
