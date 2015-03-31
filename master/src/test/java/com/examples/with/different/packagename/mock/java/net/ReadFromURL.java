package com.examples.with.different.packagename.mock.java.net;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by arcuri on 12/19/14.
 */
public class ReadFromURL {

    public static boolean checkResource() {
        boolean check;
        try {
            URL url = new URL("http://www.evosuite.org/index.html");
            url.openConnection().getInputStream().read();
            check = true;
        } catch (Exception e) {
            check = false;
        }
        if(check) {
            return true;
        } else {
            return false;
        }
    }
}
