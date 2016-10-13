package com.jerrylin.dynasql;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class CollectionTestCases {
	@Test
	public void removeAll(){
		List<String> minuend = new ArrayList<>();
		minuend.add("a");
		minuend.add("b");
		minuend.add("c");
		List<String> copyMinuend = new ArrayList<>(minuend);
		
		List<String> subtrahend = new ArrayList<>();
		subtrahend.add("b");
		subtrahend.add("c");
		subtrahend.add("e");
		copyMinuend.removeAll(subtrahend);
		System.out.println("minuend:" + minuend + ", after subtracted: " + copyMinuend);
	}
}
