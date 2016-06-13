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
package annis.adapter;

import javax.ws.rs.ext.Provider;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.DateTime;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
@Provider
public class DateTimeAdapter extends XmlAdapter<String, DateTime>
{

  @Override
  public DateTime unmarshal(String v) throws Exception
  {
    return DateTime.parse(v);
  }

  @Override
  public String marshal(DateTime v) throws Exception
  {
    return v.toString();
  }



}
