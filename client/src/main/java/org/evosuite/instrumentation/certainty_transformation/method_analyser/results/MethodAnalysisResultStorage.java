package org.evosuite.instrumentation.certainty_transformation.method_analyser.results;

import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.graph.ControlFlowGraph;
import org.evosuite.instrumentation.certainty_transformation.method_analyser.results.variables.VariableTable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MethodAnalysisResultStorage extends HashMap<MethodIdentifier, MethodAnalysisResult> {

    public MethodAnalysisResultStorage(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public MethodAnalysisResultStorage(int initialCapacity) {
        super(initialCapacity);
    }

    public MethodAnalysisResultStorage() {
        super();
    }

    public MethodAnalysisResultStorage(Map<? extends MethodIdentifier, ? extends MethodAnalysisResult> m) {
        super(m);
    }

    public MethodAnalysisResultStorage(Collection<Entry<MethodIdentifier, MethodAnalysisResult>> cfgs) {
        this();
        cfgs.forEach(entry -> super.put(entry.getKey(), entry.getValue()));
    }

    public Set<Entry<MethodIdentifier, ControlFlowGraph>> getCfgEntrySetForClass(String internalName) {
        Predicate<Entry<MethodIdentifier, ControlFlowGraph>> filterCondition = entry -> entry.getKey().matchesClass(internalName);
        return getFilteredCfgEntrySet(filterCondition);
    }

    public Set<Entry<MethodIdentifier, ControlFlowGraph>> getFilteredCfgEntrySet(
            Predicate<Entry<MethodIdentifier, ControlFlowGraph>> filter) {
        return getControlFlowGraphMap().entrySet().stream().filter(filter).collect(Collectors.toSet());
    }

    public Map<MethodIdentifier, ControlFlowGraph> getControlFlowGraphMap(){
        HashMap<MethodIdentifier, ControlFlowGraph> map = new HashMap<>();
        super.forEach((key, value) -> map.put(key, value.getControlFlowGraph()));
        return map;
    }

    public Map<MethodIdentifier, VariableTable> getVariableTableMap(){
        HashMap<MethodIdentifier, VariableTable> map = new HashMap<>();
        super.forEach((key, value) -> map.put(key, value.getVariableTable()));
        return map;
    }
}
