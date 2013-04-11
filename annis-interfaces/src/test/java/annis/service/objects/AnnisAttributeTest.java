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
package annis.service.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Benjamin Weißenfels <b.pixeldrama@gmail.com>
 */
public class AnnisAttributeTest
{

  private final Logger log = LoggerFactory.getLogger(AnnisAttributeTest.class);

  /**
   * Test of getValueSet method, of class AnnisAttribute.
   */
  @Test
  public void testGetValueSet()
  {
    List<String> values1 = new ArrayList<String>();
    List<String> values2 = new ArrayList<String>();

    values1.add("acc-ggr");
    values1.add("Idiom");
    values1.add("idiom");
    values1.add("Ne1");
    values1.add("New");
    values1.add("new");
    values1.add("New1");
    values1.add("z");

    values2.add("z");
    values2.add("Idiom");
    values2.add("acc-ggr");
    values2.add("idiom");
    values2.add("new");
    values2.add("New");
    values2.add("New1");
    values2.add("Ne1");


    AnnisAttribute instance = new AnnisAttribute();
    instance.setValueSet(values2);
    Collection<String> result = instance.getValueSet();

    int i = 0;
    for (String v : result)
    {
      log.info("compare {} {}", v, values1.get(i));
      assertEquals(v, values1.get(i));
      i++;
    }
  }
}