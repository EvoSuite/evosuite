package org.evosuite.runtime.testdata;

import java.io.Serializable;

/**
 * Created by Andrea Arcuri on 06/12/15.
 */
public class EvoName implements Serializable {

    private static final long serialVersionUID = 1734299467948600797L;

    private final String name;


    public EvoName(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString(){
        return getName();
    }
}
