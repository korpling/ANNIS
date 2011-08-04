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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import annis.exceptions.AnnisServiceFactoryException;


public class AnnisServiceFactory {
	private static AnnisService service;
	
	public static AnnisService getClient(String url) throws AnnisServiceFactoryException {
		// Assign security manager for RMI Connection
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new RMISecurityManager());
		
		try {
			service.ping();
		} catch (Exception e) {
			try {
				service = (AnnisService) Naming.lookup(url);
			} catch (MalformedURLException e1) {
				throw new AnnisServiceFactoryException("Wrong Annis Config.", e1);
			} catch (RemoteException e1) {
				throw new AnnisServiceFactoryException("Annis Service not available.", e1);
			} catch (NotBoundException e1) {
				throw new AnnisServiceFactoryException("Annis Service not bound.", e1);
			}
		}
		
		//throws MalformedURLException, RemoteException, NotBoundException, java.rmi.ConnectException
		return service;
	}
}
