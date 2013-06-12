package net.securesocial.client;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.Test;

public class SecureSocialClientMessagingTests extends SecureSocialClientAbstractTest {

	@Test
	public void testSendAndRetrieveMessage() {
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

		// accept message from user one
		client.acceptMessages(twoId, TESTPASSPHRASE, getUserTwoPrivateKey(), oneId);

		// retrieve message from timeline of user two
		
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String time = sdf.format(cal.getTime());
		
		// get messages
		List<Message> messages = client.getMessages(twoId, TESTPASSPHRASE, getUserTwoPrivateKey(), time);
		
		Assert.assertNotNull(messages);
		Assert.assertEquals(1, messages.size());
		
		System.out.println("got message:" + messages.get(0));

	}
}
