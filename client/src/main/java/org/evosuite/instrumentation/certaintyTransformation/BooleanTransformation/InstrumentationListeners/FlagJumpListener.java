package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation.InstrumentationListeners;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;

public interface FlagJumpListener {
    void notifyFlagJump(JumpInstruction jumpInstruction);
}
