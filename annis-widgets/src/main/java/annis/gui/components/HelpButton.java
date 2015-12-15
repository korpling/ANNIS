/*
 * Copyright 2013 SFB 632.
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
package annis.gui.components;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ChameleonTheme;
import java.util.Collection;
import org.apache.commons.lang3.Validate;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class HelpButton<T> extends CustomComponent implements Field<T>,
  Button.ClickListener
{
  private Field<T> field;
  public HelpButton(Field<T> field)
  {
    Validate.notNull(field);
    this.field = field;
    
    CssLayout layout = new CssLayout();
    setCompositionRoot(layout);
    
    Button btHelp = new Button("");
    btHelp.setIcon(FontAwesome.QUESTION);
    btHelp.addStyleName(ChameleonTheme.BUTTON_BORDERLESS);
    btHelp.addStyleName("helpbutton");
    btHelp.addClickListener((Button.ClickListener) this);
    
    setCaption(field.getCaption());
    field.setCaption(null);
    
    layout.addComponent(field);
    layout.addComponent(btHelp);
    
  }

  @Override
  public void buttonClick(ClickEvent event)
  {
    String caption = "Help";
    if(getCaption() != null 
      && !getCaption().isEmpty())
    {
      caption = "Help for \"" + getCaption();
    }
    caption = caption + "<br/><br/>(Click here to close)";
    Notification notify = new Notification(caption, Notification.Type.HUMANIZED_MESSAGE);
    notify.setHtmlContentAllowed(true);
    notify.setDescription(field.getDescription());
    notify.setDelayMsec(-1);
    notify.show(UI.getCurrent().getPage());
  }
  
  @Override
  public boolean isRequired()
  {
    return field.isRequired();
  }

  @Override
  public void setRequired(boolean required)
  {
    field.setRequired(required);
  }

  @Override
  public void setRequiredError(String requiredMessage)
  {
    field.setRequiredError(requiredMessage);
  }

  @Override
  public String getRequiredError()
  {
    return field.getRequiredError();
  }

  @Override
  public boolean isInvalidCommitted()
  {
    return field.isInvalidCommitted();
  }

  @Override
  public void setInvalidCommitted(boolean isCommitted)
  {
    field.setInvalidCommitted(isCommitted);
  }

  @Override
  public void commit() throws SourceException, InvalidValueException
  {
    field.commit();
  }

  @Override
  public void discard() throws SourceException
  {
    field.discard();
  }

  @Override
  public void setBuffered(boolean buffered)
  {
    field.setBuffered(buffered);
  }

  @Override
  public boolean isBuffered()
  {
    return field.isBuffered();
  }

  @Override
  public boolean isModified()
  {
    return field.isModified();
  }

  @Override
  public void addValidator(Validator validator)
  {
    field.addValidator(validator);
  }

  @Override
  public void removeValidator(Validator validator)
  {
    field.removeValidator(validator);
  }

  @Override
  public void removeAllValidators()
  {
    field.removeAllValidators();
  }

  @Override
  public Collection<Validator> getValidators()
  {
    return field.getValidators();
  }

  @Override
  public boolean isValid()
  {
    return field.isValid();
  }

  @Override
  public void validate() throws InvalidValueException
  {
    field.validate();
  }

  @Override
  public boolean isInvalidAllowed()
  {
    return field.isInvalidAllowed();
  }

  @Override
  public void setInvalidAllowed(boolean invalidValueAllowed) throws UnsupportedOperationException
  {
    field.setInvalidAllowed(invalidValueAllowed);
  }

  @Override
  public T getValue()
  {
    return field.getValue();
  }

  @Override
  public void setValue(T newValue) throws ReadOnlyException
  {
    field.setValue(newValue);
  }

  @Override
  public Class<? extends T> getType()
  {
    return field.getType();
  }

  @Override
  public void addValueChangeListener(ValueChangeListener listener)
  {
    field.addValueChangeListener(listener);
  }

  @Override
  @Deprecated
  public void addListener(ValueChangeListener listener)
  {
    field.addListener(listener);
  }

  @Override
  public void removeValueChangeListener(ValueChangeListener listener)
  {
    field.removeValueChangeListener(listener);
  }

  @Override
  @Deprecated
  public void removeListener(ValueChangeListener listener)
  {
    field.removeListener(listener);
  }

  @Override
  public void valueChange(Property.ValueChangeEvent event)
  {
    field.valueChange(event);
  }

  @Override
  public void setPropertyDataSource(Property newDataSource)
  {
    field.setPropertyDataSource(newDataSource);
  }

  @Override
  public Property getPropertyDataSource()
  {
    return field.getPropertyDataSource();
  }

  @Override
  public int getTabIndex()
  {
    return field.getTabIndex();
  }

  @Override
  public void setTabIndex(int tabIndex)
  {
    field.setTabIndex(tabIndex);
  }

  @Override
  public void focus()
  {
    field.focus();
  }

  @Override
  public void clear()
  {
    field.clear();
  }

  @Override
  public boolean isEmpty()
  {
    return field.isEmpty();
  }
  
  

}
