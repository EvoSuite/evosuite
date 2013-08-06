package org.evosuite.continuous.job;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import org.evosuite.continuous.persistency.StorageManager;
import org.evosuite.continuous.persistency.StorageManager.TestsOnDisk;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JobExecutorTest {

	private StorageManager storage;
	
	@Before
	public void init(){
		if(storage!=null){
			storage.clean();
		}
		storage = new StorageManager(".tmp_for_testing_"+this.getClass().getName());
	}
		
	@Test(timeout = 90000)
	public void testSchedule(){
		
		boolean storageOK = storage.openForWriting();
		Assert.assertTrue(storageOK);
		storageOK = storage.createNewTmpFolders();
		Assert.assertTrue(storageOK);

		List<TestsOnDisk> data = storage.gatherGeneratedTestsOnDisk();
		Assert.assertEquals(0, data.size());
		
		// no need to specify it, as com.examples are compiled with EvoSuite  
		String classpath = System.getProperty("java.class.path"); 
		
		int cores = 1;
		int memory = 1000; 
		int minutes = 1;
		
		JobExecutor exe = new JobExecutor(storage, classpath, cores, memory, minutes);
		
		JobDefinition simple = new JobDefinition(30, memory, 
				com.examples.with.different.packagename.continuous.Simple.class.getName(), 0, null, null);
		
		JobDefinition trivial = new JobDefinition(30, memory, 
				com.examples.with.different.packagename.continuous.Trivial.class.getName(), 0, null, null);
		
		Assert.assertTrue(simple.jobID < trivial.jobID);
		
		List<JobDefinition> jobs = Arrays.asList(simple,trivial);
		
		exe.executeJobs(jobs);
		
		exe.waitForJobs();

		data = storage.gatherGeneratedTestsOnDisk();
		Assert.assertEquals(2, data.size());		
		
		storage.clean();
	}
}
