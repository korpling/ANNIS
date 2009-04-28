/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package de.corpling.peper.impl;

import de.corpling.peper.Exporter;

import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import de.corpling.peper.FormatDefinition;
import de.corpling.peper.ImExporter;
import de.corpling.peper.Importer;
import de.corpling.peper.PeperFactory;
import de.corpling.peper.PeperPackage;
import de.corpling.peper.PorterEmitter;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Porter Emitter</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link de.corpling.peper.impl.PorterEmitterImpl#getProps <em>Props</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class PorterEmitterImpl extends EObjectImpl implements PorterEmitter 
{
	private Logger logger= Logger.getLogger(PorterEmitter.class);
	private static final String MSG_ERR= 	"Error("+PorterEmitter.class+"): ";
	/**
	 * The default value of the '{@link #getProps() <em>Props</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProps()
	 * @generated
	 * @ordered
	 */
	protected static final Properties PROPS_EDEFAULT = null;
	/**
	 * The cached value of the '{@link #getProps() <em>Props</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProps()
	 * @generated
	 * @ordered
	 */
	protected Properties props = PROPS_EDEFAULT;
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PorterEmitterImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return PeperPackage.Literals.PORTER_EMITTER;
	}

	/**
	 * Returns the im- and export properties. These properties describes the usable im- 
	 * and exporters.
	 * @return properties which shall be set
	 */
	public Properties getProps() {
		return props;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public void setProps(Properties newProps) {
		Properties oldProps = props;
		props = newProps;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, PeperPackage.PORTER_EMITTER__PROPS, oldProps, props));
		
		//read properties
		this.extractProperties(newProps);
	}

	/**
	 * Returns the Importer-object corrsponding to the given import definition.
	 * The importers will be searched and set to corpus.
	 * @param importDef import definition 
	 * @return Importer-object corrsponding to the given import definition
	 */
	public Importer emitImporter(FormatDefinition formatDefinition)
	{
		Importer importer= null;
		if ((this.importContainers== null) || (this.importContainers.size()< 1))
			throw new NullPointerException(MSG_ERR + "Cannot return an importer, because they aren´t set. Please call setProperties() first.");
		for (ImExporterContainer importContainer: importContainers)
		{
			//search for usable importer
			if (	(importContainer.formats.contains(formatDefinition.getFormatName())) &&
					(importContainer.versions.contains(formatDefinition.getFormatVersion())))
			{
				try {
					importer= (Importer) importContainer.clazz.newInstance();
				} catch (InstantiationException e) {
					throw new NullPointerException(MSG_ERR + "Cannot instantiate importer '"+importContainer.clazz+"' because: "+e.getMessage());
				} catch (IllegalAccessException e) {
					throw new NullPointerException(MSG_ERR + "Cannot instantiate importer '"+importContainer.clazz+"' because: "+e.getMessage());
				}	
			}				
		}
		return(importer);

	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 */
	public Exporter emitExporter(FormatDefinition formatDefinition) 
	{
		Exporter exporter= null;
		if ((this.importContainers== null) || (this.importContainers.size()< 1))
			throw new NullPointerException(MSG_ERR + "Cannot return an exporter, because they aren´t set. Please call setProperties() first.");
		for (ImExporterContainer exportContainer: exportContainers)
		{
			//search for usable exporter
			if (	(exportContainer.formats.contains(formatDefinition.getFormatName())) &&
					(exportContainer.versions.contains(formatDefinition.getFormatVersion())))
			{
				try {
					exporter= (Exporter) exportContainer.clazz.newInstance();
				} catch (InstantiationException e) {
					throw new NullPointerException(MSG_ERR + "Cannot instantiate exporter '"+exportContainer.clazz+"' because: "+e.getMessage());
				} catch (IllegalAccessException e) {
					throw new NullPointerException(MSG_ERR + "Cannot instantiate exporter '"+exportContainer.clazz+"' because: "+e.getMessage());
				}	
			}				
		}
		return(exporter);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case PeperPackage.PORTER_EMITTER__PROPS:
				return getProps();
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
			case PeperPackage.PORTER_EMITTER__PROPS:
				setProps((Properties)newValue);
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
			case PeperPackage.PORTER_EMITTER__PROPS:
				setProps(PROPS_EDEFAULT);
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
			case PeperPackage.PORTER_EMITTER__PROPS:
				return PROPS_EDEFAULT == null ? props != null : !PROPS_EDEFAULT.equals(props);
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
		result.append(" (props: ");
		result.append(props);
		result.append(')');
		return result.toString();
	}
	
	/**
	 * Internal class to store infos about im- and exporters. 
	 * @generated NOT
	 */
	private class ImExporterContainer
	{
		String name= null;
		Collection<String> formats= null;
		Collection<String> versions= null;
		Class<ImExporter> clazz= null;
		public String toString()
		{
			return(this.clazz+": name: "+ name + ", formats: "+ formats + ", versions: "+ versions);
		}
	}
	
	/**
	 * @generated NOT
	 */
	protected Collection<ImExporterContainer> importContainers= null;
	/**
	 * @generated NOT
	 */
	protected Collection<ImExporterContainer> exportContainers= null;
	/**
	 * Sets the im- and export properties. These properties describes the usable im- 
	 * and exporters.
	 * @param properties properties which shall be set
	 * @generated NOT
	 */
	@SuppressWarnings("unchecked")
	public void extractProperties(Properties properties)
	{
		if (properties == null)
			throw new NullPointerException(MSG_ERR + "Cannot set empty im- and export properties.");
		
		//extracting importers
		this.importContainers= new Vector<ImExporterContainer>();
		String importerNamesStr= this.getProps().getProperty("pepper.importers");
		if ((importerNamesStr!= null) && (!importerNamesStr.equalsIgnoreCase("")))
		{
			String[] importerNames= importerNamesStr.trim().split(",");
			ImExporterContainer importContainer= null;
			//alle Importernamen auslesen
			for (String importerName: importerNames)
			{
				importerName= importerName.trim();
				this.logger.debug("searching entries for description of importer: "+ importerName);
				importContainer= new ImExporterContainer();
				importContainer.name= importerName;
				
				//extract formats
				String formatsStr= this.getProps().getProperty("pepper.importer."+importerName+".formats");
				if ((formatsStr== null) || (formatsStr.equalsIgnoreCase("")))
					throw new NullPointerException(MSG_ERR + "Cannot instantiate importer '"+importerName+"' from im- and exporter properties, because supported formats arent´t there.");
				Vector<String> formatNames= new Vector<String>();
				formatNames.copyInto(formatsStr.trim().split(","));
				if (formatNames.size()== 0)
					formatNames.add(formatsStr.trim());
				importContainer.formats= formatNames;
				
				//extract versions
				String versionsStr= this.getProps().getProperty("pepper.importer."+importerName+".versions");
				if ((versionsStr== null) || (versionsStr.equalsIgnoreCase("")))
					throw new NullPointerException(MSG_ERR + "Cannot instantiate importer '"+importerName+"' from im- and exporter properties, because supported versions arent´t there.");
				Vector<String> versionNames= new Vector<String>();
				if (versionNames.size()== 0)
					versionNames.add(versionsStr.trim());
				versionNames.copyInto(versionsStr.trim().split(","));
				importContainer.versions= versionNames;
				
				//extract class
				String clazzName= this.getProps().getProperty("pepper.importer."+importerName+".class");
				if ((clazzName== null) || (clazzName.equalsIgnoreCase("")))
					throw new NullPointerException(MSG_ERR + "Cannot instantiate importer '"+importerName+"' from im- and exporter properties, because needed classname is empty.");
				try {
					importContainer.clazz= (Class<ImExporter>)Class.forName(clazzName);
				} catch (ClassNotFoundException e) 
					{ throw new NullPointerException(MSG_ERR + "Cannot instantiate importer '"+importerName+"' from im- and exporter properties, because the class '"+clazzName+"' does not exists.");}
				this.importContainers.add(importContainer);
			}
			importContainer= null;
		}
		
		//extracting exporters
		this.exportContainers= new Vector<ImExporterContainer>();
		String exporterNamesStr= this.getProps().getProperty("pepper.exporters");
		if ((exporterNamesStr!= null) && (!exporterNamesStr.equalsIgnoreCase("")))
		{
			String[] exporterNames= exporterNamesStr.split(",");
			ImExporterContainer exportContainer= null;
			//alle Exporternamen auslesen
			for (String exporterName: exporterNames)
			{
				exporterName= exporterName.trim(); 
				this.logger.debug("searching entries for description of exporter: "+ exporterName);
				exportContainer= new ImExporterContainer();
				exportContainer.name= exporterName;
				//extract formats
				String formatsStr= this.getProps().getProperty("pepper.exporter."+exporterName+".formats");
				if ((formatsStr== null) || (formatsStr.equalsIgnoreCase("")))
					throw new NullPointerException(MSG_ERR + "Cannot instantiate exporter '"+exporterName+"' from im- and exporter properties, because supported formats arent´t there.");
				Vector<String> formatNames= new Vector<String>();
				formatNames.copyInto(formatsStr.trim().split(","));
				if (formatNames.size()== 0)
					formatNames.add(formatsStr.trim());
				exportContainer.formats= formatNames;
				//extract versions
				String versionsStr= this.getProps().getProperty("pepper.exporter."+exporterName+".versions");
				if ((versionsStr== null) || (versionsStr.equalsIgnoreCase("")))
					throw new NullPointerException(MSG_ERR + "Cannot instantiate exporter '"+exporterName+"' from im- and exporter properties, because supported versions arent´t there.");
				Vector<String> versionNames= new Vector<String>();
				if (versionNames.size()== 0)
					versionNames.add(versionsStr.trim());
				versionNames.copyInto(versionsStr.trim().split(","));
				exportContainer.versions= versionNames;
				//extract class
				String clazzName= this.getProps().getProperty("pepper.exporter."+exporterName+".class");
				if ((clazzName== null) || (clazzName.equalsIgnoreCase("")))
					throw new NullPointerException(MSG_ERR + "Cannot instantiate exporter '"+exporterName+"' from im- and exporter properties, because needed classname is empty.");
				try {
					exportContainer.clazz= (Class<ImExporter>)Class.forName(clazzName);
				} catch (ClassNotFoundException e) 
					{ throw new NullPointerException(MSG_ERR + "Cannot instantiate exporter '"+exporterName+"' from im- and exporter properties, because the class '"+clazzName+"' does not exists.");}
				this.exportContainers.add(exportContainer);
			}
		}
	}


} //PorterEmitterImpl
