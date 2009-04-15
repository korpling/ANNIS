package annisservice.extFiles.exchangeObj;

/**
 * This class contains a reference to a file object and metainformations of it.
 * This interface is for communication between ExtFileMgr and its DAO.
 * 
 * @author Florian Zipser
 *
 */
public interface ExtFileObjectDAO extends ExtFileObject
{
	/**
	 * sets the unique id of this object
	 * @param id long - the unique id
	 */
	public void setID(Long id);
	
	/**
	 * returns the original name of this file
	 * @return
	 */
	public String getOrigFileName();
	
	/**
	 * returns the original name of this file
	 * @param fileName String - current name of the file 
	 */
	public void setOrigFileName(String fileName);
	
	/**
	 * Sets the name of the context file to the given
	 * @param current name of the context file
	 * @throws Exception
	 */
	public void setFileName(String fileName);
}
