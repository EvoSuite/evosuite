package de.unisb.cs.st.testability;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yanchuan Li
 * Date: 1/14/11
 * Time: 5:31 AM
 */
public class ClassUnit {
    private String classname;
    private File file;
    private boolean transformed;
    private byte[] transformedBytes;
    private Map<String, String> methodsMap;


    public ClassUnit(String classname, File file, boolean transformed) {
        this.classname = classname;
        this.file = file;
        this.transformed = transformed;
        this.methodsMap = new HashMap<String, String>();
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isTransformed() {
        return transformed;
    }

    public void setTransformed(boolean transformed) {
        this.transformed = transformed;
    }


    public byte[] getTransformedBytes() {
        return transformedBytes;
    }

    public void setTransformedBytes(byte[] transformedBytes) {
        this.transformedBytes = transformedBytes;
    }

    public void addMethodSignature(String old, String newSignature) {
        this.methodsMap.put(old, newSignature);
    }

    public String getNewMethodSignature(String old) {
        return this.methodsMap.get(old);
    }

    public boolean containsMethod(String signature) {
        return this.methodsMap.containsKey(signature);
    }
}
