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

		visitor.clear();

		Set<Constraint<?>> reaching_constraints = new HashSet<Constraint<?>>();
		List<BranchCondition> branches = new ArrayList<BranchCondition>();

		for (DscBranchCondition bc : dsc_path_constraint) {

			List<Constraint<?>> branch_local_constraints = new LinkedList<Constraint<?>>();
			for (ConstraintNode dsc_constraint : bc.getLocalConstraints()) {
				Vector<Constraint<?>> local_constraint_vec = (Vector<Constraint<?>>) dsc_constraint
						.accept(visitor);
				branch_local_constraints.addAll(local_constraint_vec);
			}

			if (!branch_local_constraints.isEmpty()) {

				String branch_class_name = bc.getClassName();
				String branch_method_name = bc.getMethodName();
				int branch_lineNumber = bc.getLineNumber();

				HashSet<Constraint<?>> branch_reaching_constraints = new HashSet<Constraint<?>>(
						reaching_constraints);
				BranchCondition new_branch_condition = new BranchCondition(
						branch_class_name, branch_method_name,
						branch_lineNumber, branch_reaching_constraints,
						branch_local_constraints);
				branches.add(new_branch_condition);
				reaching_constraints.addAll(branch_local_constraints);

			}
		}
		return branches;
	}

}
