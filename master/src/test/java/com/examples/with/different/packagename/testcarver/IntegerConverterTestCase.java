/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package com.examples.with.different.packagename.testcarver;

import junit.framework.TestSuite;

import com.examples.with.different.packagename.testcarver.Converter;


/**
 * Test Case for the IntegerConverter class.
 *
 * @author Rodney Waldhoff
 * @version $Revision: 745078 $ $Date: 2009-02-17 14:03:10 +0000 (Tue, 17 Feb 2009) $
 */

public class IntegerConverterTestCase extends NumberConverterTestBase {

    private Converter converter = null;

    // ------------------------------------------------------------------------

    public IntegerConverterTestCase(String name) {
        super(name);
    }
    
    // ------------------------------------------------------------------------

    public void setUp() throws Exception {
        converter = makeConverter();
        numbers[0] = new Integer("-12");
        numbers[1] = new Integer("13");
        numbers[2] = new Integer("-22");
        numbers[3] = new Integer("23");
    }

    public static TestSuite suite() {
        return new TestSuite(IntegerConverterTestCase.class);        
    }

    public void tearDown() throws Exception {
        converter = null;
    }

    // ------------------------------------------------------------------------
    
    protected NumberConverter makeConverter() {
        return new IntegerConverter();
    }
    
    protected NumberConverter makeConverter(Object defaultValue) {
        return new IntegerConverter(defaultValue);
    }
    
    protected Class getExpectedType() {
        return Integer.class;
    }

    // ------------------------------------------------------------------------

    public void testSimpleConversion() throws Exception {
        String[] message= { 
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from String",
            "from Byte",
            "from Short",
            "from Integer",
            "from Long",
            "from Float",
            "from Double"
        };
        
        Object[] input = { 
            String.valueOf(Integer.MIN_VALUE),
            "-17",
            "-1",
            "0",
            "1",
            "17",
            String.valueOf(Integer.MAX_VALUE),
            new Byte((byte)7),
            new Short((short)8),
            new Integer(9),
            new Long(10),
            new Float(11.1),
            new Double(12.2)
        };
        
        Integer[] expected = { 
            new Integer(Integer.MIN_VALUE),
            new Integer(-17),
            new Integer(-1),
            new Integer(0),
            new Integer(1),
            new Integer(17),
            new Integer(Integer.MAX_VALUE),
            new Integer(7),
            new Integer(8),
            new Integer(9),
            new Integer(10),
            new Integer(11),
            new Integer(12)
        };
        
        for(int i=0;i<expected.length;i++) {
            assertEquals(message[i] + " to Integer",expected[i],converter.convert(Integer.class,input[i]));
            assertEquals(message[i] + " to int",expected[i],converter.convert(Integer.TYPE,input[i]));
            assertEquals(message[i] + " to null type",expected[i],converter.convert(null,input[i]));
        }
    }

    /**
     * Test Invalid Amounts (too big/small)
     */
    public void testInvalidAmount() {
        Converter converter = makeConverter();
        Class clazz = Integer.class;

        Long min         = new Long(Integer.MIN_VALUE);
        Long max         = new Long(Integer.MAX_VALUE);
        Long minMinusOne = new Long(min.longValue() - 1);
        Long maxPlusOne  = new Long(max.longValue() + 1);

        // Minimum
        assertEquals("Minimum", new Integer(Integer.MIN_VALUE), converter.convert(clazz, min));

        // Maximum
        assertEquals("Maximum", new Integer(Integer.MAX_VALUE), converter.convert(clazz, max));

        // Too Small
        try {
            assertEquals("Minimum - 1", null, converter.convert(clazz, minMinusOne));
            fail("Less than minimum, expected ConversionException");
        } catch (Exception e) {
            // expected result
        }

        // Too Large
        try {
            assertEquals("Maximum + 1", null, converter.convert(clazz, maxPlusOne));
            fail("More than maximum, expected ConversionException");
        } catch (Exception e) {
            // expected result
        }
    }
}

