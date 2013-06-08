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

	@Test
	public void testCreateIdentity() {
		SecureSocialClient client = getNewClient();
		List<String> ids = client.getSuggestedIds();
		String newId = ids.get(0);
		String newName = "Name Me";
		
		// create new identity
		client.createIdentity(getClass().getResourceAsStream("/testuserone-private.asc"), getClass().getResourceAsStream("/testuserone-public.asc"), "testpassphrase", newId, newName);
		System.out.println("created new identity:" + newId);

		try { // same identity again
			client.createIdentity(getClass().getResourceAsStream("/testuserone-private.asc"), getClass().getResourceAsStream("/testuserone-public.asc"), "testpassphrase", newId, newName);
			Assert.fail("this identity already exists!");
		} catch (ServiceException se) {
			System.out.println(se.getMessage());
		}
		
		// get identity back
		Identity copy = client.getIdentity(newId);
		Assert.assertEquals(newId, copy.getId());
		Assert.assertEquals(newName, copy.getProperties().get("Name"));
		
	}

	private SecureSocialClient getNewClient() {
		SecureSocialClient client = new SecureSocialClient();
		client.setAdditionalHeader("SecureSocial.net junit client");
		return client;
	}

}
