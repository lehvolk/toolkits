package ru.lehvolk.toolkit.ws;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

import ru.lehvolk.toolkit.ws.PortsFactory.WSPortCreator;
import ru.lehvolk.toolkit.ws.impl.CXFWSConfigurator;
import ru.lehvolk.toolkit.ws.impl.JAXWSConfigurator;
import ru.lehvolk.toolkit.ws.impl.SimpleWSConfigurator;
import ru.lehvolk.toolkit.ws.log.ProtocolLogger;
import ru.lehvolk.toolkit.ws.log.SOAPLogRecord;

/**
 * Accessor for ws ports pool functionality
 */
public class WSPortPools {

	/**
	 * base implementation for ws port pool
	 * @param <T> port parameterization
	 */
	static class WSPortsPoolImpl<T> implements WSPortPool<T> {

		GenericObjectPool<T> delegate;
		WSConfigurator configurator;
		PortsFactory<T> factory;

		WSPortsPoolImpl(GenericObjectPool<T> delegate, PortsFactory<T> factory, WSConfigurator configurator) {
			this.configurator = configurator;
			this.delegate = delegate;
			this.factory = factory;
		}

		@Override
		public T getPort() {
			try {
				return delegate.borrowObject();
			} catch (Exception e) {
				throw new IllegalStateException("can't borrow object from WSPortPool", e);
			}
		}

		@Override
		public T getPort(ProtocolLogger<SOAPLogRecord> logger) {
			try {
				T port = delegate.borrowObject();
				configurator.addLogging(port, logger);
				return port;
			} catch (Exception e) {
				throw new IllegalStateException("can't borrow object from WSPortPool", e);
			}
		}

		@Override
		public void putPort(T port) {
			try {
				delegate.returnObject(port);
			} catch (Exception e) {
				throw new IllegalStateException("can't put object into WSPortPool", e);
			}
		}

		@Override
		public void shutdown() {
			try {
				delegate.close();
			} catch (Exception e) {
				throw new IllegalStateException("can't close WSPortPool", e);
			}
		}

		@Override
		public void clear() {
			delegate.clear();
		}

		public void refresh(WSClientConfiguration newConfig) {
			synchronized (delegate) {
				factory.init(newConfig);
				delegate.clear();
			}
		}
	}

	public static class WSPortPoolBuilder<T> {

		WSConfigurator configurator;
		GenericObjectPool.Config poolConfig;
		WSPortCreator<T> portCreator;
		WSClientConfiguration configuration;

		WSPortPoolBuilder() {
		}

		public WSPortPoolBuilder<T> poolConfig(GenericObjectPool.Config config) {
			this.poolConfig = config;
			return this;
		}

		public WSPortPoolBuilder<T> creator(WSPortCreator<T> portCreator) {
			this.portCreator = portCreator;
			return this;
		}

		public WSPortPoolBuilder<T> configurator(WSConfigurator configurator) {
			this.configurator = configurator;
			return this;
		}

		public WSPortPoolBuilder<T> clientConfig(WSClientConfiguration configuration) {
			this.configuration = configuration;
			return this;
		}

		public WSPortPool<T> create() {
			if (configuration == null) {
				throw new IllegalStateException("ws client configuration is null");
			}
			Config delegateConfig = null;
			if (poolConfig != null) {
				delegateConfig = poolConfig;
			} else {
				delegateConfig = new Config();
				delegateConfig.maxActive = configuration.getPoolConfig().getPoolSize();
			}
			if (portCreator == null) {
				throw new IllegalStateException("portCreator not specified");
			}
			PortsFactory<T> factory = new PortsFactory<>(portCreator, configurator, configuration);
			GenericObjectPool<T> delegate = new GenericObjectPool<>(factory, delegateConfig);
			return new WSPortsPoolImpl<>(delegate, factory, configurator);
		}
	}

	public static <T> WSPortPoolBuilder<T> cxf() {
		return new WSPortPoolBuilder<T>().configurator(new CXFWSConfigurator());
	}

	public static <T> WSPortPoolBuilder<T> jaxws() {
		return new WSPortPoolBuilder<T>().configurator(new JAXWSConfigurator());
	}

	public static <T> WSPortPoolBuilder<T> simple() {
		return new WSPortPoolBuilder<T>().configurator(new SimpleWSConfigurator());
	}

	public static <T> WSPortPoolBuilder<T> builder() {
		return new WSPortPoolBuilder<T>();
	}

	public <T> boolean update(WSPortPool<T> pool, WSClientConfiguration newConfig) {
		if (pool instanceof WSPortsPoolImpl) {
			((WSPortsPoolImpl<?>) pool).refresh(newConfig);
			return true;
		}
		return false;
	}
}
