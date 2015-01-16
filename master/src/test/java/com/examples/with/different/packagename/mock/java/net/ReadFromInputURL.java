package com.examples.with.different.packagename.mock.java.net;

import java.net.URL;

/**
 * Created by arcuri on 1/16/15.
 */
public class ReadFromInputURL {

    public boolean checkResource(URL url) {
        if(url == null){
            return false;
        }
        boolean check;
        try {
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
