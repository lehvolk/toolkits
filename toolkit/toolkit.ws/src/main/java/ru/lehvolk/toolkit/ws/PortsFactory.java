package ru.lehvolk.toolkit.ws;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.pool.PoolableObjectFactory;

import ru.lehvolk.toolkit.security.CryptoException;
import ru.lehvolk.toolkit.security.http.SSL;

/**
 * ports factory implementation based on ws port creator
 * @param <T> port type
 */
public class PortsFactory<T> implements PoolableObjectFactory<T> {

	/**
	 * port creator
	 * @param <T> port
	 */
	public static interface WSPortCreator<T> {

		/**
		 * @return new unconfigured port instance
		 */
		public T newPort();
	}

	private final WSPortCreator<T> creator;
	private final WSConfigurator configurator;
	private WSClientConfiguration configuration;
	private SSLSocketFactory sslFactory;

	public PortsFactory(WSPortCreator<T> creator, WSConfigurator configurator, WSClientConfiguration configuration) {
		this.creator = creator;
		this.configurator = configurator;
		init(configuration);
	}

	@Override
	public T makeObject() throws Exception {
		T port = creator.newPort();
		boolean verifyHost = configuration.getSslConfiguration() == null ? false : configuration.getSslConfiguration()
				.getVerifyHost();
		configurator.configurePort(port, configuration.getEndpointURL(),
				configuration.getConnectionTimeout(), configuration.getSocketReadTimeout(), sslFactory, verifyHost);
		return port;
	}

	@Override
	public void destroyObject(T obj) throws Exception {
	}

	@Override
	public boolean validateObject(T obj) {
		return true;
	}

	@Override
	public void activateObject(T obj) throws Exception {
	}

	@Override
	public void passivateObject(T obj) throws Exception {
	}

	/**
	 * apply configuration to factory
	 */
	public void init(WSClientConfiguration configuration) {
		this.configuration = configuration;
		if (configuration.getSslConfiguration() != null && configuration.getSslConfiguration().getEnabled()) {
			try {
				SSLContext ctx = SSL.createSSLContext(configuration.getSslConfiguration());
				sslFactory = ctx.getSocketFactory();
			} catch (CryptoException e) {
				throw new IllegalArgumentException("Error ssl initialization", e);
			}
		} else {
			sslFactory = null;
		}
	}
}
