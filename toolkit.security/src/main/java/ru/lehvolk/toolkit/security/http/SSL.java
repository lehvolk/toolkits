package ru.lehvolk.toolkit.security.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import ru.lehvolk.toolkit.security.CryptoException;

/**
 * Utility class for working with SSL
 */
public final class SSL {

	private SSL() {
	}

	/**
	 * This wrapper class overwrites the default behavior of a X509KeyManager and
	 * always render a specific certificate whose alias matches that provided in the constructor<br>
	 * This need for IIS web server, who have strange behavior while check client certificate
	 */
	private static class AliasForcingKeyManager implements X509KeyManager {

		X509KeyManager baseKM = null;
		String alias = null;

		public AliasForcingKeyManager(X509KeyManager keyManager, String alias) {
			baseKM = keyManager;
			this.alias = alias;
		}

		/**
		 * Always render the specific alias provided in the constructor
		 */
		@Override
		public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
			return alias;
		}

		@Override
		public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
			return baseKM.chooseServerAlias(keyType, issuers, socket);
		}

		@Override
		public X509Certificate[] getCertificateChain(String alias) {
			return baseKM.getCertificateChain(alias);
		}

		@Override
		public String[] getClientAliases(String keyType, Principal[] issuers) {
			return baseKM.getClientAliases(keyType, issuers);
		}

		@Override
		public PrivateKey getPrivateKey(String alias) {
			return baseKM.getPrivateKey(alias);
		}

		@Override
		public String[] getServerAliases(String keyType, Principal[] issuers) {
			return baseKM.getServerAliases(keyType, issuers);
		}
	}

	private static class DummyHostVerifier implements HostnameVerifier {

		@Override
		public boolean verify(String name, SSLSession sess) {
			return true;
		}
	}

	/**
	 * Trust manager, makes all hosts trusted
	 */
	public static class DummyTrustManager implements X509TrustManager {

		/**
		 * @see javax.net.ssl.X509TrustManager#checkClientTrusted(java.security.cert.X509Certificate[], java.lang.String)
		 */
		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		/**
		 * @see javax.net.ssl.X509TrustManager#checkServerTrusted(java.security.cert.X509Certificate[], java.lang.String)
		 */
		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
		}

		/**
		 * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
		 */
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	}

	private static final HostnameVerifier dammyHostVerifier = new DummyHostVerifier();
	private static final X509TrustManager dammyTrustManager = new DummyTrustManager();
	/** Internal JAXWS property name, allows setup own implementation of HostnameVerifier */
	public static final String JAXWS_HOSTNAME_VERIFIER = "com.sun.xml.internal.ws.transport.https.client.hostname.verifier";

	/** Internal JAXWS property name, allows setup own implementation of SSLSocketFactory */
	public static final String JAXWS_SSL_SOCKET_FACTORY = "com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory";

	/**
	 * Creates {@link SSLContext} according with specified parameters
	 * @param conf - {@link SSLConfiguration}
	 * @return initialized SSLSocketFactory
	 * @throws CryptoException if error while building SSLContext occurred
	 */
	public static SSLContext createSSLContext(SSLConfiguration conf) throws CryptoException {

		try {
			KeyManager[] keyManagers = null;

			if (conf.getKeyStorePath() != null && !conf.getKeyStorePath().isEmpty()) {
				KeyStore keyStore = loadKeyStore(conf.getKeyStorePath(), conf.getKeyStorePassword());

				KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
				keyManagerFactory.init(keyStore, conf.getKeyStorePassword().toCharArray());
				keyManagers = keyManagerFactory.getKeyManagers();

				if (conf.getForcedAlias() != null) {
					for (int i = 0; i < keyManagers.length; i++) {
						if (keyManagers[i] instanceof X509KeyManager) {
							keyManagers[i] = new AliasForcingKeyManager((X509KeyManager) keyManagers[i], conf.getForcedAlias());
						}
					}
				}
			}

			TrustManager[] trustManagers = null;

			if (conf.getCheckHostTrusted()) {
				if (conf.getTrustStorePath() != null && !conf.getTrustStorePath().isEmpty()) {
					KeyStore trustStore;
					trustStore = loadKeyStore(conf.getTrustStorePath(), conf.getTrustStorePassword());
					TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory
							.getDefaultAlgorithm());
					trustManagerFactory.init(trustStore);
					trustManagers = trustManagerFactory.getTrustManagers();
				}
			} else {
				trustManagers = new TrustManager[] {dammyTrustManager};
			}

			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");

			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(keyManagers, trustManagers, secureRandom);

			return sslContext;
		} catch (Exception e) {
			throw new CryptoException("Can't create SSLContext", e);
		}

	}

	/**
	 * @return {@link HostnameVerifier} implementation which allow all hosts for certificate
	 */
	public static HostnameVerifier getAllowAllHostsVerifier() {
		return dammyHostVerifier;
	}

	/**
	 * load key store
	 * @param keyStorePath path to key store
	 * @param keyStorePass key store pass
	 * @return KeyStore instance
	 * @throws KeyStoreException key store exception
	 * @throws NoSuchAlgorithmException if can't read
	 * @throws CertificateException if can't read
	 * @throws IOException if can't read
	 */
	//Initialize KeyStore from specified path and using given password
	public static KeyStore loadKeyStore(String keyStorePath, String keyStorePass) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		FileInputStream fis = null;
		try {
			KeyStore keyStore = KeyStore.getInstance("JKS");
			fis = new FileInputStream(keyStorePath);
			keyStore.load(fis, keyStorePass != null ? keyStorePass.toCharArray() : null);
			return keyStore;
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}
}
