package paulaReader_1_0;

import java.io.File;

import paulaReader_1_0.reader.PAULAReader;

public class TestMapper implements PAULAMapperInterface 
{
	//Flags, die angeben ob die entsprechenden Methoden aufgerufen wurden
	public boolean call_startCorp= false;
	public boolean call_endCorp= false;
	public boolean call_startDoc= false;
	public boolean call_endDoc= false;
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
	 * Ereigniss wird aufgerufen, wenn ein neuer Korpus bzw. Subcorpus aus dem PAULA-
	 * Datenmodell resultiert.
	 * @param corpusPath String - der Pfad der bisherigen Korpora, wenn der neu zu erstellende Korpus ein Subcorpus ist
	 * @param corpusName String - Name des neu zu erzeugenden Korpus-Objekt
	 */
	public void startCorpusData(	String corpusPath,
									String corpusName) throws Exception
	{
		this.call_startCorp= true;
		System.out.println("start corpus name: "+corpusName + ", path: " + corpusPath);
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
	 * Ereigniss wird aufgerufen, wenn ein neuer Korpus bzw. Subcorpus aus dem PAULA-
	 * Datenmodell resultiert.
	 * @param corpusPath String - der Pfad der bisherigen Korpora, wenn der neu zu erstellende Korpus ein Subcorpus ist
	 * @param corpusName String - Name des neu zu erzeugenden Korpus-Objekt
	 */
	public void endCorpusData(	String corpusPath,
								String corpusName) throws Exception
	{
		this.call_endCorp= true;
		System.out.println("end corpus name: "+corpusName + ", path: " + corpusPath);
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
	 * Ereigniss wird aufgerufen, wenn ein neues Dokument aus dem PAULA-
	 * Datenmodell resultiert.
	 * @param corpusPath String - der Pfad der bisherigen Korpora
	 * @param docName String - Name des neu zu erzeugenden Dokument-Objekt
	 * @throws Exception
	 */
	public void startDocumentData(	String corpusPath,
									String docName) throws Exception
	{
		this.call_startDoc= true;
		System.out.println("start document name: "+docName + ", path: " + corpusPath);
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation mit einem Mapper. Dieses 
	 * Ereigniss wird aufgerufen, wenn ein Dokument (nicht xml-Dokument) fertig 
	 * eingelesen wurde.
	 * @param corpusPath String - der Pfad der bisherigen Korpora
	 * @param docName String - Name des neu zu erzeugenden Dokument-Objekt
	 * @throws Exception
	 */
	public void endDocumentData(	String corpusPath,
									String docName) throws Exception
	{
		this.call_endDoc= true;
		System.out.println("end document name: "+docName + ", path: " + corpusPath);
	}
	
	
	@Override
	public void annoDataConnector(String corpusPath, File paulaFile,
			String paulaId, String paulaType, String xmlBase, String featID,
			String featHref, String featTar, String featVal, String featDesc,
			String featExp) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void complexAnnoDataConnector(String corpusPath, File paulaFile,
			String paulaId, String paulaType, String xmlBase, String featID,
			String featHref, String featTar, String featVal, String featDesc,
			String featExp) throws Exception {
		// TODO Auto-generated method stub

	}
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
	@Override
	public void multiFeatDataConnector(	String 	korpusPath,
										File 	paulaFile,
										String 	paulaId, 
										String 	multiFeatListTYPE,
										String 	multiFeatListBASE,
										String 	multiFeatID,
										String 	multiFeatHREF,
										String 	featID,
										String 	featNAME,
										String 	featVALUE) throws Exception
	{
		// TODO Auto-generated method stub
	}
	
	/**
	 * Diese Methode bietet ein Interface zur Kommunikation eines AudioDataReaders mit einer
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
									File 	audioRef) throws Exception
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void pointingRelDataConnector(	String 	korpusPath,
											File 	paulaFile,
											String 	paulaId, 
											String 	paulaType,
											String 	xmlBase,
											String	featVal,
											String 	srcHref,
											String 	dstHref) throws Exception 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void complexAnnoDataConnector(String corpusPath, File paulaFile,
			String paulaId, String paulaType, String xmlBase, String featVal,
			String srcHref, String dstHref) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void metaAnnoDataConnector(String corpusPath, File paulaFile,
			String paulaId, String paulaType, String xmlBase, String featHref,
			String featVal) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void metaStructDataConnector(String corpusPath, File paulaFile,
			String paulaId, String slType, String structID, String relID,
			String href) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void primDataConnector(String corpusPath, File paulaFile,
			String paulaId, String text) throws Exception 
	{
		System.out.println("start primary data name: "+paulaId + ", path: " + corpusPath);
	}

	@Override
	public void startDocument(	PAULAReader paulaReader, 
								File paulaFile,
								String corpusPath) throws Exception
	{
		
	}
	
	public void endDocument(	PAULAReader paulaReader, 
								File paulaFile,
								String corpusPath) throws Exception
	{
		
	}

	@Override
	public void markableDataConnector(String corpusPath, File paulaFile,
			String paulaId, String paulaType, String xmlBase, String markID,
			String href, String markType) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void structEdgeDataConnector(String corpusPath, File paulaFile,
			String paulaId, String paulaType, String xmlBase, String structID,
			String relID, String relHref, String relType) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void tokDataConnector(String corpusPath, File paulaFile,
			String paulaId, String paulaType, String xmlBase, String markID,
			String href, String markType) throws Exception 
	{
		System.out.println("start tok data name: "+paulaId + ", path: " + corpusPath);
	}

}
