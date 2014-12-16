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

package annis.gui.converter;

import com.vaadin.data.util.converter.Converter;
import java.util.Date;
import java.util.Locale;
import org.joda.time.DateTime;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class DateTimeConverter implements Converter<Date, DateTime>
{

  @Override
  public DateTime convertToModel(Date value,
    Class<? extends DateTime> targetType, Locale locale) throws ConversionException
  {
    if(value == null)
    {
      return null;
    }
    return new DateTime(value);
  }

  @Override
  public Date convertToPresentation(DateTime value,
    Class<? extends Date> targetType, Locale locale) throws ConversionException
  {
    if(value == null)
    {
      return null;
    }
    return value.toDate();
  }

  @Override
  public Class<DateTime> getModelType()
  {
    return DateTime.class;
  }

  @Override
  public Class<Date> getPresentationType()
  {
    return Date.class;
  }

  
  
}
