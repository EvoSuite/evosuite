package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.SwitchInstructions;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.ByteCodeInstructions.ByteCodeInstruction;
import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation.StackTypeSet;
import org.objectweb.asm.Opcodes;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class LookupSwitchInstruction extends SwitchInstruction {

    private final ByteCodeInstruction def;
    private final Map<Integer, ByteCodeInstruction> destinationMap;

    public LookupSwitchInstruction(String className, String methodName, int lineNumber, String methodDescriptor,int instructionNumber,
                                   int[] keys, ByteCodeInstruction def, List<ByteCodeInstruction> destinations) {
        super(className, methodName, lineNumber, methodDescriptor,"LOOKUPSWITCH", instructionNumber, Opcodes.LOOKUPSWITCH);
        if (keys.length != destinations.size()) {
            throw new IllegalArgumentException("keys and destination sizes not matching");
        }
        this.def = def;
        this.destinationMap =
                IntStream.range(0, keys.length).mapToObj(i -> new AbstractMap.SimpleImmutableEntry<>(keys[i],
                        destinations.get(i))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<Integer, ByteCodeInstruction> getDestinationMap(){
        return new HashMap<>(destinationMap);
    }

    @Override
    public List<ByteCodeInstruction> getDestinations() {
        return new ArrayList<>(destinationMap.values());
    }

    @Override
    public ByteCodeInstruction getDefault() {
        return def;
    }

    @Override
    public Collection<Integer> getSuccessors() {
        List<Integer> result =
                destinationMap.values().stream().map(ByteCodeInstruction::getOrder).collect(Collectors.toList());
        result.add(def.getOrder());
        return result;
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
