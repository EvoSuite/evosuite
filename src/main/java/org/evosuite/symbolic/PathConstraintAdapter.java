package org.evosuite.symbolic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import edu.uta.cse.dsc.ast.JvmVariable;
import edu.uta.cse.dsc.pcdump.ast.ConstraintNode;
import edu.uta.cse.dsc.pcdump.ast.DscBranchCondition;

import org.evosuite.symbolic.expr.Constraint;

/**
 * Transforms a list of Dsc constraints into a list of EvoSuite constraints. The
 * <code>ConstraintNodeTranslator</code> performs the actual translation from an
 * DSC constraint list into a EvoSuite constraint list.
 * 
 * @author galeotti
 * 
 */
public class PathConstraintAdapter {

	private final ConstraintNodeTranslator visitor;

	public PathConstraintAdapter(Map<JvmVariable, String> symbolicVariables) {
		this.visitor = new ConstraintNodeTranslator(symbolicVariables);
	}

	public List<BranchCondition> transform(
			List<DscBranchCondition> dsc_path_constraint) {
		
		List<BranchCondition> branches = new ArrayList<BranchCondition>();
		for (DscBranchCondition constraint : dsc_path_constraint) {
			BranchCondition branch_condition = transform(constraint);
			if (branch_condition != null)
				branches.add(branch_condition);
		}
		return branches;
	}

	private BranchCondition transform(DscBranchCondition bc) {
		String class_name = bc.getClassName();
		String method_name = bc.getMethodName();
		int lineNumber = bc.getLineNumber();

		visitor.clear();

		List<ConstraintNode> dsc_reaching_constraints = bc
				.getReachingConstraints();
		Set<Constraint<?>> reaching_constraints = new HashSet<Constraint<?>>();
		for (ConstraintNode dsc_constraint : dsc_reaching_constraints) {
			Vector<Constraint<?>> reaching_constraint = transform(dsc_constraint);
			reaching_constraints.addAll(reaching_constraint);
		}

		List<ConstraintNode> dsc_local_constraints = bc.getLocalConstraints();
		List<Constraint<?>> local_constraints = new LinkedList<Constraint<?>>(); // order
																					// should
																					// be
																					// mantained
		for (ConstraintNode dsc_constraint : dsc_local_constraints) {
			Vector<Constraint<?>> local_constraint = transform(dsc_constraint);
			local_constraints.addAll(local_constraint);
		}

		if (!local_constraints.isEmpty()) {
			BranchCondition branchCondition = new BranchCondition(class_name,
					method_name, lineNumber, reaching_constraints,
					local_constraints);
			return branchCondition;
		} else
			return null;
	}

	private Vector<Constraint<?>> transform(ConstraintNode dsc_constraint) {
		Vector<Constraint<?>> ret_val = (Vector<Constraint<?>>) dsc_constraint
				.accept(visitor);
		return ret_val;
	}

}
