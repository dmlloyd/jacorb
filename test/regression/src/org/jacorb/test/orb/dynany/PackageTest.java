package org.jacorb.test.orb.dynany;

/*
 *        JacORB  - a free Java ORB
 *
 *   Copyright (C) 1997-2001  Gerald Brose.
 *
 *   This library is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Library General Public
 *   License as published by the Free Software Foundation; either
 *   version 2 of the License, or (at your option) any later version.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *   Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this library; if not, write to the Free
 *   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

import junit.framework.*;

public class PackageTest extends TestCase
{
   public PackageTest (String name)
   {
      super (name);
   }

   public static Test suite ()
   {
      TestSuite suite = new TestSuite ("Package dynany");

      suite.addTest (DynAnyBaseTest.suite ());
      suite.addTestSuite(DynAnyFixedTest.class);
      suite.addTest (DynAnyEnumTest.suite ());
      suite.addTest (DynAnyBoundedSeqTest.suite ());
      suite.addTest (DynAnyUnboundedSeqTest.suite ());
      suite.addTest (DynAnyArrayTest.suite ());
      suite.addTestSuite(DynAnyStructTest.class);
      suite.addTest (DynAnyNonEmptyExTest.suite ());
      suite.addTest (DynAnyEmptyExTest.suite ());
      suite.addTest (DynAnyUnionTest.suite ());
      suite.addTest (DynAnyUnionIntTest.suite ());
      suite.addTestSuite(DynAnyObjectTest.class);
      suite.addTestSuite(DynAnyArrayObjectTest.class);
      suite.addTestSuite(DynAnySeqObjectTest.class);

      return suite;
   }
}
