package ru.lehvolk.toolkit.ws;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

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

		WSPortsPoolImpl(GenericObjectPool<T> delegate, WSConfigurator configurator) {
			this.configurator = configurator;
			this.delegate = delegate;
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
	}

	public static interface WSPortCreator<T> {

		public T newPort();
	}

	public static class WSPortPoolBuilder<T> {

		GenericObjectPool<T> pool;
		WSConfigurator configurator;
		GenericObjectPool.Config poolConfig;
		PoolableObjectFactory<T> portFactory;
		WSClientConfiguration configuration;

		WSPortPoolBuilder() {
		}

		public WSPortPoolBuilder<T> usePool(GenericObjectPool<T> pool) {
			this.pool = pool;
			return this;
		}

		public WSPortPoolBuilder<T> usePoolConfig(GenericObjectPool.Config config) {
			this.poolConfig = config;
			return this;
		}

		public WSPortPoolBuilder<T> usePortFactory(PoolableObjectFactory<T> portFactory) {
			this.portFactory = portFactory;
			return this;
		}

		public WSPortPoolBuilder<T> useCreator(final WSPortCreator<T> portCreator) {
			this.portFactory = new PoolableObjectFactory<T>() {

				@Override
				public void activateObject(T obj) throws Exception {
				}

				@Override
				public void destroyObject(T obj) throws Exception {
				}

				@Override
				public T makeObject() throws Exception {
					return portCreator.newPort();
				}

				@Override
				public boolean validateObject(T obj) {
					return true;
				}

				@Override
				public void passivateObject(T obj) throws Exception {
				}
			};
			return this;
		}

		public WSPortPoolBuilder<T> useConfigurator(WSConfigurator configurator) {
			this.configurator = configurator;
			return this;
		}
	}

	public static <T> WSPortPoolBuilder<T> cxf() {
		return new WSPortPoolBuilder<T>().useConfigurator(new CXFWSConfigurator());
	}

	public static <T> WSPortPoolBuilder<T> jaxws() {
		return new WSPortPoolBuilder<T>().useConfigurator(new JAXWSConfigurator());
	}

	public static <T> WSPortPoolBuilder<T> simple() {
		return new WSPortPoolBuilder<T>().useConfigurator(new SimpleWSConfigurator());
	}

	public static <T> WSPortPoolBuilder<T> builder() {
		return new WSPortPoolBuilder<T>();
	}
}
