package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation.InstrumentationListeners;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.ConditionalJumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.MethodIdentifier;

public interface InstrumentationListener extends FlagJumpListener,DependentUpdateListener{

    public void notifyConditionalJump(ConditionalJumpInstruction instruction);

    public void notifyHelperMethod(MethodIdentifier identifier);

    public void notifyInstrumentationStarted(String classIdentifier);

    public void notifyInstrumentationFinished(String classIdentifier);

    public void notifyMethodIdentifierChange(MethodIdentifier from, MethodIdentifier to);

    public void notifyInnerClass(String name);
}
