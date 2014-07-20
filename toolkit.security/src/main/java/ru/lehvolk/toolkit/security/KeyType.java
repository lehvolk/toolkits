package ru.lehvolk.toolkit.security;

/**
 * Represents type of secret key
 */
public enum KeyType {

	/** AES key type */
	AES("AES", 128, 128), //in bits
	/** Triple DES key type */
	TDES("DESede", 168, 64), //in bits
	/** Double DES key type */
	DDES("DESede", 128, 64); //in bits

	private final String alias;
	private final int ivSize;
	private final int keySize;

	private KeyType(String alias, int keySize, int ivSize) {
		this.alias = alias;
		this.keySize = keySize;
		this.ivSize = ivSize;
	}

	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @return the ivSize
	 */
	public int getIvSize() {
		return ivSize;
	}

	/**
	 * @return the keySize
	 */
	public int getKeySize() {
		return keySize;
	}
}
