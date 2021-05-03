package org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations;

public abstract class TypeStackManipulation {

    /**
     * applies the defined manipulation to a given stack
     * @param s the stack on which the manipulation should be applied
     * @return the new TypeStack. May return {@param s}
     */
    public abstract TypeStack apply(TypeStack s);


    /**
     * undoes the defined manipulation to a given stack
     * @param s the stack on which the manipulation should be applied
     * @return the new TypeStack. May return {@param s}
     */
    public abstract TypeStack applyBackwards(TypeStack s);

    /**
     * applies the defined manipulation to a given Frame.
     *
     * @param frameLayout
     * @return
     */
    public abstract FrameLayout apply(FrameLayout frameLayout);

    public abstract FrameLayout applyBackwards(FrameLayout frameLayout);

    public abstract FrameLayout computeMinimalBefore();
    public abstract FrameLayout computeMinimalAfter();
}
