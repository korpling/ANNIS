

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;

import annisservice.AnnisService;
import annisservice.exceptions.AnnisCorpusAccessException;
import annisservice.exceptions.AnnisQLSemanticsException;
import annisservice.exceptions.AnnisQLSyntaxException;
import annisservice.ifaces.AnnisResultSet;

public class TestUnicode {

	public static void main(String[] args) throws UnsupportedEncodingException, MalformedURLException, RemoteException, NotBoundException, AnnisQLSemanticsException, AnnisQLSyntaxException, AnnisCorpusAccessException {
		System.out.println("Default charset: " + Charset.defaultCharset());
		String hindi = "में";
		System.out.println(hindi);
		StringWriter writer = new StringWriter();
		writer.append(hindi);
		System.out.println(new String(writer.toString().getBytes("UTF-8"), "UTF-8"));
		
		AnnisService service = (AnnisService) Naming.lookup("rmi://141.20.195.231:4711/AnnisService");
		AnnisResultSet resultSet = service.getResultSet(Arrays.asList(7L), "\"इस\" & \"मुठभेड़\" & \"में\" & \"पुलिस\" & #1 . #2 & #2 . #3 & #3 . #4", 25, 0, 5, 5);
		if (resultSet.size() > 0) {
			String paula = resultSet.iterator().next().getPaula();
			System.out.println(paula);
			try {
				File tmpFile = File.createTempFile("paula", ".xml");
//				tmpFile.deleteOnExit();
				FileWriter fileWriter = new FileWriter(tmpFile);
				fileWriter.write(paula);
				fileWriter.close();
				System.out.println("wrote xml to " + tmpFile.getAbsolutePath());
			} catch (IOException e) {
				// don't bother
			}
		}
	}
	
}
