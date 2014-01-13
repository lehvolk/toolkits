package ru.lehvolk.toolkit.ws.impl;

import javax.net.ssl.SSLSocketFactory;

import ru.lehvolk.toolkit.ws.WSConfigurator;
import ru.lehvolk.toolkit.ws.log.ProtocolLogger;
import ru.lehvolk.toolkit.ws.log.SOAPLogRecord;

/**
 * Fake implementation of {@link WSConfigurator}
 */
public class FakeWSConfigurator implements WSConfigurator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T configurePort(T port, String wsAddress, long connTimeout, long readTimeout, SSLSocketFactory sf,
			boolean verifyHost) {
		return port;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void addLogging(T port, ProtocolLogger<SOAPLogRecord> logger) {
	}
}
