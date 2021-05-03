package org.evosuite.instrumentation.certainty_transformation.boolean_transformation.instrumentation_listeners;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.jump_instructions.JumpInstruction;

public interface FlagJumpListener {
    void notifyFlagJump(JumpInstruction jumpInstruction);
}
