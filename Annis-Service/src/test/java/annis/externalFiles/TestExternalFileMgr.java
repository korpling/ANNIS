package annis.externalFiles;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import javax.sql.DataSource;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Ignore;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

// FIXME: test ExternalFileMgr
@SuppressWarnings("all")
public class TestExternalFileMgr
{
	private static final String MSG_STD=		"TestEdgeIndex> ";
	private static final String MSG_TST=		MSG_STD +"Testing ";
	private static final String MSG_OK= 		"OK";
	private static final String MSG_FAILED= 	"FAILED";
	private static final String MSG_PUTFILE= 			MSG_TST + "putting file into the manager......................";
	private static final String MSG_GETTING_FICT_ID=	MSG_TST + "trying to get not existing id�s ...................";
	private static final String MSG_BRANCH=				MSG_TST + "branch management..................................";
	private static final String MSG_REMOTE=				MSG_TST + "testing remote access..............................";
	
	private static ExternalFileMgrImpl eFMgr= null;
	
	@Before
	public void setup()
	{
		DataSource dataSource = new DriverManagerDataSource("org.postgresql.Driver", "jdbc:postgresql://localhost:5432/annis", "annis_user", "annis_password");

		ExternalFileMgrDAO externalFileMgrDao = new ExternalFileMgrDAO();
		externalFileMgrDao.setDataSource(dataSource);
		
		eFMgr= new ExternalFileMgrImpl();
		eFMgr.setExternalDataFolder("extData");
		eFMgr.setExternalFileMgrDao(externalFileMgrDao);
	}
	
	/**
	 * tests the management of branches:
	 * - creating a new branch
	 * - creating an existing branch
	 * - deleting an existing branch
	 * - deleting a non existing branch 
	 * @throws Exception
	 */
	@Ignore
	public void testBranches()
	{
		System.out.print(MSG_BRANCH);
		String branch= "TEST_branch1";
		assertFalse("branch '"+branch+"' shouldn�t exist",eFMgr.hasBranch(branch));
		eFMgr.createBranch(branch);
		assertTrue("branch '"+branch+"' should exist",eFMgr.hasBranch(branch));
		try
		{
			eFMgr.createBranch(branch);
			fail("the same branch cannot be created two times: "+branch);
		}
		catch (ExternalFileMgrException e)
		{}
		eFMgr.deleteBranch(branch);
		assertFalse("branch '"+branch+"' shouldn�t exist",eFMgr.hasBranch(branch));
		try
		{
			eFMgr.deleteBranch(branch);
			fail("a not existing branch can�t be deleted: "+branch);
		}
		catch (ExternalFileMgrException e)
		{}
	}
	
	
	/**
	 * Trying to get fictive id�s which shouldn�t be there. 
	 * @throws Exception
	 */
	public void testGettingFictID() throws Exception
	{
		try
		{
			System.out.print(MSG_GETTING_FICT_ID);
			
			long fictId= 0000L;
			assertFalse("The asked id should be there ("+fictId+")", eFMgr.hasId(fictId));
			
		}
		catch (AssertionFailedError e)
		{
			System.out.println(MSG_FAILED);
			throw e;
		}
		catch (Exception e)
		{
			System.out.println(MSG_FAILED);
			throw e;
		}
		System.out.println(MSG_OK);
	}

	/**
	 * tests putting and removing one file
	 * @throws Exception
	 */
	@Ignore
	public void testPutFile()
	{
		System.out.print(MSG_PUTFILE);
		String branch= "branch1";
		eFMgr.createBranch(branch);
		ExtFileObjectCom eFile= new ExtFileObjectImpl();
		eFile.setBranch(branch);
		eFile.setFile(new File("extData/extFile01.wav"));
		eFile.setMime("audio/wav");
		long id= eFMgr.putFile(eFile);
		System.out.println("returned id: "+id);
		assertTrue("The asked id should be there ("+id+")", eFMgr.hasId(id));
		ExtFileObjectCom extFile= eFMgr.getExtFileObj(id);
		boolean same= true;

		extFile.getFile("extData/extFile02.wav");
		/*
			if (extFile.getFile().equals(eFile)) same= true;
			else same= false;
		 */
		assertTrue("the returned file isn�t the same like the inserted", same);
		//assertEquals("the returned file isn�t the same like the inserted", extFile.getBytes(), eFile.getBytes());

		eFMgr.deleteFile(id);
		assertFalse("The asked id shouldn�t be there ("+id+")", eFMgr.hasId(id));
		eFMgr.deleteBranch(branch);

	}
	
	// was macht das?
	@Ignore
	public void testRemote() throws Exception
	{
		try
		{
			System.out.print(MSG_REMOTE);
			//AnnisService server
		}
		catch (AssertionFailedError e)
		{
			System.out.println(MSG_FAILED);
			throw e;
		}
		catch (Exception e)
		{
			System.out.println(MSG_FAILED);
			throw e;
		}
		System.out.println(MSG_OK);
	}
	
}
