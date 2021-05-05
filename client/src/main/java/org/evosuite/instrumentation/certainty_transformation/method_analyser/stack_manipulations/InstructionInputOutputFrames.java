package org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.bytecodeinstructions.ByteCodeInstruction;

import java.util.*;
import java.util.stream.Collectors;

public class InstructionInputOutputFrames {


    private final ByteCodeInstruction instruction;
    private final Map<ByteCodeInstruction, TypeStackManipulation> manipulations;
    private FrameLayout inputFrameLayout;
    private Map<ByteCodeInstruction, FrameLayout> outputFrames;
    private boolean inputFrameChanged = false;
    private Set<ByteCodeInstruction> outputFrameChanged = new HashSet<>();

    public InstructionInputOutputFrames(ByteCodeInstruction instruction, FrameLayout inputFrameLayout,
                                        Map<ByteCodeInstruction, TypeStackManipulation> manipulations) {
        this.instruction = instruction;
        this.inputFrameLayout = inputFrameLayout;
        this.manipulations = manipulations;
        this.outputFrames = new HashMap<>();
        for (Map.Entry<ByteCodeInstruction, TypeStackManipulation> entry : manipulations.entrySet()) {
            outputFrames.put(entry.getKey(), entry.getValue().apply(inputFrameLayout));
        }
    }

    public InstructionInputOutputFrames(ByteCodeInstruction instruction, FrameLayout inputMerged,
                                       Map<ByteCodeInstruction, TypeStackManipulation> manipulations,
                                         Map<ByteCodeInstruction, FrameLayout> outputsMerged,
                                         boolean inputFrameChanged, Set<ByteCodeInstruction> outputFrameChanged) {
        this.instruction = instruction;
        this.inputFrameLayout = inputMerged;
        this.manipulations = manipulations;
        this.outputFrames = outputsMerged;
        this.inputFrameChanged = inputFrameChanged;
        this.outputFrameChanged = outputFrameChanged;
    }

    public boolean hasChanged(){
        return inputFrameChanged || !outputFrameChanged.isEmpty();
    }

    public boolean isInputFrameChanged() {
        return inputFrameChanged;
    }

    public Set<ByteCodeInstruction> getOutputFrameChanged() {
        return new HashSet<>(outputFrameChanged);
    }

    public List<StackTypeSet> getIncoming() {
        return inputFrameLayout.getTypes();
    }

    public FrameLayout getInputFrameLayout() {
        return inputFrameLayout;
    }

    public Map<ByteCodeInstruction, FrameLayout> getOutputFrames() {
        return outputFrames;
    }

    public boolean isUnknownLeadingIncomingTypes() {
        return inputFrameLayout.hasUnknownLeadingTypes();
    }

    /**
     * Merges the other frame into the input frame of this (Not in place).
     *
     * @param other the frame to be merged.
     * @return the new behaviour.
     */
    public InstructionInputOutputFrames updateInput(FrameLayout other) {
        FrameLayout inputMerged = merge(inputFrameLayout, other);
        HashMap<ByteCodeInstruction, FrameLayout> outputsMerged = new HashMap<>();
        for (Map.Entry<ByteCodeInstruction, TypeStackManipulation> entry : manipulations.entrySet()) {
            FrameLayout apply = entry.getValue().apply(inputMerged);
            outputsMerged.put(entry.getKey(), merge(apply, outputFrames.get(entry.getKey())));
        }
        boolean inputFrameChanged = this.inputFrameChanged || !inputFrameLayout.equals(inputMerged);
        HashSet<ByteCodeInstruction> outputFrameChanged = outputsMerged.entrySet().stream().
                filter(e -> !outputFrames.get(e.getKey()).equals(e.getValue()))
                .map(Map.Entry::getKey).collect(Collectors.toCollection(HashSet::new));


        return new InstructionInputOutputFrames(instruction, inputMerged,
                manipulations, outputsMerged, inputFrameChanged, outputFrameChanged);
    }

    public static InstructionInputOutputFrames computeChanges(InstructionInputOutputFrames old, InstructionInputOutputFrames _new){
        boolean inputsChanged = !old.inputFrameLayout.equals(_new.inputFrameLayout);
        Set<ByteCodeInstruction> outputsChanged = old.outputFrames.entrySet().stream().filter(e -> {
            ByteCodeInstruction key = e.getKey();
            FrameLayout oldValue = e.getValue();
            FrameLayout newValue = _new.outputFrames.get(key);
            return !oldValue.equals(newValue);
        }).map(Map.Entry::getKey).collect(Collectors.toSet());
        return new InstructionInputOutputFrames(_new.instruction, _new.inputFrameLayout, _new.manipulations,
                _new.outputFrames,inputsChanged,outputsChanged);
    }

    public ByteCodeInstruction getInstruction() {
        return instruction;
    }

