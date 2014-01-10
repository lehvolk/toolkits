package ru.lehvolk.toolkit.ws;

/**
 * Wrapper of web-service port
 * @param <T> - type of port
 */
public class WebServicePort<T> {

	private final T port;
	private final String soapVersion;
	private final String address;

	/**
	 * Constructs instance with parameters specified
	 * @param port - web-service port
	 * @param soapVersion - version of soap protocol
	 * @param address - web-service address
	 */
	public WebServicePort(T port, String soapVersion, String address) {
		this.port = port;
		this.soapVersion = soapVersion;
		this.address = address;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return the port
	 */
	public T getPort() {
		return port;
	}

	/**
	 * @return the soapVersion
	 */
	public String getSoapVersion() {
		return soapVersion;
	}
}
