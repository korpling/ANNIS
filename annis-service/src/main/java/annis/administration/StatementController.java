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
package annis.administration;

import java.sql.PreparedStatement;

/**
 * Provides a interface to cancel a {@link PreparedStatement} via a gui.
 */
public interface StatementController
{

  /**
   * Registers a sql statement.
   *
   * @param statement The statement which maybe get cancelled.
   */
  public void registerStatement(PreparedStatement statement);

  /**
   * Interrupts a sql statement via the JDBC-Driver.
   *
   * <p>It relies on the actual implementation of the JDBC-Driver, if this
   * method has an effect.</p>
   *
   * <p>If the {@link PreparedStatement#cancel()} It also set the internal
   * isCancelled-flat to true, so {@link #isCancelled()} will always return
   * true. This behaviour is inspired by the method interrupt method of the
   * {@link Thread#interrupt()} method. The implementation of the
   * {@link AdministrationDao} interface should poll against this flag and do
   * not execute further sql-statements.</p>
   *
   */
  public void cancelStatements();

  /**
   * Returns true when {@link #cancelStatements()} has been executed at most
   * once.
   *
   * @return the value True signals, that no sql statements should be executed
   * anymore.
   */
  public boolean isCancelled();
  
}
