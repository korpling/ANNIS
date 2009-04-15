package annisservice.extFiles.rmi;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


import annisservice.extFiles.ExternalFileMgr;
import annisservice.extFiles.ExternalFileMgrImpl;
import annisservice.extFiles.exchangeObj.ExtFileObjectCom;
import annisservice.ifaces.AnnisBinary;

public class ExtFileMgrServer extends UnicastRemoteObject implements ExtFileMgrService 
{
	ExternalFileMgr extFileMgr= null;
	
	public ExtFileMgrServer() throws Exception
	{
		this.extFileMgr= ExternalFileMgrImpl.getMgr(new File("./settings/settings_ExtFileMgr_test.xml"), null);
	}
	
	public void createBranch(String branch) throws Exception 
	{
		this.extFileMgr.createBranch(branch);
	}

	public void deleteBranch(String branch) throws Exception 
	{
		this.extFileMgr.deleteBranch(branch);
	}

	public void deleteBranch(String branch, boolean delRec) throws Exception 
	{
		this.extFileMgr.deleteBranch(branch, delRec);
	}

	public void deleteFile(long id) throws Exception 
	{
		this.extFileMgr.deleteFile(id);
	}

	public AnnisBinary getBinary(Long id) throws Exception 
	{
		return(this.extFileMgr.getBinary(id));
	}

	public ExtFileObjectCom getExtFileObj(Long id) throws Exception 
	{
		return(this.extFileMgr.getExtFileObj(id));
	}

	public boolean hasBranch(String branch) throws Exception 
	{
		return(this.extFileMgr.hasBranch(branch));
	}

	public boolean hasId(long id) throws Exception 
	{
		return(this.extFileMgr.hasId(id));
	}

	public Long putFile(ExtFileObjectCom extFile) throws Exception 
	{
		return (this.extFileMgr.putFile(extFile));
	}
	
	public static void main(String[] args) 
    {
		try {
			
				ExtFileMgrServer server= new ExtFileMgrServer();
				server.start();
			}
		    
		    catch (Exception ex) {
		      System.out.println(ex.getMessage());
		    }
    }

    /**
     * Starts this server as service
     */
    private void start() 
    {
    	try 
    	{
    		Registry reg= LocateRegistry.createRegistry(4711);
			reg.rebind("ExtFileMgrServer", new ExtFileMgrServer());
            System.out.println("ExtFileMgrServer is now waiting for requests...\n");
    	}
        catch (RemoteException e) 
        {
        	System.out.println("ERROR(ExtFileMgrServer): Cannot start server...\n");
            e.printStackTrace();
        }
        catch(Exception e) 
        { 
        	System.out.println("ERROR(ExtFileMgrServer): Cannot start server...\n");
        	e.printStackTrace(); 
        }
    }
}
