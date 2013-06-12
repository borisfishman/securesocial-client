package net.securesocial.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import junit.framework.Assert;

import org.junit.Test;

public class SecureSocialClientMessagingTests extends SecureSocialClientAbstractTest {

	String testData = "sdg gf ghfegt445 h! GDGF GFG FG FGFEW dsfdfg g ggggg";

	@Test
	public void testSendAndRetrieveMessage() {
		SecureSocialClientInterface client = getNewClient();

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
		client.sendMessage(testData, oneId, TESTPASSPHRASE, getUserOnePrivateKey(), twoId);

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

		// make sure we got right message
		Assert.assertEquals(oneId, messages.get(0).getFrom());
		Assert.assertEquals(testData, messages.get(0).getText());
		Assert.assertFalse(messages.get(0).hasAttachment());

		// delete message

		client.deleteMessage(twoId, TESTPASSPHRASE, getUserTwoPrivateKey(), time, messages.get(0));

		// now there are no messages

		messages = client.getMessages(twoId, TESTPASSPHRASE, getUserTwoPrivateKey(), time);

		Assert.assertNotNull(messages);
		Assert.assertEquals(0, messages.size());

	}

	@Test
	public void testSendAndRetrieveMessageWithAttachment() {

		String testAttachment = "sdgfg fg efg 324 545 455 cv fgbfd gf hgfgh gfh g hgfhfr ghfr hgfe   444";

		SecureSocialClientInterface client = getNewClient();

		// create two users
		String oneId = getNewIdentityId(client);
		String twoId = getNewIdentityId(client);
		createTestUserOne(client, oneId, null);
		createTestUserTwo(client, twoId, null);

		// user one sends message with attachment to user two
		client.sendMessage(testData, new ByteArrayInputStream(testAttachment.getBytes()), oneId, TESTPASSPHRASE, getUserOnePrivateKey(), twoId);

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

		// make sure we got right message
		Assert.assertEquals(oneId, messages.get(0).getFrom());
		Assert.assertEquals(testData, messages.get(0).getText());
		Assert.assertTrue(messages.get(0).hasAttachment());

		// get attachment
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		client.getMessageAttachment(twoId, TESTPASSPHRASE, getUserTwoPrivateKey(), time, messages.get(0), baos);
		Assert.assertEquals(testAttachment, new String(baos.toByteArray()));

	}
}
