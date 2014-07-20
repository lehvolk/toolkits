package ru.lehvolk.toolkit.ws;

import ru.lehvolk.toolkit.ws.log.ProtocolLogger;
import ru.lehvolk.toolkit.ws.log.SOAPLogRecord;

/**
 * Interface for WS ports pool
 * @param <T> - generic type of port
 */
public interface WSPortPool<T> {

	/**
	 * get port by address from configuration
	 * @return instance of port
	 */
	public T getPort();

	/**
	 * get port by address from configuration
	 * @param logger - protocol logger
	 * @return instance of port
	 */
	public T getPort(ProtocolLogger<SOAPLogRecord> logger);

	/**
	 * @param port - web-service port
	 */
	public void putPort(T port);

	/**
	 * Shutdowns pool
	 */
	public void shutdown();

	/**
	 * clear pool from all elements
	 */
	public void clear();
}
