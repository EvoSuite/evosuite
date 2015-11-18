package org.evosuite.runtime.classhandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class used to handle static state of JDK API classes we cannot instrument
 *
 * Created by Andrea Arcuri on 08/11/15.
 */
public class JDKClassResetter {

    private static Logger logger = LoggerFactory.getLogger(JDKClassResetter.class);

    private static Map renderingHintsKeyIdentityMap;
    private static Map renderingHintsKeyIdentityMapCopy;


    /**
     * Save current state of all JDK static fields we are going te reset later on
     */
    public static void init(){

        try {
            Field field = RenderingHints.Key.class.getDeclaredField("identitymap");
            field.setAccessible(true);
            renderingHintsKeyIdentityMap = (Map) field.get(null);
            renderingHintsKeyIdentityMapCopy = new LinkedHashMap<>(renderingHintsKeyIdentityMap.size());
            renderingHintsKeyIdentityMapCopy.putAll(renderingHintsKeyIdentityMap);

        } catch (Exception e) {
            //shouldn't really happen
            logger.error("Failed to handle 'identitymap': "+e.toString());
        }

    }

    public static void reset(){

        if(renderingHintsKeyIdentityMap != null){
            renderingHintsKeyIdentityMap.clear();
            renderingHintsKeyIdentityMap.putAll(renderingHintsKeyIdentityMapCopy);
        }
    }
}
