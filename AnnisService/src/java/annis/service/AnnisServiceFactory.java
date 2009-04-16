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
				throw new AnnisServiceFactoryException("Wrong Annis Config.");
			} catch (RemoteException e1) {
				throw new AnnisServiceFactoryException("Annis Service not available.");
			} catch (NotBoundException e1) {
				throw new AnnisServiceFactoryException("Annis Service not bound.");
			}
		}
		
		//throws MalformedURLException, RemoteException, NotBoundException, java.rmi.ConnectException
		return service;
	}
}
