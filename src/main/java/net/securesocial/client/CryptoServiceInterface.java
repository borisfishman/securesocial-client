package net.securesocial.client;

import java.io.InputStream;
import java.io.OutputStream;

public interface CryptoServiceInterface {

	// asymm
	
	public String encrypt(String in, String publicKey) throws Exception;
	public void encrypt(InputStream input, OutputStream output, String key) throws Exception;

	
	public String decrypt(String in, String privateKey, String password) throws Exception;
	public void decrypt(InputStream input, OutputStream output, String key, String password) throws Exception;
	
	// symm
	public String decryptWithPassphrase(String in, String key) throws Exception;
	public String encryptWithPassphrase(String payload, String key) throws Exception;
	public void encryptWithPassphrase(InputStream input, OutputStream output, String passphrase) throws Exception;
	public void decryptWithPassphrase(InputStream encrypted, OutputStream out, String passPhrase) throws Exception;

	// identity
	public String sign(String privateKey, String password, byte[] bytes) throws Exception;
	public boolean validate(String publicKey, String value, String signatureBytes) throws Exception;
	

	public KeyPairInterface generateKeyPair(String id, String password) throws Exception;
	public String changePassword(String privateKey, String oldPassword, String newPassword) throws Exception;

	
	


	public interface KeyPairInterface {

		String getPublicKey();

		String getPrivateKey();

	}


	

	
}
