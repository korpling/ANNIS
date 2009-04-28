/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.util;

import de.corpling.peper.*;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see de.corpling.peper.PeperPackage
 * @generated
 */
public class PeperAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static PeperPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PeperAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = PeperPackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PeperSwitch<Adapter> modelSwitch =
		new PeperSwitch<Adapter>() {
			@Override
			public Adapter caseImExportSet(ImExportSet object) {
				return createImExportSetAdapter();
			}
			@Override
			public Adapter caseExportSet(ExportSet object) {
				return createExportSetAdapter();
			}
			@Override
			public Adapter caseImportSet(ImportSet object) {
				return createImportSetAdapter();
			}
			@Override
			public Adapter caseImporter(Importer object) {
				return createImporterAdapter();
			}
			@Override
			public Adapter caseExporter(Exporter object) {
				return createExporterAdapter();
			}
			@Override
			public Adapter caseImExporter(ImExporter object) {
				return createImExporterAdapter();
			}
			@Override
			public Adapter caseFormatDefinition(FormatDefinition object) {
				return createFormatDefinitionAdapter();
			}
			@Override
			public Adapter caseConvertJob(ConvertJob object) {
				return createConvertJobAdapter();
			}
			@Override
			public Adapter caseImportObject(ImportObject object) {
				return createImportObjectAdapter();
			}
			@Override
			public Adapter caseExportObject(ExportObject object) {
				return createExportObjectAdapter();
			}
			@Override
			public Adapter casePeperConverter(PeperConverter object) {
				return createPeperConverterAdapter();
			}
			@Override
			public Adapter casePorterEmitter(PorterEmitter object) {
				return createPorterEmitterAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.ImExportSet <em>Im Export Set</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.ImExportSet
	 * @generated
	 */
	public Adapter createImExportSetAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.ExportSet <em>Export Set</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.ExportSet
	 * @generated
	 */
	public Adapter createExportSetAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.ImportSet <em>Import Set</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.ImportSet
	 * @generated
	 */
	public Adapter createImportSetAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.Importer <em>Importer</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.Importer
	 * @generated
	 */
	public Adapter createImporterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.Exporter <em>Exporter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.Exporter
	 * @generated
	 */
	public Adapter createExporterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.ImExporter <em>Im Exporter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.ImExporter
	 * @generated
	 */
	public Adapter createImExporterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.FormatDefinition <em>Format Definition</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.FormatDefinition
	 * @generated
	 */
	public Adapter createFormatDefinitionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.ConvertJob <em>Convert Job</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.ConvertJob
	 * @generated
	 */
	public Adapter createConvertJobAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.ImportObject <em>Import Object</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.ImportObject
	 * @generated
	 */
	public Adapter createImportObjectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.ExportObject <em>Export Object</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.ExportObject
	 * @generated
	 */
	public Adapter createExportObjectAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.PeperConverter <em>Converter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.PeperConverter
	 * @generated
	 */
	public Adapter createPeperConverterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link de.corpling.peper.PorterEmitter <em>Porter Emitter</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see de.corpling.peper.PorterEmitter
	 * @generated
	 */
	public Adapter createPorterEmitterAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //PeperAdapterFactory
