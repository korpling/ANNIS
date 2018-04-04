/*
 * Copyright 2017 Humboldt-Universität zu Berlin.
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
package annis.rest.provider;

import com.google.common.collect.Maps;
import com.google.common.escape.Escaper;
import com.google.common.xml.XmlEscapers;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides instances of {@link Marshaller} that use the Guava {@link XmlEscapers} to escape the characters in the output stream.
 * The Guava implementation is much more correct when handling corner cases like control characters in the values.
 * 
 * This will accept every type and caches the {@link JAXBContext} it creates for each type.
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Provider
public class GuavaEscaperMarshallerProvider implements ContextResolver<Marshaller>
{
  private final Logger log = LoggerFactory.getLogger(GuavaEscaperMarshallerProvider.class);
  
  private final ConcurrentMap<Class<?>, JAXBContext> contextCache = Maps.newConcurrentMap();

  
  @Override
  public Marshaller getContext(Class<?> type)
  {
    JAXBContext context = contextCache.computeIfAbsent(type, new Function<Class<?>, JAXBContext>()
    {
      @Override
      public JAXBContext apply(Class<?> t)
      {
        try
        {
          return JAXBContext.newInstance(t);
        }
        catch (JAXBException ex)
        {
          log.error("Can't create JAXB context", ex);
        }
        return null;
      }
    });
        
    if(context != null)
    {
      try 
      {
        Marshaller m = context.createMarshaller();
        m.setProperty("com.sun.xml.bind.characterEscapeHandler", new CharacterEscapeHandler()
        {
          @Override
          public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException
          {
            String asString = new String(ch, start, length);
            Escaper e = isAttVal ? XmlEscapers.xmlAttributeEscaper() : XmlEscapers.xmlContentEscaper();
            out.write(e.escape(asString));
          }
        });
        return m;
      }
      catch (JAXBException ex) 
      {
        log.error("Can't create XML marshaller", ex);
      }
    }
    return null;
  }
  
}
