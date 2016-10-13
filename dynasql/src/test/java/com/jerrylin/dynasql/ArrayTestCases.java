package com.jerrylin.dynasql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Array;

import org.junit.Test;

public class ArrayTestCases {
	@Test
	public void getLength(){
		String[] t1 = {"1", "2", "3"};
		assertTrue(t1.getClass().isArray());
		assertEquals(3, Array.getLength(t1));
	}
}
