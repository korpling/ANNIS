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
package annis.service;

import java.rmi.Remote;
import java.rmi.RemoteException;
import annis.service.ifaces.AnnisBinary;
import annis.service.ifaces.AnnisBinaryMetaData;

public interface AnnisService extends Remote
{
  /**
   * Get an Annis Binary object identified by its id.
   * 
   * @param id
   * @param offset the part we want to start from
   * @param length how many bytes we take
   * @return AnnisBinary
   */
  public AnnisBinary getBinary(String corpusName, int offset, int length) throws
    RemoteException;

  /**
   * Get the Metadata of an Annis Binary object identified by its id. This 
   * function calls getBinary(long id, 1, 1), so this function does not work, 
   * if the specs of getBinary(long id, int offset,int length) changed.
   * 
   * @param id
   * @return AnnisBinaryMetaData
   */
  public AnnisBinaryMetaData getBinaryMeta(String corpusName) throws
    RemoteException;

  /**
   * 
   * Ping remote Service. For internal purposes.
   * 
   * @throws RemoteException
   */
  public void ping() throws RemoteException;

}
