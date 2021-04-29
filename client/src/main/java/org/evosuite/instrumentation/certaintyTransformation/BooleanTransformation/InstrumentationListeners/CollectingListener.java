package org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation.InstrumentationListeners;

import org.evosuite.instrumentation.certaintyTransformation.BooleanTransformation.BooleanToIntMethodVisitor;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.ConditionalJumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.JumpInstructions.JumpInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.MethodIdentifier;

import java.util.*;

public class CollectingListener implements InstrumentationListener {

    private Set<ConditionalJumpInstruction> conditionalJumpInstructions = new HashSet<>();
    private Set<MethodIdentifier> helperMethods = new HashSet<>();
    private Set<String> instrumentationStarted = new HashSet<>();
    private Set<String> instrumentationFinished = new HashSet<>();
    private Set<BooleanToIntMethodVisitor.DependentUpdate> dependentUpdates = new HashSet<>();
    private Set<JumpInstruction> flagJumps = new HashSet<>();
    private Set<String> innerClasses = new HashSet<>();
    private Map<MethodIdentifier, MethodIdentifier> methodIdentifierChanges = new HashMap<>();

    @Override
    public void notifyConditionalJump(ConditionalJumpInstruction instruction) {
        conditionalJumpInstructions.add(instruction);
    }

    @Override
    public void notifyHelperMethod(MethodIdentifier identifier) {
        helperMethods.add(identifier);
    }

    @Override
    public void notifyInstrumentationStarted(String classIdentifier) {
        instrumentationStarted.add(classIdentifier);
    }

    @Override
    public void notifyInstrumentationFinished(String classIdentifier) {
        instrumentationFinished.add(classIdentifier);
    }

    @Override
    public void notifyMethodIdentifierChange(MethodIdentifier from, MethodIdentifier to) {
        methodIdentifierChanges.put(to, from);
    }

    @Override
    public void notifyDependentUpdate(BooleanToIntMethodVisitor.DependentUpdate dependentUpdate) {
        dependentUpdates.add(dependentUpdate);
    }

    @Override
    public void notifyFlagJump(JumpInstruction jumpInstruction) {
        flagJumps.add(jumpInstruction);
    }

    @Override
    public void notifyInnerClass(String name) {
        innerClasses.add(name);
    }

    /**
     * @return view of all conditional jumps
     */
    public Set<ConditionalJumpInstruction> getConditionalJumpInstructions() {
        return Collections.unmodifiableSet(conditionalJumpInstructions);
    }

    /**
     * @return view of the map (instrumented identifier) -> (original identifier)
     */
    public Map<MethodIdentifier, MethodIdentifier> getMethodIdentifierChanges() {
        return Collections.unmodifiableMap(methodIdentifierChanges);
    }

    /**
     * @return view of all helper methods
     */
    public Set<MethodIdentifier> getHelperMethods() {
        return Collections.unmodifiableSet(helperMethods);
    }

    /**
     * @return view of started instrumentations
     */
    public Set<String> getInstrumentationStarted() {
        return Collections.unmodifiableSet(instrumentationStarted);
    }

    /**
     * @return view of finished instrumentations
     */
    public Set<String> getInstrumentationFinished() {
        return Collections.unmodifiableSet(instrumentationFinished);
    }

    /**
     * @return view of dependent updates
     */
    public Set<BooleanToIntMethodVisitor.DependentUpdate> getDependentUpdates() {
        return Collections.unmodifiableSet(dependentUpdates);
    }

    /**
     * @return view of flag jumps
     */
    public Set<JumpInstruction> getFlagJumps() {
        return Collections.unmodifiableSet(flagJumps);
    }

    public Set<String> getInnerClasses() {
        return Collections.unmodifiableSet(innerClasses);
    }
}
