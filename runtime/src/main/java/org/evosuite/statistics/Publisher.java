package org.evosuite.statistics;

/**
 * Created by arcuri on 6/8/14.
 */
public interface Publisher {

    public void trackOutputVariable(RuntimeVariable variable, Object value);

}
