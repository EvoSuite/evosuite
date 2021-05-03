package org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class VariableTable extends HashSet<VariableLifetime> {

    public VariableTable() {
        super();
    }

    public VariableTable(Collection<? extends VariableLifetime> c) {
        super(c);
    }

    public Set<VariableLifetime> getLifetimesAtLocalVariableIndex(int index){
        return stream().filter(vl -> vl.getIndex() == index).collect(Collectors.toSet());
    }
}
