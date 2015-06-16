package org.evosuite.runtime.javaee.javax.enterprise.event;


import javax.enterprise.event.Event;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;

/**
 * TODO: for now, we just have an empty stub
 */

/**
 * Created by Andrea Arcuri on 15/06/15.
 */
public class  EvoEvent<T>  implements Event<T> {


    @Override
    public void fire(T t) {
        //TODO
    }

    @Override
    public Event<T> select(Annotation... annotations) {
        //TODO
        return null;
    }

    @Override
    public <U extends T> Event<U> select(Class<U> aClass, Annotation... annotations) {
        //TODO
        return null;
    }

    @Override
    public <U extends T> Event<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
        //TODO
        return null;
    }
}
