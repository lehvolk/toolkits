package ru.lehvolk.toolkit.ws.impl;

import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.ConnectionType;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import ru.lehvolk.toolkit.ws.WSConfigurator;
import ru.lehvolk.toolkit.ws.log.ProtocolLogger;
import ru.lehvolk.toolkit.ws.log.SOAPLogRecord;
import ru.lehvolk.toolkit.ws.log.cxf.CXFInLogger;
import ru.lehvolk.toolkit.ws.log.cxf.CXFOutLogger;

/**
 * CXF-specific implementation of {@link WSConfigurator}
 */
public class CXFWSConfigurator implements WSConfigurator {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T configurePort(T port, String wsAddress, long connTimeout, long readTimeout, SSLSocketFactory sf,
			boolean verifyHost) {
		Map<String, Object> reqCtx = ((BindingProvider) port).getRequestContext();
		reqCtx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsAddress);

		Client client = ClientProxy.getClient(port);
		HTTPConduit http = (HTTPConduit) client.getConduit();

		HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
		httpClientPolicy.setConnectionTimeout(connTimeout);
		httpClientPolicy.setReceiveTimeout(readTimeout);
		httpClientPolicy.setConnection(ConnectionType.CLOSE);

		http.setClient(httpClientPolicy);

		TLSClientParameters tls = new TLSClientParameters();

		if (sf != null) {
			tls.setSSLSocketFactory(sf);
			tls.setDisableCNCheck(!verifyHost);
			http.setTlsClientParameters(tls);
		} else {
			try {
				tls.setSSLSocketFactory(SSLContext.getDefault().getSocketFactory());
				http.setTlsClientParameters(tls);
			} catch (Exception e) {
			}
		}
		return port;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> void addLogging(T port, ProtocolLogger<SOAPLogRecord> logger) {
		if (logger != null) {
			Client client = ClientProxy.getClient(port);
			CXFInLogger inLogger = new CXFInLogger(logger);
			CXFOutLogger outLogger = new CXFOutLogger(logger);
			replaceOldLogger(client.getInInterceptors(), inLogger);
			replaceOldLogger(client.getOutInterceptors(), outLogger);
			client.getInInterceptors().add(inLogger);
			client.getOutInterceptors().add(outLogger);
		}
	}

	@SuppressWarnings({"rawtypes"})
	private <T> void replaceOldLogger(List<Interceptor> list, Interceptor interceptor) {
		// this code is hint for removing old interceptor from list. This is because of 
		// using special implementation of list in CXF
		// see equals method of CXFInLogger and CXFOutLogger 
		list.remove(interceptor);
	}
}
