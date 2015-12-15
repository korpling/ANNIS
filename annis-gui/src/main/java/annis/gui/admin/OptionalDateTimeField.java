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

import annis.gui.converter.DateTimeConverter;
import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import java.util.Objects;
import org.joda.time.DateTime;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class OptionalDateTimeField extends CustomField<DateTime>
{

  private final DateField dateField;
  private final CheckBox checkBox;
  private final HorizontalLayout layout;
  
  public OptionalDateTimeField()
  {
    this("");
  }

  public OptionalDateTimeField(String checkboxCaption)
  {
    dateField = new DateField();
    dateField.setConverter(new DateTimeConverter());
    dateField.setDateFormat("yyyy-MM-dd");
    dateField.setImmediate(true);
    dateField.setPropertyDataSource(OptionalDateTimeField.this);

    checkBox = new CheckBox(checkboxCaption);
    checkBox.addValueChangeListener(new ValueChangeListener()
    {
      @Override
      public void valueChange(Property.ValueChangeEvent event)
      {
        if (Objects.equals(event.getProperty().getValue(), Boolean.TRUE))
        {
          if(getValue() == null)
          {
            // only set something if changed
            setValue(DateTime.now());
          }
        }
        else
        {
          if(getValue() != null)
          {
            // only set something if changed
            setValue(null);
          }
        }
      }
    });
    
    layout = new HorizontalLayout(dateField, checkBox);
  }

  @Override
  protected Component initContent()
  {
    return layout;
  }
  
  public void setCheckboxCaption(String caption)
  {
    checkBox.setCaption(caption);
  }

  @Override
  protected void setInternalValue(DateTime newValue)
  {
    super.setInternalValue(newValue);
    dateField.setEnabled(newValue != null);
    checkBox.setValue(newValue != null);
  }
  

  @Override
  public Class<? extends DateTime> getType()
  {
    return DateTime.class;
  }

}
