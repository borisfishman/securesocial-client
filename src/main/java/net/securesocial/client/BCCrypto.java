package net.securesocial.client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

public class BCCrypto implements CryptoServiceInterface {

	
private static final String SECURE_SOCIAL_NET_CLIENT = "SecureSocial.net Client";
	
	class MyArmoredOutputStream extends ArmoredOutputStream {

		public MyArmoredOutputStream(OutputStream out) {
			super(out);
			super.setHeader("Version", SECURE_SOCIAL_NET_CLIENT);
		}

	}
	
	private static final int BUFFER_SIZE = 8192;

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public class KeyPair implements net.securesocial.client.CryptoServiceInterface.KeyPairInterface {
		private String privateKey;
		private String publicKey;

		@Override
		public String getPrivateKey() {
			return privateKey;
		}

		public void setPrivateKey(String privateKey) {
			if (StringUtils.isEmpty(privateKey))
				throw new IllegalArgumentException("value is required");
			this.privateKey = privateKey;
		}

		@Override
		public String getPublicKey() {
			return publicKey;
		}

		public void setPublicKey(String publicKey) {
			if (StringUtils.isEmpty(publicKey))
				throw new IllegalArgumentException("value is required");
			this.publicKey = publicKey;
		}

	}

	@Override
	public KeyPairInterface generateKeyPair(String id, String password) {
		try {

			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
			generator.initialize(2048);
			java.security.KeyPair internalPair = generator.generateKeyPair();

			PGPSecretKey secretKey = new PGPSecretKey(PGPSignature.DEFAULT_CERTIFICATION, PGPPublicKey.RSA_GENERAL, internalPair.getPublic(), internalPair.getPrivate(), new Date(), id, PGPEncryptedData.AES_256, password.toCharArray(), null, null, new SecureRandom(), "BC");

			KeyPair result = new BCCrypto().new KeyPair();
			ByteArrayOutputStream privateStreamBytes = getPrivateKeyBytes(secretKey);
			result.setPrivateKey(new String(privateStreamBytes.toByteArray()));

			ByteArrayOutputStream publicStreamBytes = new ByteArrayOutputStream();
			ArmoredOutputStream publicStream = new BCCrypto().new MyArmoredOutputStream(publicStreamBytes);
			secretKey.getPublicKey().encode(publicStream);
			publicStream.close();
			result.setPublicKey(new String(publicStreamBytes.toByteArray()));

			return result;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	
	

	private PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass) throws Exception {
		PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);
		if (pgpSecKey == null) {
			return null;
		}
		return pgpSecKey.extractPrivateKey(pass, "BC");
	}

