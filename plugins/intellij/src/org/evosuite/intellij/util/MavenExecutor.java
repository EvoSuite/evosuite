package org.evosuite.intellij.util;

import com.intellij.openapi.ui.Messages;
import org.evosuite.intellij.EvoParameters;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Singleton used to run EvoSuite Maven plugin on a background process
 * <p/>
 * Created by arcuri on 9/29/14.
 */
public class MavenExecutor {

    private static MavenExecutor singleton = new MavenExecutor();

    private volatile Process process; //TODO implement graceful shutdown

    private volatile Thread thread;

    private MavenExecutor() {
    }

    public static MavenExecutor getInstance() {
        return singleton;
    }


    public boolean isAlreadyRunning() {
        if (thread != null && thread.isAlive()) {
            return true;
        }
        return false;
    }


    /**
     * @param params
     * @param suts   map from Maven module folder to list of classes to tests.
     *               If this latter list is null/empty, then use all classes
     *               in that module
     * @throws IllegalArgumentException
     */
    public synchronized void run(final EvoParameters params, final Map<String, List<String>> suts, final AsyncGUINotifier notifier)
            throws IllegalArgumentException {

        if (suts == null || suts.isEmpty()) {
            throw new IllegalArgumentException("No specified classes to test");
        }

        if (params == null) {
            throw new IllegalArgumentException("No defined parameters");
        }

        //check validity of maven modules
        for (String modulePath : suts.keySet()) {
            File dir = new File(modulePath);
            if (!dir.exists()) {
                throw new IllegalArgumentException("Target module folder does not exist: " + modulePath);
            }
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("Target module folder is not a folder: " + modulePath);
            }
            File pom = new File(dir, "pom.xml");
            if (!pom.exists()) {
                throw new IllegalArgumentException("Target module folder does not contain a pom.xml file: " + modulePath);
            }
        }

        if (!isAlreadyRunning()) {
            return;
        }

        thread = new Thread() {
            @Override
            public void run() {
                for(String modulePath : suts.keySet()){
                    File dir = new File(modulePath);
                    //should be on background process
                    Process p = execute(params, dir, suts.get(modulePath));
                    int res = 0;
                    try {
                        /*
                            this is blocking, which is fine, as we want it
                            to run till completion, unless manually stopped
                         */
                        res = p.waitFor();
                    } catch (InterruptedException e) {
                        p.destroy();
                        break;
                    }
                    if(res != 0){
                        notifier.failed("EvoSuite ended abruptly");
                        break;
                    }
                }
                notifier.success("EvoSuite run is completed");
            }
        };
        thread.start();
    }

    private Process execute(EvoParameters params, File dir, List<String> classes) {

        System.out.println("Going to execute command");
        List<String> list = Arrays.asList(
                "mvn",
                "compile",
                "evosuite:generate",
                "-Dcores="+params.cores,
                "-DmemoryInMB="+params.memory,
                "-DtimeInMinutesPerClass="+params.time
        );

        if(classes != null && !classes.isEmpty()){
            String s = classes.get(0).trim();
            for(int i=1; i<classes.size(); i++){
                s += ","+classes.get(i).trim();
            }
            list.add("-Dcuts="+s);
        }

        list.add("evosuite:export");
        list.add("-DtargetFolder="+params.folder);

        String[] command = list.toArray(new String[list.size()]);


        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.directory(dir);
            builder.inheritIO();
            builder.command(command);

            //TODO should redirect to an IntelliJ panel

            return builder.start();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }
}
