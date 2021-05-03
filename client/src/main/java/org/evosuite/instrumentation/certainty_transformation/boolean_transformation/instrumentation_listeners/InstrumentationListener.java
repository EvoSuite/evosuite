package org.evosuite.instrumentation.certainty_transformation.boolean_transformation.instrumentation_listeners;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.ConditionalJumpInstruction;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.MethodIdentifier;

public interface InstrumentationListener extends FlagJumpListener,DependentUpdateListener{

    public void notifyConditionalJump(ConditionalJumpInstruction instruction);

    public void notifyHelperMethod(MethodIdentifier identifier);

    public void notifyInstrumentationStarted(String classIdentifier);

    public void notifyInstrumentationFinished(String classIdentifier);

    public void notifyMethodIdentifierChange(MethodIdentifier from, MethodIdentifier to);

    public void notifyInnerClass(String name);
}