	@Override
	public String decrypt(String in, String key, String password) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(in.getBytes());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		decrypt(bais, baos, key, password);
		return new String(baos.toByteArray());
	}

	/*
	 * public String decryptWithPassword(String in, String password) throws
	 * Exception { ByteArrayInputStream bais = new
	 * ByteArrayInputStream(in.getBytes()); ByteArrayOutputStream baos = new
	 * ByteArrayOutputStream(); decryptWithPassword(bais, baos, password);
	 * return new String(baos.toByteArray()); }
	 */
	@Override
	public String decryptWithPassphrase(String in, String password) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(in.getBytes()));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		decryptWithPassphrase(bais, baos, password);
		return new String(baos.toByteArray());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void decrypt(InputStream input, OutputStream output, String key, String password) throws Exception {
		InputStream in = PGPUtil.getDecoderStream(input);
		PGPObjectFactory factory = new PGPObjectFactory(in);
		PGPEncryptedDataList enc;
		Object o = factory.nextObject();
		if (o instanceof PGPEncryptedDataList) {
			enc = (PGPEncryptedDataList) o;
		} else {
			enc = (PGPEncryptedDataList) factory.nextObject();
		}
		Iterator it = enc.getEncryptedDataObjects();
		PGPPrivateKey sKey = null;
		PGPPublicKeyEncryptedData pbe = null;

		while (sKey == null && it.hasNext()) {
			pbe = (PGPPublicKeyEncryptedData) it.next();
			sKey = findSecretKey(new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(key.getBytes()))), pbe.getKeyID(), password.toCharArray());
		}

		if (sKey == null) {
			throw new IllegalArgumentException("Failed to find private key with ID " + pbe.getKeyID());
		}

		InputStream clear = pbe.getDataStream(sKey, "BC");

		PGPObjectFactory plainFact = new PGPObjectFactory(clear);

		PGPCompressedData cData = (PGPCompressedData) plainFact.nextObject();

		InputStream compressedStream = new BufferedInputStream(cData.getDataStream());
		PGPObjectFactory pgpFact = new PGPObjectFactory(compressedStream);

		Object message = pgpFact.nextObject();

		if (message instanceof PGPLiteralData) {
			PGPLiteralData ld = (PGPLiteralData) message;

			IOUtils.copy(ld.getInputStream(), output);

			IOUtils.closeQuietly(output);

		} else if (message instanceof PGPOnePassSignatureList) {
			throw new Exception("encrypted message contains a signed message - not literal data.");
		} else {
			throw new Exception("message is not a simple encrypted file - type unknown.");
		}

		if (pbe.isIntegrityProtected()) {
			if (!pbe.verify()) {
				throw new Exception("message failed integrity check");
			}
		} else {
			throw new Exception("no integrity check");
		}
	}

	@Override
	public String encrypt(String in, String key) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(in.getBytes());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encrypt(bais, out, key);
		return new String(out.toByteArray());
	}

	@Override
	public String encryptWithPassphrase(String in, String password) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(in.getBytes());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		encryptWithPassphrase(bais, out, password);
		return new String(Base64.encode(out.toByteArray()));
	}

	@Override
	public void encrypt(InputStream input, OutputStream output, String key) throws Exception {

		PGPPublicKey encKey = getPublicFromString(key);
		OutputStream out = new BCCrypto().new MyArmoredOutputStream(output);

		// Init encrypted data generator
		PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(PGPEncryptedData.AES_256, true, new SecureRandom(), "BC");

		encryptedDataGenerator.addMethod(encKey);

		OutputStream encryptedOut = encryptedDataGenerator.open(out, new byte[BUFFER_SIZE]);

		// Init compression
		PGPCompressedDataGenerator compressedDataGenerator = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
		OutputStream compressedOut = compressedDataGenerator.open(encryptedOut);

		PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
		OutputStream literalOut = literalDataGenerator.open(compressedOut, PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, new Date(), new byte[BUFFER_SIZE]);
		IOUtils.copy(input, literalOut);

		IOUtils.closeQuietly(literalOut);

		literalDataGenerator.close();

		IOUtils.closeQuietly(compressedOut);

		compressedDataGenerator.close();

		IOUtils.closeQuietly(encryptedOut);

		encryptedDataGenerator.close();

		IOUtils.closeQuietly(input);

		IOUtils.closeQuietly(out);
	}

	@Override
	public void encryptWithPassphrase(InputStream input, OutputStream output, String passPhrase) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(passPhrase), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
		OutputStream os = new CipherOutputStream(output, cipher);
		IOUtils.copy(input, os);
		IOUtils.closeQuietly(os);
	}

	@Override
	public void decryptWithPassphrase(InputStream encrypted, OutputStream out, String passPhrase) throws Exception {
		SecretKeySpec skeySpec = new SecretKeySpec(getRawKey(passPhrase), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec);
		IOUtils.copy(new CipherInputStream(encrypted, cipher), out);
		IOUtils.closeQuietly(out);
	}

	private byte[] getRawKey(String s) throws Exception {
		/*
		byte[] seed = (s + "abba").getBytes();
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);
		kgen.init(128, sr); // 192 and 256 bits may not be available
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();
		return raw;
		*/
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] bytes = md.digest((s + "abba").getBytes());
		return Arrays.copyOf(bytes, 16);
	}

	@Override
	public String sign(String privateKey, String password, byte[] digestBytes) throws Exception {

		PGPSecretKey secretKey = secretKeyFromPrivateKeyString(privateKey);
		PGPSignatureGenerator sigGenerator = new PGPSignatureGenerator(secretKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA256, new BouncyCastleProvider());

		sigGenerator.initSign(PGPSignature.BINARY_DOCUMENT, secretKey.extractPrivateKey(password.toCharArray(), new BouncyCastleProvider()));
		sigGenerator.update(digestBytes);
		PGPSignature signature = sigGenerator.generate();

		return new String(Hex.encode(signature.getEncoded()));

	}

	@Override
	public boolean validate(String publicKeyString, String value, String signatureBytes) {
		try {
			PGPObjectFactory factory = new PGPObjectFactory(Hex.decode(signatureBytes.getBytes()));
			PGPSignatureList list = (PGPSignatureList) factory.nextObject();
			PGPSignature signature = list.get(0);
			PGPPublicKey publicKey = getPublicFromString(publicKeyString);
			signature.initVerify(publicKey, new BouncyCastleProvider());
			signature.update(value.getBytes());
			return signature.verify();
		} catch (Exception e) {

			return false;
		}

	}

	@Override
	public String changePassword(String privateKey, String oldPassword, String newPassword) throws Exception {
		
		PGPSecretKey secretKey = secretKeyFromPrivateKeyString(privateKey);
		PGPSecretKey newKey = PGPSecretKey.copyWithNewPassword(secretKey, oldPassword.toCharArray(), newPassword.toCharArray(), PGPEncryptedData.AES_256, new SecureRandom(), "BC");
		ByteArrayOutputStream privateStreamBytes = getPrivateKeyBytes(newKey);
		
		return new String(privateStreamBytes.toByteArray());
	}

	protected ByteArrayOutputStream getPrivateKeyBytes(PGPSecretKey secretKey) throws IOException {
		ByteArrayOutputStream privateStreamBytes = new ByteArrayOutputStream();
		ArmoredOutputStream privateStream = new MyArmoredOutputStream(privateStreamBytes);
		secretKey.encode(privateStream);
		privateStream.close();
		return privateStreamBytes;
	}

	
	protected PGPPublicKey getPublicFromString(String key) throws IOException {
		PGPPublicKey publicKey = null;
		PGPObjectFactory factory = new PGPObjectFactory(PGPUtil.getDecoderStream(new ByteArrayInputStream(key.getBytes())));
		Object o = factory.nextObject();
		if (o instanceof PGPPublicKeyRing) {
			PGPPublicKeyRing ring = (PGPPublicKeyRing) o;
			publicKey = ring.getPublicKey();
		}
		return publicKey;
	}

	
	protected PGPSecretKey secretKeyFromPrivateKeyString(String privateKey) throws IOException, PGPException {
		PGPSecretKeyRingCollection skrc = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(new ByteArrayInputStream(privateKey.getBytes())));
		@SuppressWarnings("unchecked")
		Iterator<PGPSecretKeyRing> it = skrc.getKeyRings();
		PGPSecretKey secretKey = it.next().getSecretKey();
		return secretKey;
	}
}
