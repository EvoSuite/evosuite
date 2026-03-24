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
package org.evosuite.runtime.sandbox;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Shared sandbox execution state. This is intentionally independent of
 * SecurityManager so we can coordinate SUT execution even on modern JDKs.
 */
class SandboxContext {

    private final Set<Thread> privilegedThreads;
    private final Set<File> filesToDelete;

    private volatile boolean executingTestCase;
    private volatile Thread privilegedThreadToIgnore;

    SandboxContext() {
        privilegedThreads = new CopyOnWriteArraySet<>();
        privilegedThreads.add(Thread.currentThread());
        filesToDelete = new CopyOnWriteArraySet<>();
        executingTestCase = false;
        privilegedThreadToIgnore = null;
    }

    Set<Thread> getPrivilegedThreads() {
        return new LinkedHashSet<>(privilegedThreads);
    }

    Set<File> getFilesToDelete() {
        return filesToDelete;
    }

    synchronized void addPrivilegedThread(Thread thread) {
        privilegedThreads.add(thread);
    }

    boolean isPrivilegedThread(Thread thread) {
        return privilegedThreads.contains(thread);
    }

    Thread getPrivilegedThreadToIgnore() {
        return privilegedThreadToIgnore;
    }

    synchronized void goingToExecuteUnsafeCodeOnSameThread() {
        if (!isPrivilegedThread(Thread.currentThread())) {
            throw new SecurityException("Current thread is not privileged");
        }
        if (privilegedThreadToIgnore != null) {
            throw new IllegalStateException("The thread is already executing unsafe code");
        }
        privilegedThreadToIgnore = Thread.currentThread();
    }

    synchronized void doneWithExecutingUnsafeCodeOnSameThread() {
        if (!isPrivilegedThread(Thread.currentThread())) {
            throw new SecurityException(
                    "Only a privileged thread can return from unsafe code execution");
        }
        if (privilegedThreadToIgnore == null) {
            throw new IllegalStateException("The thread was not executing unsafe code");
        }
        privilegedThreadToIgnore = null;
    }

    boolean isSafeToExecuteSUTCode() {
        Thread current = Thread.currentThread();
        if (!isPrivilegedThread(current)) {
            return true;
        }
        return privilegedThreadToIgnore == current;
    }

    synchronized void goingToExecuteTestCase() {
        if (executingTestCase) {
            throw new IllegalStateException("Trying to set up the sandbox while executing a test case");
        }
        executingTestCase = true;
    }

    boolean isExecutingTestCase() {
        return executingTestCase;
    }

    synchronized void goingToEndTestCase() {
        if (!executingTestCase) {
            throw new IllegalStateException("Trying to disable sandbox when not test case was run");
        }
        org.evosuite.runtime.System.restoreProperties();
        for (File file : filesToDelete) {
            file.deleteOnExit();
        }
        executingTestCase = false;
    }
}
