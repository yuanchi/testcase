package com.jerrylin.erp.function;
@FunctionalInterface
public interface PredicateThrowable<T> {
	public boolean test(T t);
}
