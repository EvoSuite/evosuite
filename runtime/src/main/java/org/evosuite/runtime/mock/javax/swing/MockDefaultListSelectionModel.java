package org.evosuite.runtime.mock.javax.swing;

import org.evosuite.runtime.mock.OverrideMock;

import javax.swing.*;
import java.lang.reflect.Field;

/**
 * Created by gordon on 28/01/2016.
 */
public class MockDefaultListSelectionModel extends DefaultListSelectionModel implements OverrideMock {

    public String toString() {
        String s =  ((getValueIsAdjusting()) ? "~" : "=");
        try {
            // Value is private...
            Field f = DefaultListSelectionModel.class.getField("value");
            f.setAccessible(true);
            Object value = f.get(this);
            if(value != null)
                s += value.toString();
        } catch (Throwable t) {
           // ignore
        }
        // Integer.toString(System.identityHashCode(this))  -- Ignoring hash code in string
        return DefaultListSelectionModel.class.getName() + " " +  s;
    }

}
