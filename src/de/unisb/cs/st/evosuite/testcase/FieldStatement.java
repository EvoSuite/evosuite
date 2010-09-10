package de.unisb.cs.st.evosuite.testcase;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * Statement that accesses an instance/class field
 * @author Gordon Fraser
 *
 */
public class FieldStatement extends Statement {

	Field field;
	VariableReference source;
	//VariableReference ret_val;
	
	public FieldStatement(Field field, VariableReference source, VariableReference ret_val) {
		this.field = field;
		this.source = source;
		this.retval = ret_val;
	}

	@Override
	public boolean isValid() {
		return retval.isAssignableFrom(field.getType());
	}

	@Override
	public String getCode() {
		String cast_str = "";
		if(!retval.getVariableClass().isAssignableFrom(field.getType())) {
			cast_str = "(" + retval.getSimpleClassName()+ ")";
		}

		if(!Modifier.isStatic(field.getModifiers()))
			return retval.getSimpleClassName() +" "+ retval.getName() + " = " + cast_str + source.getName() + "." + field.getName();
		else
			return retval.getSimpleClassName() +" "+ retval.getName() + " = " + cast_str + field.getDeclaringClass().getSimpleName()+"." + field.getName();
	}


	@Override
	public String getCode(Throwable exception) {
		String cast_str = "  ";
		if(!retval.getVariableClass().isAssignableFrom(field.getType())) {
			cast_str += "(" + retval.getSimpleClassName()+ ")";
		}

		
		String result = retval.getSimpleClassName() +" "+ retval.getName() + " = null;\n";
		result += "try {\n";
		if(!Modifier.isStatic(field.getModifiers()))
			result += cast_str + source.getName() + "." + field.getName()+";\n";
		else
			result += cast_str + field.getDeclaringClass().getSimpleName()+"." + field.getName()+";\n";
		result += "} catch("+exception.getClass().getSimpleName()+" e) {}";
		
		return result;
	}
	
	@Override
	public Statement clone() {
		if(Modifier.isStatic(field.getModifiers()))
			return new FieldStatement(field, null, retval.clone());
		else
			return new FieldStatement(field, source.clone(), retval.clone());
	}

	@Override
	public Throwable execute(Scope scope, PrintStream out)
			throws InvocationTargetException, IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		Object source_object = null;
		if(!Modifier.isStatic(field.getModifiers())) {
			source_object = scope.get(source);
			if(source_object == null) {
		        scope.set(retval, null);
		        return exceptionThrown;
			}
				
		}
		Object ret = field.get(source_object);
        scope.set(retval, ret);
        return exceptionThrown;
	}

	@Override
	public void adjustVariableReferences(int position, int delta) {		
		if(!Modifier.isStatic(field.getModifiers()))
			source.adjust(delta, position);
		retval.adjust(delta, position);
		adjustAssertions(position, delta);
	}

	@Override
	public boolean references(VariableReference var) {
		if(!Modifier.isStatic(field.getModifiers()))
			return source.equals(var);
		else
			return false;
	}

	@Override
	public Set<VariableReference> getVariableReferences() {
		Set<VariableReference> references = new HashSet<VariableReference>();
		references.add(retval);
		if(!Modifier.isStatic(field.getModifiers()))
			references.add(source);
		return references;

	}
	
	@Override
	public boolean equals(Statement s) {
		if (this == s)
			return true;
		if (s == null)
			return false;
		if (getClass() != s.getClass())
			return false;
				
		FieldStatement fs = (FieldStatement)s;
		if(!Modifier.isStatic(field.getModifiers()))
			return source.equals(fs.source) && retval.equals(fs.retval) && field.equals(fs.field);
		else
			return retval.equals(fs.retval) && field.equals(fs.field);
	}

	@Override
	public int hashCode() {
		final int prime = 51;
		int result = 1;
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		return result;
	}
	
	public Field getField() {
		return field;
	}

	@Override
	public void replace(VariableReference oldVar, VariableReference newVar) {
		if(retval.equals(oldVar))
			retval = newVar;
		if(source != null && source.equals(oldVar))
			source = newVar;
		
	}
	
}
