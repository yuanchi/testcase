package com.jerrylin.general;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Test;
import static org.junit.Assert.*;

public class FunctionTestCase {
	private Predicate<String> p1(){
		Predicate<String> lenEqualsToThree = (t)->t.length() == 3;
		return lenEqualsToThree;
	}
	private Predicate<String> p2(){
		Predicate<String> containsA = (t)->t.contains("A");
		return containsA;
	}
	@Test
	public void predicateAnd(){
		List<String> codes = Arrays.asList("AAA", "BBB", "CCCC", "CCA");
		List<String> matched = codes.stream().filter(p1().and(p2())).collect(Collectors.toList());
		assertEquals(2, matched.size());
		assertTrue(matched.containsAll(Arrays.asList("AAA", "CCA")));
	}
	@Test
	public void chainVariablePredicateAnd(){
		List<String> codes = Arrays.asList("AAA", "BBB", "CCCC", "CCA");
		
		List<Predicate<String>> predicates = Arrays.asList(p1(), p2());
		Predicate<String> all = predicates.stream().reduce(Predicate::and).orElse(x->false);
		
		List<String> matched = codes.stream().filter(all).collect(Collectors.toList());
		assertEquals(2, matched.size());
		System.out.println(matched);
		assertTrue(matched.containsAll(Arrays.asList("AAA", "CCA")));
	}
}
