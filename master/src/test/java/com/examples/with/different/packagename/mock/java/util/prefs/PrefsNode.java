/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
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
        if (Preferences.userNodeForPackage(PrefsNode.class).get(key, null).equals("Foo")) {
            return true;
        } else {
            return false;
        }
    }
}
