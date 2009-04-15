package annis.security;

import java.io.IOException;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;

public interface AnnisSecurityManager {

	public AnnisUser login(String userName, String password) throws NamingException, AuthenticationException;
	public void setProperties(Properties properties);
	public  void storeUserProperties(AnnisUser user) throws NamingException, AuthenticationException, IOException;
}