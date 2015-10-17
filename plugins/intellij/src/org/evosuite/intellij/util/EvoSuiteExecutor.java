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
package org.evosuite.intellij.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileStatusNotification;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.evosuite.intellij.EvoParameters;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Singleton used to run EvoSuite Maven plugin on a background process
 * <p/>
 * Created by arcuri on 9/29/14.
 */
public class EvoSuiteExecutor {

    private static EvoSuiteExecutor singleton = new EvoSuiteExecutor();

    private volatile Thread thread;

    private EvoSuiteExecutor() {
    }

    public static EvoSuiteExecutor getInstance() {
        return singleton;
    }


    public boolean isAlreadyRunning() {
        if (thread != null && thread.isAlive()) {
            return true;
        }
        return false;
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

        thread = new Thread() {
            @Override
            public void run() {

                for (String modulePath : suts.keySet()) {

                    if(isInterrupted()){
                        return;
                    }

                    final Module module = Utils.getModule(project,modulePath);
                    if(module == null){
                        notifier.failed("Failed to determine IntelliJ module for "+modulePath);
                        return;
                    } else {
                        final AtomicBoolean ok = new AtomicBoolean(true);
                        final CountDownLatch latch = new CountDownLatch(1);


                        //TODO: maybe this is not really needed if using Maven plugin?
                        ApplicationManager.getApplication().invokeAndWait(new Runnable() {
                            public void run() {
                                CompilerManager.getInstance(project).make(module, new CompileStatusNotification() {
                                    @Override
                                    public void finished(boolean aborted, int errors, int warnings, CompileContext compileContext) {
                                        if (errors > 0) {
                                            ok.set(false);
                                        }
                                        latch.countDown();
                                    }
                                });
                            }
                        }, ModalityState.defaultModalityState());

                        try {
                            latch.await();
                        } catch (InterruptedException e) {
                            return;
                        }
                        if(! ok.get()){
                            notifier.failed("Compilation failure. Fix the compilation issues before running EvoSuite.");
                            return;
                        }
                    }

                    File dir = new File(modulePath);
                    //should be on background process
                    Process p = execute(project,notifier, params, dir, suts.get(modulePath));
                    if(p == null){
                        return;
                    }

                    int res = 0;
                    try {
                        /*
                            this is blocking, which is fine, as we want it
                            to run till completion, unless manually stopped
                         */
                        res = p.waitFor();
                    } catch (InterruptedException e) {
                        p.destroy();
                        return;
                    }
                    if (res != 0) {
                        notifier.failed("EvoSuite ended abruptly");
                        return;
                    }
                }
                VirtualFileManager.getInstance().asyncRefresh(null);
                notifier.success("EvoSuite run is completed");
            }
        };
        thread.start();
    }

    private Process execute( Project project, AsyncGUINotifier notifier, EvoParameters params, File dir, Set<String> classes) {

        List<String> list;
        if(params.usesMaven()){
            list = getMavenCommand(params, classes);
        } else {
            list = getEvoJarCommand(project, dir,params, classes);
        }

        String[] command = list.toArray(new String[list.size()]);

        String concat = "Going to execute command:\n";
        for(String c : command){
            concat += c + "  ";
        }
        concat += "\nin folder: "+dir.getAbsolutePath();
        concat += "\n";

        System.out.println(concat);
        notifier.printOnConsole(concat);//FIXME: done here it gets cleared by IntelliJ... really fucking annoying

        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(dir);
            builder.command(command);

            Map<String,String> map = builder.environment();
            map.put("JAVA_HOME", params.getJavaHome());

            Process p =  builder.start();
            notifier.attachProcess(p);

            notifier.printOnConsole(concat); //this doesn't work either...

            return p;
        } catch (IOException e) {
            notifier.failed("Failed to execute EvoSuite: "+e.getMessage());
            return null;
        }

    }

    private List<String> getEvoJarCommand( Project project, File dir, EvoParameters params, Set<String> classes) throws IllegalArgumentException{
        List<String> list = new ArrayList<String>();
        String java = "java";
        if(Utils.isWindows()){
            java += ".exe";
        }
        list.add(params.getJavaHome() + File.separator + "bin" + File.separator + java);
        list.add("-jar");
        list.add(params.getEvosuiteJarLocation());

        list.add("-continuous");
        list.add("execute");

        //No need to specify target, as we do specify the CUT list
        //list.add("-target");
        //list.add(target);
        if(classes==null || classes.isEmpty()){
            //if we want to change it, we need to handle 'target'
            throw new IllegalArgumentException("Need to specify class list");
        }

        list.add("-Dctg_memory="+params.getMemory());
        list.add("-Dctg_cores="+params.getCores());
        list.add("-Dctg_time_per_class=" + params.getTime());
        list.add("-Dctg_export_folder=" + params.getFolder());

        if(classes != null && classes.size() >= 0) {
            if (classes.size() <= 10) {
                String cuts = getCommaList(classes);
                list.add("-Dctg_selected_cuts=" + cuts);
            } else {
                String filePath = writeClassesToFile(classes);
                list.add("-Dctg_selected_cuts_file_location="+filePath);
            }
        }

        if(dir==null || !dir.exists()){
            throw new IllegalArgumentException("Invalid module dir");
        }
        String folderPath = dir.getAbsolutePath();

        Module module = null;
        for(Module m : ModuleManager.getInstance(project).getModules()){
            String modulePath = Utils.getFolderLocation(m);
            if(modulePath.equals(folderPath)){
                module = m;
                break;
            }
        }

        if(module == null){
            throw new IllegalArgumentException("Cannot determine module for "+folderPath);
        }

        String cp = Utils.getFullClassPath(module);
        list.add("-DCP=" + cp);

        return list;
    }

    private String writeClassesToFile(Set<String> classes) {

        try {
            File file = File.createTempFile("EvoSuite_ctg_CUT_file","txt");
            file.deleteOnExit();

            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            String line = getCommaList(classes);
            out.write(line);
            out.newLine();
            out.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to create tmp file for CUTs specification: "+e.getMessage());
        }
    }

    @NotNull
    private List<String> getMavenCommand(EvoParameters params, Set<String> classes) {
        List<String> list = new ArrayList<String>();
        list.add(params.getMvnLocation());
        list.add("compile");
        list.add("evosuite:generate");
        list.add("-Dcores=" + params.getCores());
        list.add("-DmemoryInMB=" + params.getMemory());
        list.add("-DtimeInMinutesPerClass=" + params.getTime());

        if(classes != null && classes.size() >= 0) {
            if (classes.size() <= 10) {
                String cuts = getCommaList(classes);
                list.add("-Dcuts=" + cuts);
            } else {
                String filePath = writeClassesToFile(classes);
                list.add("-DcutsFile="+filePath);
            }
        }

        list.add("evosuite:export"); //note, here -Dctg_export_folder would do as well
        list.add("-DtargetFolder=" + params.getFolder());
        return list;
    }

    private String getCommaList(Set<String> set){
        if (set != null && !set.isEmpty()) {
            StringBuffer s = new StringBuffer("");
            boolean first = true;
            for (String cl : set) {
                if(first){
                    first = false;
                } else {
                    s.append(",");
                }
                s.append(cl);
            }
            return s.toString();
        }
        return null;
    }
}
