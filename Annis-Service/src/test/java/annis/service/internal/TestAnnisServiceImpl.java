package annis.service.internal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import test.TestHelper;

import annis.AnnisHomeTest;
import annis.externalFiles.ExternalFileMgrImpl;
import annis.service.AnnisService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"AnnisServiceRunner-context.xml"})
public class TestAnnisServiceImpl extends AnnisHomeTest {

	@Autowired private AnnisService springManagedAnnisServiceImpl;
	
	@Test
	public void springManagedInstanceHasAllDependencies() {
		// Spring exports only the AnnisService interface, because
		// the bean is wrapped using RmiServiceExporter
		AnnisServiceImpl annisServiceImpl = (AnnisServiceImpl) TestHelper.proxyTarget(springManagedAnnisServiceImpl);
		assertThat(annisServiceImpl.getDddQueryMapper(), is(not(nullValue())));
		assertThat(annisServiceImpl.getDddQueryParser(), is(not(nullValue())));
		assertThat(annisServiceImpl.getAnnisDao(), is(not(nullValue())));
		assertThat(annisServiceImpl.getExternalFileMgr(), is(not(nullValue())));
    assertThat(annisServiceImpl.getWekaDaoHelper(), is(not(nullValue())));
		
		// dependencies for ExternalFileManager
		ExternalFileMgrImpl externalFileMgrImpl = (ExternalFileMgrImpl) annisServiceImpl.getExternalFileMgr();
		assertThat(externalFileMgrImpl.getExternalFileMgrDao(), is(not(nullValue())));
		assertThat(externalFileMgrImpl.getExternalDataFolder(), is(not(nullValue())));
		System.out.println(externalFileMgrImpl.getExternalDataFolder());
	}
	
}
