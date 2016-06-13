/*
 * Copyright 2012 SFB 632.
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
package annis.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import annis.service.objects.MatchGroup;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Provider
public class PlainTextMatchGroupProvider implements
  MessageBodyWriter<MatchGroup>,
  MessageBodyReader<MatchGroup>
{

  @Override
  public boolean isWriteable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return (MediaType.TEXT_PLAIN_TYPE.isCompatible(mediaType)
      && MatchGroup.class.isAssignableFrom(type));
  }

  @Override
  public long getSize(MatchGroup t,
    Class<?> type, Type genericType, Annotation[] annotations,
    MediaType mediaType)
  {
    return -1;
  }

  @Override
  public void writeTo(MatchGroup match,
    Class<?> type, Type genericType, Annotation[] annotations,
    MediaType mediaType,
    MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
    throws IOException, WebApplicationException
  {

    
    try(OutputStreamWriter writer = new OutputStreamWriter(entityStream,
      Charsets.UTF_8))
    {
      writer.append(match.toString());
    }
  }

  @Override
  public boolean isReadable(Class<?> type, Type genericType,
    Annotation[] annotations, MediaType mediaType)
  {
    return (MediaType.TEXT_PLAIN_TYPE.isCompatible(mediaType)
      && MatchGroup.class.isAssignableFrom(type));
  }

  @Override
  public MatchGroup readFrom(Class<MatchGroup> type, Type genericType,
    Annotation[] annotations, MediaType mediaType,
    MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws
    IOException,
    WebApplicationException
  {

    String val = CharStreams.toString(new InputStreamReader(entityStream,
      Charsets.UTF_8));
    MatchGroup result = MatchGroup.parseString(val);
    return result;

  }

}
