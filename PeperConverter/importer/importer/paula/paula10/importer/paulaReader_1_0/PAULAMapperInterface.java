package importer.paula.paula10.importer.paulaReader_1_0;

import java.io.File;

import importer.paula.paula10.importer.paulaReader_1_0.reader.PAULAReader;


/**
 * Dieses Interface dient der Kommunikation der spezifischen Reader des paulaReader_1_0
 * Packages mit einem diese Klasse implementierenden Mapper. Die Reader stoßen dabei 
 * die hier beschriebenen Methoden des Mappers an. <br/>
 * Dabei werden die grundsätzlichen Eigenschaften eines PAULA-Korpus unterstützt. Es 
 * wird ein Ereigniss in dem implementierenden Mapper erzeugt, wenn der PAULAConnector,
 * bzw. die spezifischen Reader ein PAULA-Datum eingelsen haben. Hierzu gehört auch das
 * Ereigniss Korpus gelesen (ein neuer Korpus bzw. Subkorpus beginnt) und Dokument
 * gelesen (ein neues Dokument beginnt).
 * 
 * @author Florian Zipser
 * @version 1.0
 */
public interface PAULAMapperInterface 
{
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
	 * Ereigniss wird aufgerufen, wenn ein neuer Korpus bzw. Subkorpus aus dem PAULA-
	 * Datenmodell resultiert.
	 * @param corpusPath String - der Pfad der bisherigen Korpora, wenn der neu zu erstellende Korpus ein Subkorpus ist
	 * @param corpusName String - Name des neu zu erzeugenden Korpus-Objekt
	 */
	public void startCorpusData(	String parentPath,
									String corpusName) throws Exception;
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
	 * Ereigniss wird aufgerufen, wenn ein neuer Korpus bzw. Subkorpus aus dem PAULA-
	 * Datenmodell resultiert.
	 * @param corpusPath String - der Pfad der bisherigen Korpora, wenn der neu zu erstellende Korpus ein Subkorpus ist
	 * @param corpusName String - Name des neu zu erzeugenden Korpus-Objekt
	 */
	public void endCorpusData(	String parentPath,
								String corpusName) throws Exception;
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
	 * Ereigniss wird aufgerufen, wenn ein neues Dokument aus dem PAULA-
	 * Datenmodell resultiert.
	 * @param corpusPath String - der Pfad der bisherigen Korpora
	 * @param docName String - Name des neu zu erzeugenden Dokument-Objekt
	 * @throws Exception
	 */
	public void startDocumentData(	String corpusPath,
									String docName) throws Exception;
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
	 * Ereigniss wird aufgerufen, wenn ein Dokument (nicht xml-Dokument) fertig 
	 * eingelesen wurde.
	 * @param corpusPath String - der Pfad der bisherigen Korpora
	 * @param docName String - Name des neu zu erzeugenden Dokument-Objekt
	 * @throws Exception
	 */
	public void endDocumentData(	String corpusPath,
									String docName) throws Exception;
	
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines MetaStructDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabeio werden alle Daten zu einem
	 * gelesenen Textelementes an die Mapper-Klasse übergeben. 
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID des Textelementes
	 * @param slType String - Der Typ der TructList in diesem Document
	 * @param structID String - ID der übergeordneten Struktur des rel-Elementes (struct-Element)
	 * @param relID String - ID dieses rel-Elementes
	 * @param href String -	Verweisziel dieses rel-Elementes
	 */
	public void metaStructDataConnector(	String korpusPath,
											File paulaFile,
											String paulaId,
											String slType,
											String structID,
											String relID,
											String href) throws Exception;
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines MetaAnnoDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabeio werden alle Daten zu einem
	 * gelesenen Textelementes an die Mapper-Klasse übergeben. 
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID des Textelementes
	 * @param paulaType String - Der Typ der Meta-Annotationsdaten dieses Dokumentes
	 * @param xmlBase String - Das Basisdokument für Verweisziele dieses Dokumentes 
	 * @param featHref String -	Verweisziel eines feat-Elementes
	 * @param featVal String Value-Wert eines feat-Elementes
	 */
	public void metaAnnoDataConnector(	String korpusPath,
										File paulaFile,
										String paulaId,
										String paulaType,
										String xmlBase,
										String featHref,
										String featVal) throws Exception;	
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines PrimDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabeio werden alle Daten zu einem
	 * gelesenen Textelementes an die Mapper-Klasse übergeben. 
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID des Textelementes
	 * @param text String - Text des Textelementes
	 */
	public void primDataConnector(	String korpusPath,
									File paulaFile,
									String paulaId, 
									String text) throws Exception;
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines TokDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabeio werden alle Daten zu einem
	 * gelesenen Tokenelementes an die Mapper-Klasse übergeben. 
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Tokenelementes
	 * @param paulaType String - Paula-Typ dieses Tokenelementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Tokenelement bezieht
	 * @param markID String - Mark-ID dieses Tokenelementes
	 * @param href String - Bezugselement, auf die sich dieses Tokenelementes bezieht
	 * @param markType String - Mark-Type dieses Tokenelementes
	 */
	public void tokDataConnector(	String korpusPath,
									File paulaFile,
									String paulaId, 
									String paulaType,
									String xmlBase,
									String markID,
									String href,
									String markType) throws Exception;
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines StructDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. 
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Strukturelementes
	 * @param paulaType String - Paula-Typ dieses Strukturelementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Strukturelement bezieht
	 * @param markID String - Mark-ID dieses Strukturelementes
	 * @param href String - Bezugselement, auf die sich dieses Strukturelementes bezieht
	 * @param markType String - Mark-Type dieses Strukturelementes
	 */
	public void markableDataConnector(	String 	korpusPath,
										File 	paulaFile,
										String 	paulaId, 
										String 	paulaType,
										String 	xmlBase,
										String 	markID,
										String 	href,
										String 	markType) throws Exception;
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines StructDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. 
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Strukturelementes
	 * @param paulaType String - Paula-Typ dieses Strukturelementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Strukturelement bezieht
	 * @param markID String - Mark-ID dieses Strukturelementes
	 * @param href String - Bezugselement, auf die sich dieses Strukturelementes bezieht
	 * @param markType String - Mark-Type dieses Strukturelementes
	 */
	/*
	public void structDataConnector(	String 	korpusPath,
										File 	paulaFile,
										String 	paulaId, 
										String 	paulaType,
										String 	xmlBase,
										String 	markID,
										String 	href,
										String 	markType) throws Exception;
	*/
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines AnnoDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. 
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Annotationselementes
	 * @param paulaType String - Paula-Typ dieses Annotationselementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
	 * @param featID String - feat-ID dieses Annotationselementes
	 * @param featHref String - Bezugselement, auf die sich dieses Annotationselementes bezieht
	 * @param featTar String - feat-Target dieses Annotationselementes
	 * @param featVal String - feat-Value dieses Annotationselementes
	 * @param featDesc String - feat-Description dieses Annotationselementes
	 * @param featExp String - feat-Example dieses Annotationselementes
	 */
	public void annoDataConnector(	String 	korpusPath,
									File 	paulaFile,
									String 	paulaId, 
									String 	paulaType,
									String 	xmlBase,
									String 	featID,
									String 	featHref,
									String 	featTar,
									String 	featVal,
									String 	featDesc,
									String 	featExp) throws Exception;
	
