/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.continuous.job;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Assert;

import org.evosuite.Properties;
import org.evosuite.classpath.ClassPathHandler;
import org.evosuite.continuous.CtgConfiguration;
import org.evosuite.Properties.AvailableSchedule;
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
		Properties.CTG_DIR = ".tmp_for_testing_" + JobExecutorTest.class.getName();
		if(storage!=null){
			storage.clean();
		}
		storage = new StorageManager();
	}

	@Test(timeout = 90000)
	public void testActualExecutionOfSchedule(){

		Properties.TEST_SCAFFOLDING = true;
		
		boolean storageOK = storage.isStorageOk();
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
		// if Properties.TEST_SCAFFOLDING is enabled, we should have
		// 4 java files (2 test cases and 2 scaffolding files), however
		// 'storage' just returns the 2 test cases
		Assert.assertEquals("Tmp folder: "+Properties.CTG_DIR, 2, data.size());

		storage.clean();
	}

	@Test
	public void testEventSequenceWhenWrongSchedule() throws InterruptedException{

		boolean storageOK = storage.isStorageOk();
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

		storage.clean();
	}
}
