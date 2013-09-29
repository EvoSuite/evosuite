package org.evosuite.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.evosuite.rmi.service.MasterNodeLocal;
import org.evosuite.rmi.service.MasterNodeRemote;
import org.evosuite.rmi.service.MasterNodeImpl;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class should be used only in the Master process, not the clients.
 * Used to initialize and store all the RMI services in the master.
 * It is also used to start the RMI registry
 * 
 * @author arcuri
 *
 */
public class MasterServices {

	private static Logger logger = LoggerFactory.getLogger(MasterServices.class);
	
	private static MasterServices instance = new MasterServices();
	
	private int registryPort = -1;
	
	/**
	 *  We store it to avoid issues with GC
	 */
	private Registry registry;

	private MasterNodeImpl masterNode; 
	
	
	protected MasterServices(){		
	}
	
	
	public static MasterServices getInstance(){
		return instance;
	}
	
	/**
	 * A registry is created on the "localhost".
	 * But what is the "localhost"? usually, this is a loopback
	 * address 127.0.0.1. But, if there are network interfaces, depending
	 * on the operating system the "localhost" can bind to them.
	 * 
	 * <p>
	 * So, if we start EvoSuite with a connection on (eg WiFi), losing such connection
	 * would mess up EvoSuite. Even worse, if EvoSuite is started from Eclipse, Eclipse
	 * will always use the binding it used at it start! In that case, Eclipse would need
	 * to be restarted each time connection change. 
	 * 
	 * <p>
	 * For these reasons, here we try to force to always bind to 127.0.0.1
	 */		
	public static void ensureRegistryOnLoopbackAddress(){
		 /*
		  *  Here, it does not seem to be possible in a clean way
		  *  to force the biding to 127.0.0.1.
		  *  So we need to modify the property used for it 
		  */
		System.setProperty("java.rmi.server.hostname", "127.0.0.1");
	}
	
	public boolean startRegistry() throws IllegalStateException{
		
		if(registry != null){
			throw new IllegalStateException("RMI registry is already running");
		}
		
		/*
		 * Unfortunately, it does not seem possible to start a RMI registry on an
		 * ephemeral port. So, we start with a port, and see if free. If not, try the
		 * next one, etc. Note, it is important to start from a random port to avoid issues
		 * with several masters running on same node, eg when experiments on cluster.
		 */
		
		int port = 2000;
		port += Randomness.nextInt(20000);
		
		final int TRIES = 100;
		for(int i=0; i<TRIES; i++){
			try {
				int candidatePort = port+i;								
				ensureRegistryOnLoopbackAddress();
				
				registry = LocateRegistry.createRegistry(candidatePort);
				registryPort = candidatePort;
				return true;
			} catch (RemoteException e) {								
			}		
		}
		
		return false;
	}
	
	/**
	 * Return the port on which the registry is running.
	 * 
	 * @return a negative value if no registry is running
	 */
	public int getRegistryPort(){
		return registryPort; 
	}
	
	public void registerServices() throws RemoteException{
		masterNode = new MasterNodeImpl(registry);
		MasterNodeRemote stub = (MasterNodeRemote) UtilsRMI.exportObject(masterNode);
		registry.rebind(MasterNodeRemote.RMI_SERVICE_NAME, stub);
	}
	


	public MasterNodeLocal getMasterNode() {
		return masterNode;
	}
	
	public void stopServices(){
		if(masterNode != null){
			try {
				UnicastRemoteObject.unexportObject(masterNode,true);
			} catch (NoSuchObjectException e) {
				logger.warn("Failed to delete MasterNode RMI instance",e);
			}
			masterNode = null;
		}
		
		if(registry != null){
			try {
				UnicastRemoteObject.unexportObject(registry,true);
			} catch (NoSuchObjectException e) {
				logger.warn("Failed to stop RMI registry",e);
			}
			registry = null;
		}
	}
}
