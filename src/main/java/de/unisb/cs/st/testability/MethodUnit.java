package de.unisb.cs.st.testability;

/**
 * Created by Yanchuan Li
 * Date: 1/18/11
 * Time: 9:49 PM
 */
public class MethodUnit {

    private String originalName;
    private String newName;
    private MethodOrigin origin;
    private boolean booleanReturnType;

    public MethodUnit() {
    }

    public MethodUnit(String originalName, String newName, MethodOrigin origin, boolean booleanReturnType) {
        this.originalName = originalName;
        this.newName = newName;
        this.origin = origin;
        this.booleanReturnType = booleanReturnType;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public MethodOrigin getOrigin() {
        return origin;
    }

    public void setOrigin(MethodOrigin origin) {
        this.origin = origin;
    }

    public boolean isBooleanReturnType() {
        return booleanReturnType;
    }

    public void setBooleanReturnType(boolean booleanReturnType) {
        this.booleanReturnType = booleanReturnType;
    }
}
