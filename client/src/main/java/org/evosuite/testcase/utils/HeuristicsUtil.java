package org.evosuite.testcase.utils;
import java.util.ArrayList;

public class HeuristicsUtil {
    /**
     * List of particles of a method name that can be excluded or avoided when syggesting names
     */
    private static ArrayList<String> avoidableParticles = new ArrayList<String>(){
        {
            add("get");
            add("to");
            add("has");
            add("is");
            add("are");
        }
    };

    /**
     * Returns a boolean value that indicates if the first word of a method can be avoided/excluded
     * on method name suggestion
     * @return boolean
     */

    public static boolean containsAvoidableParticle(String firstWord){
        return avoidableParticles.contains(firstWord);
    }
    /**
     * Separates camelcase strings and retrieves the parts in a list
     * @return ArrayList<String>
     */
    public static ArrayList<String> separateByCamelCase(String name){
        ArrayList<String> separatedName = new ArrayList<>();
        for (String word : name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            separatedName.add(word);
        }
        return separatedName;
    }

}
