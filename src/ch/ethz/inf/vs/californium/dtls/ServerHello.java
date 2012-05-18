package ch.ethz.inf.vs.californium.dtls;

import java.util.Arrays;
import java.util.logging.Logger;

import ch.ethz.inf.vs.californium.util.DatagramReader;
import ch.ethz.inf.vs.californium.util.DatagramWriter;

/**
 * The server will send this message in response to a {@link ClientHello}
 * message when it was able to find an acceptable set of algorithms. If it
 * cannot find such a match, it will respond with a handshake failure alert.
 * 
 * @author Stefan Jucker
 * 
 */
public class ServerHello extends HandshakeMessage {
	
	// Logging ///////////////////////////////////////////////////////////

	protected static final Logger LOG = Logger.getLogger(ServerHello.class.getName());

	// DTLS-specific constants ///////////////////////////////////////////

	private static final int VERSION_BITS = 8; // for major and minor each

	private static final int RANDOM_BYTES = 32;

	private static final int SESSION_ID_LENGTH_BITS = 8;

	private static final int CIPHER_SUITE_BITS = 16;

	private static final int COMPRESSION_METHOD_BITS = 8;

	// Members ///////////////////////////////////////////////////////////

	/**
	 * This field will contain the lower of that suggested by the client in the
	 * {@link ClientHello} and the highest supported by the server.
	 */
	private ProtocolVersion serverVersion;

	/**
	 * This structure is generated by the server and MUST be independently
	 * generated from the {@link ClientHello}.random.
	 */
	private Random random;

	/**
	 * This is the identity of the session corresponding to this connection.
	 */
	private SessionId sessionId;

	/**
	 * The single {@link CipherSuite} selected by the server from the list in
	 * {@link ClientHello}.cipher_suites.
	 */
	private CipherSuite cipherSuite;

	/**
	 * The single compression algorithm selected by the server from the list in
	 * ClientHello.compression_methods.
	 */
	private CompressionMethod compressionMethod;

	// TODO extensions

	public ServerHello() {

	}

	public ServerHello(ProtocolVersion version, Random random, SessionId sessionId, CipherSuite cipherSuite, CompressionMethod compressionMethod) {
		this.serverVersion = version;
		this.random = random;
		this.sessionId = sessionId;
		this.cipherSuite = cipherSuite;
		this.compressionMethod = compressionMethod;
	}

	@Override
	public byte[] toByteArray() {
		DatagramWriter writer = new DatagramWriter();
		writer.writeBytes(super.toByteArray());

		writer.write(serverVersion.getMajor(), VERSION_BITS);
		writer.write(serverVersion.getMinor(), VERSION_BITS);

		writer.writeBytes(random.getRandomBytes());

		writer.write(sessionId.length(), SESSION_ID_LENGTH_BITS);
		writer.writeBytes(sessionId.getSessionId());

		writer.write(cipherSuite.getCode(), CIPHER_SUITE_BITS);
		writer.write(compressionMethod.getCode(), COMPRESSION_METHOD_BITS);

		// TODO extensions

		return writer.toByteArray();
	}

	public static HandshakeMessage fromByteArray(byte[] byteArray) {
		DatagramReader reader = new DatagramReader(byteArray);

		int major = reader.read(VERSION_BITS);
		int minor = reader.read(VERSION_BITS);
		ProtocolVersion version = new ProtocolVersion(major, minor);

		Random random = new Random(reader.readBytes(RANDOM_BYTES));

		int sessionIdLength = reader.read(SESSION_ID_LENGTH_BITS);
		SessionId sessionId = new SessionId(reader.readBytes(sessionIdLength));

		CipherSuite cipherSuite = CipherSuite.getTypeByCode(reader.read(CIPHER_SUITE_BITS));
		CompressionMethod compressionMethod = CompressionMethod.getTypeByCode(reader.read(COMPRESSION_METHOD_BITS));

		// TODO extensions

		return new ServerHello(version, random, sessionId, cipherSuite, compressionMethod);
	}

	@Override
	public HandshakeType getMessageType() {
		return HandshakeType.SERVER_HELLO;
	}

	@Override
	public int getMessageLength() {
		// fixed sizes: version (2) + random (32) + session ID length (1) +
		// cipher suit (2) + compression method (1) = 38
		// variable sizes: session ID

		// TODO extensions
		return 38 + sessionId.length();
	}

	public ProtocolVersion getServerVersion() {
		return serverVersion;
	}

	public void setServerVersion(ProtocolVersion serverVersion) {
		this.serverVersion = serverVersion;
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	public SessionId getSessionId() {
		return sessionId;
	}

	public void setSessionId(SessionId sessionId) {
		this.sessionId = sessionId;
	}

	public CipherSuite getCipherSuite() {
		return cipherSuite;
	}

	public void setCipherSuite(CipherSuite cipherSuite) {
		this.cipherSuite = cipherSuite;
	}

	public CompressionMethod getCompressionMethod() {
		return compressionMethod;
	}

	public void setCompressionMethod(CompressionMethod compressionMethod) {
		this.compressionMethod = compressionMethod;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append("\t\tServer Version: " + serverVersion.getMajor() + ", " + serverVersion.getMinor() + "\n");
		sb.append("\t\tServer Random: " + Arrays.toString(random.getRandomBytes()) + "\n");
		sb.append("\t\tSession ID Length: " + sessionId.length() + "\n");
		if (sessionId.length() > 0) {
			sb.append("\t\tSession ID: " + Arrays.toString(sessionId.getSessionId()) + "\n");
		}
		sb.append("\t\tCipher Suite: " + cipherSuite.toString() + "\n");
		sb.append("\t\tCompression Method: " + compressionMethod.toString() + "\n");

		return sb.toString();
	}

}