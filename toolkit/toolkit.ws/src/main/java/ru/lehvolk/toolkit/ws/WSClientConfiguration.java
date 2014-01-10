package ru.lehvolk.toolkit.ws;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import ru.lehvolk.toolkit.security.http.SSLConfiguration;

/**
 * Configuration of clients of OperatorAPI protocol
 */
@XmlRootElement(name = "ws-client")
@XmlAccessorType(XmlAccessType.FIELD)
public class WSClientConfiguration implements Serializable {

	private static final long serialVersionUID = -499127037976521464L;

	@XmlElement(name = "connectionTimeout", required = false)
	private Long connectionTimeout = 30 * 1000L; //30 seconds

	@XmlElement(name = "endpoint-url", required = false)
	private String operatorAddress;

	@XmlElement(name = "PoolConfiguration", required = true)
	private PortsPoolConfiguration poolConfig;

	@XmlElement(name = "socket-read-timeout", required = false)
	private Long socketReadTimeout = 60 * 1000L; //60 seconds

	@XmlElement(name = "ssl", required = false)
	private SSLConfiguration sslConfiguration;

	@XmlElement(name = "protocol-version")
	private String protocolVersion = "1.1";

	/**
	 * @return the connectionTimeout
	 */
	public Long getConnectionTimeout() {
		return connectionTimeout;
	}

	/**
	 * @return the operatorAddress
	 */
	public String getOperatorAddress() {
		return operatorAddress;
	}

	/**
	 * @return the poolConfig
	 */
	public PortsPoolConfiguration getPoolConfig() {
		return poolConfig;
	}

	/**
	 * @return the protocolVersion
	 */
	public String getProtocolVersion() {
		return protocolVersion;
	}

	/**
	 * @return the socketReadTimeout
	 */
	public Long getSocketReadTimeout() {
		return socketReadTimeout;
	}

	/**
	 * @return the sslConfiguration
	 */
	public SSLConfiguration getSslConfiguration() {
		return sslConfiguration;
	}

	/**
	 * @param connectionTimeout the connectionTimeout to set
	 */
	public void setConnectionTimeout(Long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	/**
	 * @param operatorAddress the operatorAddress to set
	 */
	public void setOperatorAddress(String operatorAddress) {
		this.operatorAddress = operatorAddress;
	}

	/**
	 * @param poolConfig the poolConfig to set
	 */
	public void setPoolConfig(PortsPoolConfiguration poolConfig) {
		this.poolConfig = poolConfig;
	}

	/**
	 * @param protocolVersion the protocolVersion to set
	 */
	public void setProtocolVersion(String protocolVersion) {
		this.protocolVersion = protocolVersion;
	}

	/**
	 * @param socketReadTimeout the socketReadTimeout to set
	 */
	public void setSocketReadTimeout(Long socketReadTimeout) {
		this.socketReadTimeout = socketReadTimeout;
	}

	/**
	 * @param sslConfiguration the sslConfiguration to set
	 */
	public void setSslConfiguration(SSLConfiguration sslConfiguration) {
		this.sslConfiguration = sslConfiguration;
	}
}
