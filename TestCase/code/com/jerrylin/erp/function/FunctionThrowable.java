package com.jerrylin.erp.function;
@FunctionalInterface
public interface FunctionThrowable<T, R> {
	public R apply(T t)throws Throwable;
}
