package org.evosuite.eclipse.quickfixes;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;

public class FileQueue {
	private ArrayList<IResource> resources;
	private IResource currentTest;
	private EvoSuiteThread currentThread;
	public static long lastTestTime = 0;

	public FileQueue() {
		resources = new ArrayList<IResource>();
	}

	public void addFile(IResource s) {
		System.out.println(s.getFullPath() + " added to queue!");
		if (currentThread != null && s.getFullPath().equals(currentTest.getFullPath())){
			// currentThread.stop();
		}
		if (!isInQueue(s)){
			resources.add(s);
		}
	}


	private void nextFile() {
		if (resources.size() > 0){
			resources.remove(0);
		}
	}

	private void runNextTest() {
		if (resources.size() > 0) {
			final IResource res = resources.get(0);
			
			EvoSuiteThread r = new EvoSuiteThread(res);
			currentThread = r;
			currentTest = res;
			new Thread(r).start();
		}
	}

	public boolean isInQueue(IResource r){
		for (IResource flr : resources){
			if (flr.getFullPath().equals(r.getFullPath())){
				return true;
			}
		}
		return false;
	}
	
	public void update(){
		//System.out.println("Updating FileQueue (" + resources.size() + ")");
		long currentTime = System.currentTimeMillis();
		//start if  atest is in progress and a test hasn't been started in last quarter of second.
		if ((currentThread == null || currentThread.hasStopped()) && currentTime > lastTestTime + 250){
			lastTestTime = currentTime;
			runNextTest();
			nextFile();
		}
	}
	
	public int getSize(){
		return resources.size();
	}
	
	public void stop(){
		if (currentThread != null){
			currentThread.stop();
		}
	}
}
