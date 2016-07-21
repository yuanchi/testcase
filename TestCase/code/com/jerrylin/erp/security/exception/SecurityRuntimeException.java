package com.jerrylin.erp.security.exception;

public class SecurityRuntimeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 983637256226232450L;
	
	public SecurityRuntimeException(){}
	public SecurityRuntimeException(String message){super(message);}
	public SecurityRuntimeException(String message, Throwable cause){super(message, cause);}

}
