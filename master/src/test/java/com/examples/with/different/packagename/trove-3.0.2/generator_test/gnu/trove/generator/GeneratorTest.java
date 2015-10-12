/*
 * ///////////////////////////////////////////////////////////////////////////////
 * // Copyright (c) 2009, Rob Eden All Rights Reserved.
 * //
 * // This library is free software; you can redistribute it and/or
 * // modify it under the terms of the GNU Lesser General Public
 * // License as published by the Free Software Foundation; either
 * // version 2.1 of the License, or (at your option) any later version.
 * //
 * // This library is distributed in the hope that it will be useful,
 * // but WITHOUT ANY WARRANTY; without even the implied warranty of
 * // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * // GNU General Public License for more details.
 * //
 * // You should have received a copy of the GNU Lesser General Public
 * // License along with this program; if not, write to the Free Software
 * // Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * ///////////////////////////////////////////////////////////////////////////////
 */
package gnu.trove.generator;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 *
 */
public class GeneratorTest extends TestCase {
	public void testReplicatedContentNoMatch() throws IOException {
		String test = "This is a test\nLine 2\nLine 3";

		StringBuilder buf = new StringBuilder();
		assertNull( Generator.findReplicatedBlocks( test, buf ) );
		assertEquals( 0, buf.length() );
	}

	public void testReplicatedContent() throws IOException {
		String test = "This is a test\nLine 2\n====START_REPLICATED_CONTENT #1====\n" +
			"Replicated 1\n=====END_REPLICATED_CONTENT #1=====\n" +
			"====START_REPLICATED_CONTENT #2====\n" +
			"Replicated 2\n=====END_REPLICATED_CONTENT #2=====\n";

		StringBuilder buf = new StringBuilder();
		Map<Integer,String> map = Generator.findReplicatedBlocks( test, buf );

		assertNotNull( map );
		assertEquals( 2, map.size() );
		assertEquals( "Replicated 1", map.get( Integer.valueOf( 1 ) ) );
		assertEquals( "Replicated 2", map.get( Integer.valueOf( 2 ) ) );
		assertEquals( "This is a test\nLine 2", buf.toString() );
	}

	public void testProcessReplication() {
		String test = "Line 1\n#REPLICATED1#\n#REPLICATED2#\nLine 4";

		Map<Integer,String> map = new HashMap<Integer,String>();
		map.put( Integer.valueOf( 1 ), "Line 2 - #E#" );
		map.put( Integer.valueOf( 2 ), "Line 3 - #K# #V#" );
		
		String output = Generator.processReplication( test, map );
		
		String goal = "Line 1\n" +
			
			"Line 2 - Double\n\n" + "Line 2 - Float\n\n" + "Line 2 - Int\n\n" + 
			"Line 2 - Long\n\n" + "Line 2 - Byte\n\n" + "Line 2 - Short\n\n" +
			"Line 2 - Char\n" +
			
			"Line 3 - Double Double\n\n" + "Line 3 - Double Float\n\n" + "Line 3 - Double Int\n\n" + 
			"Line 3 - Double Long\n\n" + "Line 3 - Double Byte\n\n" + "Line 3 - Double Short\n\n" +
			"Line 3 - Double Char\n\n" +
			"Line 3 - Float Double\n\n" + "Line 3 - Float Float\n\n" + "Line 3 - Float Int\n\n" + 
			"Line 3 - Float Long\n\n" + "Line 3 - Float Byte\n\n" + "Line 3 - Float Short\n\n" +
			"Line 3 - Float Char\n\n" +
			"Line 3 - Int Double\n\n" + "Line 3 - Int Float\n\n" + "Line 3 - Int Int\n\n" + 
			"Line 3 - Int Long\n\n" + "Line 3 - Int Byte\n\n" + "Line 3 - Int Short\n\n" +
			"Line 3 - Int Char\n\n" +
			"Line 3 - Long Double\n\n" + "Line 3 - Long Float\n\n" + "Line 3 - Long Int\n\n" + 
			"Line 3 - Long Long\n\n" + "Line 3 - Long Byte\n\n" + "Line 3 - Long Short\n\n" +
			"Line 3 - Long Char\n\n" +
			"Line 3 - Byte Double\n\n" + "Line 3 - Byte Float\n\n" + "Line 3 - Byte Int\n\n" + 
			"Line 3 - Byte Long\n\n" + "Line 3 - Byte Byte\n\n" + "Line 3 - Byte Short\n\n" +
			"Line 3 - Byte Char\n\n" +
			"Line 3 - Short Double\n\n" + "Line 3 - Short Float\n\n" + "Line 3 - Short Int\n\n" + 
			"Line 3 - Short Long\n\n" + "Line 3 - Short Byte\n\n" + "Line 3 - Short Short\n\n" +
			"Line 3 - Short Char\n\n" +
			"Line 3 - Char Double\n\n" + "Line 3 - Char Float\n\n" + "Line 3 - Char Int\n\n" + 
			"Line 3 - Char Long\n\n" + "Line 3 - Char Byte\n\n" + "Line 3 - Char Short\n\n" +
			"Line 3 - Char Char\n" +
			
			"Line 4";

		assertEquals( goal, output );
	}
}
