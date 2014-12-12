package org.evosuite.runtime.testdata;

import org.evosuite.runtime.RuntimeSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * When using a mocked environment, new methods will be added to the test cluster to
 * manipulate the environment. Those methods might take data as input (eg String).
 * However, we might want to put constraints on what data is used, and how it is
 * manipulated by the search operators (eg mutation operators).
 *
 * Created by arcuri on 12/11/14.
 */
public class EnvironmentDataList {

    public static List<Class<?>> getListOfClasses(){
        List<Class<?>> classes = new ArrayList<>();

        if(RuntimeSettings.useVFS) {
            classes.add(EvoSuiteFile.class);
        }

        if(RuntimeSettings.useVNET){
            classes.add(EvoSuiteAddress.class);
        }

        return classes;
    }

}
