package de.unisb.cs.st.testability;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;


/**
 * Created by Yanchuan Li
 * Date: 12/28/10
 * Time: 12:27 AM
 */
public class ScanProject {

    private static String prefix;
    public static Map<String, ClassUnit> classesMap = new HashMap<String, ClassUnit>();
    public static Map<String, String> methodsMap = new HashMap<String, String>();
    private static Logger log = Logger.getLogger(ScanProject.class);

    public static void main(String[] args) {
        //System.out.println("Scanning project for test suite generation.");
        //MutationProperties.checkProperty(MutationProperties.PROJECT_PREFIX_KEY);
//        prefix = System.getProperty("PROJECT_PREFIX");
        prefix = "org.jdom";
        System.out.println("* Project prefix: " + prefix);
        searchClasses(prefix);
        //TestDetector.scanForTests(prefix);

    }

    public static List<String> getClassSet() {
        List<String> result = new ArrayList<String>();
        for (String s : classesMap.keySet()) {
            result.add(s);
        }
        return result;
    }

    public static List<Class<?>> findClasses(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        FileWriter writer;

        for (File file : files) {
//            System.out.println("* File: " + file);
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                try {
                    String classname = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    log.debug("class:" + classname);
                    ClassUnit cu = new ClassUnit(classname, file, false);
                    classesMap.put(classname, cu);
//                    classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                } catch (IllegalAccessError e) {
                    System.out.println("Cannot access class " + packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                } catch (NoClassDefFoundError e) {
                    System.out.println("Cannot find class " + packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                } catch (ExceptionInInitializerError e) {
                    System.out.println("Exception in initializer of " + packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                }
            }
        }
        return classes;
    }

    public static Class<?>[] searchClasses(String packageName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = null;
        try {
            resources = classLoader.getResources(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));

        }
        return classes.toArray(new Class[classes.size()]);
    }

}