    public InstructionInputOutputFrames updateOutput(FrameLayout other, ByteCodeInstruction destination) {
        FrameLayout outputMerged = merge(other, outputFrames.get(destination));
        // here is a fuck up
        // output merged = [Z]
        // manip.applyBackwards(outputMerged) results to []
        // No changes at all.
        FrameLayout inputMerged = merge(manipulations.get(destination).applyBackwards(outputMerged), inputFrameLayout);
        Map<ByteCodeInstruction, FrameLayout> newOutput = new HashMap<>();
        newOutput.put(destination, outputMerged);
        outputFrames.entrySet().stream().filter(e -> !e.getKey().equals(destination)).forEach(entry -> {
            ByteCodeInstruction key = entry.getKey();
            FrameLayout value = entry.getValue();
            TypeStackManipulation typeStackManipulation = manipulations.get(key);
            newOutput.put(key, merge(typeStackManipulation.apply(inputMerged), value));
        });
        boolean inputFrameChanged = !inputMerged.equals(inputFrameLayout);
        Set<ByteCodeInstruction> outputFrameChanged =
          newOutput.entrySet().stream()
                    .filter(e -> !e.getValue().equals(outputFrames.get(e.getKey())))
                    .map(Map.Entry::getKey).collect(Collectors.toSet());
        return new InstructionInputOutputFrames(instruction,inputMerged,manipulations,newOutput,
                inputFrameChanged, outputFrameChanged);
    }

    /**
     * Computes the output frame that should be merged into the input frame for destination.
     *
     * @param destination the next Instruction.
     * @return the output frame on the edge to the destination instruction.
     */
    public FrameLayout computeOutputFrame(ByteCodeInstruction destination) {
        return outputFrames.get(destination);
    }

    public static FrameLayout merge(FrameLayout first, FrameLayout second) {
        List<StackTypeSet> firstTypes = first.getTypes();
        List<StackTypeSet> secondTypes = second.getTypes();
        List<StackTypeSet> resultTypes = new ArrayList<>();
        ListIterator<StackTypeSet> firstIterator = firstTypes.listIterator(firstTypes.size());
        ListIterator<StackTypeSet> secondIterator = secondTypes.listIterator(secondTypes.size());
        while (firstIterator.hasPrevious() && secondIterator.hasPrevious()) {
            StackTypeSet firstPrevious = firstIterator.previous();
            StackTypeSet secondPrevious = secondIterator.previous();
            Optional<StackTypeSet> merge = merge(firstPrevious, secondPrevious);
            if (!merge.isPresent()) {
                throw new IllegalArgumentException("Frames can not be merged: first=" +first + " second="+second);
            }
            resultTypes.add(merge.get());
        }
        if (firstIterator.hasPrevious()) {
            if (!second.hasUnknownLeadingTypes())
                throw new IllegalArgumentException("Frames can not be merged: first=" +first + " second="+second);
            else {
                while (firstIterator.hasPrevious())
                    resultTypes.add(firstIterator.previous());
            }
        } else if (secondIterator.hasPrevious()) {
            if (!first.hasUnknownLeadingTypes())
                throw new IllegalArgumentException("Frames can not be merged");
            else
                while (secondIterator.hasPrevious())
                    resultTypes.add(secondIterator.previous());
        }
        Collections.reverse(resultTypes);
        return new FrameLayout(resultTypes, first.hasUnknownLeadingTypes() && second.hasUnknownLeadingTypes());
    }

    public static Optional<StackTypeSet> merge(StackTypeSet first, StackTypeSet second) {
        if(first.equals(StackTypeSet.OBJECT) && second.equals(StackTypeSet.ARRAY)){
            return Optional.of(StackTypeSet.AO);
        }
        if(first.equals(StackTypeSet.ARRAY) && second.equals(StackTypeSet.OBJECT)){
            return Optional.of(StackTypeSet.AO);
        }
        Set<Integer> intersection = first.intersection(second);
        if (intersection.isEmpty())
            return Optional.empty();
        return Optional.of(StackTypeSet.of(intersection));
    }

    public static FrameLayout merge(FrameLayout[] args){
        return Arrays.stream(args).reduce(new FrameLayout(Collections.emptyList(), true),
                InstructionInputOutputFrames::merge);
    }

    @Override
    public String toString() {
        return "InstructionInputOutputFrames{" +
                "instruction=" + instruction +
                ", manipulations=" + manipulations +
                ", inputFrameLayout=" + inputFrameLayout +
                ", outputFrames=" + outputFrames +
                ", inputFrameChanged=" + inputFrameChanged +
                ", outputFrameChanged=" + outputFrameChanged +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstructionInputOutputFrames that = (InstructionInputOutputFrames) o;

        if (inputFrameChanged != that.inputFrameChanged) return false;
        if (instruction != null ? !instruction.equals(that.instruction) : that.instruction != null) return false;
        if (manipulations != null ? !manipulations.equals(that.manipulations) : that.manipulations != null)
            return false;
        if (inputFrameLayout != null ? !inputFrameLayout.equals(that.inputFrameLayout) : that.inputFrameLayout != null)
            return false;
        if (outputFrames != null ? !outputFrames.equals(that.outputFrames) : that.outputFrames != null) return false;
        return outputFrameChanged != null ? outputFrameChanged.equals(that.outputFrameChanged) : that.outputFrameChanged == null;
    }

    @Override
    public int hashCode() {
        int result = instruction != null ? instruction.hashCode() : 0;
        result = 31 * result + (manipulations != null ? manipulations.hashCode() : 0);
        result = 31 * result + (inputFrameLayout != null ? inputFrameLayout.hashCode() : 0);
        result = 31 * result + (outputFrames != null ? outputFrames.hashCode() : 0);
        result = 31 * result + (inputFrameChanged ? 1 : 0);
        result = 31 * result + (outputFrameChanged != null ? outputFrameChanged.hashCode() : 0);
        return result;
    }

    public InstructionInputOutputFrames resetChanged() {
        return new InstructionInputOutputFrames(instruction, inputFrameLayout, manipulations, outputFrames, false,
                Collections.emptySet());
    }
}
