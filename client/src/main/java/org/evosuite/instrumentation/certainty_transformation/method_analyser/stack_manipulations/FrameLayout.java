package org.evosuite.instrumentation.certainty_transformation.method_analyser.stack_manipulations;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FrameLayout {
    private final List<StackTypeSet> types;
    private final boolean hasUnknownLeadingTypes;

    public FrameLayout(List<StackTypeSet> acceptingTypes, boolean unknownLeadingTypes) {
        this.types = acceptingTypes;
        this.hasUnknownLeadingTypes = unknownLeadingTypes;
    }

    public List<StackTypeSet> getTypes() {
        return new ArrayList<>(types);
    }

    public boolean hasUnknownLeadingTypes() {
        return hasUnknownLeadingTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FrameLayout that = (FrameLayout) o;

        if (hasUnknownLeadingTypes != that.hasUnknownLeadingTypes) return false;
        return Objects.equals(types, that.types);
    }

    @Override
    public int hashCode() {
        int result = types != null ? types.hashCode() : 0;
        result = 31 * result + (hasUnknownLeadingTypes ? 1 : 0);
        return result;
    }

    public String _toString() {
        return "FrameLayout{" +
                "types=" + types +
                ", hasUnknownLeadingTypes=" + hasUnknownLeadingTypes +
                '}';
    }

    @Override
    public String toString(){
        String s;
        s = types.stream().map(StackTypeSet::toString).collect(Collectors.joining(""));
        return "[" + (hasUnknownLeadingTypes ? "..." : "" )+ s +"]";
    }
}