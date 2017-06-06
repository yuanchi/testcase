package com.jerrylin.dynasql3;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static TestSuite suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
    
    interface Test{
    	default String toStr(){
    		return "this is Test";
    	}
    }
    class TestImpl implements Test{
    	@Override
    	public String toStr(){
    		String superStr = Test.super.toStr();
    		return superStr + " under TestImpl";
    	}
    }
    
    public void testAccessInterfaceDefault(){
    	System.out.println(new TestImpl().toStr());
    }
    
    public void testArraysList(){
    	List<String> a = Arrays.asList("1");
    	System.out.println(a.getClass());
    }
}
