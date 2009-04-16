package annis.frontend.servlets.visualizers;

import java.io.StringReader;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.org.apache.xpath.internal.XPathAPI;

public class ExampleVisualizer extends Visualizer {

	@Override
	public void writeOutput(Writer writer) {
		try {
			//Retrieve DOM-Document for PAULA
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(this.paula)));
			
			//Use XPath API to retrieve all token from document
			NodeList tokenNodeList = XPathAPI.selectNodeList(document, ".//tok");
			
			//Write HTML preamble
			writer.append("<html><head><title>Output from ExampleVisualizer</title></head><body>");
			
			//Write output
			for(int i=0; i<tokenNodeList.getLength();i++) {
				Node tokenNode = tokenNodeList.item(i);
				
				//Use markableMap to set the color for this token
				String tokenId = tokenNode.getAttributes().getNamedItem("_id").getNodeValue();
				String color = this.markableMap.containsKey(tokenId) ? this.markableMap.get(tokenId) : "";
				
				writer.append("<font color=\"" + color + "\">" + tokenNode.getTextContent() + "</font> ");
			}

			//Write HTML footer
			writer.append("</body></html>");
		} catch (Exception e) {
			//Ignore all exceptions in this example
		}
	}
}
