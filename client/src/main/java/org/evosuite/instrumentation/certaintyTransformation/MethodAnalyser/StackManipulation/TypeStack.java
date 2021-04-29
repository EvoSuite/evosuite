package org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.StackManipulation;

import org.evosuite.instrumentation.certaintyTransformation.MethodAnalyser.Results.Variables.VariableTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * keeps track of the type layout of a program
 */
public class TypeStack extends Stack<StackTypeSet> {

    private final VariableTable table;

    public TypeStack(VariableTable table){
        super();
        this.table = table;
    }

    public StackTypeSet getOrDefault(int index, Integer def){
        if(index < size())
            return get(index);
        return StackTypeSet.of(def);
    }

    public void push(List<StackTypeSet> integers, boolean reversed){
        if(reversed){
            List<StackTypeSet> ints = new ArrayList<>(integers);
            push(ints,false);
            return;
        }
        integers.forEach(this::push);
    }
}
