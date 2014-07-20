package ru.lehvolk.toolkit.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration of WS-ports pool
 */
@XmlRootElement(name = "pool")
@XmlAccessorType(XmlAccessType.FIELD)
public class PortsPoolConfiguration {

	@XmlAttribute(name = "size", required = false)
	private Integer poolSize = 300;

	/**
	 * @return the poolSize
	 */
	public Integer getPoolSize() {
		return poolSize;
	}

	/**
	 * @param poolSize the poolSize to set
	 */
	public void setPoolSize(Integer poolSize) {
		this.poolSize = poolSize;
	}
}
