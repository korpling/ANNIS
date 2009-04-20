/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.impl;

import de.corpling.peper.*;

import de.corpling.salt.SaltProject;
import java.io.File;

import java.util.Properties;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class PeperFactoryImpl extends EFactoryImpl implements PeperFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static PeperFactory init() {
		try {
			PeperFactory thePeperFactory = (PeperFactory)EPackage.Registry.INSTANCE.getEFactory("www.corpling.de/peper"); 
			if (thePeperFactory != null) {
				return thePeperFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new PeperFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PeperFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case PeperPackage.IM_EXPORT_SET: return createImExportSet();
			case PeperPackage.EXPORT_SET: return createExportSet();
			case PeperPackage.IMPORT_SET: return createImportSet();
			case PeperPackage.IMPORTER: return createImporter();
			case PeperPackage.EXPORTER: return createExporter();
			case PeperPackage.IM_EXPORTER: return createImExporter();
			case PeperPackage.FORMAT_DEFINITION: return createFormatDefinition();
			case PeperPackage.CONVERT_JOB: return createConvertJob();
			case PeperPackage.IMPORT_OBJECT: return createImportObject();
			case PeperPackage.EXPORT_OBJECT: return createExportObject();
			case PeperPackage.PEPER_CONVERTER: return createPeperConverter();
			case PeperPackage.PORTER_EMITTER: return createPorterEmitter();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case PeperPackage.FILE:
				return createFileFromString(eDataType, initialValue);
			case PeperPackage.PROPERTIES:
				return createPropertiesFromString(eDataType, initialValue);
			case PeperPackage.SALT_PROJECT:
				return createSaltProjectFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case PeperPackage.FILE:
				return convertFileToString(eDataType, instanceValue);
			case PeperPackage.PROPERTIES:
				return convertPropertiesToString(eDataType, instanceValue);
			case PeperPackage.SALT_PROJECT:
				return convertSaltProjectToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ImExportSet createImExportSet() {
		ImExportSetImpl imExportSet = new ImExportSetImpl();
		return imExportSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ExportSet createExportSet() {
		ExportSetImpl exportSet = new ExportSetImpl();
		return exportSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ImportSet createImportSet() {
		ImportSetImpl importSet = new ImportSetImpl();
		return importSet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Importer createImporter() {
		ImporterImpl importer = new ImporterImpl();
		return importer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Exporter createExporter() {
		ExporterImpl exporter = new ExporterImpl();
		return exporter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ImExporter createImExporter() {
		ImExporterImpl imExporter = new ImExporterImpl();
		return imExporter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FormatDefinition createFormatDefinition() {
		FormatDefinitionImpl formatDefinition = new FormatDefinitionImpl();
		return formatDefinition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ConvertJob createConvertJob() {
		ConvertJobImpl convertJob = new ConvertJobImpl();
		return convertJob;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ImportObject createImportObject() {
		ImportObjectImpl importObject = new ImportObjectImpl();
		return importObject;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ExportObject createExportObject() {
		ExportObjectImpl exportObject = new ExportObjectImpl();
		return exportObject;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PeperConverter createPeperConverter() {
		PeperConverterImpl peperConverter = new PeperConverterImpl();
		return peperConverter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PorterEmitter createPorterEmitter() {
		PorterEmitterImpl porterEmitter = new PorterEmitterImpl();
		return porterEmitter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public File createFileFromString(EDataType eDataType, String initialValue) {
		return (File)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertFileToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Properties createPropertiesFromString(EDataType eDataType, String initialValue) {
		return (Properties)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPropertiesToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SaltProject createSaltProjectFromString(EDataType eDataType, String initialValue) {
		return (SaltProject)super.createFromString(eDataType, initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertSaltProjectToString(EDataType eDataType, Object instanceValue) {
		return super.convertToString(eDataType, instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PeperPackage getPeperPackage() {
		return (PeperPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static PeperPackage getPackage() {
		return PeperPackage.eINSTANCE;
	}

} //PeperFactoryImpl
