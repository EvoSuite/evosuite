/**
 * Copyright (C) 2010-2015 Gordon Fraser, Andrea Arcuri and EvoSuite
 * contributors
 *
 * This file is part of EvoSuite.
 *
 * EvoSuite is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser Public License as published by the
 * Free Software Foundation, either version 3.0 of the License, or (at your
 * option) any later version.
 *
 * EvoSuite is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along
 * with EvoSuite. If not, see <http://www.gnu.org/licenses/>.
 */
package org.evosuite.idNaming;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
