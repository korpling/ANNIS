package annisservice.annisresolver.dao;


import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.junit.Before;

import annisservice.annisresolver.dao.AnnisVizDAO;

public class TestAnnisVizDao extends TestCase
{
//	 ============================================== private Variablen ==============================================
	private static final String TOOLNAME= 	"TestAnnisVizDao";		//Name dieses Tools
	
	/**
	 * Object under Test
	 */
	private AnnisVizDAO aVDAO= null;
	//	 *************************************** Meldungen ***************************************
	private static final String MSG_TST=			TOOLNAME + "> Testing ";
	private static final String MSG_OK= 			"OK";
	private static final String MSG_FAILED= 		"FAILED";
	private static final String MSG_OPENING=		MSG_TST + "opening database connection.................................";
	private static final String MSG_CLOSING=		MSG_TST + "closing database connection.................................";
	private static final String MSG_NEW_VIZ_TYPE=	MSG_TST + "inserting a new visualization type and checking if exists...";
	//	 *************************************** Fehlermeldungen ***************************************
//	 ============================================== Konstruktoren ==============================================
//	 ============================================== private Methoden ==============================================
//	 ============================================== öffentliche Methoden ==============================================
	
	@Before
	public void setUp() throws Exception 
	{
		this.aVDAO= new AnnisVizDAO(null);
	}
	
	/**
	 * Tests opening a database connection.
	 * @throws Exception
	 */
	public void testOpenConnection() throws Exception
	{
		try
		{
			System.out.print(MSG_OPENING);
			this.aVDAO.open();
			
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
	 * Tests closing a database connection.
	 * @throws Exception
	 */
	public void testCloseConnection() throws Exception
	{
		try
		{
			System.out.print(MSG_CLOSING);
			this.aVDAO.open();
			this.aVDAO.close();
			
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

	public void testGetVizType() throws Exception
	{
		try
		{
			/*
			System.out.print(MSG_CLOSING);
			this.aVDAO.open();
			String vizType= this.aVDAO.getVizType(999l, "exmaralda:cat");
			assertTrue("the value should be the same", vizType.equalsIgnoreCase("tree"));
			vizType= this.aVDAO.getVizType(999l, "notthere");
			assertNull("the returned value should be null",vizType);
			vizType= this.aVDAO.getVizType(999l, "exmaralda:bla");
			assertNull("the returned value should be null",vizType);
			vizType= this.aVDAO.getVizType(0l, "notthere");
			assertNull("the returned value should be null",vizType);
			this.aVDAO.close();
			*/
			
		}
		catch (AssertionFailedError e)
		{
			System.out.println(MSG_FAILED);
			throw e;
		}
		catch (Exception e)
		{
			System.out.println(MSG_FAILED);
			e.printStackTrace();
			throw e;
		}
		System.out.println(MSG_OK);
	}
	
	/**
	 * Tries inserting a new visualization type and checking if the visualization type
	 * has been inserted. After all, the value would be deleted.
	 */
	public void testNewVizType() throws Exception
	{
		try
		{
			System.out.print(MSG_NEW_VIZ_TYPE);
			this.aVDAO.open();
			String vizType= null;
			
			//check 1
			vizType= "VizType1";
			this.aVDAO.insertVizType(vizType);
			assertTrue("the asked viz type should exist: "+vizType, this.aVDAO.checkVizType(vizType));
			this.aVDAO.remVizType(vizType);
			assertFalse("the asked viz type should be deleted: "+vizType, this.aVDAO.checkVizType(vizType));
			
			//check 2
			vizType= "VizType2";
			this.aVDAO.insertVizType(vizType);
			assertTrue("the asked viz type should exist", this.aVDAO.checkVizType(vizType));
			this.aVDAO.remVizType(vizType);
			assertFalse("the asked viz type should be deleted: "+vizType, this.aVDAO.checkVizType(vizType));
			
			//check 3
			vizType= "VizType3";
			assertFalse("the asked viz type should not exist", this.aVDAO.checkVizType(vizType));
			
			//check 4 
			vizType= "VizType2";
			this.aVDAO.insertVizType(vizType);
			assertTrue("the asked viz type should exist: "+vizType, this.aVDAO.checkVizType(vizType));
			try
			{
				this.aVDAO.insertVizType(vizType);
				fail("the viz type which is tried to insert should have been there: "+ vizType);
			}
			catch (Exception e)
			{}
			this.aVDAO.remVizType(vizType);
			assertFalse("the asked viz type should be deleted: "+vizType, this.aVDAO.checkVizType(vizType));
			
			
			this.aVDAO.close();
			
		}
		catch (AssertionFailedError e)
		{
			System.out.println(MSG_FAILED);
			throw e;
		}
		catch (Exception e)
		{
			System.out.println(MSG_FAILED);
			e.printStackTrace();
			throw e;
		}
		System.out.println(MSG_OK);
	}
}
