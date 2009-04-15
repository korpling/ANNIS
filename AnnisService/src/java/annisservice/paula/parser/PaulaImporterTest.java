package annisservice.paula.parser;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
 
public class PaulaImporterTest { 	
	
	public static void main(String[] args) throws Exception { 
		File baseDir = new File("/Users/black/Desktop/paula/Tiger_070115-inline");
		//baseDir = new File("/Users/black/Desktop/paula/AD2006");
		//baseDir = new File("/Users/black/Desktop/paula/pcc176-inline");
		baseDir = new File("/Users/black/Desktop/paula/test");
		
		long globalStart = System.currentTimeMillis();
		int max = 500;
		int count=0;
		Properties dbProperties = new Properties();
		dbProperties.load(new FileInputStream("db.properties_localhost"));
		
		PaulaImporter paulaImporter = new PaulaImporter(dbProperties);
		long corpusId = paulaImporter.addCorpus("Test");
		//long corpusId = paulaImporter.addCorpus("pcc176");
		for(File dir : baseDir.listFiles()) {
			long startTime = Calendar.getInstance().getTimeInMillis();
			File file = new File(dir.getAbsoluteFile() + "/inline.xml");
			if(file.exists()) {
				System.out.println(dir.getName());
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
				factory.setValidating(false);
				factory.setSchema(null);
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document document = builder.parse(file);

				paulaImporter.run(document, dir.getName(), corpusId, "/RESULT/paula/inline");
				//paulaImporter.testAttributeLimit(1600);
				long endTime = Calendar.getInstance().getTimeInMillis();
				System.out.println("\tCreated " + paulaImporter.getNodeCount() + " nodes and " + paulaImporter.getEdgeCount() + " edges (" + (paulaImporter.getEdgeCount() + paulaImporter.getNodeCount()) +" elements) in " + (endTime - startTime) + "msec.");
				count++;
			}
			if(max-- == 0) break;
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Imported " + count + " Texts in " + (endTime - globalStart) + "msec (" + ((endTime - globalStart) / count) + "msecs per text avg).");
	} 
}