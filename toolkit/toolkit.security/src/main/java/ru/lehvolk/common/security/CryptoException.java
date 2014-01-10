package ru.lehvolk.common.security;

/**
 * cryptographic processes base exception
 */
public class CryptoException extends Exception {

	private static final long serialVersionUID = -3297519577509076464L;

	/**
	 * @param msg - description of error
	 */
	public CryptoException(String msg) {
		super(msg);
	}

	/**
	 * @param msg - description of error
	 * @param cause - cause of error
	 */
	public CryptoException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
