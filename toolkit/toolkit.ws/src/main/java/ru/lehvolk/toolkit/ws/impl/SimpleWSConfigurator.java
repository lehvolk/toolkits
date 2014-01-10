package ru.lehvolk.toolkit.ws.impl;

import java.util.Map;

import javax.net.ssl.SSLSocketFactory;
import javax.xml.ws.BindingProvider;

import ru.lehvolk.toolkit.ws.WSConfigurator;
import ru.lehvolk.toolkit.ws.log.ProtocolLogger;
import ru.lehvolk.toolkit.ws.log.SOAPLogRecord;

/**
 * simple implementation of {@link WSConfigurator}
 */
public class SimpleWSConfigurator implements WSConfigurator {

	/**
	 * @see WSConfigurator#configurePort(java.lang.Object, java.lang.String, long, long, javax.net.ssl.SSLSocketFactory,
	 *      boolean)
	 */
	@Override
	public <T> T configurePort(T port, String wsAddress, long connTimeout, long readTimeout, SSLSocketFactory sf,
			boolean verifyHost) {
		Map<String, Object> reqCtx = ((BindingProvider) port).getRequestContext();
		reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsAddress);
		return port;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void addLogging(T port, ProtocolLogger<SOAPLogRecord> logger) {
	}
}
