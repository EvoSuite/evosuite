package com.examples.with.different.packagename.mock.java.util.prefs;

import java.util.prefs.Preferences;

/**
 * Created by gordon on 26/12/2016.
 */
public class PrefsUser {

    public void setPref(String key, String value) {
        Preferences.userRoot().put(key, value);
    }

    public boolean coverMe(String key) {
        if(Preferences.userRoot().get(key, null).equals("Foo")) {
            return true;
        } else {
            return false;
        }
    }
}
