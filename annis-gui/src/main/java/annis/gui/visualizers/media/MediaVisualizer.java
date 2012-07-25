package annis.gui.visualizers.media;

import annis.CommonHelper;
import annis.gui.visualizers.VisualizerInput;
import annis.gui.visualizers.WriterVisualizer;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

public abstract class MediaVisualizer extends WriterVisualizer
{

  @Override
  public void writeOutput(VisualizerInput input, Writer writer)
  {
    List<String> corpusPath = CommonHelper.getCorpusPath(input.getDocument().getSCorpusGraph(), input.getDocument());
    try
    {       
      String binaryServletPath = input.getContextPath() + "/Binary?"
        + "documentName=" + corpusPath.get(0)
        + "&toplevelCorpusName=" + corpusPath.get(corpusPath.size()-1);
      writer.append("<!DOCTYPE html>");
      writer.append("<html>");
      writer.append("<head>");
      writer.append(
        "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");      
      writer.append("<script type=\"text/javascript\" src=\"");
      writer.append(input.getResourcePath("jquery-1.7.1.min.js"));
      writer.append("\"></script>");
      writer.append("<script type=\"text/javascript\" src=\"");
      writer.append(input.getResourcePath("media_control.js"));
      writer.append("\"></script>");      
      writer.append("</head>");
      writer.append("<body>");      
      writer.append("<" + getTag() + " controls preload=\"metadata\" >");
      writer.append("<source src=\"");
      writer.append(binaryServletPath);
      writer.append("\" type=\"" + getMediaMime() + "\">");
      writer.append("Please update your browser!");
      writer.append("</" + getTag() + ">");
      writer.append("</body></html>");
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Return a string which represented a snippet of javascript-code, the could 
   * be read by media_control.js
   * 
   * @param input
   * @return 
   */
  private String getTimeRange(VisualizerInput input)
  {
    AnnotationGraph g = input.getResult().getGraph();
    List<AnnisNode> resultList = g.getNodes();
    AnnisNode n = resultList.get(0);
    Set<Annotation> nodeAnnotations = n.getNodeAnnotations();
    StringBuffer ret;

    for (Annotation annotation : nodeAnnotations)
    {
      if ("time".equals(annotation.getName()))
      {
        ret = new StringBuffer("var startTime = ");
        ret.append(annotation.getValue().split("-")[0]);        
        ret.append(";");
        
        return ret.toString();
      }
    }

    return "no time anno";
  }
  
  public abstract String getTag();
  public abstract String getMediaMime();
  
}
