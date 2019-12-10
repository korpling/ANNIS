package annis.gui.objects;

import java.util.List;

import annis.QueryGenerator.ContextQueryGenerator;
import annis.libgui.exporter.ExporterPlugin;

public class ExportQueryGenerator
     extends ContextQueryGenerator<ExportQuery, ExportQueryGenerator>
  {
    public ExportQueryGenerator()
    {
      super(new ExportQuery());
    }
    public ExportQueryGenerator exporter(Class<? extends ExporterPlugin> exporter)
    {
      getCurrent().setExporter(exporter);
      return this;
    }
    
    public ExportQueryGenerator annotations(List<String> annotationKeys)
    {
      getCurrent().setAnnotationKeys(annotationKeys);
      return this;
    }
    
    public ExportQueryGenerator param(String parameters)
    {
      getCurrent().setParameters(parameters);
      return this;
    }
    
    public ExportQueryGenerator alignmc(boolean alignmc)
    {
      getCurrent().setAlignmc(alignmc);
      return this;
    }
  }