package relANNIS_2_0.relANNISDAO;


import java.io.File;
import java.util.Vector;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.junit.Before;

public class TestBLOBTupleWriter extends TestCase 
{
	private static final String MSG_STD=		"TestBLOBTupleWriter> ";
	private static final String MSG_TST=		MSG_STD +"Testing ";
	private static final String MSG_OK= 		"OK";
	private static final String MSG_FAILED= 	"FAILED";
	private static final String TST_ADD_TUPLES=		MSG_TST + "adding different tuples............................";
	
	private BLOBTupleWriter writer= null;
	
	@Before
	public void setUp() throws Exception 
	{
		String absName= "abstractRelName";	
		String relName=	"images"; 
		File outFile= new File("tst/data/TMP/blobTupleWriter.sql");
		File blobDir= new File("tst/data/TMP/blobs");
		boolean override=	true;
		long relId=			1l;
		boolean isTemp=		false;
		this.writer= new BLOBTupleWriter(absName, relName, outFile, blobDir, override, relId, isTemp);
		Vector<String> attNames= new Vector<String>();
		attNames.add("ID");
		attNames.add("pic");
		attNames.add("groesse");
		attNames.add("ursprung");
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
			System.out.println();
			String absName= "abstractRelName";	
			String relName=	"images"; 
			File outFile= new File("tst/data/TMP/blobTupleWriter.sql");
			boolean override=	true;
			long relId=			1l;
			boolean isTemp=		false;
			try
			{
				this.writer= new BLOBTupleWriter(absName, null, outFile, override, relId, isTemp);
				fail("this construcor call should have been wrong: null value for relName");
			}
			catch (Exception e)
			{}
			try
			{
				this.writer= new BLOBTupleWriter(absName, relName, null, override, relId, isTemp);
				fail("this construcor call should have been wrong: null value for outFile");
			}
			catch (Exception e)
			{}
			try
			{
				this.writer= new BLOBTupleWriter(absName, relName, outFile, override, (Long)null, isTemp);
				fail("this construcor call should have been wrong: null value for relId");
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
			System.out.println(TST_ADD_TUPLES);
			Vector<String> tuple= new Vector<String>();
			tuple.add("200");
			tuple.add("aus nem Ordner");
			tuple.add("audio/wav");
			File blobFile= new File("./tst/data/blobs/blob1.wav");
			writer.addTuple2(tuple, blobFile, 1l);
			this.writer.flush();
			writer.addTuple2(tuple, blobFile, 1l);
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
