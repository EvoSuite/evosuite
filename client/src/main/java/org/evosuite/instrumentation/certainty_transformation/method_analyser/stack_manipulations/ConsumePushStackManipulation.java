package org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations;

import org.objectweb.asm.Type;

import java.util.*;
import java.util.stream.Collectors;

public class ConsumePushStackManipulation extends TypeStackManipulation {
    private final List<StackTypeSet> consumed;
    private final StackTypeSet pushed;

    public ConsumePushStackManipulation(Collection<StackTypeSet> consumed, StackTypeSet pushed) {
        this.consumed = new ArrayList<>(consumed);
        // Type,, which is pushed to the stack.
        this.pushed = pushed;
    }

    @Override
    public TypeStack apply(TypeStack s) {
        consumed.stream().filter(c -> c != StackTypeSet.VOID).forEach(i -> {
            if (!s.pop().matches(i))
                throw new IllegalStateException("Manipulation can not be applied to given Stack");
        });
        if (pushed != StackTypeSet.VOID)
            s.push(pushed);
        return s;
    }

    @Override
    public TypeStack applyBackwards(TypeStack s) {
        if (pushed != StackTypeSet.VOID && !s.pop().matches(pushed))
            throw new IllegalStateException("Manipulation can not be applied backwards to given Stack");
        s.push(consumed.stream().filter(c -> !c.contains(Type.VOID)).collect(Collectors.toList()), false);
        return s;
    }

    @Override
    public FrameLayout apply(FrameLayout frameLayout) {
        if (frameLayout.getTypes().size() < consumed.size()) {
            throw new IllegalArgumentException("");
        }
        List<StackTypeSet> types = new ArrayList<>(frameLayout.getTypes());
        ListIterator<StackTypeSet> consumedIterator = consumed.listIterator(consumed.size());
        while (consumedIterator.hasPrevious()) {
            StackTypeSet previous = consumedIterator.previous();
            if (!types.remove(types.size() - 1).matches(previous)) {
                throw new IllegalStateException("Manipulation can not be applied to given Frame");
            }
        }
        /*
        consumed.stream().filter(c -> c != StackTypeSet.VOID).forEach(i ->{
            StackTypeSet remove = types.remove(0);
            if(!remove.matches(i))
                throw new IllegalStateException("Manipulation can not be applied to given Frame");
        });*/

        if (pushed != StackTypeSet.VOID)
            types.add(pushed);
        return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
    }

    @Override
    public FrameLayout applyBackwards(FrameLayout frameLayout) {
        List<StackTypeSet> types = new ArrayList<>(frameLayout.getTypes());
        if (pushed != StackTypeSet.VOID && !types.remove(types.size() - 1).matches(pushed))
            throw new IllegalStateException("Manipulation can not be applied backwards to given frame");
        types.addAll(consumed.stream().filter(c -> !c.contains(Type.VOID)).collect(Collectors.toList()));
        return new FrameLayout(types, frameLayout.hasUnknownLeadingTypes());
    }

    @Override
    public FrameLayout computeMinimalBefore() {
        return new FrameLayout(new ArrayList<>(consumed), true);
    }

    @Override
    public FrameLayout computeMinimalAfter() {
        return new FrameLayout(Collections.singletonList(pushed), true);
    }


    @Override
    public String toString() {
        return "ConsumePushStackManipulation{" +
                "consumed=" + consumed +
                ", pushed=" + pushed +
                '}';
    }
}
