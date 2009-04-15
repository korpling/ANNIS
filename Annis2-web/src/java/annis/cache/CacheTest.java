package annis.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class CacheTest {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		try {
			Cache cache = new FilesystemCache("Cache Test");
			String value = "दaSDasd‚åी";
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/black/Desktop/testutf8.txt"), "UTF-8"));
			String line;
			StringBuffer sBuffer = new StringBuffer();
			while( (line = in.readLine()) != null) {	
				sBuffer.append(line + "\n");
			}
			value = sBuffer.toString();
			
			System.out.println(value);
			System.out.println("Put: " + value);
			cache.put("key", value);
			value = cache.get("key");
			System.out.println("return: " + value);
		} catch (CacheInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CacheException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}