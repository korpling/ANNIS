package paulaReader_1_0;


import java.io.File;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.junit.Before;

public class TestPAULAConnector extends TestCase
{

	private static final String TOOLNAME=		"TestPAULAConnector";
	private static final String MSG_TST=		TOOLNAME + "> Testing ";
	private static final String MSG_OK= 		"OK";
	private static final String MSG_FAILED= 	"FAILED";
	
	private static final String MSG_CREATING=		MSG_TST + "creating PAULAConnector.............";
	private static final String MSG_START_READING=	MSG_TST + "start reading of paula corpus.......";
	
	/**
	 * Object under test.
	 */
	private PAULAConnector con= null;
	
	/**
	 * Corpus wich should be read.
	 */
	protected File paulaCorpus= null;
	
	/**
	 * name of the corpus wich should be tested
	 */
	protected String corpFile= "tst/data/ENV_testCorpus1";
	
	/**
	 * The mapper wich listens to call back methods.
	 */
	protected PAULAMapperInterface mapper= null;
	
	@Before
	public void setUp() throws Exception 
	{
		paulaCorpus= new File(corpFile);
		this.mapper= new TestMapper();
		
		//PAULAConnector erstellen
		con= new PAULAConnector(paulaCorpus, mapper, null);
	}
	
	/**
	 * Checks to create an object of type PAULAConnector.
	 * @throws Exception
	 */
	public void testCreatePAULAConnector() throws Exception
	{
		try
		{
			System.out.print(MSG_CREATING);
			File paulaCorp= null;
			try
			{
				con= new PAULAConnector(null, null, null);
				fail("should have faild, because no corpus and no mapper were given");
			}
			catch (Exception e)
			{}
			try
			{
				paulaCorp= new File("not.there");
				con= new PAULAConnector(paulaCorp, null, null);
				fail("should have faild, because no corpus and no mapper were given");
			}
			catch (Exception e)
			{}
			//PAULAConnector erstellen
			con= new PAULAConnector(paulaCorpus, mapper, null);
			
		}
		catch (AssertionFailedError e)
		{
			System.out.println(MSG_FAILED);
			throw new Exception(e.getMessage());
		}
		System.out.println(MSG_OK);
	}

	public void testStartReading() throws Exception
	{
		try
		{
			System.out.print(MSG_START_READING);
			this.con.startReading();
		}
		catch (AssertionFailedError e)
		{
			System.out.println(MSG_FAILED);
			throw new Exception(e.getMessage());
		}
		catch (Exception e)
		{
			System.out.println(MSG_FAILED);
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		System.out.println(MSG_OK);
	}
}
