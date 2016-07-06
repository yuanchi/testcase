package com.jerrylin.general;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayTestCase {
	@Test
	public void isArray(){
		int[] container = new int[]{1,2,3};
		Object obj = container;
		assertTrue(obj.getClass().isArray());
		if(obj.getClass().isArray()){
			assertEquals(int.class, obj.getClass().getComponentType());
		}
	}
}
