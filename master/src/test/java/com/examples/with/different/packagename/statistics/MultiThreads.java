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
package com.examples.with.different.packagename.statistics;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiThreads {

    private static volatile AtomicBoolean FLAG = new AtomicBoolean(false);

    public void foo() throws InterruptedException {
        synchronized (FLAG) {
            if (FLAG.get()) {
                return;
            }
            Thread a = new Thread(new Foo());
            Thread b = new Thread(new Foo());
            a.start();
            b.start();
            b.join();
            a.join();
            FLAG.set(true);
        }
    }

    private static class Foo implements Runnable {
        @Override
        public void run() {
            new File(".").exists();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
