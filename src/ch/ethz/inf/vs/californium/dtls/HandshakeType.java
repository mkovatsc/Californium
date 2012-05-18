package ch.ethz.inf.vs.californium.dtls;

public enum HandshakeType {
	HELLO_REQUEST(0), CLIENT_HELLO(1), SERVER_HELLO(2), HELLO_VERIFY_REQUEST(3), CERTIFICATE(11), SERVER_KEY_EXCHANGE(12), CERTIFICATE_REQUEST(13), SERVER_HELLO_DONE(14), CERTIFICATE_VERIFY(15), CLIENT_KEY_EXCHANGE(16), FINISHED(20);

	private int code;

	private HandshakeType(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static HandshakeType getTypeByCode(int code) {
		switch (code) {
		case 0:
			return HandshakeType.HELLO_REQUEST;
		case 1:
			return HandshakeType.CLIENT_HELLO;
		case 2:
			return HandshakeType.SERVER_HELLO;
		case 3:
			return HandshakeType.HELLO_VERIFY_REQUEST;
		case 11:
			return HandshakeType.CERTIFICATE;
		case 12:
			return HandshakeType.SERVER_KEY_EXCHANGE;
		case 13:
			return HandshakeType.CERTIFICATE_REQUEST;
		case 14:
			return HandshakeType.SERVER_HELLO_DONE;
		case 15:
			return HandshakeType.CERTIFICATE_VERIFY;
		case 16:
			return HandshakeType.CLIENT_KEY_EXCHANGE;
		case 20:
			return HandshakeType.FINISHED;

		default:
			return null;
		}
	}

	@Override
	public String toString() {
		switch (code) {
		case 0:
			return "Hello Request (0)";
		case 1:
			return "Client Hello (1)";
		case 2:
			return "Server Hello (2)";
		case 3:
			return "Hello Verify Request (3)";
		case 11:
			return "Certificate (11)";
		case 12:
			return "Server Key Exchange (12)";
		case 13:
			return "Certificate Request (13)";
		case 14:
			return "Server Hello Done (14)";
		case 15:
			return "Certificate Verify (15)";
		case 16:
			return "Client Key Exchange (16)";
		case 20:
			return "Finished (20)";

		default:
			return "Unknown Handshake Message Type";
		}

	}

}