	/**
	 * Diese Methode bietet ein vollständiges Interface zur Kommunikation eines ComplexAnnoDataReaders 
	 * mit einer dieses Interface implementierenden Mapperklasse. Diese Funktion geht dabei auf die
	 * PAULA 1.0 Spezifika ein. Es sollte lieber die Methode 
	 * @see PAULAMapperInterface#complexAnnoDataConnector(String, File, String, String, String, String, String, String, String, String, String) 
	 * genutzt werden.
	 * Dabei werden alle Daten zu einem ComplexAnnotations-Element an die Mapper-Klasse übergeben. 
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Annotationselementes
	 * @param paulaType String - Paula-Typ dieses Annotationselementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
	 * @param featID String - feat-ID dieses Annotationselementes
	 * @param featHref String - Bezugselement, auf die sich dieses Annotationselementes bezieht
	 * @param featTar String - feat-Target dieses Annotationselementes
	 * @param featVal String - feat-Value dieses Annotationselementes
	 * @param featDesc String - feat-Description dieses Annotationselementes
	 * @param featExp String - feat-Example dieses Annotationselementes
	 * @deprecated stark auf PAULA 1.0 bezogen replaced by PAULAMapperInterface#complexAnnoDataConnector(String, File, String, String, String, String, String, String, String, String, String)
	 */
	public void complexAnnoDataConnector(	String 	korpusPath,
											File 	paulaFile,
											String 	paulaId, 
											String 	paulaType,
											String 	xmlBase,
											String 	featID,
											String 	featHref,
											String 	featTar,
											String 	featVal,
											String 	featDesc,
											String 	featExp) throws Exception;
	
