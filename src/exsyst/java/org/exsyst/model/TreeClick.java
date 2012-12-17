package org.exsyst.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.evosuite.utils.Randomness;
import org.uispec4j.Tree;
import org.uispec4j.UIComponent;

import org.exsyst.run.AbstractUIEnvironment;

public class TreeClick extends UIAction<Tree> {
	private static final long serialVersionUID = 1L;

	enum Mode {
		LeftClick,
		RightClick,
		DoubleClick
	}

	private double rowRand;
	private Mode mode;

	TreeClick(Mode mode) {
		assert(mode != null);
		this.mode = mode;
	}

	@Override
	public void executeOn(AbstractUIEnvironment env, final Tree tree) {
		this.checkTarget(tree);

		this.run(env, new Runnable() {
			@Override
			public void run() {
				TreePath path = TreeClick.this.getTreePath(tree);
				
				switch (TreeClick.this.mode) {
				case LeftClick:
					tree.click(path);
					break;
				
				case RightClick:
					tree.rightClick(path);
					break;
					
				case DoubleClick:
					tree.doubleClick(path);
					break;
				}
			}
		});
	}

	protected TreePath getTreePath(Tree tree) {
		TreeModel model = tree.getJTree().getModel();
		// TODO: This can be quite expensive as we need to
		// construct a list of all paths through the tree first...
		//
		// Would be better to use a branching heuristic for very
		// large trees.
		ArrayList<TreePath> paths = getAllTreePaths(model);
		
		int pathIdx = (int) (this.rowRand * paths.size());
		return paths.get(pathIdx);
	}

	private ArrayList<TreePath> getAllTreePaths(TreeModel model) {
		ArrayList<TreePath> result = new ArrayList<TreePath>();
		appendAllSubtreePaths(result, model, model.getRoot(), null);
		return result;
	}

	private void appendAllSubtreePaths(ArrayList<TreePath> result, 
			TreeModel model, Object currentNode, TreePath parentPath) {
		TreePath currentPath = parentPath == null ?
				new TreePath(currentNode) : parentPath.pathByAddingChild(currentNode);
		
		result.add(currentPath);
		
		for (int i = 0; i < model.getChildCount(currentNode); i++) {
			Object node = model.getChild(currentNode, i);
			appendAllSubtreePaths(result, model, node, currentPath);
		}
	}

	@Override
	public boolean randomize() {
		this.rowRand = Randomness.nextDouble();
		
		super.randomize();
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TreeClick other = (TreeClick) obj;
		if (mode != other.mode)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("%s[%.4f]", this.graphVizString(), rowRand);
	}
	
	@Override
	public String graphVizString() {
		switch (this.mode) {
		case LeftClick: return "TreeClick";
		case RightClick: return "TreeRightClick";
		case DoubleClick: return "TreeDoubleClick";
		default: return "ListUnknownClick";
		}
	}

	public static void addActions(List<UIAction<? extends UIComponent>> toList) {
		toList.add(new TreeClick(Mode.LeftClick));
		toList.add(new TreeClick(Mode.RightClick));
		toList.add(new TreeClick(Mode.DoubleClick));
	}

}
