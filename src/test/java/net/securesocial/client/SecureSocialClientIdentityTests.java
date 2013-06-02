package net.securesocial.client;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class SecureSocialClientIdentityTests {

	@Test
	public void testSuggestedIds() {
		SecureSocialClient client = getNewClient();
		List<String> ids = client.getSuggestedIds();
		Assert.assertNotNull(ids);
		Assert.assertTrue(ids.size() > 1);
		System.out.println(ids);
	}

	private SecureSocialClient getNewClient() {
		SecureSocialClient client = new SecureSocialClient();
		client.setAdditionalHeader("SecureSocial.net junit client");
		return client;
	}

}
