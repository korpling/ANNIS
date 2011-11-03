package annis.frontend.servlets.visualizers.media.audio;

import java.io.IOException;
import java.io.Writer;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import annis.frontend.servlets.visualizers.VisualizerInput;
import annis.frontend.servlets.visualizers.WriterVisualizer;

@PluginImplementation
public class AudioVisualizer extends WriterVisualizer
{

    @Override
    public void writeOutput(VisualizerInput input, Writer writer)
    {
        try
        {
            String binaryServletPath = input.getContextPath() + "/secure/Binary?id=2";
            writer.append("<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
            writer.append("<body>");
            writer.append("<audio controls preload=none style=\"padding-top:70px\">");
            writer.append("<source src=\"" + binaryServletPath + "\" type=\"audio/ogg\">");
            writer.append("[Browser zu antik]");
            writer.append("</audio>");
            writer.append("</body></html>");

        } catch (IOException e)
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
