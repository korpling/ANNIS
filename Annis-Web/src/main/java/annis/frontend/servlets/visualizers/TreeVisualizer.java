/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import com.sun.org.apache.xpath.internal.XPathAPI;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class TreeVisualizer extends WriterVisualizer
{

  @Override
  public String getShortName()
  {
    return "old_tree";
  }
  
  

  @Override
  public void writeOutput(VisualizerInput input, Writer writer)
  {
    try
    {
      //Converting paulaInline to SVG Tree
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new InputSource(new StringReader(input.getPaula())));

      //Using PaulaInline2DotWriter
      PaulaInline2DotWriter paula2Dot = new PaulaInline2DotWriter(document);

      Set<String> namespaceSet = new HashSet<String>();
      namespaceSet.add(input.getNamespace());

      paula2Dot.setNamespaceSet(namespaceSet);
      paula2Dot.setFillMap(input.getMarkableMap());
      paula2Dot.run();

      //width: 125 -> 80
      //depth: 90 -> 45
      paula2Dot.setScale(50);
      paula2Dot.setOutputFormat("png");
      paula2Dot.writeOutput(input, writer);
    }
    catch(RemoteException e)
    {
      //ignore
    }
    catch(ParserConfigurationException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(SAXException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public String getContentType()
  {
    return "image/png";
  }

  @Override
  public String getCharacterEncoding()
  {
    return "ISO-8859-1";
  }

  //An internal helper class
  private class PaulaInline2DotWriter
  {

    private Node rootNode;
    private Properties nodeLabelMapping;
    private Set<String> namespaceSet = new HashSet<String>();
    private int depth = 0;
    private int width = 0;
    private String outputFormat = "dot";
    private int scale = 50;
    private Map<String, String> fillMap = new HashMap<String, String>();
    private Set<Edge> edgeSet = new HashSet<Edge>();
    private Map<String, StringBuffer> nodeBufferList = new HashMap<String, StringBuffer>();
    private Map<String, StringBuffer> edgeBufferList = new HashMap<String, StringBuffer>();
    private Map<String, String> lastNodeList = new HashMap<String, String>();
    private Set<String> nodeNameSet = new HashSet<String>();

    private class Edge
    {

      public String srcId = "", dstId = "", label = "";

      @Override
      public int hashCode()
      {
        int src = 0;
        int dst = 0;
        try
        {
          src = Integer.parseInt(srcId.replaceAll("[^0-9]", ""));
        }
        catch(NumberFormatException e)
        {
          //ignore
        }
        try
        {
          dst = Integer.parseInt(dstId.replaceAll("[^0-9]", ""));
        }
        catch(NumberFormatException e)
        {
          //ignore
        }
        int hashCode = src * 10000000 + dst;
        return (hashCode == 0) ? super.hashCode() : hashCode;
      }

      @Override
      public String toString()
      {
        return srcId + " -> " + dstId + " (" + label + ")";
      }

      @Override
      public boolean equals(Object o)
      {
        if(o == null)
        {
          return false;
        }
        else
        { 
          return new Integer(this.hashCode()).equals(o.hashCode());
        }
      }
    }

    public Map<String, String> getFillMap()
    {
      return fillMap;
    }

    public void setFillMap(Map<String, String> fillMap)
    {
      this.fillMap = fillMap;
    }

    public PaulaInline2DotWriter(Document document, String xPath) throws TransformerException
    {
      this(XPathAPI.selectSingleNode(document, xPath));
      this.namespaceSet.add("tok");
    }

    public PaulaInline2DotWriter(Node rootNode)
    {
      this.rootNode = rootNode;
    }

    public int getNodeCount()
    {
      return nodeNameSet.size();
    }

    public int getEdgeCount()
    {
      return edgeSet.size();
    }

    private void buildDefinition(Node node) throws IOException
    {
      //determining level of root node
      int level = 0;
      Node parentNode = node;
      while(parentNode != null)
      {
        level++;
        parentNode = parentNode.getParentNode();
      }
      buildDefinition(node, level, 1);
    }

    private boolean writeNode(Node node, int level, int outputLevel)
    {
      if("tok".equals(node.getNodeName()))
      {
        width++;
      }
      String nodeName = getNodeName(node);
      String levelString = getLevelString(node, level);
      //setting up levelBuffer if needed
      if(null == nodeBufferList.get(levelString))
      {
        nodeBufferList.put(levelString, new StringBuffer());
        edgeBufferList.put(levelString, new StringBuffer());
      }

      if(!nodeNameSet.contains(nodeName) && (namespaceSet.contains(getNameSpace(node)) || namespaceSet.contains(node.getNodeName())))
      {
        String nodeMarkup = getNodeMarkup(node);
        if(!"".equals(nodeMarkup))
        {
          if(outputLevel > depth)
          {
            depth = outputLevel;
          }
          nodeBufferList.get(levelString).append(nodeMarkup);
          //Appending ordering edges for current level
          String lastNodeName = lastNodeList.get(levelString);
          if("token".equals(levelString) && lastNodeName != null && !nodeName.equals(lastNodeName))
          {
            edgeBufferList.get(levelString).append("\t\"" + lastNodeName + "\" -> \"" + nodeName + "\" [style=invis];\n");
          }
          lastNodeList.put(levelString, nodeName);
          nodeNameSet.add(nodeName);
          return true;
        }
      }
      return false;
    }

    private String getNameSpace(Node node)
    {
      return node.getNodeName().replaceFirst("[:.].*$", "");
    }

    private String getLevelString(Node node, int level)
    {
      //Using Level "token" for all Tokens
      if("tok".equals(node.getNodeName()))
      {
        return "token";
      }
      return Integer.toString(level);
    }

    private void buildDefinition(Node node, int level, int outputLevel) throws IOException
    {
      String levelString = getLevelString(node, level);

      //setting up levelBuffer if needed
      if(null == nodeBufferList.get(levelString))
      {
        nodeBufferList.put(levelString, new StringBuffer());
        edgeBufferList.put(levelString, new StringBuffer());
      }


      //TODO implement edges -> labels

      //Writing Current Node
      if(writeNode(node, level, outputLevel))
      {
        outputLevel++;
      }

      //Traversing Child Nodes
      NodeList childNodeList = node.getChildNodes();
      for(int i = 0; i < childNodeList.getLength(); i++)
      {
        Node n = childNodeList.item(i);
        buildDefinition(n, level + 1, outputLevel);
      }

      //writing edges
      Node parentNode = node;
      try
      {
        //We have to find the last valid parent node
        while(!(namespaceSet.contains(getNameSpace(parentNode)) || namespaceSet.contains(parentNode.getNodeName())))
        {
          parentNode = parentNode.getParentNode();
        }
        String parentNodeName = getNodeName(parentNode);
        for(int i = 0; i < childNodeList.getLength(); i++)
        {
          Node n = childNodeList.item(i);
          String currentNodeName = getNodeName(n);
          if(namespaceSet.contains(getNameSpace(n)))
          {
            Edge edge = new Edge();
            edge.srcId = parentNodeName;
            edge.dstId = currentNodeName;

            if(!edgeSet.contains(edge))
            {
              //If we cannot find the refferenced parent in our nodeNameSet we have to look it up
              if(!nodeNameSet.contains(parentNodeName))
              {
                //since this node must be one of our ancestors... track it back
                Node traceBackNode = node.getParentNode();
                int traceBackLevel = level - 1;
                boolean found = false;
                while(!found && traceBackNode != null)
                {
                  if(parentNodeName.equals(getNodeName(traceBackNode)))
                  {
                    found = writeNode(traceBackNode, traceBackLevel, 0);
                    //ATTENTION: The node order in TraceBackLevel might get corrupted...
                  }
                  traceBackNode = traceBackNode.getParentNode();
                  traceBackLevel--;
                }
              }
              //edgeBufferList.get(levelString).append("\t\"" + parentNodeName + "\" -> \"" + currentNodeName  + "\" [label=\"" + "" + "\" dir=none headport=n tailport=s];\n");
              edgeSet.add(edge);
            }
          }
        }
      }
      catch(NullPointerException e)
      {
        //We could not find a displayable parentNode so we skip this edge completely
      }
    }

    private String getNodeLabel(Node node)
    {
      if("tok".equals(node.getNodeName()))
      {
        return node.getTextContent().replace("\n", "").replace("\t", "").trim();
      }
      else
      {
        if(this.nodeLabelMapping != null)
        {
          String labelTemplate = (String) this.nodeLabelMapping.get(node.getNodeName().replace(':', '.'));
          if(labelTemplate != null)
          {
            //Replacing placeholder in labelTemplate
            Pattern pattern = Pattern.compile("\\{.*?\\}");
            Matcher matcher = pattern.matcher(labelTemplate);
            StringBuffer labelBuffer = new StringBuffer();
            while(matcher.find())
            {
              String match = labelTemplate.substring(matcher.start() + 1, matcher.end() - 1);
              try
              {
                String attValue = node.getAttributes().getNamedItem(match).getNodeValue();
                matcher.appendReplacement(labelBuffer, attValue);
              }
              catch(NullPointerException e)
              {
                //ignore
              }
            }
            matcher.appendTail(labelBuffer);
            return labelBuffer.toString().replaceAll("\\{.*?\\}", "");
          }
        }
      }
      NamedNodeMap attributes = node.getAttributes();
      String out = "";
      for(int i = 0; i < attributes.getLength(); i++)
      {
        if(!attributes.item(i).getNodeName().startsWith("_"))
        {
          out += attributes.item(i).getNodeValue().replace("\n", "").replace("\t", "").trim();
        }
      }
      return out;
    }

    private String getNodeName(Node node)
    {
      StringBuffer name = new StringBuffer();
      //name.append(node.getNodeName() + " -> ");
      try
      {
        //name.append(node.getAttributes().getNamedItem("_gid").getNodeValue() + ".");
      }
      catch(NullPointerException e)
      {
        //ignore
      }

      try
      {
        name.append(node.getAttributes().getNamedItem("_id").getNodeValue().replaceAll("(id_\\d+)_\\d+$", "$1"));
      }
      catch(NullPointerException e)
      {
        //ignore
      }
      //name.insert(0, node.getNodeName() + ".");
      return name.toString();
    }

    public void run() throws IOException
    {
      this.depth = 0;
      this.width = 0;

      //building edge map
      Map<String, Edge> edgeMap = new HashMap<String, Edge>();
      try
      {
        Node edgeNode;
        for(NodeIterator edgeNodeIterator = XPathAPI.selectNodeIterator(this.rootNode, ".//_rel"); (edgeNode = edgeNodeIterator.nextNode()) != null;)
        {
          try
          {
            NamedNodeMap attributes = edgeNode.getAttributes();
            Edge edge = new Edge();
            try
            {
              edge.srcId = attributes.getNamedItem("_src").getNodeValue();
            }
            catch(NullPointerException e)
            {
              edge.srcId = edgeNode.getParentNode().getParentNode().getAttributes().getNamedItem("_id").getNodeValue();
            }
            try
            {
              edge.dstId = attributes.getNamedItem("_dst").getNodeValue();
            }
            catch(NullPointerException e)
            {
              try
              {
                edge.dstId = attributes.getNamedItem("_target").getNodeValue();
              }
              catch(NullPointerException e2)
              {
                edge.dstId = ""; //The target definition of this edge is empty
              }
            }

            //Constructing lable
            for(int i = 0; i < attributes.getLength(); i++)
            {
              if(edge.label.length() > 0)
              {
                edge.label += ", ";
              }
              Node attribute = attributes.item(i);
              if(!attribute.getNodeName().startsWith("_"))
              {
                edge.label += attribute.getNodeValue();
              }
            }
            edgeSet.add(edge);
            edgeMap.put(edge.srcId + ";" + edge.dstId, edge);
          }
          catch(NullPointerException e)
          {
            Logger.getLogger(PaulaInline2DotWriter.class.getName())
              .log(Level.SEVERE, "Something is wrong...", e);
          }
        }
      }
      catch(TransformerException e)
      {
        Logger.getLogger(PaulaInline2DotWriter.class.getName())
              .log(Level.SEVERE, "", e);
      }

      buildDefinition(rootNode);
    }

    private void writeDOT(Writer writer) throws IOException
    {
      writer.append("digraph test {\n\tratio = auto;\n\tremincross=true; splines=false; ranksep=\"0.20 equally\"; nodesep=\"0.10\";\n");
      //System.out.println("digraph test {\n\tratio = auto;\n\tremincross=true; ranksep=\"0.25 equally\"; nodesep=\"0.10\";\n");

      for(String levelString : nodeBufferList.keySet())
      {
        StringBuffer nodeBuffer = nodeBufferList.get(levelString);
        StringBuffer edgeBuffer = edgeBufferList.get(levelString);
        writer.append("\t{\n\t\trank=same;\n");
        //System.out.println("\t{\n\t\trank=same;\n");

        writer.append(nodeBuffer.toString());
        //System.out.println(nodeBuffer.toString());

        writer.append("\t}\n");
        //System.out.println("\t}\n");

        writer.append(edgeBuffer.toString());
        //System.out.println(edgeBuffer.toString());
      }
      for(Edge e : edgeSet)
      {
        //System.out.println(e.hashCode() + ": " + e);
        if(nodeNameSet.contains(e.srcId) && nodeNameSet.contains(e.dstId))
        {
          writer.append("\t\"" + e.srcId + "\" -> \"" + e.dstId + "\" [label=\"" + e.label + "\" dir=none fontsize=9 fontname=Helvetica fontcolor=grey];\n"); //headport=n tailport=s 
        }
      }
      writer.append("}");
      //System.out.println("}");
      writer.flush();
    }

    public String getOutputFormat()
    {
      return outputFormat;
    }

    public void setOutputFormat(String outputFormat)
    {
      this.outputFormat = outputFormat;
    }

    public int getScale()
    {
      return scale;
    }

    public void setScale(int scale)
    {
      this.scale = scale;
    }

    public void writeOutput(VisualizerInput input, Writer writer)
    {
      try
      {
        //Initiating External Process
        String cmd = input.getDotPath() + " -s" + scale + ".0 -T" + outputFormat;
        Runtime runTime = Runtime.getRuntime();

        //check if neato exists
//				try {
//					Process p = runTime.exec("neato -V");
//					p.waitFor();
//					cmd = "/bin/sh -c dot | neato -n -s" + scale + ".0 -T" + outputFormat;
//				} catch (IOException e) {
//					//neato does not exist on this system
//				} catch (InterruptedException e) {
//					//ignore
//				}
        Process p = runTime.exec(cmd);
        writeDOT(new OutputStreamWriter(p.getOutputStream(), "UTF-8"));
        p.getOutputStream().close();
        Integer chr;
        InputStream is = p.getInputStream();
        while((chr = is.read()) != -1)
        {
          writer.write(chr);
        }
        p.destroy();
        writer.flush();
      }
      catch(IOException e)
      {
        e.printStackTrace();
      }
    }

    private String getNodeMarkup(Node node)
    {
      String nodeName = getNodeName(node);
      String nodeLabel = getNodeLabel(node);
      String color = fillMap.get(nodeName);
      String style = "none";
      if(color == null)
      {
        color = "black";
      }
      if("".equals(nodeLabel))
      {
        return "";
      }
      return "\t\t\"" + nodeName + "\" [shape=box label=\"" + nodeLabel + "\" fontsize=9 fontname=Helvetica fontcolor=\"" + color + "\" style=\"" + style + "\" color=\"white\"];\n";
    }

    public Properties getNodeLabelMapping()
    {
      return nodeLabelMapping;
    }

    public void setNodeLabelMapping(Properties nodeLabelMapping)
    {
      this.nodeLabelMapping = nodeLabelMapping;
    }

    public Set<String> getNamespaceSet()
    {
      return namespaceSet;
    }

    public void setNamespaceSet(Set<String> namespaceSet)
    {
      this.namespaceSet = namespaceSet;
      this.namespaceSet.add("tok");
    }

    public int getDepth()
    {
      return depth;
    }

    public int getWidth()
    {
      return width;
    }
  }
}
