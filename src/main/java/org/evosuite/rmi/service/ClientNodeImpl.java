package org.evosuite.rmi.service;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.evosuite.ClientProcess;
import org.evosuite.Properties;
import org.evosuite.TestSuiteGenerator;
import org.evosuite.ga.GeneticAlgorithm;
import org.evosuite.sandbox.Sandbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientNodeImpl implements ClientNodeLocal, ClientNodeRemote{

	private static Logger logger = LoggerFactory.getLogger(ClientNodeImpl.class);
	
	private volatile ClientState state;
	private MasterNodeRemote masterNode;
	private String clientRmiIdentifier;
	protected volatile CountDownLatch latch;
	protected Registry registry;
	
	protected final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	public ClientNodeImpl(Registry registry){
		this.registry = registry;
		state = ClientState.NOT_STARTED;
		/*
		 * TODO: for now it is a constant because we have only one client
		 */
		clientRmiIdentifier = "ClientNode";
		latch = new CountDownLatch(1);
	}
	
	@Override
	public void startNewSearch() throws RemoteException, IllegalStateException {
		if(!state.equals(ClientState.NOT_STARTED)){
			throw new IllegalArgumentException("Search has already been started");
		}

		/*
		 * Needs to be done on separated thread, otherwise the master will block on this
		 * function call until end of the search, even if it is on remote process
		 */
		executor.submit(new Runnable(){
			@Override
			public void run() {				
				changeState(ClientState.STARTED);
				
				
				//Before starting search, let's activate the sandbox
				if (Properties.SANDBOX){
					Sandbox.initializeSecurityManagerForSUT();
				}
				//Object instruction = util.receiveInstruction();
				/*
				 * for now, we ignore the instruction (originally was meant to support several client in parallel and
				 * restarts, but that will be done in RMI)
				 */

				
				// Starting a new search
				TestSuiteGenerator generator = new TestSuiteGenerator();
				generator.generateTestSuite();

				GeneticAlgorithm ga = generator.getEmployedGeneticAlgorithm();

				if (Properties.CLIENT_ON_THREAD) {
					/*
					 * this is done when the client is run on same JVM, to avoid
					 * problems of serializing ga
					 */
					ClientProcess.geneticAlgorithmStatus = ga;
				}

				
				changeState(ClientState.DONE);
				
				if (Properties.SANDBOX){
					/*
					 * Note: this is mainly done for debugging purposes, to simplify how test cases are run/written 
					 */
					Sandbox.resetDefaultSecurityManager();
				}
			}			
		});
	}

	@Override
	public void cancelCurrentSearch() throws RemoteException {
		System.exit(1);
	}

	@Override
	public boolean waitUntilDone(long timeoutInMs) throws RemoteException, InterruptedException {
		return latch.await(timeoutInMs, TimeUnit.MILLISECONDS); 
	}
	
	@Override
	public void waitUntilDone()  {
		try{
			latch.await(); 
		} catch(InterruptedException e){			
		}
	}
	
	

	@Override
	public void changeState(ClientState state) {
		logger.info("Client changing state from "+state+" to "+this.state);
		this.state = state;
		
		if(this.state.equals(ClientState.DONE)){
			latch.countDown();
		}
		
		try {
			masterNode.informChangeOfStateInClient(clientRmiIdentifier, state);
		} catch (RemoteException e) {
			logger.error("Cannot inform master of change of state",e);
		}
	}

	@Override
	public boolean init() {
		try {			
			masterNode = (MasterNodeRemote) registry.lookup(MasterNodeRemote.RMI_SERVICE_NAME);
			masterNode.registerClientNode(clientRmiIdentifier);
			masterNode.informChangeOfStateInClient(clientRmiIdentifier, state);
		} catch (Exception e) {
			logger.error("Error when connecting to master via RMI",e);
			return false;
		}		
		return true;
	}

	public String getClientRmiIdentifier() {
		return clientRmiIdentifier;
	}

}
