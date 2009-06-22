package annis.service;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import test.TestHelper;
import annis.AnnisHomeTest;
import annis.dao.AnnisDao;
import annis.service.internal.AnnisServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:annis/service/internal/AnnisServiceRunner-context.xml"})
public class TestAnnisServiceExceptionHandler extends AnnisHomeTest {
	
	// AnnisService instance provided by Spring should have AnnisServiceExceptionHandler applied
	@Autowired private AnnisService annisService;
	
	// convert any exception to AnnisServiceException
	@Test
	public void convertException() throws RemoteException {
		// force an exception in a dependent class
		AnnisDao annisDao = mock(AnnisDao.class);
		when(annisDao.listCorpora()).thenThrow(new NotImplementedException());
		// can't use AnnisServiceImpl because of Spring AOP proxying
		// see http://static.springframework.org/spring/docs/2.5.x/reference/aop.html#aop-understanding-aop-proxies
		((AnnisServiceImpl) TestHelper.proxyTarget(annisService)).setAnnisDao(annisDao);
		
		try {
			// trigger the exception
			annisService.getCorpusSet();
			fail("no exception thrown");
		} catch (NotImplementedException e) {
			fail("exception not converted");
		} catch (AnnisServiceException e) {
			// ok
		}
	}
	
}
