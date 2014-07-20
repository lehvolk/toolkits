package ru.lehvolk.toolkit.ws.log;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * HTTP communication data representation
 */
public class HttpLogRecord extends ProtocolLogRecord {

	private String headString;
	private Map<String, List<String>> headers;
	private String body;

	/**
	 * Construct empty instance
	 */
	public HttpLogRecord() {
	}

	/**
	 * Construct instance with parameters specified
	 * @param head - head string
	 * @param headers - headers
	 * @param body - body
	 */
	public HttpLogRecord(String head, Map<String, List<String>> headers, String body) {
		this.headString = head;
		this.headers = headers;
		this.body = body;
	}

	/**
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * @return the headers
	 */
	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	/**
	 * @return the headString
	 */
	public String getHeadString() {
		return headString;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	/**
	 * @param headString the headString to set
	 */
	public void setHeadString(String headString) {
		this.headString = headString;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (headString != null && !headString.isEmpty()) {
			sb.append(headString).append("\n");
		}
		if (headers != null && !headers.isEmpty()) {
			for (Entry<String, List<String>> header : headers.entrySet()) {
				sb.append(header.getKey()).append(": ").append(header.getValue().toString()).append("\n");
			}
		}
		if (body != null && !body.isEmpty()) {
			sb.append("\n").append(body);
		}
		return sb.toString();
	}
}
