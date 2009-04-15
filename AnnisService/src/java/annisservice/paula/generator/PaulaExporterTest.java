package annisservice.paula.generator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

public class PaulaExporterTest {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		Properties dbProperties = new Properties();
		dbProperties.load(new FileInputStream("db.properties_localhost"));
		
		PaulaExporter exporter = new PaulaExporter(dbProperties);
		long startTime = System.currentTimeMillis();
		exporter.writeXML(new OutputStreamWriter(System.out), 8, 367); //367, 34
		
		System.out.println("Time: " + (System.currentTimeMillis() - startTime) + "msecs.");
	}
}