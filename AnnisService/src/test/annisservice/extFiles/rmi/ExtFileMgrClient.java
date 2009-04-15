package annisservice.extFiles.rmi;

import java.io.File;
import java.io.FileOutputStream;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import annisservice.AnnisService;
import annisservice.extFiles.ExternalFileMgr;
import annisservice.extFiles.exchangeObj.ExtFileObjectCom;
import annisservice.extFiles.exchangeObj.ExtFileObjectImpl;
import annisservice.ifaces.AnnisBinary;

public class ExtFileMgrClient implements Runnable
{
	private Long id= null; 
	public ExtFileMgrClient(long id)
	{
		this.id= id;
	}
	
	public void run()
	{
		try 
		{
            //Registry registry = LocateRegistry.getRegistry();
            //ExtFileMgrService service = (ExtFileMgrService)registry.lookup("extFileMgrService");
			//String url= "//localhost:4711/AnnisService";
			String url= "//localhost:4711/ExtFileMgrServer";
			ExtFileMgrService service = (ExtFileMgrService)Naming.lookup(url);
           
			String branch= "branch" + this.id;
            System.out.println("Client + "+this.id+" creates branch: "+branch +"...");
            service.createBranch(branch);
            for (int i = 0; i < 10000; i++)
            {}
            System.out.println("Client "+this.id+" checking if branch exists: "+branch +"..."+service.hasBranch(branch));
            for (int i = 0; i < 10000; i++)
            {}
            
            //checking working with file
            ExtFileObjectCom extFile= new ExtFileObjectImpl();
            extFile.setBranch(branch);
            extFile.setComment("bla bla bla");
            extFile.setMime("audio/wav");
            //extFile.setOrigName("irgenein name");
            extFile.setFile( new File("e:/UniJob/eclipse/ExtFiles/tst/extData/extFile01.wav"));
            if (service== null) throw new Exception("Service is null");
            long fid= 0L;
            System.out.println(service.hasId(0));
            fid= service.putFile(extFile);
            for (int i = 0; i < 10000; i++)
            {}
            System.out.println("Client "+ service.hasId(fid)+ " checking if file is there with id: "+fid +"..."+ service.hasId(fid));
            for (int i = 0; i < 10000; i++)
            {}
            ExtFileObjectCom extFile2= service.getExtFileObj(fid);
            extFile2.getFile("e:/UniJob/eclipse/ExtFiles/tst/extData/extFile02_supernew.wav");
            for (int i = 0; i < 10000; i++)
            {}
            AnnisBinary aBin= service.getBinary(fid);
            File newFile= new File("e:/UniJob/eclipse/ExtFiles/tst/extData/extFile02_supernew_aBin.wav");
            FileOutputStream fos = new FileOutputStream(newFile);
    		fos.write(aBin.getBytes());
            for (int i = 0; i < 10000; i++)
            {}
            System.out.println("Client "+ service.hasId(id)+ " deleting file with id: "+fid +"...");
            service.deleteFile(fid);
            for (int i = 0; i < 10000; i++)
            {}
            System.out.println("Client "+this.id+" delete branch: "+branch +"...");
            service.deleteBranch(branch);
            
        } catch (AccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NotBoundException e) {
        	System.err.println("Cannot find the server ExtFileMgrService...\n");
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

    public static void main(String[] args) 
    {
    	ExtFileMgrClient client= null;
    	Thread t= null;
    	for (int i= 0; i< 1;i++)
    	{
    		client= new ExtFileMgrClient(i);
    		t = new Thread(client);
			t.start();
    	}
        
    }
}
