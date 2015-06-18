package org.evosuite.runtime.javaee.injection;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Provide a list of JavaEE tags which we handle for dependency injection
 *
 * Created by Andrea Arcuri on 30/05/15.
 */
public class InjectionList {

    private static final List<Class<? extends Annotation>> list =
            Collections.unmodifiableList(Arrays.<Class<? extends Annotation>>asList(
        javax.inject.Inject.class,
        javax.persistence.PersistenceContext.class,
        javax.persistence.PersistenceUnit.class
    ));

    public static List<Class<? extends Annotation>> getList(){
        return list;
    }

}
