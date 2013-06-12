package net.securesocial.client;

import java.io.InputStream;
import java.util.List;

public abstract class SecureSocialClientAbstractTest {

	protected static final String TESTPASSPHRASE = "testpassphrase";

	protected String getNewIdentityId(SecureSocialClientInterface client) {
		List<String> ids = client.getSuggestedIds();
		String newId = ids.get(0);
		return newId;
	}

	protected void createTestUserOne(SecureSocialClientInterface client, String newId, String newName) {
		client.createIdentity(getUserOnePrivateKey(), getUserOnePublicKey(), TESTPASSPHRASE, newId, newName);
	}
	
	protected void createTestUserTwo(SecureSocialClientInterface client, String newId, String newName) {
		client.createIdentity(getUserTwoPrivateKey(), getUserTwoPublicKey(), TESTPASSPHRASE, newId, newName);
	}

	protected InputStream getUserOnePublicKey() {
		return getClass().getResourceAsStream("/testuserone-public.asc");
	}

	protected InputStream getUserOnePrivateKey() {
		return getClass().getResourceAsStream("/testuserone-private.asc");
	}

	protected InputStream getUserTwoPublicKey() {
		return getClass().getResourceAsStream("/testusertwo-public.asc");
	}

	protected InputStream getUserTwoPrivateKey() {
		return getClass().getResourceAsStream("/testusertwo-private.asc");
	}

	protected SecureSocialClientInterface getNewClient() {
		SecureSocialClient client = new SecureSocialClient();
		client.setAdditionalHeader("SecureSocial.net junit client");
		return client;
	}

}
