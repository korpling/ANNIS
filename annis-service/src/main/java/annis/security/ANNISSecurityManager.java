/*
 * Copyright 2014 SFB 632.
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

import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

/**
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ANNISSecurityManager extends DefaultWebSecurityManager
{
  private ANNISUserConfigurationManager confManager;

  public ANNISUserConfigurationManager getConfManager()
  {
    return confManager;
  }

  public void setConfManager(ANNISUserConfigurationManager confManager)
  {
    this.confManager = confManager;
  }
  
  public ANNISUserRealm getANNISUserRealm()
  {
    if(getRealms() != null)
    {
      for(Realm r : getRealms())
      {
        if(r instanceof ANNISUserRealm)
        {
          return (ANNISUserRealm) r;
        }
      }
    }
    
    return null;
  }
  
  
}
