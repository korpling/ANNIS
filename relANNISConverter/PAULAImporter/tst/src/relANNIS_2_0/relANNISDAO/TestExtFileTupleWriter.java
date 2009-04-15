package relANNIS_2_0.relANNISDAO;


import java.io.File;
import java.util.Vector;

import org.junit.Before;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class TestExtFileTupleWriter extends TestCase
{

	private static final String MSG_STD=		"TestExtFileTupleWriter> ";
	private static final String MSG_TST=		MSG_STD +"Testing ";
	private static final String MSG_OK= 		"OK";
	private static final String MSG_FAILED= 	"FAILED";
	private static final String TST_CREATE_TUPLES=	MSG_TST + "creating a tuple writer object.....................";
	private static final String TST_ADD_TUPLES=		MSG_TST + "adding different tuples............................";
	private static final String TST_ADD_TUPLES2=	MSG_TST + "adding different tuples with subDir................";
	
	private ExtFileTupleWriter writer= null;
	
	File outFile= new File("tst/data/TMP/extTupleWriter.tab");
	File extDir= new File("tst/data/TMP/extDir");
	
	@Before
	public void setUp() throws Exception 
	{
		String relName=	"images"; 
		boolean override=	true;
		boolean isTemp=		false;
		this.writer= new ExtFileTupleWriter(relName, outFile, extDir, override, isTemp);
		Vector<String> attNames= new Vector<String>();
		attNames.add("ID");
		attNames.add("pic");
		attNames.add("groesse");
		attNames.add("mime");
		this.writer.setAttNames(attNames);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void testCreateTWriter() throws Exception
	{
		try
		{
			System.out.print(TST_CREATE_TUPLES);
			String relName=	"images"; 
			File extDir= new File("tst/data/TMP/extDir");
			File outFile= new File("tst/data/TMP/extTupleWriter.tab");
			boolean override=	true;
			boolean isTemp=		false;
			try
			{
				this.writer= new ExtFileTupleWriter(null, outFile, extDir, override, isTemp);
				fail("this construcor call should have been wrong: null value for relName");
			}
			catch (Exception e)
			{}
			try
			{
				this.writer= new ExtFileTupleWriter(relName, null, extDir, override, isTemp);
				fail("this construcor call should have been wrong: null value for outFile");
			}
			catch (Exception e)
			{}
			try
			{
				this.writer= new ExtFileTupleWriter(relName, outFile, null, override, isTemp);
				fail("this construcor call should have been wrong: null value for external directory");
			}
			catch (Exception e)
			{}
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
	 * Tested das schreiben von Tupeln in den TupleWriter
	 * @throws Exception
	 */
	public void testAddTuples() throws Exception
	{
		try
		{
			System.out.print(TST_ADD_TUPLES);
			Vector<String> tuple= new Vector<String>();
			tuple.add("200");
			tuple.add("aus nem Ordner");
			tuple.add("audio/wav");
			File extFile= new File("./tst/data/TMP/audio/audio1.wav");
			try{
				this.writer.addTuple(null, extFile, 1);
				fail("there should be thrown an exception, because a null value for tuple was given");
			}
			catch (Exception e)
			{}
			try{
				this.writer.addTuple(tuple, null, 1);
				fail("there should be thrown an exception, because a null value for external file was given");
			}
			catch (Exception e)
			{}
			
			writer.addTuple(tuple, extFile, 1l, true);
			this.writer.flush();
			writer.addTuple(tuple, extFile, 1l, true);
			this.writer.flush();
			this.writer.close();
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
	 * Tested das schreiben von Tupeln in den TupleWriter mit SubDirectory
	 * @throws Exception
	 */
	public void testAddTuples2() throws Exception
	{
		try
		{
			System.out.print(TST_ADD_TUPLES2);
			Vector<String> tuple= new Vector<String>();
			tuple.add("200");
			tuple.add("aus nem Ordner");
			tuple.add("audio/wav");
			File extFile= new File("./tst/data/TMP/audio/audio1.wav");
			String subDir1= "sub1/";
			String subDir2= subDir1+"sub2/";
			
			try{
				this.writer.addTuple(null, extFile, 1, subDir1);
				fail("there should be thrown an exception, because a null value for tuple was given");
			}
			catch (Exception e)
			{}
			try{
				this.writer.addTuple(tuple, null, 1, subDir1);
				fail("there should be thrown an exception, because a null value for external file was given");
			}
			catch (Exception e)
			{}
			File extPath= null;
			extPath= writer.addTuple(tuple, extFile, 1l, null, true);
			assertNotNull("the return value of addTuple shouldn´t be null", extPath);
			assertEquals(extPath.getCanonicalPath(), new File(extDir.getCanonicalPath() + "/"+extFile.getName()).getCanonicalPath());
			try{
				writer.addTuple(tuple, extFile, 1l, null);
				fail("there should be thrown an exception, the file to where should be copied already exists");
			}
			catch (Exception e)
			{}
			extPath= writer.addTuple(tuple, extFile, 1l, subDir1, true);
			assertNotNull("the return value of addTuple shouldn´t be null", extPath);
			assertEquals(extPath.getCanonicalPath(), new File(extDir.getCanonicalPath() +"/"+subDir1 +"/"+ extFile.getName()).getCanonicalPath());
			this.writer.flush();
			extPath= writer.addTuple(tuple, extFile, 1l, subDir2, true);
			assertNotNull("the return value of addTuple shouldn´t be null", extPath);
			assertEquals(extPath.getCanonicalPath(), new File(extDir.getCanonicalPath() +"/"+subDir2 +"/"+ extFile.getName()).getCanonicalPath());
			this.writer.flush();
			this.writer.close();
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
