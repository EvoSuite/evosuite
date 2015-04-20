package org.evosuite.rmi;


import org.evosuite.Properties;
import org.evosuite.rmi.service.ClientNodeLocal;
import org.evosuite.rmi.service.ClientState;
import org.evosuite.rmi.service.MasterNodeLocal;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServicesTest {

	private int currentPort;
	
	@Before
	public void init(){
		currentPort = Properties.PROCESS_COMMUNICATION_PORT;
	}
	
	@After
	public void tearDown(){
		Properties.PROCESS_COMMUNICATION_PORT = currentPort;
	}
	
	@Test
	public void testMasterClientCommunication() throws Exception{
		MasterServices master = new MasterServices();
		master.startRegistry();
		master.registerServices();
		
		Properties.PROCESS_COMMUNICATION_PORT = master.getRegistryPort();
		
		ClientServices clients = new ClientServices();
		clients.registerServices();
		
		ClientNodeLocal clientNode = clients.getClientNode();
		clientNode.changeState(ClientState.STARTED);
		
		MasterNodeLocal masterNode = master.getMasterNode();
		String summary = masterNode.getSummaryOfClientStatuses();
		
		Assert.assertNotNull(summary);
		Assert.assertTrue("summary="+summary,summary.contains(ClientState.STARTED.toString()));
	}
	
}
