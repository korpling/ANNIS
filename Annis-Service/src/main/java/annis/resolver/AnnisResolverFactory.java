package annis.resolver;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;



public class AnnisResolverFactory {
	private static AnnisResolverService service;
	
	public static AnnisResolverService getClient(String url) throws AnnisResolverFactoryException {
		// Assign security manager for RMI Connection
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new RMISecurityManager());
		
		try {
			service.ping();
		} catch (Exception e) {
			try {
				service = (AnnisResolverService) Naming.lookup(url);
			} catch (MalformedURLException e1) {
				throw new AnnisResolverFactoryException("Wrong Annis Config.");
			} catch (RemoteException e1) {
				throw new AnnisResolverFactoryException("Annis Service not available.");
			} catch (NotBoundException e1) {
				throw new AnnisResolverFactoryException("Annis Service not bound.");
			}
		}
		
		//throws MalformedURLException, RemoteException, NotBoundException, java.rmi.ConnectException
		return service;
	}
}
