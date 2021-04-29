package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.TABLESWITCH;

public class TableSwitchInstruction extends SwitchInstruction {
    private final ByteCodeInstruction def;
    private final List<ByteCodeInstruction> destinations;
    private final int min;
    private final int max;

    public TableSwitchInstruction(String className,
                                  String methodName,
                                  int lineNumber,String methodDescriptor,
                                  int instructionNumber,
                                  int min,
                                  int max,
                                  ByteCodeInstruction def,
                                  Collection<ByteCodeInstruction> destinations) {
        super(className, methodName, lineNumber,methodDescriptor, "TABLESWITCH", instructionNumber, TABLESWITCH);
        this.min = min;
        this.max = max;
        if (destinations.size() != (max - min + 1))
            throw new IllegalArgumentException("Unmatching length of destinations");
        this.def = def;
        this.destinations = new ArrayList<>(destinations);
    }

    public ByteCodeInstruction getDefault(){
        return def;
    }

    public List<ByteCodeInstruction> getDestinations(){
        return new ArrayList<>(destinations);
    }

    @Override
    public Collection<Integer> getSuccessors() {
        Collection<Integer> successors =
                destinations.stream().map(ByteCodeInstruction::getOrder).collect(Collectors.toSet());
        successors.add(def.getOrder());
        return successors;
    }

    @Override
    public List<StackTypeSet> consumedFromStack() {
        return Collections.singletonList(StackTypeSet.TWO_COMPLEMENT);
    }

    @Override
    public StackTypeSet pushedToStack() {
        return StackTypeSet.VOID;
    }

}
