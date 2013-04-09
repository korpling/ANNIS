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
package annis.utils;

import java.util.Properties;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * A datasource that is able to configure the usage of SSL for PostgreSQL.
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class SSLEnabledDataSource extends DriverManagerDataSource
{
  public boolean useSSL()
  {
    if(getConnectionProperties() == null)
    {
      setConnectionProperties(new Properties());
    }
    
    String val = getConnectionProperties().getProperty("ssl", "false");
    return Boolean.parseBoolean(val);
  }
  
  public void setUseSSL(boolean useSSL)
  {
    if(getConnectionProperties() == null)
    {
      setConnectionProperties(new Properties());
    }
    
    if(useSSL)
    {
      getConnectionProperties().put("ssl", "true");
    }
    else
    {
      // setting this to "false" won't help, it should not be included at all
      getConnectionProperties().remove("ssl");
    }
  }
}
