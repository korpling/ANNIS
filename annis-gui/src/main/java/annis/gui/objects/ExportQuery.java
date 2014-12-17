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
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ExportQuery extends ContextualizedQuery
{
  private String exporterName;
  
  private List<String> annotationKeys;
  private String parameters;

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

  public String getExporterName()
  {
    return exporterName;
  }

  public void setExporterName(String exporterName)
  {
    this.exporterName = exporterName;
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
  
  public ExportQuery exporter(String exporter)
  {
    this.exporterName = exporter;
    return this;
  }
  
  @Override
  public int hashCode()
  {
    return Objects.hash(getCorpora(), getQuery(), getLeftContext(), getRightContext(), getSegmentation(), 
      getAnnotationKeys(), getExporterName(), getParameters());
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
      && Objects.equals(getExporterName(), other.getExporterName())
      && Objects.equals(getParameters(), other.getParameters());
  }
  
}
