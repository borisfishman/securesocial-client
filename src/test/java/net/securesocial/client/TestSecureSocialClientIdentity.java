package net.securesocial.client;

import java.util.List;

import junit.framework.Assert;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

public class TestSecureSocialClientIdentity extends SecureSocialClientAbstractTest {

	@Test
	public void testSuggestedIds() {
		SecureSocialClientInterface client = getNewClient();
		List<String> ids = client.getSuggestedIds();
		Assert.assertNotNull(ids);
		Assert.assertTrue(ids.size() > 1);
		System.out.println(ids);
	}

	@Test
	public void testCreateAndRetrieveIdentity() {
		SecureSocialClientInterface client = getNewClient();
		String newId = getNewIdentityId(client);
		String newName = "Name Me";

		// create new identity
		createTestUserOne(client, newId, newName);
		System.out.println("created new identity:" + newId);

		try { // same identity again
			createTestUserOne(client, newId, newName);
			Assert.fail("this identity already exists!");
		} catch (ServiceException se) {
			System.out.println(se.getMessage());
		}

		// get identity back
		Identity copy = client.getIdentity(newId);
		Assert.assertEquals(newId, copy.getId());
		Assert.assertEquals(newName, copy.getProperties().get("Name"));

	}

	@Test
	public void testUpdateIdentity() throws InterruptedException {
		SecureSocialClientInterface client = getNewClient();
		String newId = getNewIdentityId(client);
		String newName = "Name Me";
		createTestUserOne(client, newId, newName);
		// get identity back
		Identity copy = client.getIdentity(newId);
		Assert.assertEquals(newName, copy.getProperties().get("Name"));
		// change name
		client.updateIdentity(newId, getUserOnePrivateKey(), getUserOnePublicKey(), TESTPASSPHRASE, "New Name");
		// get identity back, see name updated
		long start = System.currentTimeMillis();
		while (true) {
			copy = client.getIdentity(newId);
			if("New Name".equals(copy.getProperties().get("Name"))) {
				break;
			}
			long delta = System.currentTimeMillis() - start;
			if(delta > 5000) {
				Assert.fail("update did not work after 5 sec");
			}
			System.out.println("wating for eventual consistency..");
			Thread.sleep(1000);
		}
	}
	
	@Test
	public void testFindByName() {
		SecureSocialClientInterface client = getNewClient();

		// create two users
		String randomPrefix = RandomStringUtils.randomAlphanumeric(4);
		String oneId = getNewIdentityId(client);
		String twoId = getNewIdentityId(client);
		createTestUserOne(client, oneId, randomPrefix + " one");
		createTestUserTwo(client, twoId, randomPrefix + " two");
		
		List<Identity> found = client.findIdentitiesByName(randomPrefix);
		Assert.assertEquals(2, found.size());
		
		found = client.findIdentitiesByName(randomPrefix + " tw");
		Assert.assertEquals(1, found.size());
		
	}
 
}
