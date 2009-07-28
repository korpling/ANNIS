/*
 * Copyright 2009 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.frontend.servlets.visualizers;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;

import paula.parser.coreference.CoreferenceProvider;
import paula.parser.coreference.MMAXCoreferenceProvider;
import paula.parser.coreference.CoreferenceProvider.Reference;
import paula.parser.coreference.CoreferenceProvider.Token;

public class MmaxVisualizer extends Visualizer {

	@Override
	public void writeOutput(Writer writer) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
			DocumentBuilder builder = factory.newDocumentBuilder(); 
			Document document = builder.parse(new InputSource(new StringReader(super.paula)));
			
			PaulaInline2MerkelWriter paula2Merkel = new PaulaInline2MerkelWriter(document,  "/RESULT");
			paula2Merkel.run();
			
			paula2Merkel.writeOutput(writer);			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//An internal helper class
	
	private class PaulaInline2MerkelWriter {
		private Node rootNode;
		
		private Map<String,String> fillMap = new HashMap<String,String>();
		private Map<String,String> strokeMap = new HashMap<String,String>();
		private CoreferenceProvider provider = new MMAXCoreferenceProvider();
		
		public Map<String, String> getFillMap() {
			return fillMap;
		}

		public void setFillMap(Map<String, String> fillMap) {
			this.fillMap = fillMap;
		}

		public Map<String, String> getStrokeMap() {
			return strokeMap;
		}

		public void setStrokeMap(Map<String, String> strokeMap) {
			this.strokeMap = strokeMap;
		}

		public PaulaInline2MerkelWriter(Document document, String xPath) throws TransformerException {
			this(XPathAPI.selectSingleNode(document, xPath));
			
		}
		
		public PaulaInline2MerkelWriter(Node rootNode) {
			this.rootNode = rootNode;
		}

		public int getNodeCount() {
			return 0;
		}
		
		public int getEdgeCount() {
			return 0;
		}

		private void buildDefinition(Node node) {
			provider.feedNode(node);
			
			//Traversing Child Nodes
			NodeList childNodeList = node.getChildNodes();
			for(int i=0; i<childNodeList.getLength(); i++) {
				Node n = childNodeList.item(i);
				buildDefinition(n);
			}			
		}
		
		StringBuffer html = new StringBuffer();
		
		public void run() throws IOException {
			buildDefinition(rootNode);
			html.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">" +
			"<link rel=\"stylesheet\" type=\"text/css\" href=\"" + contextPath + "/javascript/extjs/resources/css/ext-all.css\" />");
			html.append("<script type=\"text/javascript\" src=\"" + contextPath + "/javascript/extjs/adapter/ext/ext-base.js\"></script>");
			html.append("<script type=\"text/javascript\" src=\"" + contextPath + "/javascript/extjs/ext-all.js\"></script>");
			html.append("</head><body>");
			html.append("<div id=\"toolbar\"></div><div id=\"mmax\" style=\"font-family: arial,tahoma,helvetica,sans-serif; font-size: 11px;\" style=\"position: absolute; top: 30px; left: 0px;\">\n");
			
			Map<String, List<Reference>> discourseGroup = new HashMap<String, List<Reference>>();
			Set<Reference> referenceSet 		= provider.getReferenceSet();
			Map<String, String> referenceMap 	= provider.getReferenceMap();
			List<Token> tokenList 				= provider.getTokenList();
			
			for(Reference ref : referenceSet) {
				String groupId = ("referring".equals(ref.type)) ? ref.refersTo : ref.id;
				//resolving main group id recursively
				while(referenceMap.containsKey(groupId))
					groupId = referenceMap.get(groupId);
				//updating discourse group
				List<Reference> currentGroup = discourseGroup.get(groupId);
				try {
					currentGroup.add(ref);
				} catch (NullPointerException e) {
					currentGroup = new ArrayList<Reference>();
					currentGroup.add(ref);
				}
				if("".equals(groupId)) {
					System.out.println("Empty: " + ref.type + ": " + ref.id + " -> " + ref.refersTo);
				} else {
					discourseGroup.put(groupId, currentGroup);
				}
			}
			
			html.append("<script type=\"text/javascript\">\nvar discourseGroups = new Array();\n");
			for(Entry<String, List<Reference>> entry : discourseGroup.entrySet()) {
				html.append("\tdiscourseGroups['" + entry.getKey() + "'] = [");
				int count = 0;		
				for(Reference ref : entry.getValue())
					for(String tokenId : ref.tokenIdList)
						html.append((count++ > 0 ? ", " : "") + "'id" + tokenId + "'");
				html.append("];\n");
			}		
			html.append("\n");
			html.append("</script>");
			//creating selection pane
			html.append("<div style=\"width: 350px; float: right;\"");
			html.append("<ul style=\"list-style: none;\">");
			for(Entry<String, List<Reference>> entry : discourseGroup.entrySet()) {
				StringBuffer checkBoxContent = new StringBuffer();
				html.append("\t<li><input id=\"checkbox_group_" + entry.getKey() + "\" name=\"" + entry.getKey() + "\" type=\"checkbox\" onclick=\"toggleGroupClass(this.name)\"/>");
				checkBoxContent.append("<select style=\"width: 200px;\">");
				int itemCount = 0;
				boolean isSelected = false;
				for(Reference ref : entry.getValue()) {
					checkBoxContent.append("<option");
					if("discourse-new".equals(ref.type) || "discourse_cataphor".equals(ref.type) && !isSelected) {
						checkBoxContent.append(" selected=\"t\"");
						isSelected = true;
					}
					checkBoxContent.append(">" + ref.text +  "</option>");
					itemCount++;
				}

				checkBoxContent.append("</select>");
				if(itemCount > 1)
					html.append(checkBoxContent);
				else
					html.append(entry.getValue().get(0).text);
				html.append("</li>\n");
			}
			html.append("</ul>");
			html.append("</div>");
			
			for(Token token : tokenList) {
				if(!token.text.matches("^[,.!?;]$"))
					html.append("\n");
				html.append("<font id=\"id" + token.id + "\" onmouseover=\"markText(this.id, true)\" onmouseout=\"markText(this.id, false)\" class=\"dummy\">" + token.text + "</font>");
			}
			html.append("<script type=\"text/javascript\" src=\"" + contextPath + "/javascript/annis/visualizer/MmaxVisualizer.js\"></script>");
			html.append("</div></body></html>");
		}
		
		public void writeOutput(Writer writer) {
			try {
				writer.append(html);
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
