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

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.vaadin.data.util.converter.Converter;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class CommaSeperatedStringConverter implements Converter<String, Set>
{
  private final Splitter splitter = Splitter.on(',').trimResults().omitEmptyStrings();
  private final Joiner joiner = Joiner.on(", ");

  @Override
  public Set convertToModel(String value,
    Class<? extends Set> targetType, Locale locale) throws ConversionException
  {
    return new TreeSet<>(splitter.splitToList(value));
  }

  @Override
  public String convertToPresentation(Set value,
    Class<? extends String> targetType, Locale locale) throws ConversionException
  {
    return joiner.join(value);
  }

  @Override
  public Class<String> getPresentationType()
  {
    return String.class;
  }

  @Override
  public Class<Set> getModelType()
  {
    return Set.class;
  }
  
}
