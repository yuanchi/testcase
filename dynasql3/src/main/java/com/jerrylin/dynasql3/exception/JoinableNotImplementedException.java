package com.jerrylin.dynasql3.exception;

public class JoinableNotImplementedException extends RuntimeException {
	private static final long serialVersionUID = -6128324937725897338L;
	public JoinableNotImplementedException(){
		super();
	}
	public JoinableNotImplementedException(String message){
		super(message);
	}
	public JoinableNotImplementedException(String message, Throwable throwable){
		super(message, throwable);
	}
	public JoinableNotImplementedException(Throwable throwable){
		super(throwable);
	}
}
