package ru.lehvolk.toolkit.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * Encryption utility
 */
public class Crypto {

	static {
		// add BouncyCastle cryptographic provider
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * Decrypts data with given secret key
	 * @param encrypted - bytes of encrypted data
	 * @param key - secret key
	 * @return - original bytes
	 * @throws CryptoException if any cryptographic error occurred
	 */
	public static byte[] decrypt(byte[] encrypted, RawKeyPackage key) throws CryptoException {
		Cipher cipher = getCipher(key, Cipher.DECRYPT_MODE);

		try {
			return cipher.doFinal(encrypted);
		} catch (Exception e) {
			throw new CryptoException("Decryption error", e);
		}
	}

	/**
	 * Decrypts specified File with given secret key
	 * @param source - File with encrypted content
	 * @param dest - File for storing clear content
	 * @param key - secret key
	 * @throws CryptoException if any cryptographic error occurred
	 */
	public static void decryptFile(File source, File dest, RawKeyPackage key) throws CryptoException {
		processFile(source, dest, key, Cipher.DECRYPT_MODE);
	}

	/**
	 * Decrypts data from Base64 encoded string with given secret key
	 * @param encrypted - Base64 encoded string of encrypted data
	 * @param key - secret key
	 * @return - restored text
	 * @throws CryptoException if any cryptographic error occurred
	 */
	public static String decryptFromBase64(String encrypted, RawKeyPackage key) throws CryptoException {
		byte[] bytes = fromBase64(encrypted);
		return decryptToString(bytes, key);
	}

	/**
	 * Decrypts data with given secret key to string
	 * @param encrypted - bytes of encrypted data
	 * @param key - secret key
	 * @return - restored text
	 * @throws CryptoException if any cryptographic error occurred
	 */
	public static String decryptToString(byte[] encrypted, RawKeyPackage key) throws CryptoException {
		try {
			return new String(decrypt(encrypted, key), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 charset is absent");
		}
	}

	/**
	 * Encrypts byte array with given secret key
	 * @param data - array for encrypt
	 * @param key - secret key package
	 * @return - encrypted bytes
	 * @throws CryptoException if any cryptographic error occurred
	 */
	public static byte[] encrypt(byte[] data, RawKeyPackage key) throws CryptoException {
		Cipher cipher = getCipher(key, Cipher.ENCRYPT_MODE);

		try {
			return cipher.doFinal(data);
		} catch (Exception e) {
			throw new CryptoException("Error encrypting bytes", e);
		}
	}

	/**
	 * Encrypts string with given secret key
	 * @param clearText - text for encrypt
	 * @param key - secret key package
	 * @return - encrypted bytes
	 * @throws CryptoException if any cryptographic error occurred
	 */
	public static byte[] encrypt(String clearText, RawKeyPackage key) throws CryptoException {
		try {
			return encrypt(clearText.getBytes("UTF-8"), key);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 charset is absent");
		}
	}

	/**
	 * Encrypts specified File with given secret key
	 * @param source - File with encrypted content
	 * @param dest - File for storing clear content
	 * @param key - secret key
	 * @throws CryptoException if any cryptographic error occurred
	 */
	public static void encryptFile(File source, File dest, RawKeyPackage key) throws CryptoException {
		processFile(source, dest, key, Cipher.ENCRYPT_MODE);
	}

	/**
	 * Encrypts string and make Base64 transformation
	 * @param clearText - text to be encrypted
	 * @param key - secret key
	 * @return Base64 encoded string
	 * @throws CryptoException if any cryptographic error occurred
	 */
	public static String encryptToBase64(String clearText, RawKeyPackage key) throws CryptoException {
		return toBase64(encrypt(clearText, key));
	}

	/**
	 * Decodes Base64-encoded string
	 * @param text - Base64 encoded string
	 * @return original bytes
	 */
	public static byte[] fromBase64(String text) {
		return Base64.decodeBase64(text.trim());
	}

	private static SecretKey generateSecretKey(KeyType type) throws Exception {
		KeyGenerator keygen = KeyGenerator.getInstance(type.getAlias());
		keygen.init(type.getKeySize(), SecureRandom.getInstance("SHA1PRNG", "SUN"));
		return keygen.generateKey();
	}

	/**
	 * Generates secret key for specified type
	 * @param type - type of key, {@link KeyType}
	 * @return {@link SecretKeyPackage} of secret key and key represents IV
	 * @throws CryptoException if any cryptographic error occurred
	 */
	public static SecretKeyPackage generateKey(KeyType type) throws CryptoException {
		try {
			KeyGenerator keygen = KeyGenerator.getInstance(type.getAlias());
			keygen.init(type.getKeySize(), SecureRandom.getInstance("SHA1PRNG", "SUN"));
			SecretKey sKey = generateSecretKey(type);
			SecretKey iv = generateSecretKey(type);

			return new SecretKeyPackage(sKey, iv, type);
		} catch (Exception e) {
			throw new CryptoException("Error generating key", e);
		}
	}

	/**
	 * Generates secret key for specified type without IV
	 * @param type - type of key, {@link KeyType}
	 * @return {@link SecretKeyPackage} of secret key
	 * @throws CryptoException if any cryptographic error occurred
	 */
	public static RawKeyPackage generateRawKey(KeyType type) throws CryptoException {
		try {
			SecretKey key1 = generateSecretKey(type);
			SecretKey key2 = generateSecretKey(type);

			byte[] key = xor(key1.getEncoded(), key2.getEncoded());

			return new RawKeyPackage(key, new byte[type.getIvSize()], type);
		} catch (Exception e) {
			throw new CryptoException("Error generating key", e);
		}
	}

	private static Cipher getCipher(RawKeyPackage key, int mode) throws CryptoException {
		SecretKeySpec skeySpec = new SecretKeySpec(key.getKey(), key.getType().getAlias());
		IvParameterSpec ivSpec = new IvParameterSpec(key.getIv());
		try {
			Cipher cipher = Cipher.getInstance(key.getType().getAlias() + "/CBC/PKCS5Padding");
			cipher.init(mode, skeySpec, ivSpec);

			return cipher;
		} catch (Exception e) {
			throw new CryptoException("Error creating cipher", e);
		}
	}

	/**
	 * Generates "process" key on base of two given keys
	 * @param k1 - key #1
	 * @param k2 - key #2
	 * @return {@link RawKeyPackage} consists "process" key and process IV
	 */
	public static RawKeyPackage makeProcessKey(SecretKeyPackage k1, SecretKeyPackage k2) {

		if (!k1.getType().equals(k2.getType())) {
			throw new IllegalArgumentException("Keys must be the same type!");
		}

		byte[] key1 = k1.getKey().getEncoded();
		byte[] iv1 = k1.getIv().getEncoded();

		byte[] key2 = k2.getKey().getEncoded();
		byte[] iv2 = k2.getIv().getEncoded();

		byte[] p = xor(key1, key2);
		byte[] iv = xor(iv1, iv2);

		iv = Arrays.copyOf(iv, k1.getType().getIvSize() / 8);

		return new RawKeyPackage(p, iv, k1.getType());
	}

	private static void processFile(File source, File dest, RawKeyPackage key, int mode) throws CryptoException {
		FileInputStream in = null;
		FileOutputStream out = null;

		try {
			in = new FileInputStream(source);
			out = new FileOutputStream(dest);
			processStream(in, out, key, mode);
		} catch (Exception e) {
			throw new CryptoException("Error file processing", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Encrypts/Decrypts input stream to specified output stream
	 * @param is - {@link InputStream} to be processed
	 * @param os - {@link OutputStream} for output
	 * @param key - {@link RawKeyPackage}
	 * @param mode - mode: Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
	 * @throws CryptoException if any error occurred
	 */
	public static void processStream(InputStream is, OutputStream os, RawKeyPackage key, int mode)
			throws CryptoException {
		Cipher cipher = getCipher(key, mode);
		CipherOutputStream out = null;

		try {
			out = new CipherOutputStream(os, cipher);

			byte[] buf = new byte[8];

			int count;
			while ((count = is.read(buf)) > -1) {
				out.write(buf, 0, count);
			}
			out.flush();
		} catch (Exception e) {
			throw new CryptoException("Error decrypting stream", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Encodes given bytes to Base64-encoded string
	 * @param bytes - original bytes
	 * @return Base64-encoded string
	 */
	public static String toBase64(byte[] bytes) {
		return safeTrim(Base64.encodeBase64String(bytes));
	}

	/**
	 * Generates MAC signature for data specified with given key
	 * @param key - {@link RawKeyPackage}
	 * @param data - data to be signed
	 * @param signedLen - portion size of data to sign
	 * @param macLen - size of MAC
	 * @return MAC bytes
	 * @throws CryptoException if cryptographic error occurred
	 */
	public static byte[] createMAC(RawKeyPackage key, byte[] data, int signedLen, int macLen) throws CryptoException {
		try {
			SecretKeySpec spec = new SecretKeySpec(key.getKey(), key.getType().getAlias());
			Mac mac = Mac.getInstance("ISO9797Alg3Mac");
			mac.init(spec);
			return Arrays.copyOf(mac.doFinal(Arrays.copyOf(data, signedLen)), macLen);
		} catch (Exception e) {
			throw new CryptoException("MAC creation error", e);
		}
	}

	/**
	 * Generates HmacSHA1 MAC signature for data specified with given key
	 * @param key - key characters as string
	 * @param source - source to sign
	 * @return MAC bytes
	 * @throws CryptoException if cryptographic error occurred
	 */
	public static byte[] hmacSign(String key, String source) throws CryptoException {
		try {
			SecretKeySpec spec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(spec);
			return mac.doFinal(source.getBytes("UTF-8"));
		} catch (Exception e) {
			throw new CryptoException("MAC creation error", e);
		}
	}

	/**
	 * Calculate md5 hash function from given string
	 * @param source - source string
	 * @return result of hash function calculation
	 */
	public static String toMD5(String source) {
		if (source == null) {
			return null;
		}
		return DigestUtils.md5Hex(source).toLowerCase();
	}

	/**
	 * Calculate SHA-1 hash function from given string
	 * @param source - source string
	 * @return result of hash function calculation
	 */
	public static String toSHA1(String source) {
		if (source == null) {
			return null;
		}
		return DigestUtils.sha1Hex(source);
	}

	/**
	 * Calculate SHA-256 hash function from given string
	 * @param source - source string
	 * @return result of hash function calculation
	 */
	public static String toSHA256(String source) {
		if (source == null) {
			return null;
		}
		return DigestUtils.sha256Hex(source);
	}

	/**
	 * Decodes hex-string into original bytes
	 * @param hexString - hex-encoded string
	 * @return original bytes
	 * @throws CryptoException if cryptographic error occurred
	 */
	public static byte[] fromHex(String hexString) throws CryptoException {
		if (hexString == null) {
			return null;
		}
		try {
			return Hex.decodeHex(safeTrim(hexString).toCharArray());
		} catch (DecoderException e) {
			throw new CryptoException("Error decoding hex-string", e);
		}
	}

	/**
	 * Encodes given bytes to hex
	 * @param data - bytes to encode
	 * @return encoded string
	 */
	public static String toHex(byte[] data) {
		return Hex.encodeHexString(data);
	}

	private static byte[] xor(byte[] b1, byte[] b2) {

		byte[] result = new byte[Math.min(b1.length, b2.length)];

		for (int i = 0, len = result.length; i < len; i++) {
			result[i] = (byte) (b1[i] ^ b2[i]);
		}

		return result;
	}

	private static String safeTrim(String s) {
		return (s != null) ? s.trim() : null;
	}
}
