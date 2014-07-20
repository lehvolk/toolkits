package ru.lehvolk.toolkit.ws.log;

/**
 * CXF log record
 */
public class SOAPLogRecord extends HttpLogRecord {

	private final String address;
	private final String operation;

	/**
	 * @param address address
	 * @param operation operation
	 */
	public SOAPLogRecord(String address, String operation) {
		this.address = address;
		this.operation = operation;
	}

	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @return the operation
	 */
	public String getOperation() {
		return operation;
	}

}
