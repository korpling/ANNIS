package importer.paula.paula10.importer.mapperV1;

import java.io.File;

/**
 * Diese Klasse stellt Objekte zur Verfügung, die zum temporären Zwischenspeichern con
 * StructEdge-Elementen genutzt werden können, um sie nach dem Lesen des Dokumentes weiter
 * zu verarbeiten.
 * 
 * @author Florian Zipser
 *
*/
public class TmpStructEdgeDN 
{
		public String 	corpusPath = null;
		public File 	paulaFile = null;
		public String 	paulaId = null;
		public String 	paulaType= null;
		public String 	xmlBase = null;
		public String 	structID = null;
		public String	relID = null;
		public String	relHref = null;
		public String	relType = null;
		
		
		public TmpStructEdgeDN(	String 	corpusPath,
								File 	paulaFile,
								String 	paulaId, 
								String 	paulaType,
								String 	xmlBase,
								String 	structID,
								String	relID,
								String	relHref,
								String	relType)
		{
			this.corpusPath= corpusPath;
			this.paulaFile= paulaFile;
			this.paulaId= paulaId;
			this.paulaType= paulaType;
			this.xmlBase= xmlBase;
			this.structID= structID;
			this.relID= relID;
			this.relHref= relHref;
			this.relType= relType;
		}
	
	public String toString()
	{
		return(this.relID);
	}
}