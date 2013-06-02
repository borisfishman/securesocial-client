package net.securesocial.client;

import java.util.HashMap;
import java.util.Map;

public class Identity {

	private final String id;
	private final String publicKey;
	private final Map<String, String> properties = new HashMap<String, String>();
	
	
	
	public Identity(String id, String publicKey) {
		super();
		this.id = id;
		this.publicKey = publicKey;
	}

	public String getId() {
		return id;
	}
	
	public String getPublicKey() {
		return publicKey;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	
	
}
