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
package com.examples.with.different.packagename;

public class PrintingThatShouldBeMuted{

	public void foo(int x){
		
		if(x > 0){
			System.out.println("Greater");
		} else {
			System.out.println("Lower");
		}
		
	}
	
	/*
	public void doPrint() { //throws RuntimeException{
		System.err.println("This should not be printed.");
		
		Thread t  = new Thread(){
			@Override
			public void run(){
				//throw new RuntimeException("Also this one should not be printed");
			}
		};
		t.start();
		try {
			t.join(50);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Thread k = new Thread(){
			@Override
			public void run(){
				System.err.println("Going to sleep for a bit. EvoSuite should throw a timeout");
				System.err.println("We enter in infinite loop, even if EvoSuite try to interrupt");
				while(true){
					try {
						long time = 200;
						System.err.println("Going to sleep for "+time+" ms");
						Thread.sleep(time);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		k.start();
		try {
			k.join(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} */
}
