/*
 * Copyright 2015 SFB 632.
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
package annis.security;

import com.google.common.base.Preconditions;
import org.apache.shiro.ShiroException;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.Ini.Section;
import org.apache.shiro.util.Initializable;
import org.apache.shiro.web.env.DefaultWebEnvironment;
import org.apache.shiro.web.env.IniWebEnvironment;

/**
 * An extension of the {@link DefaultWebEnvironment} class allows several
 * {@link Ini} files. 
 * 
 * <p>
 * This class assumes that the config locations ({@link #setConfigLocations(java.lang.String[]) ) }
 * are set and has no way of fallbacking to any default locations.
 * </p>
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class MultipleIniWebEnvironment extends IniWebEnvironment 
  implements Initializable
{

  @Override
  public void init() throws ShiroException
  {
    Ini ini = new Ini();
    
    Preconditions.checkNotNull(getConfigLocations());
    
    for(String p : getConfigLocations())
    {
      Ini subIni = new Ini(ini);
      subIni.loadFromPath(p);
      
      // add all values from the sub file to the main configuration
      for(Section section : subIni.getSections())
      {
        Section existing = ini.getSection(section.getName());
        if(existing == null)
        {
          existing = ini.addSection(section.getName());
        }
        existing.putAll(section);
      }
    }

    setIni(ini);
    configure();
  }

  
}
