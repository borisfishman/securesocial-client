package net.securesocial.client;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class SecureSocialClientMessagingTests extends SecureSocialClientAbstractTest {

	@Test
	public void testSendMessage() {
		SecureSocialClient client = getNewClient();
		String testData = "sdg gf ghfegt445 h! GDGF GFG FG FGFEW dsfdfg g ggggg";
		
		// create two users
		String oneId = getNewIdentityId(client);
		String twoId = getNewIdentityId(client);
		createTestUserOne(client, oneId, null);
		createTestUserTwo(client, twoId, null);

		// check that second user does not have any messages
		List<String> users = client.getIncomingUsers(twoId, getUserTwoPrivateKey(), TESTPASSPHRASE);
		Assert.assertNotNull(users);
		Assert.assertTrue(users.size() == 0);
		
		// user one sends message to user two
		client.send(testData, oneId, TESTPASSPHRASE, getUserOnePrivateKey(), twoId);
		
		// now, there is one user in user two's list of incoming users
		users = client.getIncomingUsers(twoId, getUserTwoPrivateKey(), TESTPASSPHRASE);
		Assert.assertNotNull(users);
		Assert.assertTrue(users.size() == 1);
		// and it is user one
		Assert.assertEquals(oneId, users.get(0));
		
		
	}
}
