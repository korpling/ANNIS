package annis.externalFiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * This class contains a reference to a file object and metainformations of it.
 * @author Florian Zipser
 *
 */
public class ExtFileObjectImpl implements ExtFileObjectCom, ExtFileObjectDAO  
{
	private static final String TOOLNAME=	"ExtFileObjectImpl"; 
	private static final long serialVersionUID = 1678615866336637980L;
	
	private static final String MSG_ERR=			"ERROR(" +TOOLNAME+ "): ";
	private static final String ERR_ID_NULL= 		MSG_ERR + "The given id is null, but must not be.";
	private static final String ERR_EMPTY_CORP=		MSG_ERR + "The given corpus name is empty.";
	private static final String ERR_FILE_NOT_EXISTS=MSG_ERR + "Cannot add the given file, because it doesn�t exists: ";
	private static final String ERR_EMPTY_FNAME=	MSG_ERR + "Cannot add the given name for file, because its empty.";
	
	/**
	 * the unique id of this extFile-object
	 */
	private Long id= null; 
	
	/**
	 * The current name of the referenced file
	 */
	private String fileName= null;
	
	/**
	 * a file object to this file
	 */
	//private transient File file= null;
	
	/**
	 * this file as byte-array
	 */
	private byte[] bytes= null;
	/**
	 * the original name
	 */
	private String origName= null;
	
	/**
	 * the unique branch name, to which this file depends
	 */
	private String branch= null;
	
	/**
	 * the mime type
	 */
	private String mime= null;
	
	/**
	 * a comment for this extFile-object
	 */
	private String comment= null;
	
	/**
	 * Creates a new object
	 * @throws Exception
	 */
	public ExtFileObjectImpl()
	{
		
	}
	
	/**
	 * Creates a new object from an existing ExtFileObjectCom-object
	 * @param extFileObj ExtFileObjectCom - object which should be casted
	 * @param origFileName String - the new and current name of the context file 
	 * @throws Exception
	 */
	public ExtFileObjectImpl(	ExtFileObjectCom extFileObj, 
								String newFileName)
	{
		//this.setID(extFileObj.getID());
		this.setBranch(extFileObj.getBranch());
		this.setComment(extFileObj.getComment());
		this.setOrigFileName(extFileObj.getFileName());
		this.setMime(extFileObj.getMime());
		this.setFileName(newFileName);
	}
	
	/**
	 * Creates a new object from an existing ExtFileObjectDAO-object
	 * @param extFileObj ExtFileObjectDAO - object which should be casted
	 * @param origFileName String - the new and current name of the context file
	 * @param cFile File - context file 
	 * @throws Exception
	 */
	public ExtFileObjectImpl(	ExtFileObjectDAO extFileObj, 
								File cFile)
	{
		this.setID(extFileObj.getID());
		this.setBranch(extFileObj.getBranch());
		this.setComment(extFileObj.getComment());
		this.setOrigFileName(extFileObj.getFileName());
		this.setMime(extFileObj.getMime());
		this.setFile(cFile);
	}
	
	/**
	 * returns the unique id of this object
	 * @return
	 */
	public Long getID()
	{ return(this.id); }
	
	/**
	 * sets the unique id of this object
	 * @param id long - the unique id
	 */
	public void setID(Long id)
	{ 
		if (id== null) throw new ExternalFileMgrException(ERR_ID_NULL);
		this.id= id; 
	}
	
	/**
	 * returns the current name of this file
	 * @return
	 */
	public String getFileName()
	{ return(this.fileName); }
	
	
	/**
	 * returns the current name of this file
	 * @param fileName String - current name of the file 
	 */
	public void setFileName(String fileName)
	{
		if ((fileName== null) || (fileName.equalsIgnoreCase(""))) 
			throw new ExternalFileMgrException(ERR_EMPTY_FNAME);
		this.fileName= fileName;
	}
	
	/**
	 * returns a file object to this file
	 * @return
	 */
	public File getFile()
	{ 
		return(this.getFile(this.getFileName()));
	}
	
