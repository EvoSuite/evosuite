///////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2001-2006, Eric D. Friedman All Rights Reserved.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
///////////////////////////////////////////////////////////////////////////////


package gnu.trove.impl;

import junit.framework.*;
import gnu.trove.impl.PrimeFinder;



/**
 *
 * Created: Sun Nov  4 11:37:24 2001
 *
 * @author Eric D. Friedman
 * @version $Id: PrimeFinderTest.java,v 1.1.2.1 2009/11/07 03:55:33 robeden Exp $
 */

public class PrimeFinderTest extends TestCase  {

    public PrimeFinderTest(String name) {
        super(name);
    }

    public void testPrimeFinder() throws Exception {
        int r = PrimeFinder.nextPrime(999999);
        assertEquals(1070981,r);
    }
} // PrimeFinderTests
