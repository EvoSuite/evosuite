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
package org.evosuite.graphs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.evosuite.TestGenerationContext;
import org.evosuite.coverage.lcsaj.LCSAJ;
import org.evosuite.graphs.cfg.BytecodeInstruction;
import org.evosuite.graphs.cfg.ControlFlowEdge;
import org.evosuite.graphs.cfg.RawControlFlowGraph;
import org.kohsuke.graphviz.Edge;
import org.kohsuke.graphviz.Graph;
import org.kohsuke.graphviz.Node;

public class LCSAJGraph {

	private final RawControlFlowGraph graph;

	private final LCSAJ lcsaj;

	private final boolean fitnessGraph;

	/**
	 * <p>
	 * Constructor for LCSAJGraph.
	 * </p>
	 * 
	 * @param lcsaj
	 *            a {@link org.evosuite.coverage.lcsaj.LCSAJ} object.
	 * @param fitnessGraph
	 *            a boolean.
	 */
	public LCSAJGraph(LCSAJ lcsaj, boolean fitnessGraph) {
		graph = GraphPool.getInstance(TestGenerationContext.getInstance().getClassLoaderForSUT()).getRawCFG(lcsaj.getClassName(),
		                                                                                lcsaj.getMethodName());
		this.lcsaj = lcsaj;
		this.fitnessGraph = fitnessGraph;
	}

	/**
	 * <p>
	 * generate
	 * </p>
	 * 
	 * @param file
	 *            a {@link java.io.File} object.
	 */
	public void generate(File file) {
		if (fitnessGraph)
			System.out.println("Generating Graph for uncoverd LCSAJ No: " + lcsaj.getID()
			        + " in " + lcsaj.getClassName() + "/" + lcsaj.getMethodName());
		else
			System.out.println("Generating Graph for LCSAJ No: " + lcsaj.getID() + " in "
			        + lcsaj.getClassName() + "/" + lcsaj.getMethodName());

		Graph lcsaj_graph = new Graph();
		ArrayList<Node> allNodes = new ArrayList<Node>();

		for (BytecodeInstruction b : graph.vertexSet()) {
			Node n = new Node().attr("label", b.toString());
			lcsaj_graph = lcsaj_graph.node(n);
			allNodes.add(n);
		}

		for (ControlFlowEdge edge : graph.edgeSet())
			for (Node source : allNodes)
				for (Node target : allNodes) {
					BytecodeInstruction b1 = graph.getEdgeSource(edge);
					BytecodeInstruction b2 = graph.getEdgeTarget(edge);

					if (source.attr("label").equals(b1.toString())
					        && target.attr("label").equals(b2.toString())) {
						if (b1.isBranch()) {
							Edge newEdge = new Edge(source, target).attr("label",
							                                             edge.toString());
							lcsaj_graph.edge(newEdge);
						} else
							lcsaj_graph.edge(source, target);
					}
				}

		BytecodeInstruction l1 = lcsaj.getStartBranch().getInstruction();
		BytecodeInstruction l2 = lcsaj.getLastBranch().getInstruction();
		if (fitnessGraph)
			l2 = lcsaj.getBranch(lcsaj.getdPositionReached()).getInstruction();

		for (Node source : allNodes)
			for (Node target : allNodes) {
				if (source.attr("label").equals(l1.toString())
				        && target.attr("label").equals(l2.toString())) {
					if (!fitnessGraph
					        || lcsaj.getdPositionReached() == lcsaj.length() - 1) {
						Edge newEdge = new Edge(source, target).attr("color", "green").attr("label",
						                                                                    "LCSAJ No."
						                                                                            + lcsaj.getID());
						lcsaj_graph.edge(newEdge);
					} else {
						Edge newEdge = new Edge(source, target).attr("color", "red").attr("label",
						                                                                  " LCSAJ No."
						                                                                          + lcsaj.getID()
						                                                                          + ". Covered till:");
						lcsaj_graph.edge(newEdge);
					}

				}
			}

		ArrayList<String> commandos = new ArrayList<String>();
		commandos.add("dot");
		try {
			lcsaj_graph.generateTo(commandos, file);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
