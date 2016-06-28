package com.funmix.exception;

public class TimeoutException extends Exception {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	public TimeoutException() {

		super();

	}

	public TimeoutException(String msg) {

		super(msg);

	}

	public TimeoutException(String msg, Throwable cause) {

		super(msg, cause);

	}

	public TimeoutException(Throwable cause) {

		super(cause);

	}
}