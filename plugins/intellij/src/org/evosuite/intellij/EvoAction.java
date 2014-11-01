package org.evosuite.intellij;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ToolWindowType;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.evosuite.intellij.util.AsyncGUINotifier;
import org.evosuite.intellij.util.MavenExecutor;

import javax.swing.*;
import java.io.File;
import java.util.*;

/**
 * Created by arcuri on 9/24/14.
 */
public class EvoAction extends AnAction {

    public EvoAction() {
        super("Run EvoSuite");
    }


    public void actionPerformed(AnActionEvent event) {

        String title = "EvoSuite Plugin";
        Project project = event.getData(PlatformDataKeys.PROJECT);

        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.getToolWindow("EvoSuite");

        final AsyncGUINotifier notifier = IntelliJNotifier.getNotifier(project);

        if (MavenExecutor.getInstance().isAlreadyRunning()) {
            Messages.showMessageDialog(project, "An instance of EvoSuite is already running",
                    title, Messages.getErrorIcon());
            return;
        }

        Map<String,List<String>> map = getCUTsToTest(event);
        if(map==null || map.isEmpty()){
            Messages.showMessageDialog(project, "No '.java' file or non-empty source folder was selected in a valid Maven module",
                    title, Messages.getErrorIcon());
            return;
        }

        EvoStartDialog dialog = new EvoStartDialog();
        dialog.initFields(project, EvoParameters.getInstance());
        dialog.setModal(true);
        dialog.setLocationRelativeTo(null);
        //dialog.setLocationByPlatform(true);
        dialog.pack();
        dialog.setVisible(true);

        if (dialog.isWasOK()) {
            toolWindow.show(new Runnable(){@Override public void run(){
                notifier.clearConsole();
            }
            });
            EvoParameters.getInstance().save(project);
            MavenExecutor.getInstance().run(EvoParameters.getInstance(),map,notifier);
        }
    }

    /**
     *
     *
     * @return a map where key is a maven module root path, and value a list of full class names of CUTs
     */
    private Map<String, List<String>> getCUTsToTest(AnActionEvent event){

        Map<String,List<String>> map = new LinkedHashMap<String, List<String>>();

        /*
            full paths of all source root folders.
            this is needed to calculate the Java class names from the .java file paths
         */
        Set<String> roots = new LinkedHashSet<String>();

        Project project = event.getData(PlatformDataKeys.PROJECT);
        String projectDir = project.getBaseDir().getCanonicalPath();

        for (Module module : ModuleManager.getInstance(project).getModules()) {
            for(VirtualFile sourceRoot : ModuleRootManager.getInstance(module).getSourceRoots()){
                String path = sourceRoot.getCanonicalPath();
                if(getMavenModuleFolder(projectDir, path) != null) {
                    roots.add(path);
                }
            }
        }

        if(roots.isEmpty()){
            return null;
        }

        for(VirtualFile virtualFile : event.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)){
            String path = virtualFile.getCanonicalPath();
            String maven = getMavenModuleFolder(projectDir, path);

            if(maven==null){
                //the selected file is not inside any maven folder
                continue;
            }

            String root = getSourceRootForFile(path,roots);
            if(root == null){
                /*
                    the chosen file is not in a source folder.
                    Need to check if its parent of any of them
                 */
                if(isParentOfSourceRoot(path,roots)){
                    /*
                        we chose a parent of a source root, eg src in src/main/java, so take whole module.
                        This is represented by a null list
                     */
                    map.put(maven, null);
                }

                continue;
            }

            if(map.containsKey(maven) && map.get(maven)==null){
                /*
                    special case: we are already covering the whole module, so no point
                    in also specifying single files inside it
                 */
                continue;
            }

            List<String> classes = map.get(maven);
            if(classes == null){
                classes = new ArrayList<String>();
                map.put(maven, classes);
            }

            if(!virtualFile.isDirectory()){
                if(!path.endsWith(".java")){
                    // likely a resource file
                    continue;
                }

                String name = getCUTName(path, root);
                classes.add(name);
            } else {
                scanFolder(virtualFile,classes,root);
            }
        }

        return map;
    }

    private void scanFolder(VirtualFile virtualFile, List<String> classes, String root) {
        for(VirtualFile child : virtualFile.getChildren()){
            if(child.isDirectory()){
                scanFolder(child,classes,root);
            } else {
                String path = child.getCanonicalPath();
                if(path.endsWith(".java")){
                    String name = getCUTName(path,root);
                    classes.add(name);
                }
            }
        }
    }

    private String getCUTName(String path, String root) {
        String name = path.substring(root.length()+1, path.length() - ".java".length());
        name = name.replace('/','.'); //posix
        name = name.replace("\\", ".");  // windows
        return name;
    }

    private boolean isParentOfSourceRoot(String path, Set<String> roots){
        for(String root : roots){
            if(root.startsWith(path)){
                return true;
            }
        }
        return false;
    }

    private String getSourceRootForFile(String path, Set<String> roots){
        for(String root : roots){
            if(path.startsWith(root)){
                return root;
            }
        }
        return null;
    }

    private String getMavenModuleFolder( String projectDir, String source){
        File file = new File(source);
        while(file != null){
            if(! file.getAbsolutePath().startsWith(projectDir)){
                return null; //we went too up in the hierarchy
            }
            if(file.isDirectory()){
                File pom = new File(file,"pom.xml");
                if(pom.exists()){
                    return file.getAbsolutePath();
                }
            }
            file = file.getParentFile();
        }
        return null;
    }
}
