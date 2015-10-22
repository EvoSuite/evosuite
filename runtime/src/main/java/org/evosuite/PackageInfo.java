package org.evosuite;

/**
 * Package information should never be hardcoded, as packages can be refactored or shaded.
 * All access should be through reflection or .class variables
 *
 * Created by Andrea Arcuri on 22/10/15.
 */
public class PackageInfo {

    public static String getEvoSuitePackage(){
        return PackageInfo.class.getPackage().getName();
    }

    public static String getEvoSuitePackageWithSlash(){
        return getEvoSuitePackage().replace(".","/");
    }

    /**
     * The package were third-party libraries are shaded into
     * @return
     */
    public static String getShadedPackage(){
        return getEvoSuitePackage() + ".shaded";
    }

    public static String getNameWithSlash(Class<?> klass){
        return klass.getName().replace(".","/");
    }

    //TODO
    //public static String get
}
