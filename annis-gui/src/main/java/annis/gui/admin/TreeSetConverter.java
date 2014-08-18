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

package annis.gui.admin;

import com.vaadin.data.util.converter.Converter;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class TreeSetConverter implements Converter<Object, TreeSet>
{

  @Override
  public TreeSet convertToModel(Object value,
    Class<? extends TreeSet> targetType, Locale locale) throws ConversionException
  {
    TreeSet result = new TreeSet(String.CASE_INSENSITIVE_ORDER);
    if(value instanceof Collection)
    {
      result.addAll((Collection) value);
    }
    else
    {
      result.add(value);
    }
    return result;
  }

  @Override
  public Object convertToPresentation(TreeSet value,
    Class<? extends Object> targetType, Locale locale) throws ConversionException
  {
    return value;
  }

  @Override
  public Class<TreeSet> getModelType()
  {
    return TreeSet.class;
  }

  @Override
  public Class<Object> getPresentationType()
  {
    return Object.class;
  }


}
