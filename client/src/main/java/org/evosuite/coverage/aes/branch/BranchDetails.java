package org.evosuite.coverage.aes.branch;

public class BranchDetails {

    private String methodname;
    private int branchID;
    private boolean evaluation;
    private int lineno;
    public BranchDetails(String a, int b, boolean c, int d)
    {
        this.methodname = a;
        this.branchID = b;
        this. evaluation = c;
        this.lineno = d;
    }
    public String getMethodName(){ return methodname;}

    public int getBranchId() { return branchID;}

    public boolean getEvaluation() {return evaluation;}

    public int getLineno() {return lineno;}
}