	/**
	 * returns a file object to this file
	 * @param fName String - a user defined name for the context file 
	 * @return the context of this object with the given name
	 */
	public File getFile(String fName)
	{
		File retFile= null;
		retFile= new File(fName);
		
		try {
			FileOutputStream fos = new FileOutputStream(retFile);
			fos.write(this.bytes);
		} catch (IOException e) {
			throw new ExternalFileMgrException(e);
		}
		return(retFile);
	}
	
	/**
	 * sets the file of this object, and calls function setBytes() and setOrigName()
	 * @param file File - the file
	 */
	public void setFile(File file)
	{
		try {
			if (!file.exists())
				throw new ExternalFileMgrException(ERR_FILE_NOT_EXISTS + file.getCanonicalPath());
			//this.file= file; 
			long length = file.length();


			FileInputStream fis = new FileInputStream(file);


			byte[] bytes = new byte[(int)length];


			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
					&& (numRead=fis.read(bytes, offset, bytes.length-offset)) >= 0) 
			{
				offset += numRead;
			}


			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file "+file.getName());
			}

			fis.close();
			this.bytes= bytes;
			this.setFileName(file.getName());
		} catch (IOException e) {
			throw new ExternalFileMgrException(e);
		}
	}
	
	/**
	 * returns the original file name of this file
	 * @return
	 */
	public String getOrigFileName()
	{return(this.origName);}
	
	/**
	 * sets the original name of file
	 * @param origName String - the original name
	 */
	public void setOrigFileName(String origName)
	{this.origName= origName;}
	
	/**
	 * sets the branch to which the external file depends on.
	 * @param branch String - a unique name of the branch
	 */
	public void setBranch(String branch)
	{ 
		if ((branch == null) || (branch.equalsIgnoreCase("")))
			throw new ExternalFileMgrException(ERR_EMPTY_CORP);
		this.branch= branch;
	}
	
	/**
	 * returns the unique Branch name of this file
	 * @return unique Branch nam
	 */
	public String getBranch()
	{ return(this.branch); }
	
	/**
	 * returns the mime type of this file
	 * @return
	 */
	public String getMime()
	{return(this.mime);}
	
	
	/**
	 * sets the mime type of file
	 * @param mime String - the mime type
	 */
	public void setMime(String mime)
	{this.mime= mime;}
	
	
	/**
	 * returns the comment belongs to this object
	 * @return
	 */
	public String getComment()
	{ return(this.comment); }
	
	/**
	 * sets a comment to the file of this object
	 * @param comment String - a comment
	 */
	public void setComment(String comment)
	{
		this.comment= comment;
	}
	/*
	public String getJSON() 
	{
		return("{id: }"); //+ this.id + ", mimeType: '" + this.mimeType + ", bytes: '" + new String(this.bytes) + "'}";
	}
	
	private void writeObject(
		      ObjectOutputStream aOutputStream
		    ) throws IOException {
		      //perform the default serialization for all non-transient, non-static fields
		      aOutputStream.defaultWriteObject();
		    }

	/*
	private void writeObject(ObjectOutputStream stream) throws IOException
	{
		try
		{
			//writing id-value
			stream.writeLong(this.getID());
			//writing current filename-value
			stream.writeObject(this.getCurrFileName());
			//writing original filename-value
			stream.writeObject(this.getOrigName());
			//writing branch name
			stream.writeObject(this.getBranch());
			//writing mime-type
			stream.writeObject(this.getMime());
			//writing comment
			stream.writeObject(this.getComment());
		}
		catch (Exception e)
		{throw new IOException(e.getMessage()); }
		
	}
	
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		try
		{
			//writing id-value
			this.setID(stream.readLong());
			//writing current filename-value
			this.setCurrFileName((String )stream.readObject());
			//writing original filename-value
			this.setOrigName((String )stream.readObject());
			//writing branch name
			this.setBranch((String )stream.readObject());
			//writing mime-type
			this.setMime((String )stream.readObject());
			//writing comment
			this.setComment((String )stream.readObject());
		}
		catch (Exception e)
		{throw new IOException(e.getMessage()); }
	}
	*/
}
