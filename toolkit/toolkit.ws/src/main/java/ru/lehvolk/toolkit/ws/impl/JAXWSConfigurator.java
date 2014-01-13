package ru.lehvolk.toolkit.ws.impl;

import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.ws.BindingProvider;

import ru.lehvolk.toolkit.security.http.SSL;
import ru.lehvolk.toolkit.ws.WSConfigurator;
import ru.lehvolk.toolkit.ws.log.ProtocolLogger;
import ru.lehvolk.toolkit.ws.log.SOAPLogRecord;

/**
 * JAX-WS specific implementation of ws configurator
 */
public class JAXWSConfigurator implements WSConfigurator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T configurePort(T port, String wsAddress, long connTimeout, long readTimeout, SSLSocketFactory sf,
			boolean verifyHost) {

		Map<String, Object> reqCtx = ((BindingProvider) port).getRequestContext();
		reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsAddress);

		if (sf == null) {
			try {
				reqCtx.put(SSL.JAXWS_SSL_SOCKET_FACTORY, SSLContext.getDefault().getSocketFactory());
			} catch (Exception e) {
				throw new IllegalArgumentException("fail to configure ws client by configuration", e);
			}
		} else {
			reqCtx.put(SSL.JAXWS_SSL_SOCKET_FACTORY, sf);
		}
		return port;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void addLogging(T port, ProtocolLogger<SOAPLogRecord> logger) {
	}
}
