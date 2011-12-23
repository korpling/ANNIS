package annis.gui.visualizers.media.audio;

import annis.gui.visualizers.VisualizerInput;
import annis.gui.visualizers.WriterVisualizer;
import java.io.IOException;
import java.io.Writer;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
public class AudioVisualizer extends WriterVisualizer
{

  @Override
  public void writeOutput(VisualizerInput input, Writer writer)
  {
    try
    {
      String binaryServletPath = input.getContextPath() + "/Binary?name="
        + input.getResult().getDocumentName();
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
      writer.append("<audio controls preload=\"metadata\" style=\"padding-top:70px;\">");
      writer.append("<source src=\"");
      writer.append(binaryServletPath);
      writer.append("\" type=\"audio/ogg\">");
      writer.append("[Browser zu antik]");
      writer.append("</audio>");
      writer.append("</body></html>");
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public String getShortName()
  {
    return "audio";
  }
}
