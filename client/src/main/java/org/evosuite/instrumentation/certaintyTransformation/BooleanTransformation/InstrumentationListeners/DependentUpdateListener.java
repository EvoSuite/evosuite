package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation.InstrumentationListeners;

import org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation.BooleanToIntMethodVisitor;

public interface DependentUpdateListener {
    void notifyDependentUpdate(BooleanToIntMethodVisitor.DependentUpdate dependentUpdate);
}