	/**
	 * Diese Methode bietet ein einfaches Interface zur Kommunikation eines ComplexAnnoDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. 
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Annotationselementes
	 * @param paulaType String - Paula-Typ dieses Annotationselementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
	 * @param featVal String - Wert, den diese Kante haben kann
	 * @param srcHref String - Quelle von der aus diese Nicht-Dominanz-Kante zu ziehen ist
	 * @param dstHref String - Ziel zu dem diese Nicht-Dominanz-Kante zu ziehen ist
	 */
	public void complexAnnoDataConnector(	String 	korpusPath,
											File 	paulaFile,
											String 	paulaId, 
											String 	paulaType,
											String 	xmlBase,
											String	featVal,
											String 	srcHref,
											String 	dstHref)throws Exception;
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines StructEdgeDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. 
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Strukturelementes
	 * @param paulaType String - Paula-Typ dieses Strukturelementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Strukturelement bezieht
	 * @param structID String - ID des StructListElementes dem dieses Struct-Element unterstellt ist
	 * @param relID String - ID dieses Struct-Elementes
	 * @param relHref String - Verweis auf untergeordnete Struktur- oder Tokenelemente 
	 * @param relType String - Kantenannotation dieses Struct-Elementes
	 */
	public void structEdgeDataConnector(	String 	korpusPath,
											File 	paulaFile,
											String 	paulaId, 
											String 	paulaType,
											String 	xmlBase,
											String 	structID,
											String	relID,
											String	relHref,
											String	relType) throws Exception;
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines StructEdgeDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. Diese Methode ist für
	 * das verarbeiten von STRUCTLIST-Elementen zuständig.
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Strukturelementes
	 * @param paulaType String - Paula-Typ dieses Strukturelementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Strukturelement bezieht
	 * @param structID String - ID dieses StructListElementes
	 */
	/*
	public void structEdgeDataConnector1(	String 	korpusPath,
											File 	paulaFile,
											String 	paulaId, 
											String 	paulaType,
											String 	xmlBase,
											String 	structID) throws Exception;
	*/
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines StructEdgeDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. Diese Methode ist für
	 * das verarbeiten von STRUCT-Elementen zuständig.
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Strukturelementes
	 * @param paulaType String - Paula-Typ dieses Strukturelementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Strukturelement bezieht
	 * @param structID String - ID des StructListElementes dem dieses Struct-Element unterstellt ist
	 * @param relID String - ID dieses Struct-Elementes
	 * @param relHref String - Verweis auf untergeordnete Struktur- oder Tokenelemente 
	 * @param relType String - Kantenannotation dieses Struct-Elementes
	 */
	/*
	public void structEdgeDataConnector2(	String 	korpusPath,
											File 	paulaFile,
											String 	paulaId, 
											String 	paulaType,
											String 	xmlBase,
											String 	structID,
											String	relID,
											String	relHref,
											String	relType) throws Exception;
	*/
	
	/**
	 * Diese Methode bietet ein einfaches Interface zur Kommunikation eines ComplexAnnoDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. 
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Annotationselementes
	 * @param paulaType String - Paula-Typ dieses Annotationselementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
	 * @param featVal String - Wert, den diese Kante haben kann
	 * @param srcHref String - Quelle von der aus diese Nicht-Dominanz-Kante zu ziehen ist
	 * @param dstHref String - Ziel zu dem diese Nicht-Dominanz-Kante zu ziehen ist
	 */
	public void pointingRelDataConnector(	String 	korpusPath,
											File 	paulaFile,
											String 	paulaId, 
											String 	paulaType,
											String 	xmlBase,
											String	featVal,
											String 	srcHref,
											String 	dstHref) throws Exception;
	
