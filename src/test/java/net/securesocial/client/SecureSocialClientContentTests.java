package net.securesocial.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

public class SecureSocialClientContentTests extends SecureSocialClientAbstractTest {

	@Test
	public void testPrivateContent() {
		SecureSocialClientInterface client = getNewClient();
		String newId = getNewIdentityId(client);
		String newName = "Name Me";
		String testData = "dsf dg fhf hbte hteh tghbtgrh trgh 3235 4tg rg fg dfg f";
		createTestUserOne(client, newId, newName);
		String guid = UUID.randomUUID().toString();
		client.savePrivateContent(newId, getUserOnePrivateKey(), getUserOnePublicKey(), TESTPASSPHRASE, guid, new ByteArrayInputStream(testData.getBytes()));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		client.getPrivateContent(newId, getUserOnePrivateKey(), TESTPASSPHRASE, guid, baos);
		Assert.assertEquals(testData, new String(baos.toByteArray()));
	}
}
