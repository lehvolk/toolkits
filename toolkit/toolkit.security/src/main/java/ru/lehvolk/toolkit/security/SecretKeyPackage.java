package ru.lehvolk.toolkit.security;

import java.util.Arrays;

import javax.crypto.SecretKey;

/**
 * Class encapsulates information about secret key
 */
public class SecretKeyPackage {

	private final SecretKey iv;
	private final SecretKey key;
	private final KeyType type;

	/**
	 * Constructs instance with specified parameters
	 * 
	 * @param key - {@link SecretKey} secret key
	 * @param iv - {@link SecretKey} initialization vector (IV)
	 * @param type - type of key {@link KeyType}
	 */
	public SecretKeyPackage(SecretKey key, SecretKey iv, KeyType type) {
		if (type == null || key == null) {
			throw new IllegalArgumentException("Key and KeyType can't be null");
		}

		this.key = key;
		this.iv = iv;
		this.type = type;
	}

	/**
	 * @return the iv
	 */
	public SecretKey getIv() {
		return iv;
	}

	/**
	 * @return the key
	 */
	public SecretKey getKey() {
		return key;
	}

	/**
	 * @return the type
	 */
	public KeyType getType() {
		return type;
	}

	/**
	 * @return {@link RawKeyPackage} representation of this secret key package
	 */
	public RawKeyPackage toRawPackage() {
		return new RawKeyPackage(key.getEncoded(), Arrays.copyOf(iv.getEncoded(), type.getIvSize() / 8), type);
	}
}
