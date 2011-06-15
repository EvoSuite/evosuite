/**
 * 
 */
package de.unisb.cs.st.evosuite.contracts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.unisb.cs.st.evosuite.testcase.ConstructorStatement;
import de.unisb.cs.st.evosuite.testcase.FieldStatement;
import de.unisb.cs.st.evosuite.testcase.MethodStatement;
import de.unisb.cs.st.evosuite.testcase.Scope;
import de.unisb.cs.st.evosuite.testcase.StatementInterface;

/**
 * Based on ObjectContract / Randoop
 * 
 * @author Gordon Fraser
 */
public abstract class Contract {

	protected class Pair {
		Object object1;
		Object object2;

		public Pair(Object o1, Object o2) {
			object1 = o1;
			object2 = o2;
		}
	}

	public abstract boolean check(StatementInterface statement, Scope scope, Throwable exception);

	protected Collection<Pair> getAffectedObjectPairs(StatementInterface statement, Scope scope) {
		Set<Pair> pairs = new HashSet<Pair>();

		if ((statement instanceof ConstructorStatement) || (statement instanceof FieldStatement)) {
			Object o = scope.get(statement.getReturnValue());
			if (o != null) {
				for (Object o1 : scope.getObjects(o.getClass())) {
					for (Object o2 : scope.getObjects(o.getClass())) {
						pairs.add(new Pair(o1, o2));
					}
				}
			}
		} else if (statement instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) statement;
			Object o = scope.get(statement.getReturnValue());
			if (o != null) {
				for (Object o1 : scope.getObjects(o.getClass())) {
					for (Object o2 : scope.getObjects(o.getClass())) {
						pairs.add(new Pair(o1, o2));
					}
				}
			}
			if (!ms.isStatic()) {
				o = scope.get(ms.getCallee());
				if (o != null) {
					for (Object o1 : scope.getObjects(o.getClass())) {
						for (Object o2 : scope.getObjects(o.getClass())) {
							pairs.add(new Pair(o1, o2));
						}
					}
				}
			}
		}
		return pairs;
	}

	protected Collection<Object> getAffectedObjects(StatementInterface statement, Scope scope) {
		Set<Object> objects = new HashSet<Object>();
		if ((statement instanceof ConstructorStatement) || (statement instanceof FieldStatement)) {
			objects.add(scope.get(statement.getReturnValue()));
		} else if (statement instanceof MethodStatement) {
			MethodStatement ms = (MethodStatement) statement;
			Object o = scope.get(statement.getReturnValue());
			if (o != null) {
				objects.add(o);
			}
			if (!ms.isStatic()) {
				objects.add(scope.get(ms.getCallee()));
			}

		}
		return objects;
	}

	protected Collection<Pair> getAllObjectPairs(Scope scope) {
		Set<Pair> pairs = new HashSet<Pair>();
		for (Object o1 : scope.getObjects()) {
			for (Object o2 : scope.getObjects(o1.getClass())) {
				pairs.add(new Pair(o1, o2));
			}
		}
		return pairs;
	}

	protected Collection<Object> getAllObjects(Scope scope) {
		return scope.getObjects();
	}

}