	/**
	 * Diese Methode bietet ein einfaches Interface zur Kommunikation eines MultiFeatDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. 
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Annotationselementes
	 * @param multiFeatListTYPE String - 	Typ/ Annotationsebene all der Annotation
	 * @param multiFeatListBASE String - 	Basisdokument, auf dass sich alle Referenzen beziehen
	 * @param multiFeatID String - 			Identifier eines feat-Satzes
	 * @param multiFeatHREF String - 		Referenzziel eines feat-Satzes
	 * @param multiFeatID String - 			Identifier einer einzelnen Annotation
	 * @param multiFeatNAME String - 		Name einer einzelnen Annotation
	 * @param multiFeatVALUE String - 		Annotationswert einer einzelnen Annotation
	 */
	public void multiFeatDataConnector(	String 	korpusPath,
										File 	paulaFile,
										String 	paulaId, 
										String 	multiFeatListTYPE,
										String 	multiFeatListBASE,
										String 	multiFeatID,
										String 	multiFeatHREF,
										String 	featID,
										String 	featNAME,
										String 	featVALUE) throws Exception;
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines AudioDataReaders mit einer
	 * dieses Interface implementierenden Mapperklasse. Dabei werden alle Daten zu einem
	 * gelesenen Strukturelementes an die Mapper-Klasse übergeben. Wenn AudioRef= null wird kein AudioDN erzeugt.
	 * @param korpusPath String - Pfad durch den Korpus in dem das aktuelle Dokument liegt
	 * @param paulaFile File - geparste PAULA-Datei
	 * @param paulaId String - PAULA-ID dieses Annotationselementes
	 * @param paulaType String - Paula-Typ dieses Annotationselementes
	 * @param xmlBase String - Bezugsdokument, auf das sich dieses Annotationselementes bezieht
	 * @param featID String - feat-ID dieses Annotationselementes
	 * @param featHref String - Bezugselement, auf die sich dieses Annotationselementes bezieht
	 * @param featTar String - feat-Target dieses Annotationselementes
	 * @param featDesc String - feat-Description dieses Annotationselementes
	 * @param featExp String - feat-Example dieses Annotationselementes
	 * @param audioRef File - File-Referenz auf die Audio-Datei
	 */
	public void audioDataConnector(	String 	korpusPath,
									File 	paulaFile,
									String 	paulaId, 
									String 	paulaType,
									String 	xmlBase,
									String 	featID,
									String 	featHref,
									String 	featTar,
									String 	featDesc,
									String 	featExp,
									File 	audioRef) throws Exception;
	
	// --------------------------------- allgemeine Methoden ---------------------------------
	/**
	 * Diese Methode bietet ein Interface, damit ein spezifischer Reader dem Mapper-Objekt
	 * mitteilen kann wenn das parsen einer PAULA-Datei gestartet wurde. Zur Identifikation
	 * welches Objekt dieses Event erzeugt hat, muss es als Parameter mitgegeben werden.
	 * @param paulaReader PAULAReader - Reader, der dieses Event erzeugt hat.
	 * @param paulaFile File - geparste Datei
	 * @param corpusPath String - der aktuelle Korpuspfad
	 */
	public void startDocument(	PAULAReader paulaReader, 
								File paulaFile,
								String corpusPath) throws Exception;
	
	/**
	 * Diese Methode bietet ein Interface, damit ein spezifischer Reader dem Mapper-Objekt
	 * mitteilen kann wenn das parsen einer PAULA-Datei beendet wurde. Zur Identifikation
	 * welches Objekt dieses Event erzeugt hat, muss es als Parameter mitgegeben werden.
	 * @param paulaReader PAULAReader - Reader, der dieses Event erzeugt hat.
	 * @param paulaFile File- geparste Datei
	 * @param corpusPath String - der aktuelle Korpuspfad
	 */
	public void endDocument(	PAULAReader paulaReader, 
								File paulaFile,
								String corpusPath) throws Exception;
	
}
