package org.corpus_tools.annis.gui.objects;

import java.util.List;
import org.corpus_tools.annis.gui.QueryGenerator.ContextQueryGenerator;
import org.corpus_tools.annis.gui.exporter.ExporterPlugin;

public class ExportQueryGenerator extends ContextQueryGenerator<ExportQuery, ExportQueryGenerator> {
    public ExportQueryGenerator() {
        super(new ExportQuery());
    }

    public ExportQueryGenerator alignmc(boolean alignmc) {
        getCurrent().setAlignmc(alignmc);
        return this;
    }

    public ExportQueryGenerator annotations(List<String> annotationKeys) {
        getCurrent().setAnnotationKeys(annotationKeys);
        return this;
    }

    public ExportQueryGenerator exporter(Class<? extends ExporterPlugin> exporter) {
        getCurrent().setExporter(exporter);
        return this;
    }

    public ExportQueryGenerator param(String parameters) {
        getCurrent().setParameters(parameters);
        return this;
    }
}