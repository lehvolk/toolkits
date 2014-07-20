package ru.lehvolk.toolkit.ws.log.cxf;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.cxf.helpers.CastUtils;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingMessage;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import ru.lehvolk.toolkit.ws.log.ProtocolLogger;
import ru.lehvolk.toolkit.ws.log.SOAPLogRecord;

/**
 * CXF interceptor for logging incoming message
 */
public class CXFInLogger extends AbstractPhaseInterceptor<Message> {

	private ProtocolLogger<SOAPLogRecord> logger;

	/**
	 * Construct instance
	 * @param logger logger isntance
	 */
	public CXFInLogger(ProtocolLogger<SOAPLogRecord> logger) {
		super(Phase.RECEIVE);
		this.logger = logger;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleMessage(Message message) throws Fault {
		if (message.containsKey(LoggingMessage.ID_KEY)) {
			return;
		}
		String id = (String) message.getExchange().get(LoggingMessage.ID_KEY);
		if (id == null) {
			id = LoggingMessage.nextId();
			message.getExchange().put(LoggingMessage.ID_KEY, id);
		}
		message.put(LoggingMessage.ID_KEY, id);
		String uri = (String) message.get(Message.REQUEST_URI);
		final SOAPLogRecord record = new SOAPLogRecord(uri, null);
		Map<String, List<String>> hds = CastUtils.cast((Map<?, ?>) message.get(Message.PROTOCOL_HEADERS));
		record.setHeaders(hds);
		InputStream is = message.getContent(InputStream.class);
		if (is != null) {
			CachedOutputStream bos = new CachedOutputStream();
			try {
				IOUtils.copy(is, bos);

				bos.flush();
				is.close();

				message.setContent(InputStream.class, bos.getInputStream());
				StringBuilder sb = new StringBuilder();
				bos.writeCacheTo(sb);
				bos.close();
				record.setBody(sb.toString());
			} catch (IOException e) {
				throw new Fault(e);
			}
		}

		if (logger != null) {
			logger.logResponse(record);
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
	 * This is hint for in-interceptors of CXF client. This method return true if obj instance of CXFInLogger
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj != null && (obj instanceof CXFInLogger)) {
			return true;
		}
		return false;
	}
}
