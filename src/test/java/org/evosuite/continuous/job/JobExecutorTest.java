package org.evosuite.continuous.job;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;

import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.continuous.CtgConfiguration;
import org.evosuite.continuous.job.JobScheduler.AvailableSchedule;
import org.evosuite.continuous.persistency.StorageManager;
import org.evosuite.continuous.persistency.StorageManager.TestsOnDisk;
import org.junit.Before;
import org.junit.Test;

import com.examples.with.different.packagename.continuous.Simple;
import com.examples.with.different.packagename.continuous.Trivial;
import com.examples.with.different.packagename.continuous.UsingSimpleAndTrivial;

public class JobExecutorTest {

	private StorageManager storage;

	@Before
	public void init(){
		if(storage!=null){
			storage.clean();
		}
		storage = new StorageManager(".tmp_for_testing_"+this.getClass().getName());
		storage.clean();
	}

	@Test(timeout = 90000)
	public void testActualExecutionOfSchedule(){

		boolean storageOK = storage.openForWriting();
		Assert.assertTrue(storageOK);
		storageOK = storage.createNewTmpFolders();
		Assert.assertTrue(storageOK);

		List<TestsOnDisk> data = storage.gatherGeneratedTestsOnDisk();
		Assert.assertEquals(0, data.size());

		ClassPathHandler.getInstance().changeTargetCPtoTheSameAsEvoSuite();
		String classpath =  ClassPathHandler.getInstance().getTargetProjectClasspath();

		int cores = 1;
		int memory = 1000; 
		int minutes = 1;

		CtgConfiguration conf = new CtgConfiguration(memory, cores, minutes, 1, false, AvailableSchedule.SIMPLE);
		JobExecutor exe = new JobExecutor(storage, classpath, conf);

		JobDefinition simple = new JobDefinition(30, memory, 
				com.examples.with.different.packagename.continuous.Simple.class.getName(), 0, null, null);

		JobDefinition trivial = new JobDefinition(30, memory, 
				com.examples.with.different.packagename.continuous.Trivial.class.getName(), 0, null, null);

		Assert.assertTrue(simple.jobID < trivial.jobID);

		List<JobDefinition> jobs = Arrays.asList(simple,trivial);

		exe.executeJobs(jobs,cores);

		exe.waitForJobs();

		data = storage.gatherGeneratedTestsOnDisk();
		Assert.assertEquals(2, data.size());		

		storage.clean();
	}

	@Test
	public void testEventSequeneWhenWrongSchedule() throws InterruptedException{

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
		int minutes = 10000;

		CtgConfiguration conf = new CtgConfiguration(memory, cores, minutes, 1, false, AvailableSchedule.SIMPLE);
		final JobExecutor exe = new JobExecutor(storage, classpath, conf);

		JobDefinition simple = new JobDefinition(30, memory, 
				Simple.class.getName(), 0, null, null);

		JobDefinition trivial = new JobDefinition(30, memory, 
				Trivial.class.getName(), 0, null, null);

		JobDefinition ust = new JobDefinition(30, memory, 
				UsingSimpleAndTrivial.class.getName(), 0, 
				new HashSet<String>(Arrays.asList(new String[]{Simple.class.getName(),Trivial.class.getName()})), 
				null);


		/*
		 * ust is in the middle, instead of last element.
		 * still, even if the schedule is wrong, the executor should be able
		 * to properly handle it 
		 */		
		final List<JobDefinition> jobs = Arrays.asList(simple,ust,trivial);

		exe.initExecution(jobs);
		
		Thread t = new Thread(){
			@Override
			public void run(){
				exe.execute(jobs);
			}
		};
		try{
			t.start();

			exe.doneWithJob(exe.pollJob());
			exe.doneWithJob(exe.pollJob());

			JobDefinition last = exe.pollJob();
			exe.doneWithJob(last);
			
			Assert.assertEquals(ust.cut,last.cut);
		}
		finally{
			t.interrupt();
		}
	}
}
