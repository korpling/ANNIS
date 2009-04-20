/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see de.corpling.peper.PeperPackage
 * @generated
 */
public interface PeperFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	PeperFactory eINSTANCE = de.corpling.peper.impl.PeperFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Im Export Set</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Im Export Set</em>'.
	 * @generated
	 */
	ImExportSet createImExportSet();

	/**
	 * Returns a new object of class '<em>Export Set</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Export Set</em>'.
	 * @generated
	 */
	ExportSet createExportSet();

	/**
	 * Returns a new object of class '<em>Import Set</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Import Set</em>'.
	 * @generated
	 */
	ImportSet createImportSet();

	/**
	 * Returns a new object of class '<em>Importer</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Importer</em>'.
	 * @generated
	 */
	Importer createImporter();

	/**
	 * Returns a new object of class '<em>Exporter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Exporter</em>'.
	 * @generated
	 */
	Exporter createExporter();

	/**
	 * Returns a new object of class '<em>Im Exporter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Im Exporter</em>'.
	 * @generated
	 */
	ImExporter createImExporter();

	/**
	 * Returns a new object of class '<em>Format Definition</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Format Definition</em>'.
	 * @generated
	 */
	FormatDefinition createFormatDefinition();

	/**
	 * Returns a new object of class '<em>Convert Job</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Convert Job</em>'.
	 * @generated
	 */
	ConvertJob createConvertJob();

	/**
	 * Returns a new object of class '<em>Import Object</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Import Object</em>'.
	 * @generated
	 */
	ImportObject createImportObject();

	/**
	 * Returns a new object of class '<em>Export Object</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Export Object</em>'.
	 * @generated
	 */
	ExportObject createExportObject();

	/**
	 * Returns a new object of class '<em>Converter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Converter</em>'.
	 * @generated
	 */
	PeperConverter createPeperConverter();

	/**
	 * Returns a new object of class '<em>Porter Emitter</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Porter Emitter</em>'.
	 * @generated
	 */
	PorterEmitter createPorterEmitter();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	PeperPackage getPeperPackage();

} //PeperFactory
