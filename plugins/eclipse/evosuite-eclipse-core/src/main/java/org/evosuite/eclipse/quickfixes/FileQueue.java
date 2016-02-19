/**
 * Copyright (C) 2010-2016 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
