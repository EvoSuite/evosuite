/**
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
package org.evosuite.intellij.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.evosuite.intellij.EvoParameters;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Singleton used to run EvoSuite Maven plugin on a background process
 * <p/>
 * Created by arcuri on 9/29/14.
 */
public class EvoSuiteExecutor {

    private static EvoSuiteExecutor singleton = new EvoSuiteExecutor();

    private volatile Thread thread;

    private final AtomicBoolean running = new AtomicBoolean(false);


    private EvoSuiteExecutor() {
    }

    public static EvoSuiteExecutor getInstance() {
        return singleton;
    }


    public boolean isAlreadyRunning() {
        return running.get();
    }

    public synchronized void stopRun(){
        if(isAlreadyRunning()){
            thread.interrupt();
            try {
                thread.join(2000);
            } catch (InterruptedException e) {
            }
        }
    }


    //TODO should refactor 'final EvoParameters params' to be independent from IntelliJ
    /**
     * @param params
     * @param suts   map from Maven module folder to list of classes to tests.
     *               If this latter list is null/empty, then use all classes
     *               in that module
     * @throws IllegalArgumentException
     */
    public synchronized void run(final Project project, final EvoParameters params, final Map<String, Set<String>> suts, final AsyncGUINotifier notifier)
            throws IllegalArgumentException, IllegalStateException {

        if (suts == null || suts.isEmpty()) {
            throw new IllegalArgumentException("No specified classes to test");
        }

        if (params == null) {
            throw new IllegalArgumentException("No defined parameters");
        }

        //check validity of (maven) modules
        for (String modulePath : suts.keySet()) {
            File dir = new File(modulePath);
            if (!dir.exists()) {
                throw new IllegalArgumentException("Target module folder does not exist: " + modulePath);
            }
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("Target module folder is not a folder: " + modulePath);
            }

            if(params.usesMaven()) {
                File pom = new File(dir, "pom.xml");
                if (!pom.exists()) {
                    throw new IllegalArgumentException("Target module folder does not contain a pom.xml file: " + modulePath);
                }
            }
        }

        if (isAlreadyRunning()) {
            throw new IllegalStateException("EvoSuite already running");
        }


        Task.Backgroundable task = new EvoTask(project,"EvoSuite",true,null,suts,notifier,params);
        BackgroundableProcessIndicator progressIndicator = new EvoIndicator(task);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, progressIndicator);
    }



    private void executeOnAllModules(Map<String, Set<String>> suts, Project project, AsyncGUINotifier notifier,
                                     EvoParameters params, ProgressIndicator progressIndicator) {

        thread = Thread.currentThread();

        int modules = suts.keySet().size();
        int total = suts.values().stream().mapToInt(Set::size).sum();
        String msg = "Going to generate tests in "+modules+" module(s) for a total of "+total+ " classes";
        System.out.println(msg);
        notifier.printOnConsole(msg+"\n");

        if(modules > 1) {

            for(Map.Entry<String,Set<String>> entry : suts.entrySet()){
                notifier.printOnConsole("Module "+entry.getKey()+" -> to test "+entry.getValue().size()+" class(es) \n");
            }

            notifier.success(msg);
        }

        for (String modulePath : suts.keySet()) {

            if(Thread.currentThread().isInterrupted()){
                return;
            }
            progressIndicator.checkCanceled();

            final Module module = Utils.getModule(project,modulePath);
            if(module == null){
                notifier.failed("Failed to determine IntelliJ module for "+modulePath);
                return;
            } else {
                if (! Utils.compileModule(project, notifier, module)){
                    return;
                }
            }

            File dir = new File(modulePath);
            Process p;

            SpawnProcessKeepAliveCheckerIntelliJ checker = new SpawnProcessKeepAliveCheckerIntelliJ(notifier);

            try {
                int port = checker.startServer();

                p = ProcessRunner.execute(project, notifier, params, dir, suts.get(modulePath), port);
                if (p == null) {
                    return;
                }

                boolean done = false;

                while (!done) {
                    try {
                   /*
                    this is blocking, which is fine, as we want it
                    to run till completion, unless manually stopped
                     */
                        done = p.waitFor(1, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        p.destroy();
                        progressIndicator.checkCanceled();
                        return;
                    }
                    progressIndicator.checkCanceled();
                }
            } finally {
                notifier.detachLastProcess();
                checker.stopServer();
            }

            int res = p.exitValue();
            if (res != 0) {
                notifier.failed("EvoSuite ended abruptly");
                return;
            }
        }
        VirtualFileManager.getInstance().asyncRefresh(null);
        notifier.success("EvoSuite run is completed");
    }

    private class EvoIndicator extends BackgroundableProcessIndicator{

        public EvoIndicator(@NotNull Task.Backgroundable task) {
            super(task);
        }

//        @Override  //can't override because final
//        public void cancel(){
//        }

        @Override
        protected void delegateRunningChange(@NotNull IndicatorAction action) {
            try {
                Field f = com.intellij.openapi.progress.util.AbstractProgressIndicatorExBase.class.getDeclaredField("CANCEL_ACTION");
                f.setAccessible(true);
                Object obj = f.get(null);
                if(action.equals(obj)){
                    thread.interrupt();
                }
            } catch (Exception e) {
            }
            super.delegateRunningChange(action);
        }

    }



    private class EvoTask extends Task.Backgroundable{

        private final Map<String, Set<String>>  suts;
        private final AsyncGUINotifier notifier;
        private final EvoParameters params;

        public EvoTask(@Nullable Project project,
                       @Nls(capitalization = Nls.Capitalization.Title) @NotNull String title,
                       boolean canBeCancelled,
                       @Nullable PerformInBackgroundOption backgroundOption,
                       Map<String, Set<String>> suts,  AsyncGUINotifier notifier, EvoParameters params) {
            super(project, title, canBeCancelled, backgroundOption);
            this.suts = suts;
            this.notifier = notifier;
            this.params = params;
        }

        @Override
        public void run(@NotNull ProgressIndicator progressIndicator) {

            running.set(true);

            executeOnAllModules(suts, getProject(), notifier, params, progressIndicator);

            running.set(false);
        }


        @Override
        public void onCancel(){
            notifier.printOnConsole("\n\n\nEvoSuite run has been cancelled\n");
            running.set(false);
        }

        @Override
        public void onSuccess(){
            running.set(false);
        }
    }
}
