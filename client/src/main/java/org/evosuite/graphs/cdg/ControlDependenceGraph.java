/*
 * Copyright (C) 2010-2018 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3.0 of the License, or
 * (at your option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.graphs.cdg;

import org.evosuite.coverage.branch.Branch;
import org.evosuite.graphs.EvoSuiteGraph;
import org.evosuite.graphs.cfg.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.Set;

public class ControlDependenceGraph extends EvoSuiteGraph<BasicBlock, ControlFlowEdge> {

    private static final Logger logger = LoggerFactory.getLogger(ControlDependenceGraph.class);

    private final ActualControlFlowGraph cfg;

    private final String className;
    private final String methodName;

    /**
     * <p>Constructor for ControlDependenceGraph.</p>
     *
     * @param cfg a {@link org.evosuite.graphs.cfg.ActualControlFlowGraph} object.
     */
    public ControlDependenceGraph(ActualControlFlowGraph cfg) {
        super(ControlFlowEdge.class);

        this.cfg = cfg;
        this.className = cfg.getClassName();
        this.methodName = cfg.getMethodName();

        computeGraph();
        // TODO check sanity
    }

    /**
     * Convenience method redirecting to getControlDependentBranches(BasicBlock)
     * if the given instruction is known to this CDG. Otherwise an
     * IllegalArgumentException will be thrown.
     *
     * Should no longer be used: rather ask a BasicBlock for its CDs, so it can
     * cache it.
     */
    // public Set<ControlDependency>
    // getControlDependentBranches(BytecodeInstruction ins) {
    // if (ins == null)
    // throw new IllegalArgumentException("null not accepted");
    // if (!knowsInstruction(ins))
    // throw new IllegalArgumentException(
    // "instruction not known to this CDG: " + methodName
    // + ins.toString());
    //
    // BasicBlock insBlock = ins.getBasicBlock();
    //
    // return getControlDependentBranches(insBlock);
    // }

    /**
     * Checks whether this graph knows the given instruction. That is there is a
     * BasicBlock in this graph's vertexSet containing the given instruction.
     *
     * @param ins a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @return a boolean.
     */
    public boolean knowsInstruction(BytecodeInstruction ins) {
        return cfg.knowsInstruction(ins);
    }

    /**
     * <p>getControlDependenceDepth</p>
     *
     * @param dependence a {@link org.evosuite.graphs.cfg.ControlDependency} object.
     * @return a int.
     */
    public int getControlDependenceDepth(ControlDependency dependence) {
        int min = Integer.MAX_VALUE;
        for (BasicBlock root : determineEntryPoints()) {
            int distance = getDistance(root,
                    dependence.getBranch().getInstruction().getBasicBlock());
            if (distance < min)
                min = distance;
        }
        return min;
    }

    /**
     * <p>getAlternativeBlocks</p>
     *
     * @param dependency a {@link org.evosuite.graphs.cfg.ControlDependency} object.
     * @return a {@link java.util.Set} object.
     */
    public Set<BasicBlock> getAlternativeBlocks(ControlDependency dependency) {
        Set<BasicBlock> blocks = new LinkedHashSet<>();
        Branch branch = dependency.getBranch();

        BasicBlock block = branch.getInstruction().getBasicBlock();
        for (ControlFlowEdge e : outgoingEdgesOf(block)) {
            // TODO: Why can this be null?
            if (e.getControlDependency() == null
                    || e.getControlDependency().equals(dependency))
                continue;
            BasicBlock next = getEdgeTarget(e);
            blocks.add(next);
            getReachableBasicBlocks(blocks, next);
            //			blocks.addAll(getReachableBasicBlocks(next));
        }
        return blocks;
    }

    private void getReachableBasicBlocks(Set<BasicBlock> blocks, BasicBlock start) {
        for (ControlFlowEdge e : outgoingEdgesOf(start)) {
            BasicBlock next = getEdgeTarget(e);
            if (!blocks.contains(next)) {
                blocks.add(next);
                getReachableBasicBlocks(blocks, next);
                //				blocks.addAll(getReachableBasicBlocks(next));
            }
        }
        //		return blocks;
    }

    /**
     * Returns a Set containing all Branches the given BasicBlock is control
     * dependent on.
     * <p>
     * This is for each incoming ControlFlowEdge of the given block within this
     * CDG, the branch instruction of that edge will be added to the returned
     * set.
     *
     * @param insBlock a {@link org.evosuite.graphs.cfg.BasicBlock} object.
     * @return a {@link java.util.Set} object.
     */
    public Set<ControlDependency> getControlDependentBranches(BasicBlock insBlock) {
        if (insBlock == null)
            throw new IllegalArgumentException("null not accepted");
        if (!containsVertex(insBlock))
            throw new IllegalArgumentException("unknown block: " + insBlock.getName());

        if (insBlock.hasControlDependenciesSet())
            return insBlock.getControlDependencies();

        Set<ControlDependency> r = retrieveControlDependencies(insBlock,
                new LinkedHashSet<>());

        return r;
    }

    private Set<ControlDependency> retrieveControlDependencies(BasicBlock insBlock,
                                                               Set<ControlFlowEdge> handled) {

        Set<ControlDependency> r = new LinkedHashSet<>();

        for (ControlFlowEdge e : incomingEdgesOf(insBlock)) {
            if (handled.contains(e))
                continue;
            handled.add(e);

            ControlDependency cd = e.getControlDependency();
            if (cd != null)
                r.add(cd);
            else {
                BasicBlock in = getEdgeSource(e);
                if (!in.equals(insBlock))
                    r.addAll(retrieveControlDependencies(in, handled));
            }

        }

        // TODO need RootBranch Object!!!
        // TODO the following does not hold! a node can be dependent on the root
        // branch AND another branch! TODO !!!
        // // sanity check
        // if (r.isEmpty()) {
        // Set<BasicBlock> insParents = getParents(insBlock);
        // if (insParents.size() != 1) {
        //
        // for (BasicBlock b : insParents)
        // logger.error(b.toString());
        //
        // throw new IllegalStateException(
        // "expect instruction dependent on root branch to have exactly one parent in it's CDG namely the EntryBlock: "
        // + insBlock.toString());
        // }
        //
        // for (BasicBlock b : insParents)
        // if (!b.isEntryBlock() && !getControlDependentBranches(b).isEmpty())
        // throw new IllegalStateException(
        // "expect instruction dependent on root branch to have exactly one parent in it's CDG namely the EntryBlock"
        // + insBlock.toString() + methodName);
        // }

        return r;
    }

    /**
     * <p>getControlDependentBranchIds</p>
     *
     * @param ins a {@link org.evosuite.graphs.cfg.BasicBlock} object.
     * @return a {@link java.util.Set} object.
     */
    public Set<Integer> getControlDependentBranchIds(BasicBlock ins) {

        Set<ControlDependency> dependentBranches = getControlDependentBranches(ins);

        Set<Integer> r = new LinkedHashSet<>();

        for (ControlDependency cd : dependentBranches) {
            if (cd == null)
                throw new IllegalStateException(
                        "expect set returned by getControlDependentBranches() not to contain null");

            r.add(cd.getBranch().getActualBranchId());
        }

        // to indicate this is only dependent on root branch,
        // meaning entering the method
        if (isRootDependent(ins))
            r.add(-1);

        return r;
    }

    // /**
    // * Determines whether the given Branch has to be evaluated to true or to
    // * false in order to reach the given BytecodeInstruction - given the
    // * instruction is directly control dependent on the given Branch.
    // *
    // * In other words this method checks whether there is an incoming
    // * ControlFlowEdge to the given instruction's BasicBlock containing the
    // * given Branch as it's BranchInstruction and if so, that edges
    // * branchExpressionValue is returned. If the given instruction is directly
    // * control dependent on the given branch such a ControlFlowEdge must
    // exist.
    // * Should this assumption be violated an IllegalStateException is thrown.
    // *
    // * If the given instruction is not known to this CDG or not directly
    // control
    // * dependent on the given Branch an IllegalArgumentException is thrown.
    // */
    // public boolean getBranchExpressionValue(BytecodeInstruction ins, Branch
    // b) {
    // if (ins == null)
    // throw new IllegalArgumentException("null given");
    // if (!ins.isDirectlyControlDependentOn(b))
    // throw new IllegalArgumentException(
    // "only allowed to call this method for instructions and their directly control dependent branches");
    // if (b == null)
    // return true; // root branch special case
    //
    // BasicBlock insBlock = ins.getBasicBlock();
    //
    // for (ControlFlowEdge e : incomingEdgesOf(insBlock)) {
    // if (e.isExceptionEdge() && !e.hasControlDependency())
    // continue;
    //
    // Branch current = e.getBranchInstruction();
    // if (current == null) {
    // try {
    // BasicBlock in = getEdgeSource(e);
    // return getBranchExpressionValue(in.getFirstInstruction(), b);
    // } catch (Exception ex) {
    // continue;
    // }
    // } else if (current.equals(b))
    // return e.getBranchExpressionValue();
    // }
    //
    // throw new IllegalStateException(
    // "expect CDG to contain an incoming edge to the given instructions basic block containing the given branch if isControlDependent() returned true on those two ");
    // }

    // initialization

    /**
     * Determines whether the given BytecodeInstruction is directly control
     * dependent on the given Branch. It's BasicBlock is control dependent on
     * the given Branch.
     * <p>
     * If b is null, it is assumed to be the root branch.
     * <p>
     * If the given instruction is not known to this CDG an
     * IllegalArgumentException is thrown.
     *
     * @param ins a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @param b   a {@link org.evosuite.coverage.branch.Branch} object.
     * @return a boolean.
     */
    public boolean isDirectlyControlDependentOn(BytecodeInstruction ins, Branch b) {
        if (ins == null)
            throw new IllegalArgumentException("null given");

        BasicBlock insBlock = ins.getBasicBlock();

        return isDirectlyControlDependentOn(insBlock, b);
    }

    /**
     * Determines whether the given BasicBlock is directly control dependent on
     * the given Branch. Meaning within this CDG there is an incoming
     * ControlFlowEdge to this instructions BasicBlock holding the given Branch
     * as it's branchInstruction.
     * <p>
     * If b is null, it is assumed to be the root branch.
     * <p>
     * If the given instruction is not known to this CDG an
     * IllegalArgumentException is thrown.
     *
     * @param insBlock a {@link org.evosuite.graphs.cfg.BasicBlock} object.
     * @param b        a {@link org.evosuite.coverage.branch.Branch} object.
     * @return a boolean.
     */
    public boolean isDirectlyControlDependentOn(BasicBlock insBlock, Branch b) {
        Set<ControlFlowEdge> incomming = incomingEdgesOf(insBlock);

        if (incomming.size() == 1) {
            // in methods with a try-catch-block it is possible that there
            // are nodes in the CDG that have exactly one parent with an
            // edge without a branchInstruction that is a non exceptional
            // edge
            // should the given instruction be such a node, follow the parents
            // until
            // you reach one where the above conditions are not met

            for (ControlFlowEdge e : incomming) {
                if (!e.hasControlDependency() && !e.isExceptionEdge()) {
                    return isDirectlyControlDependentOn(getEdgeSource(e), b);
                }
            }
        }

        boolean isRootDependent = isRootDependent(insBlock);
        if (b == null)
            return isRootDependent;
        if (isRootDependent && b != null)
            return false;

        for (ControlFlowEdge e : incomming) {
            Branch current = e.getBranchInstruction();

            if (e.isExceptionEdge()) {
                if (current != null)
                    throw new IllegalStateException(
                            "expect exception edges to have no BranchInstruction set");
                else
                    continue;
            }

            if (current == null)
                continue;
            // throw new IllegalStateException(
            // "expect non exceptional ControlFlowEdges whithin the CDG that don't come from EntryBlock to have branchInstructions set "
            // + insBlock.toString() + methodName);

            if (current.equals(b))
                return true;
        }

        return false;

    }

    /**
     * Checks whether the given instruction is dependent on the root branch of
     * it's method
     * <p>
     * This is the case if the BasicBlock of the given instruction is directly
     * adjacent to the EntryBlock
     *
     * @param ins a {@link org.evosuite.graphs.cfg.BytecodeInstruction} object.
     * @return a boolean.
     */
    public boolean isRootDependent(BytecodeInstruction ins) {

        return isRootDependent(ins.getBasicBlock());
    }

    /**
     * Checks whether the given basicBlock is dependent on the root branch of
     * it's method
     * <p>
     * This is the case if the BasicBlock of the given instruction is directly
     * adjacent to the EntryBlock
     *
     * @param insBlock a {@link org.evosuite.graphs.cfg.BasicBlock} object.
     * @return a boolean.
     */
    public boolean isRootDependent(BasicBlock insBlock) {
        if (isAdjacentToEntryBlock(insBlock))
            return true;

        for (ControlFlowEdge in : incomingEdgesOf(insBlock)) {
            if (in.hasControlDependency())
                continue;
            BasicBlock inBlock = getEdgeSource(in);
            if (inBlock.equals(insBlock))
                continue;

            if (isRootDependent(inBlock))
                return true;
        }

        return false;

    }

    /**
     * Returns true if the given BasicBlock has an incoming edge from this CDG's
     * EntryBlock or is itself the EntryBlock
     *
     * @param insBlock a {@link org.evosuite.graphs.cfg.BasicBlock} object.
     * @return a boolean.
     */
    public boolean isAdjacentToEntryBlock(BasicBlock insBlock) {

        if (insBlock.isEntryBlock())
            return true;

        Set<BasicBlock> parents = getParents(insBlock);
        for (BasicBlock parent : parents)
            if (parent.isEntryBlock())
                return true;

        return false;
    }

    // /**
    // * If the given instruction is known to this graph, the BasicBlock holding
    // * that instruction is returned. Otherwise an IllegalArgumentException
    // will
    // * be thrown.
    // *
    // * Just a convenience method that more or less just redirects the call to
    // * the CFG
    // */
    // public BasicBlock getBlockOf(BytecodeInstruction ins) {
    // if (ins == null)
    // throw new IllegalArgumentException("null given");
    // if (!cfg.knowsInstruction(ins))
    // throw new IllegalArgumentException("unknown instruction");
    //
    // BasicBlock insBlock = cfg.getBlockOf(ins);
    // if (insBlock == null)
    // throw new IllegalStateException(
    // "expect CFG to return non-null BasicBlock for instruction it knows");
    //
    // return insBlock;
    // }

    // init

    private void computeGraph() {

        createGraphNodes();
        computeControlDependence();
    }

    private void createGraphNodes() {
        // copy CFG nodes
        addVertices(cfg);

        for (BasicBlock b : vertexSet())
            if (b.isExitBlock() && !graph.removeVertex(b)) // TODO refactor
                throw new IllegalStateException("internal error building up CDG");

    }

    private void computeControlDependence() {

        ActualControlFlowGraph rcfg = cfg.computeReverseCFG();
        DominatorTree<BasicBlock> dt = new DominatorTree<>(rcfg);

        for (BasicBlock b : rcfg.vertexSet())
            if (!b.isExitBlock()) {

                logger.debug("DFs for: " + b.getName());
                for (BasicBlock cd : dt.getDominatingFrontiers(b)) {
                    ControlFlowEdge orig = cfg.getEdge(cd, b);

                    if (!cd.isEntryBlock() && orig == null) {
                        // in for loops for example it can happen that cd and b
                        // were not directly adjacent to each other in the CFG
                        // but rather there were some intermediate nodes between
                        // them and the needed information is inside one of the
                        // edges
                        // from cd to the first intermediate node. more
                        // precisely cd is expected to be a branch and to have 2
                        // outgoing edges, one for evaluating to true (jumping)
                        // and one for false. one of them can be followed and b
                        // will eventually be reached, the other one can not be
                        // followed in that way. TODO TRY!

                        logger.debug("cd: " + cd);
                        logger.debug("b: " + b);

                        // TODO this is just for now! unsafe and probably not
                        // even correct!
                        Set<ControlFlowEdge> candidates = cfg.outgoingEdgesOf(cd);
                        if (candidates.size() < 2)
                            throw new IllegalStateException("unexpected");

                        boolean leadToB = false;
                        boolean skip = false;

                        for (ControlFlowEdge e : candidates) {
                            if (!e.hasControlDependency()) {
                                skip = true;
                                break;
                            }

                            if (cfg.leadsToNode(e, b)) {
                                if (leadToB)
                                    orig = null;
                                // throw new
                                // IllegalStateException("unexpected");
                                leadToB = true;

                                orig = e;
                            }
                        }
                        if (skip)
                            continue;
                        if (!leadToB)
                            throw new IllegalStateException("unexpected");
                    }

                    if (orig == null)
                        logger.debug("orig still null!");

                    if (!addEdge(cd, b, new ControlFlowEdge(orig)))
                        throw new IllegalStateException(
                                "internal error while adding CD edge");

                    logger.debug("  " + cd.getName());
                }
            }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        // return "CDG" + graphId + "_" + methodName;
        return methodName + "_" + "CDG";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String dotSubFolder() {
        return toFileString(className) + "/CDG/";
    }

    /**
     * <p>Getter for the field <code>className</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassName() {
        return className;
    }

    /**
     * <p>Getter for the field <code>methodName</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMethodName() {
        return methodName;
    }
}
