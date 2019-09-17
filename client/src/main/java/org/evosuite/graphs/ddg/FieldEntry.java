package org.evosuite.graphs.ddg;

/**
 * Java Bean that represents a field of a class. The Bean holds the field's name and the name of its
 * owner class.
 */
public class FieldEntry extends ClassMember {
    private final String className;
    private final String fieldName;

    /**
     * Constructs a new FieldEntry object with the given owner class and field name.
     *
     * @param className fully qualified name of the field's owner class
     * @param fieldName the name of the field
     */
    public FieldEntry(final String className, final String fieldName) {
        this.className = className;
        this.fieldName = fieldName;
    }

    public String getClassName() {
        return className;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + className.hashCode();
        result = prime * result + fieldName.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FieldEntry other = (FieldEntry) obj;
        return (className.equals(other.className) && fieldName
                .equals(other.fieldName));
    }

    @Override
    public String toString() {
        return className + "." + fieldName;
    }

    @Override
    public boolean isField() {
        return true;
    }

    @Override
    public boolean isMethod() {
        return false;
    }
}
