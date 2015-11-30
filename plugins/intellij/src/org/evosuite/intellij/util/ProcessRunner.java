package org.evosuite.intellij.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
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

/**
 * Created by Andrea Arcuri on 28/11/15.
 */
public class ProcessRunner {


    public static Process execute(Project project, AsyncGUINotifier notifier, EvoParameters params, File dir, Set<String> classes) {

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

    private static List<String> getEvoJarCommand( Project project, File dir, EvoParameters params, Set<String> classes) throws IllegalArgumentException{
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

        //the memory is per core
        list.add("-Dctg_memory="+ (params.getMemory() * params.getCores()));
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
        String path = writeLineToFile("EvoSuite_ctg_classpath_file", cp);
        list.add("-DCP_file_path="+path);
        //list.add("-DCP=" + cp);//this did not work properly on Windows

        return list;
    }

    private static String writeClassesToFile(Set<String> classes) {
        return writeLineToFile("EvoSuite_ctg_CUT_file", getCommaList(classes));
    }

    private static String writeLineToFile(String fileName, String line) {

        try {
            File file = File.createTempFile(fileName,".txt");
            file.deleteOnExit();

            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write(line);
            out.newLine();
            out.close();

            return file.getAbsolutePath();

        } catch (Exception e) {
            throw new IllegalStateException("Failed to create tmp file: "+e.getMessage());
        }
    }

    @NotNull
    private static List<String> getMavenCommand(EvoParameters params, Set<String> classes) {
        List<String> list = new ArrayList<String>();
        list.add(params.getMvnLocation());
        list.add("compile");
        list.add("evosuite:generate");
        list.add("-Dcores=" + params.getCores());
        //the memory is per core
        list.add("-DmemoryInMB=" + (params.getMemory() * params.getCores()));
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

    private static String getCommaList(Set<String> set){
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
