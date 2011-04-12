package annis.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class Utils {

	public static String min(List<Long> runtimeList) {
		long min = Long.MAX_VALUE;
		for (long value : runtimeList)
			min = Math.min(min, value);
		return String.valueOf(min);
	}

	public static String max(List<Long> runtimeList) {
		long max = Long.MIN_VALUE;
		for (long value : runtimeList)
			max = Math.max(max, value);
		return String.valueOf(max);
	}
	
	public static String avg(List<Long> runtimeList) {
		if (runtimeList.isEmpty())
			return "";
		
		long sum = 0;
		for (long value : runtimeList)
			sum += value;
		return String.valueOf(sum / runtimeList.size());
	}

  /** Hashes a string using SHA-256. */
  public static String calculateSHAHash(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException
  {
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    md.update(s.getBytes("UTF-8"));
    byte[] digest = md.digest();

    String hashVal = "";
    for(byte b : digest)
    {
      hashVal += String.format("%02x", b);
    }

    return hashVal;
  }


}
