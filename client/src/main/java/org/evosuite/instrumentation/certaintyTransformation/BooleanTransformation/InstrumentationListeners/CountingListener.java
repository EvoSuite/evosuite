package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation.InstrumentationListeners;

import org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation.BooleanToIntMethodVisitor;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.ConditionalJumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.MethodIdentifier;

public class CountingListener implements InstrumentationListener{

    private int conditionalJumpCount = 0;
    private int helperMethodCount = 0;
    private int instrumentationStartedCount = 0;
    private int instrumentationFinishedCount = 0;
    private int dependentUpdateCount = 0;
    private int flagJumpCount = 0;
    private int innerClasses = 0;
    private int methodIdentifierChanges = 0;


    @Override
    public void notifyConditionalJump(ConditionalJumpInstruction instruction) {
        conditionalJumpCount++;
    }

    @Override
    public void notifyHelperMethod(MethodIdentifier identifier) {
        helperMethodCount++;
    }

    @Override
    public void notifyInstrumentationStarted(String classIdentifier) {
        instrumentationStartedCount++;
    }

    @Override
    public void notifyInstrumentationFinished(String classIdentifier) {
        instrumentationFinishedCount++;
    }

    @Override
    public void notifyMethodIdentifierChange(MethodIdentifier from, MethodIdentifier to) {
        methodIdentifierChanges++;
    }

    @Override
    public void notifyInnerClass(String name) {
        innerClasses++;
    }

    @Override
    public void notifyDependentUpdate(BooleanToIntMethodVisitor.DependentUpdate dependentUpdate) {
        dependentUpdateCount++;
    }

    @Override
    public void notifyFlagJump(JumpInstruction jumpInstruction) {
        flagJumpCount++;
    }


    public int getConditionalJumpCount() {
        return conditionalJumpCount;
    }

    public int getHelperMethodCount() {
        return helperMethodCount;
    }

    public int getInstrumentationStartedCount() {
        return instrumentationStartedCount;
    }

    public int getInstrumentationFinishedCount() {
        return instrumentationFinishedCount;
    }

    public int getDependentUpdateCount() {
        return dependentUpdateCount;
    }

    public int getFlagJumpCount() {
        return flagJumpCount;
    }
}
