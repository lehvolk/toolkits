package ru.lehvolk.toolkit.ws.log;
/**
 * Protocol request/response logger
 * 
 * @param <T> - generic record type
 */
public interface ProtocolLogger<T extends ProtocolLogRecord> {

	/**
	 * Perform request logging
	 * 
	 * @param record - data to log
	 */
	public void logRequest(T record);
	
	/**
	 * Perform response logging
	 * 
	 * @param record - data to log
	 */
	public void logResponse(T record);
}
