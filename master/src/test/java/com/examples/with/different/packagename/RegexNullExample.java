package com.examples.with.different.packagename;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gordon on 28/12/2016.
 */
public class RegexNullExample {

    public static boolean testMe(String[] args) {
        Pattern argPattern = Pattern.compile("(--[a-zA-Z_]+)=(.*)");
        try {
            for(String arg : args) {
                Matcher matcher = argPattern.matcher(arg);
                matcher.matches();
            }
            return true;
        } catch(NullPointerException e) {
            return false;
        }
    }
}
