package com.examples.with.different.packagename.mock.java.util.prefs;

import java.util.prefs.Preferences;

/**
 * Created by gordon on 26/12/2016.
 */
public class PrefsNode {

    public void setPref(String key, String value) {
        Preferences.userNodeForPackage(PrefsNode.class).put(key, value);
    }

    public boolean coverMe(String key) {
        if(Preferences.userNodeForPackage(PrefsNode.class).get(key, null).equals("Foo")) {
            return true;
        } else {
            return false;
        }
    }
}
