/*
 * Copyright 2014 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package annis.gui.converter;

import annis.CaseSensitiveOrder;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.vaadin.v7.data.util.converter.Converter;
import java.util.Collection;
import java.util.Locale;
import java.util.TreeSet;

/**
 *
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 */
@SuppressWarnings("rawtypes")
public class CommaSeperatedStringConverterSet implements Converter<String, Collection> {

  /**
   * 
   */
  private static final long serialVersionUID = -2875078513051528093L;

  private static final Splitter splitter = Splitter.on(',').trimResults().omitEmptyStrings();

  private static final Joiner joiner = Joiner.on(", ");

  @Override
  public Collection convertToModel(String value, Class<? extends Collection> targetType,
      Locale locale) throws ConversionException {
    TreeSet<String> result = new TreeSet<>(CaseSensitiveOrder.INSTANCE);
    for (String s : splitter.split(value)) {
      result.add(s);
    }
    return result;
  }

  @Override
  public String convertToPresentation(Collection value, Class<? extends String> targetType,
      Locale locale) throws ConversionException {
    if (value == null) {
      return null;
    } else {
      return joiner.join(value);
    }
  }

  @Override
  public Class<Collection> getModelType() {
    return Collection.class;
  }

  @Override
  public Class<String> getPresentationType() {
    return String.class;
  }

}