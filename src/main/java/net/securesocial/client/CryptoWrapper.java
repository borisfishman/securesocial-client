package net.securesocial.client;

import java.io.InputStream;
import java.io.OutputStream;

public class CryptoWrapper {

	private static CryptoServiceInterface service = new BCCrypto();
	
	
	
	
	public static String decrypt(String in, String privateKey, String password) throws Exception {
		return service.decrypt(in, privateKey, password);
				
	}
	
	public static void decrypt(InputStream in, OutputStream out, String privateKey, String password) throws Exception {
		service.decrypt(in, out, privateKey, password);
				
	}

	public static String decryptWithPassphrase(String in, String key) throws Exception {
		return service.decryptWithPassphrase(in, key);
	}

	public static String encryptWithPassphrase(String payload, String key) throws Exception {
		return service.encryptWithPassphrase(payload, key);
	}

	public static String sign(String privateKey, String password, byte[] bytes) throws Exception {
		return service.sign(privateKey, password, bytes);
	}

	public static String encrypt(String in, String publicKey) throws Exception {
		return service.encrypt(in, publicKey);
	}
	
	public static void encrypt(InputStream in, OutputStream out, String publicKey) throws Exception {
		service.encrypt(in, out, publicKey);
	}

	public static void encryptWithPassphrase(InputStream input, OutputStream output, String passphrase) throws Exception {
		service.encryptWithPassphrase(input, output, passphrase);
		
	}

	public static void decryptWithPassphrase(InputStream encrypted, OutputStream out, String passPhrase) throws Exception {
		service.decryptWithPassphrase(encrypted, out, passPhrase);
	}

	public static CryptoServiceInterface.KeyPairInterface generateKeyPair(String id, String password) throws Exception {
		return service.generateKeyPair(id, password);
	}

	public static boolean validate(String publicKey, String value, String signatureBytes) throws Exception {
		return service.validate(publicKey, value, signatureBytes);
	}
	
	public static String changePassword(String privateKey, String oldPassword, String newPassword) throws Exception {
		return service.changePassword(privateKey, oldPassword, newPassword);
	}

}
