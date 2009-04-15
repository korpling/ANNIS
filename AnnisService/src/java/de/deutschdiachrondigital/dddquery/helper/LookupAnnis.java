package de.deutschdiachrondigital.dddquery.helper;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

public class LookupAnnis {

	public static void main(String[] args) throws MalformedURLException, RemoteException, NotBoundException {
		// Assign security manager
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new RMISecurityManager());

		// Call registry for PowerService
	}

}
