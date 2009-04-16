package annis.security;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;

import annis.frontend.servlets.CorpusList;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisCorpus;
import annis.service.ifaces.AnnisCorpusSet;

public class TestSecurityManager implements AnnisSecurityManager {
		
	/* (non-Javadoc)
	 * @see annis.security.AnnisSecurityManager#login(java.lang.String, java.lang.String)
	 */
	public AnnisUser login(String userName, String password) throws NamingException, AuthenticationException {
		if(!"test".equals(userName) || !"test".equals(password))
			throw new AuthenticationException();
		
		AnnisUser user = new AnnisUser("test");
		user.setPassword(password);
		List<Long> corpusIdList = user.getCorpusIdList();
		
		String favorites = "";
		
		try {
      AnnisService service = AnnisServiceFactory.getClient("rmi://localhost:4711/AnnisService");
			AnnisCorpusSet corpusSet = service.getCorpusSet();
			int i=0;
			for(AnnisCorpus corpus : corpusSet) {
				corpusIdList.add(corpus.getId());
				favorites += ((i++>0) ? "," : "") + corpus.getId();
			}
		} catch (Exception e) {
			for(long i=1; i<=100 ;i++) {
				corpusIdList.add(i);
				favorites += ((i>0) ? "," : "") + i;
			}
		}
		user.setCorpusIdList(corpusIdList);
		user.put(CorpusList.KEY_CORPUS_FAVORITES, favorites);
		return user;
	}

	public void setProperties(Properties properties) {
		//ignored in this implementation
	}

	public void storeUserProperties(AnnisUser user) throws NamingException, AuthenticationException, IOException {
		//ignored in this implementation
	}
}
