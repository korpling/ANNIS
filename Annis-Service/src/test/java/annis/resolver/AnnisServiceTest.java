package annis.resolver;

import java.rmi.RMISecurityManager;

import org.junit.Ignore;

@Ignore
public class AnnisServiceTest {
	
	/**
	 * 
	 * ATTENTION: This method must be started with jvm parameter
	 * 
	 * 		-Djava.security.policy=service.policy
	 * 
	 * otherwise it will fail to connect to service.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		System.out.println("****************************************** Testing ANNIS Resolver ******************************************");
		try
		{
			System.out.println("POLICY FILE: " + System.getProperty("java.security.policy"));
			// Assign security manager
			if (System.getSecurityManager() == null)
				System.setSecurityManager(new RMISecurityManager());
			
			AnnisResolverService service = AnnisResolverFactory.getClient("rmi://localhost:4712/AnnisResolverService");
			
			long startMillis = System.currentTimeMillis();
	
			service.ping();
			//TODO: put your tests here
			System.out.println("The Viztype for 'exmaralda:cat' is: " + service.getVizualizationType(999l, "exmaralda:cat"));
			System.out.println("The Viztype for 'trallalla' is: " + service.getVizualizationType(999l, "trallalla"));
			
			
			long timeDiff = System.currentTimeMillis() - startMillis;
			System.out.println("Elapsed Time: " + timeDiff + "mils. ");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("****************************************** End Testing ANNIS Resolver ******************************************");
	}
}