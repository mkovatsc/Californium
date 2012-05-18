package ch.ethz.inf.vs.californium.dtls;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.Cipher;

import ch.ethz.inf.vs.californium.util.DatagramReader;
import ch.ethz.inf.vs.californium.util.DatagramWriter;

/**
 * If RSA is being used for key agreement and authentication, the client
 * generates a 48-byte premaster secret, encrypts it using the public key from
 * the server's certificate, and sends the result in an encrypted premaster
 * secret message. This structure is a variant of the {@link ClientKeyExchange}
 * message and is not a message in itself.
 * 
 * @author Stefan Jucker
 * 
 */
public class RSAClientKeyExchange extends ClientKeyExchange {

	private static final int LENGTH_BITS = 16;

	/**
	 * The latest (newest) version supported by the client. This is used to
	 * detect version rollback attacks.
	 */
	private ProtocolVersion clientVersion = new ProtocolVersion();

	/** 48 securely-generated random bytes */
	private byte[] premasterSecret = null;

	/**
	 * This random value is generated by the client and used to generate the
	 * master secret.
	 */
	private byte[] encrypted;

	/**
	 * Generates a random premaster key and encrypts it with the server's public
	 * key.
	 * 
	 * @param generator
	 *            a PRF to create premaster secret
	 * @param serverPublicKey
	 *            the server's public key
	 */
	public RSAClientKeyExchange(SecureRandom generator, PublicKey serverPublicKey) {
		// generate premaster secret
		this.premasterSecret = new byte[48];
		generator.nextBytes(premasterSecret);

		// overwrite the first 2 bytes with client version
		this.premasterSecret[0] = (byte) clientVersion.getMajor();
		this.premasterSecret[1] = (byte) clientVersion.getMinor();

		// public-key encryption
		try {
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);

			this.encrypted = cipher.doFinal(premasterSecret);
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Receives the encrypted premaster secret and decrypts it with server's
	 * public key.
	 * 
	 * @param encrypted
	 *            the encrypted premaster key
	 * @param serverPrivateKey
	 *            the server's private key
	 */
	public RSAClientKeyExchange(byte[] encrypted) {
		this.encrypted = encrypted;
	}

	public byte[] getPremasterSecret(PrivateKey privateKey) {
		if (premasterSecret == null) {
			// decrypt it first with private key
			try {
				Cipher cipher = Cipher.getInstance("RSA");
				cipher.init(Cipher.DECRYPT_MODE, privateKey);
				
				premasterSecret = cipher.doFinal(encrypted);
			} catch (GeneralSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return premasterSecret;
	}

	@Override
	public byte[] toByteArray() {
		DatagramWriter writer = new DatagramWriter();

		writer.writeBytes(super.toByteArray());

		writer.write(encrypted.length, LENGTH_BITS);
		writer.writeBytes(encrypted);

		return writer.toByteArray();
	}

	public static HandshakeMessage fromByteArray(byte[] byteArray) {
		DatagramReader reader = new DatagramReader(byteArray);

		int length = reader.read(LENGTH_BITS);
		byte[] encrypted = reader.readBytes(length);

		return new RSAClientKeyExchange(encrypted);

	}

	@Override
	public int getMessageLength() {
		return 2 + encrypted.length;
	}

	public ProtocolVersion getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(ProtocolVersion clientVersion) {
		this.clientVersion = clientVersion;
	}

	public byte[] getPremasterSecret() {
		return premasterSecret;
	}

	public void setPremasterSecret(byte[] premasterSecret) {
		this.premasterSecret = premasterSecret;
	}

	public byte[] getEncrypted() {
		return encrypted;
	}

	public void setEncrypted(byte[] encrypted) {
		this.encrypted = encrypted;
	}

}
