package ru.lehvolk.common.security;

/**
 * Class for encapsulation secret "process" key
 */
public class RawKeyPackage {

	private final byte[] iv;
	private final byte[] key;
	private final KeyType type;

	/**
	 * Constructs instants with specified parameters
	 * 
	 * @param key - secret key byte array
	 * @param iv - initialization vector byte array
	 * @param type - type of key
	 */
	public RawKeyPackage(byte[] key, byte[] iv, KeyType type) {
		if (key == null || iv == null || type == null) {
			throw new IllegalArgumentException("All arguments are mandatory!");
		}

		this.key = key;
		this.iv = iv;
		this.type = type;
	}

	/**
	 * @return the iv
	 */
	public byte[] getIv() {
		return iv;
	}

	/**
	 * @return the key
	 */
	public byte[] getKey() {
		return key;
	}

	/**
	 * @return the type
	 */
	public KeyType getType() {
		return type;
	}
}
