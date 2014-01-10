package ru.lehvolk.common.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.CompressionAlgorithmTags;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;

/**
 * Class contains methods for encrypting and signing data with PGP technology
 */
public class PGPEncoder {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	private int compression = CompressionAlgorithmTags.ZIP;
	private int symmetricAlgorithm = SymmetricKeyAlgorithmTags.AES_128;
	private int hashAlgorithm = HashAlgorithmTags.SHA256;
	private static final int BUFFER_SIZE = 1 << 16;
	private boolean armor = false;

	private void closeStream(OutputStream stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (Exception e) {
				//ignore
			}
		}
	}

	/**
	 * Encrypts data input stream with specified input stream of public key and sign with private key
	 * @param out - {@link OutputStream} where encrypted data will be passed
	 * @param data - data to be encrypted {@link InputStream}
	 * @param fileName - name under witch data will be encrypted
	 * @param pubKeyStream - public key to encrypt {@link InputStream}
	 * @param privKeyStream - private key for signing
	 * @param pass - password for private key
	 * @throws CryptoException if any error occurred
	 */
	@SuppressWarnings("unchecked")
	public void encrypt(OutputStream out, InputStream data, String fileName, InputStream pubKeyStream,
			InputStream privKeyStream, String pass) throws CryptoException {

		OutputStream output = out;
		if (armor) {
			output = new ArmoredOutputStream(out);
		}

		PGPEncryptedDataGenerator encryptedDataGenerator = null;
		OutputStream encryptedOutputStream = null;
		PGPCompressedDataGenerator compressedDataGenerator = null;
		OutputStream compressedOutputStream = null;
		PGPSignatureGenerator signatureGenerator = null;
		PGPLiteralDataGenerator literalDataGenerator = null;
		OutputStream literalOutputStream = null;

		PGPPublicKey encryptionKey = null;
		PGPSecretKey signatureKey = null;

		boolean wasError = false;

		try {
			try {
				encryptionKey = readPublicKey(pubKeyStream);
			} catch (IOException e) {
				throw new CryptoException("Error reading public key", e);
			}
			if (encryptionKey == null) {
				throw new CryptoException("Public key not found");
			}

			if (privKeyStream != null) {
				try {
					signatureKey = readPrivateKey(privKeyStream);
				} catch (Exception e) {
					throw new CryptoException("Error reading public key", e);
				}
				if (signatureKey == null) {
					throw new CryptoException("Private key not found");
				}
			}

			encryptedDataGenerator =
					new PGPEncryptedDataGenerator(symmetricAlgorithm, false, new SecureRandom(), "BC");
			encryptedDataGenerator.addMethod(encryptionKey);
			encryptedOutputStream = encryptedDataGenerator.open(output, new byte[BUFFER_SIZE]);

			compressedDataGenerator = new PGPCompressedDataGenerator(compression);
			compressedOutputStream = compressedDataGenerator.open(encryptedOutputStream);

			if (signatureKey != null) {

				signatureGenerator = new PGPSignatureGenerator(signatureKey.getPublicKey().getAlgorithm(),
						hashAlgorithm, "BC");
				PGPPrivateKey signaturePrivateKey = signatureKey.extractPrivateKey(pass.toCharArray(), "BC");
				signatureGenerator.initSign(PGPSignature.BINARY_DOCUMENT, signaturePrivateKey);

				Iterator<String> it = signatureKey.getPublicKey().getUserIDs();
				if (it.hasNext()) {
					PGPSignatureSubpacketGenerator signatureSubpacketGenerator = new PGPSignatureSubpacketGenerator();
					signatureSubpacketGenerator.setSignerUserID(false, it.next());
					signatureGenerator.setHashedSubpackets(signatureSubpacketGenerator.generate());
				}
				signatureGenerator.generateOnePassVersion(false).encode(compressedOutputStream);
			}

			literalDataGenerator = new PGPLiteralDataGenerator();
			literalOutputStream = literalDataGenerator.open(compressedOutputStream, PGPLiteralData.BINARY, fileName,
					new Date(), new byte[BUFFER_SIZE]);

			int len;
			byte[] buf = new byte[128];
			while ((len = data.read(buf)) > -1) {
				literalOutputStream.write(buf, 0, len);
				if (signatureGenerator != null) {
					signatureGenerator.update(buf, 0, len);
				}
			}
		} catch (Exception e) {
			wasError = true;
			if (e instanceof CryptoException) {
				throw (CryptoException) e;
			}
			throw new CryptoException("Error while data encryption", e);
		} finally {
			closeStream(literalOutputStream);
			try {
				if (literalDataGenerator != null) {
					literalDataGenerator.close();
				}
			} catch (IOException e) {
			}
			if (signatureGenerator != null && !wasError) {
				try {
					signatureGenerator.generate().encode(compressedOutputStream);
				} catch (Exception e) {
					throw new CryptoException("Error creating signature", e);
				}
			}
			closeStream(compressedOutputStream);
			try {
				if (compressedDataGenerator != null) {
					compressedDataGenerator.close();
				}
			} catch (IOException e) {
			}
			closeStream(encryptedOutputStream);
			try {
				if (encryptedDataGenerator != null) {
					encryptedDataGenerator.close();
				}
			} catch (IOException e) {
			}
			closeStream(output);
		}
	}

	@SuppressWarnings("unchecked")
	private PGPSecretKey readPrivateKey(InputStream privateStream) throws IOException, PGPException {

		try {
			PGPSecretKeyRing ring = new PGPSecretKeyRing(PGPUtil.getDecoderStream(privateStream));

			Iterator<PGPSecretKey> keys = ring.getSecretKeys();
			while (keys.hasNext()) {
				PGPSecretKey key = keys.next();
				if (key.isSigningKey()) {
					return key;
				}
			}
			return null;
		} finally {
			privateStream.close();
		}
	}

	@SuppressWarnings("unchecked")
	private PGPPublicKey readPublicKey(InputStream pubKeyStream) throws IOException {
		try {
			PGPPublicKeyRing ring = new PGPPublicKeyRing(PGPUtil.getDecoderStream(pubKeyStream));

			Iterator<PGPPublicKey> keys = ring.getPublicKeys();
			while (keys.hasNext()) {
				PGPPublicKey key = keys.next();
				if (key.isEncryptionKey()) {
					return key;
				}
			}
			return null;
		} finally {
			pubKeyStream.close();
		}
	}

	/**
	 * @param armor the armor to set
	 */
	public void setArmor(boolean armor) {
		this.armor = armor;
	}

	/**
	 * @param compression the compression to set
	 */
	public void setCompression(int compression) {
		this.compression = compression;
	}

	/**
	 * @param hashAlgorithm the hashAlgorithm to set
	 */
	public void setHashAlgorithm(int hashAlgorithm) {
		this.hashAlgorithm = hashAlgorithm;
	}

	/**
	 * @param symmetricAlgorithm the symmetricAlgorithm to set
	 */
	public void setSymmetricAlgorithm(int symmetricAlgorithm) {
		this.symmetricAlgorithm = symmetricAlgorithm;
	}
}
