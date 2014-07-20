package ru.lehvolk.toolkit.ws;

import javax.net.ssl.SSLSocketFactory;

import ru.lehvolk.toolkit.ws.log.ProtocolLogger;
import ru.lehvolk.toolkit.ws.log.SOAPLogRecord;

/**
 * Interface of class, for configuration web-service port, e.g. timeout, security aspects.
 */
public interface WSConfigurator {

	/**
	 * Configures web-service port
	 * 
	 * @param <T> - type of port
	 * @param port - web-service port
	 * @param wsAddress - address of web-service
	 * @param connTimeout - connection timeout
	 * @param readTimeout - socket read timeout
	 * @param sf - {@link SSLSocketFactory} instance
	 * @param verifyHost - host verification enabled
	 * @return - configured web-service port
	 */
	public <T> T configurePort(T port, String wsAddress, long connTimeout, long readTimeout, SSLSocketFactory sf,
			boolean verifyHost);

	/**
	 * add inn web-service port
	 * 
	 * @param logger protocol logger
	 * @param <T> - type of port
	 * @param port - web-service port
	 */
	/**
	 * Configures web-service port
	 * 
	 * @param <T> - type of port
	 * @param port - web-service port
	 * @param logger protocol logger
	 */
	public <T> void addLogging(T port, ProtocolLogger<SOAPLogRecord> logger);

}
