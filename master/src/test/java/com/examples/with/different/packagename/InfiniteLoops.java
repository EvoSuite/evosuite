/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
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

public class InfiniteLoops {

    public void easyLoop() throws InterruptedException {
        Thread t = new Thread() {
            @Override
            public void run() {
                System.out.println("ERROR This should not be printed");
                while (true) {
                    try {
                        System.out.println("In the loop going to sleep. Thread " + Thread.currentThread().getId());
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        System.out.println("ERROR This should not be printed: " + e);
                        return;
                    }
                }
            }
        };
        t.start();
    }


    public void ignoreIterrupt() {
        Thread t = new Thread() {
            @Override
            public void run() {
                System.out.println("ERROR This should not be printed");
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        System.out.println("ERROR This should not be printed: " + e);
                    }
                }
            }
        };
        t.start();
    }


    public void hardToKill() {
        Thread t = new Thread() {
            @Override
            public void run() {
                System.out.println("ERROR This should not be printed");
                while (true) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                        System.out.println("ERROR This should not be printed: " + e);
                    }
                }
            }
        };
        t.start();
    }

}
