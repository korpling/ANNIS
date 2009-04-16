
package annis.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class for cryptographic functions.
 * @author thomas
 */
public class Crypto 
{
  
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
