package com.jerrylin.dynasql;

import org.junit.Test;

public class StringTestCases {
	@Test
	public void trim(){
		String t1 = " ";
		String t2 = "  ";
		String t3 = "   ";
		System.out.println("after trim t1:" + t1.trim());
		System.out.println("after trim t2:" + t2.trim());
		System.out.println("after trim t3:" + t3.trim());
	}
}
