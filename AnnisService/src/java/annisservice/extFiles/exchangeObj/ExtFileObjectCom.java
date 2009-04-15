package annisservice.extFiles.exchangeObj;

import java.io.File;
import java.io.Serializable;

/**
 * This class contains a reference to a file object and metainformations of it.
 * This interface is just for communication between ExtFileMgr and and all clients.
 * 
 * @author Florian Zipser
 *
 */
public interface ExtFileObjectCom extends ExtFileObject, Serializable
{
	/**
	 * returns a file object to this file
	 * @return the context of this object
	 */
	public File getFile();
	
	/**
	 * returns a file object to this file
	 * @param fName String - a user defined name for the context file 
	 * @return the context of this object with the given name
	 */
	public File getFile(String fName);
	
	/**
	 * sets the file of this object
	 * @param file File - the file
	 */
	public void setFile(File file);
	
	/**
	 * Returns this file as Byte-array, this method is needed for RMI. 
	 * @return
	 */
	//public byte[] getBytes();
	
	/**
	 * Sets this Byte-array, this method is needed for RMI. 
	 * @return
	 */
	//public void setBytes(byte[] bytes);
}
