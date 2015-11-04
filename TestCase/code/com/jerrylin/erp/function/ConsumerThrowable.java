package com.jerrylin.erp.function;
@FunctionalInterface
public interface ConsumerThrowable<T> {
	public void accept(T t)throws Throwable;
}
