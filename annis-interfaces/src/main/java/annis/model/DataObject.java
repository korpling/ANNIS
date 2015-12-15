/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.model;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base implementation of a class that only holds data values.
 * 
 * <p>
 * Overrides equals() so that all fields in this class and its
 * superclasses are checked for equality.  hashCode() is overridden
 * accordingly.
 * 
 * <p>
 * The following fields are skipped:
 * <ul>
 * <li>fields annotated with @Transient</li>
 * <li>Logger instances</i>
 * <li>serialVersionUID</i>
 * </ul>
 */
public class DataObject implements Serializable 
{

	// this class is sent to the front end
	

	@Retention(RetentionPolicy.RUNTIME)
	@Target(value=ElementType.FIELD)
	public static @interface Transient { }
	
	private static final Logger log = LoggerFactory.getLogger(DataObject.class);
	
	// encapsulates the actions for each field in equals() and hashCode()
	private interface FieldCallBack {
		
		void doForField(Field field) throws IllegalAccessException;
		
	}
	
	// walks the class hierarchy down to DataObject and applies 
	// fieldCallBack.doForField() for each field in each class
	private void forEachFieldDo(FieldCallBack fieldCallBack) {
		Class<?> clazz = this.getClass();
		
		while (true) {
			
			Field[] fields = clazz.getDeclaredFields();
			try {
				for (Field field : fields) {
					// FIXME: test skipped cases
					// skip Logger object
					if (field.getType() == Logger.class)
						continue;
					// skip fields annotated with @Transient
					if (field.getAnnotation(Transient.class) != null)
						continue;
					// skip serialVersionUID
					if ("serialVersionUID".equals(field.getName()))
						continue;
					field.setAccessible(true);
					fieldCallBack.doForField(field);
				} 
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			
			if (clazz.getName().equals(DataObject.class.getName()))
				break;
			
			clazz = clazz.getSuperclass();
		}
	}

	@Override
	public boolean equals(final Object obj) {
		// enforce equals contract (turn on debugging for this class to see the bug)
		if (obj == null)
			return false;
		
		if (this.getClass() != obj.getClass())
			return false;
		
		final EqualsBuilder equalsBuilder = new EqualsBuilder();
		
		final Object _this = this;
		forEachFieldDo(new FieldCallBackEqualsImpl(_this, obj, equalsBuilder));
		
		return equalsBuilder.isEquals();
	
	}
  
	@Override
	public int hashCode() {
		// sort fields by name for predictable results
		final SortedMap<String, Object> fieldValues = new TreeMap<String, Object>();
		
		final Object _this = this;
		forEachFieldDo(new FieldCallBackHashImpl(fieldValues, _this));
		
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		
		for (Object fieldValue : fieldValues.values())
			hashCodeBuilder.append(fieldValue);
		
		return hashCodeBuilder.toHashCode();
	}

  private static class FieldCallBackEqualsImpl implements FieldCallBack
  {

    private final Object _this;

    private final Object obj;

    private final EqualsBuilder equalsBuilder;

    public FieldCallBackEqualsImpl(Object _this, Object obj,
      EqualsBuilder equalsBuilder)
    {
      this._this = _this;
      this.obj = obj;
      this.equalsBuilder = equalsBuilder;
    }

    @Override
    public void doForField(Field field) throws IllegalAccessException {
      Object thisValue = field.get(_this);
      Object otherValue = field.get(obj);
      if (log.isDebugEnabled()) {
        String fieldName = field.getDeclaringClass().getSimpleName() + "." + field.getName();
        try {
          boolean equal = thisValue != null && thisValue.equals(otherValue) || thisValue == null && otherValue == null;
          log.debug(fieldName + ": " + thisValue + " " + (equal ? "=" : "!=") + " " + otherValue);
        } catch (RuntimeException e) {
          log.error("Exception while comparing " + fieldName + "(" + thisValue + ", " + otherValue + ")");
          throw e;
        }
      }
      equalsBuilder.append(thisValue, otherValue);
    }
  }

  private static class FieldCallBackHashImpl implements FieldCallBack
  {

    private final SortedMap<String, Object> fieldValues;

    private final Object _this;

    public FieldCallBackHashImpl(SortedMap<String, Object> fieldValues, Object _this)
    {
      this.fieldValues = fieldValues;
      this._this = _this;
    }

    @Override
    public void doForField(Field field) throws IllegalAccessException 
    {
      fieldValues.put(field.getName(), field.get(_this));
    }
  }
	
}