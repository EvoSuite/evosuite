package org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations;

import java.util.Collections;

public class ThrowExceptionStackManipulation extends TypeStackManipulation {

    @Override
    public TypeStack apply(TypeStack s) {
        s.clear();
        s.push(StackTypeSet.OBJECT);
        return s;
    }

    @Override
    public TypeStack applyBackwards(TypeStack s) {
        throw new UnsupportedOperationException("Cannot apply backwards");
    }

    @Override
    public FrameLayout apply(FrameLayout frameLayout) {
        return new FrameLayout(Collections.singletonList(StackTypeSet.OBJECT), false);
    }

    @Override
    public FrameLayout applyBackwards(FrameLayout frameLayout) {
        return new FrameLayout(Collections.emptyList(), true);
    }

    @Override
    public FrameLayout computeMinimalBefore() {
        return new FrameLayout(Collections.emptyList(), true);
    }

    @Override
    public FrameLayout computeMinimalAfter() {
        return new FrameLayout(Collections.singletonList(StackTypeSet.OBJECT), false);
    }
}
