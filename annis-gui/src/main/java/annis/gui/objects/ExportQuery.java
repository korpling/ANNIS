/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.objects;

import com.google.common.base.Splitter;

import annis.libgui.exporter.ExporterPlugin;

import java.util.List;
import java.util.Objects;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ExportQuery extends ContextualizedQuery
{
  private Class<? extends ExporterPlugin> exporter;
  
  private List<String> annotationKeys;
  private String parameters;
  private boolean alignmc;

  public List<String> getAnnotationKeys()
  {
    return annotationKeys;
  }

  public void setAnnotationKeys(List<String> annotationKeys)
  {
    this.annotationKeys = annotationKeys;
  }

  public String getParameters()
  {
    return parameters;
  }

  public void setParameters(String parameters)
  {
    this.parameters = parameters;
  } 
  
  public boolean getAlignmc()
  {
	  return alignmc;
  }
  
  public void setAlignmc (boolean alignmc)
  {
	  this.alignmc = alignmc;
  }

  
  public Class<? extends ExporterPlugin> getExporter()
  {
    return exporter;
  }

  public void setExporter(Class<? extends ExporterPlugin> exporter)
  {
    this.exporter = exporter;
  }

  public ExportQuery annotationKeys(String annotationKeys)
  {
    this.annotationKeys = Splitter.on(',').omitEmptyStrings()
        .trimResults().splitToList(annotationKeys);
    return this;
  }
  
  public ExportQuery params(String parameters)
  {
    this.parameters = parameters;
    return this;
  }
  
  public ExportQuery alignmc(boolean alignmc)
  {
    this.alignmc = alignmc;
    return this;
  }
  
  public ExportQuery exporter(Class<? extends ExporterPlugin> exporter)
  {
    this.exporter = exporter;
    return this;
  }
  
  @Override
  public int hashCode()
  {
    return Objects.hash(getCorpora(), getQuery(), getLeftContext(), getRightContext(), getSegmentation(), 
      getAnnotationKeys(), getExporter(), getParameters());
  }

  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    final ExportQuery other = (ExportQuery) obj;
    return 
      Objects.equals(getQuery(), other.getQuery())
      && Objects.equals(getCorpora(), other.getCorpora())
      && Objects.equals(getLeftContext(), other.getLeftContext())
      && Objects.equals(getRightContext(), other.getRightContext())
      && Objects.equals(getSegmentation(), other.getSegmentation())
      && Objects.equals(getAnnotationKeys(), other.getAnnotationKeys())
      && Objects.equals(getExporter(), other.getExporter())
      && Objects.equals(getParameters(), other.getParameters());
  }
  
}
