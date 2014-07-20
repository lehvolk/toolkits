package ru.lehvolk.toolkit.ws.log.cxf;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingMessage;
import org.apache.cxf.interceptor.StaxOutInterceptor;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import ru.lehvolk.toolkit.ws.log.ProtocolLogger;
import ru.lehvolk.toolkit.ws.log.SOAPLogRecord;

/**
 * CXF out logger
 */
public class CXFOutLogger extends AbstractPhaseInterceptor<Message> {

	private ProtocolLogger<SOAPLogRecord> logger;

	/**
	 * Construct instance
	 * @param logger logger isntance
	 */
	public CXFOutLogger(ProtocolLogger<SOAPLogRecord> logger) {
		super(Phase.PRE_STREAM);
		addBefore(StaxOutInterceptor.class.getName());
		this.logger = logger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleMessage(Message message) throws Fault {
		try {
			final OutputStream os = message.getContent(OutputStream.class);
			if (os != null) {
				final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);
				message.setContent(OutputStream.class, newOut);
				newOut.registerCallback(new LoggingCallback(message, os));
			}
		} catch (Exception e) {
			Logger.getLogger(getClass().getName()).log(Level.WARNING,
					"exception catched during log message in interaction log", e);
		}
	}

	class LoggingCallback implements CachedOutputStreamCallback {

		private final Message message;
		private final OutputStream origStream;

		public LoggingCallback(final Message msg, final OutputStream os) {
			this.message = msg;
			this.origStream = os;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onFlush(CachedOutputStream cos) {
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onClose(CachedOutputStream cos) {
			String id = (String) message.getExchange().get(LoggingMessage.ID_KEY);
			if (id == null) {
				id = LoggingMessage.nextId();
				message.getExchange().put(LoggingMessage.ID_KEY, id);
			}

			String address = (String) message.get(Message.ENDPOINT_ADDRESS);
			QName qOperation = (QName) message.get(Message.WSDL_OPERATION);

			final SOAPLogRecord record = new SOAPLogRecord(address, qOperation == null ? null : qOperation.getLocalPart());

			Map<String, List<String>> hds = CastUtils.cast((Map<?, ?>) message.get(Message.PROTOCOL_HEADERS));
			Map<String, List<String>> headers = new HashMap<>(hds);
			String ct = (String) message.get(Message.CONTENT_TYPE);
			if (ct != null) {
				headers.put(Message.CONTENT_TYPE, Arrays.asList(ct));
			}

			StringBuilder sb = new StringBuilder();
			try {
				cos.writeCacheTo(sb);
				record.setBody(sb.toString());
			} catch (Exception ex) {
				//ignore
			}

			if (logger != null) {
				logger.logRequest(record);
			}
			try {
				//empty out the cache
				cos.lockOutputStream();
				cos.resetOut(null, false);
			} catch (Exception ex) {
				//ignore
			}
			message.setContent(OutputStream.class, origStream);
		}
	}

	/**
	 * always return 1
	 */
	@Override
	public int hashCode() {
		return 1;
	}

	/**
	 * This is hint for out-interceptors of CXF client. This method return true if obj instance of CXFOutLogger
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && (obj instanceof CXFOutLogger)) {
			return true;
		}
		return false;
	}
}
