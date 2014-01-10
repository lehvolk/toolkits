package ru.lehvolk.toolkit.ws;

import ru.lehvolk.toolkit.ws.log.ProtocolLogger;
import ru.lehvolk.toolkit.ws.log.SOAPLogRecord;

/**
 * Interface for WS ports pool
 * @param <T> - generic type of port
 */
public interface WebServicePortPool<T> {

	/**
	 * get port by address from configuration
	 * @return instance of port
	 */
	public WebServicePort<T> getPort();

	/**
	 * get port by address from configuration
	 * @param logger - protocol logger
	 * @return instance of port
	 */
	public WebServicePort<T> getPort(ProtocolLogger<SOAPLogRecord> logger);

	/**
	 * @param address - web-service address
	 * @return port for given address
	 */
	public WebServicePort<T> getPort(String address);

	/**
	 * @param address - web-service address
	 * @param logger - protocol logger
	 * @return port for given address
	 */
	public WebServicePort<T> getPort(String address, ProtocolLogger<SOAPLogRecord> logger);

	/**
	 * Initialize service
	 */
	public void init();

	/**
	 * @param port - web-service port
	 */
	public void putPort(WebServicePort<T> port);

	/**
	 * Reinitialize pool with new configuration
	 */
	public void reset();

	/**
	 * Shutdowns pool
	 */
	public void shutdown();

}
