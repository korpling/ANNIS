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

package annis.utils;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * This class is an implementation of an DataSource that delegates
 * the work to a dynamically changeable inner DataSource.
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class DynamicDataSource extends AbstractDataSource
{
  private DataSource innerDataSource;
  
  @Override
  public Connection getConnection() throws SQLException
  {
    if(innerDataSource != null)
    {
      return innerDataSource.getConnection();
    }
    return null;
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException
  {
    if(innerDataSource != null)
    {
      return innerDataSource.getConnection(username, password);
    }
    return null;
  }
  
  
  
  private void clearTransactionInfo()
  {
    ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this);
    if (conHolder != null && conHolder.
      isSynchronizedWithTransaction())
    {
      TransactionSynchronizationManager.unbindResourceIfPossible(this);
    }
  }

  public DataSource getInnerDataSource()
  {
    return innerDataSource;
  }

  public void setInnerDataSource(DataSource innerDataSource)
  {
    this.innerDataSource = innerDataSource;
    clearTransactionInfo();
  }
  
  
  
}
