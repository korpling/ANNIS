package de.deutschdiachrondigital.dddquery.helper;

import java.rmi.Naming;
import java.rmi.Remote;

import org.apache.log4j.Logger;

import annisservice.AnnisService;

public class RmiAnnis {

	private static Logger log = Logger.getLogger(RmiAnnis.class);
	
	private static final String SERVICE_NAME = "AnnisService";
	private static final int PORT = 4711;
	private static final String HOST = "localhost";

	public static void main(String args[]) throws Exception {
//		setupSecurity();
//		startService();

		new BeanFactory().runService();
		
		AnnisService service = lookup();

		System.out.println(service.getCorpusSet());

//		System.out.println(service.getCount(Arrays.asList(1L), "\"man\""));
		
		System.exit(1);
	}

//	private static void setupSecurity() {
//		if (System.getSecurityManager() == null)
//			System.setSecurityManager ( new RMISecurityManager() );
//	}
//
//	private static void startService() throws RemoteException, AlreadyBoundException,
//			AccessException {
//		AnnisService svr = new AnnisServiceImpl();
//
//		registry = LocateRegistry.createRegistry(PORT);
//		registry.bind (SERVICE_NAME, svr);
//	}

	private static AnnisService lookup() throws Exception {
		String serviceUrl = "rmi://" + HOST + ":" + PORT + "/" + SERVICE_NAME;
//		System.out.println(serviceUrl);
		
		try {
			Remote lookup = Naming.lookup(serviceUrl);
			AnnisService service = (AnnisService) lookup;
			service.ping();
			return service;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
			throw e;
		}
	}

}
