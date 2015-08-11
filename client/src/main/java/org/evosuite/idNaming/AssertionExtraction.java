package org.evosuite.idNaming;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class AssertionExtraction
{
    private int total_assertions;

    private double av_assertions;
    
    private int additional_assertions;
    
    private int has_assertions;
    
    private List<String> assertion_statement;
    
    public static final String[] ASSERTION_TYPES = {"assertEquals",
    												"assertArrayEquals",
    												"assertFalse",
    												"assertTrue",
    												"assertNotNull",
    												"assertNull",
    												"assertNotSame",
    												"assertSame",
    												"assertThat",
    												"fail"};

    public AssertionExtraction(String s) {
        total_assertions = 0;
        av_assertions = 0.0;
        additional_assertions=0;
        has_assertions=0;
        assertion_statement = new ArrayList<String>();
        this.setAssertion(s);
    }

    private void setAssertion(String test_code) {
    	String lines[] = test_code.split("\n");
        test_code = test_code.replaceAll("[\\p{Punct}]", " ");
        String line[] = test_code.split("\n");
		
        for (String assertionType : ASSERTION_TYPES) {
            total_assertions += StringUtils.countMatches(test_code, assertionType);
            if(StringUtils.countMatches(test_code, assertionType)>0 && !assertionType.equals("fail")){
	            for(String statement: lines){
	            	if(statement.contains(assertionType)){
	            		assertion_statement.add(statement);
	            	}
	            }
            }
        }
        
        if (total_assertions>1){
        	additional_assertions=total_assertions-1;
        }
        if (total_assertions>0){
        	has_assertions=1;
        }

        av_assertions = (double) total_assertions / (double) line.length;
        
        
    }

    public int get_total_assertion() {
        return total_assertions;
    }

    public double get_av_assertions() {
        return av_assertions;
    }
    public int getAdditionalAssertions(){
    	return additional_assertions;
    }
    public int getHasAssertions(){
    	return has_assertions;
    }
    public List<String> getStatements(){
    	return assertion_statement;
    }
    
}
