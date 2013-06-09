package net.securesocial.client;

import java.io.InputStream;
import java.util.List;

public abstract class SecureSocialClientAbstractTest {

	protected String getNewIdentityId(SecureSocialClient client) {
		List<String> ids = client.getSuggestedIds();
		String newId = ids.get(0);
		return newId;
	}

	protected void createTestUserOne(SecureSocialClient client, String newId, String newName) {
		client.createIdentity(getUserOnePrivateKey(), getUserOnePublicKey(), "testpassphrase", newId, newName);
	}

	protected InputStream getUserOnePublicKey() {
		return getClass().getResourceAsStream("/testuserone-public.asc");
	}

	protected InputStream getUserOnePrivateKey() {
		return getClass().getResourceAsStream("/testuserone-private.asc");
	}

	private InputStream getUserTwoPublicKey() {
		return getClass().getResourceAsStream("/testusertwo-public.asc");
	}

	private InputStream getUserTwoPrivateKey() {
		return getClass().getResourceAsStream("/testusertwo-private.asc");
	}

	protected SecureSocialClient getNewClient() {
		SecureSocialClient client = new SecureSocialClient();
		client.setAdditionalHeader("SecureSocial.net junit client");
		return client;
	}

}
